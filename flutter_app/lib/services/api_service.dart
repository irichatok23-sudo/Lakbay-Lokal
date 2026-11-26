import 'dart:async';
import 'dart:convert';
import 'dart:io';

import '../models/lakbay_models.dart';

/// REST API Client for communicating with the Lakbay Lokal Java Backend.
/// All MySQL database credentials and queries remain strictly inside the Java backend.
class LakbayApiService {
  static final LakbayApiService instance = LakbayApiService._internal();
  LakbayApiService._internal();

  String _baseUrl = Platform.isAndroid
      ? 'http://10.0.2.2:8080'
      : 'http://127.0.0.1:8080';

  String? _authToken;
  UserAccount? _currentUser;
  Process? _spawnedBackendProcess;

  String get baseUrl => _baseUrl;
  String? get authToken => _authToken;
  UserAccount? get currentUser => _currentUser;
  bool get isAuthenticated => _authToken != null && _currentUser != null;

  void setBaseUrl(String url) {
    final trimmed = url.trim();
    if (trimmed.isNotEmpty) {
      _baseUrl = trimmed.endsWith('/')
          ? trimmed.substring(0, trimmed.length - 1)
          : trimmed;
    }
  }

  /// Ensures the Java backend (and WAMP MySQL if installed on Windows) is running
  Future<bool> ensureBackendReady() async {
    if (await checkBackendHealth()) {
      return true;
    }
    if (Platform.isWindows) {
      await _tryStartWindowsBackend();
      for (int i = 0; i < 12; i++) {
        await Future.delayed(const Duration(milliseconds: 500));
        if (await checkBackendHealth()) {
          return true;
        }
      }
    }
    return await checkBackendHealth();
  }

  Future<bool> checkBackendHealth() async {
    try {
      final res = await _request('GET', '/api/settings', timeoutSeconds: 3);
      return res is Map && res.containsKey('appName');
    } catch (_) {
      return false;
    }
  }

  Future<void> _tryStartWindowsBackend() async {
    try {
      // Find project root (either current directory or parent of flutter_app)
      Directory rootDir = Directory.current;
      if (!File('${rootDir.path}\\bin\\com\\lakbaylokal\\Main.class').existsSync()) {
        final parent = rootDir.parent;
        if (File('${parent.path}\\bin\\com\\lakbaylokal\\Main.class').existsSync()) {
          rootDir = parent;
        }
      }

      // Resolve java.exe
      String javaExe = 'java';
      final envJavaHome = Platform.environment['JAVA_HOME'];
      if (envJavaHome != null && File('$envJavaHome\\bin\\java.exe').existsSync()) {
        javaExe = '$envJavaHome\\bin\\java.exe';
      } else {
        final localAppData = Platform.environment['LOCALAPPDATA'] ?? '';
        final adoptiumDir = Directory('$localAppData\\Programs\\Eclipse Adoptium');
        if (adoptiumDir.existsSync()) {
          final jdks = adoptiumDir.listSync().whereType<Directory>().toList();
          if (jdks.isNotEmpty) {
            final candidate = '${jdks.first.path}\\bin\\java.exe';
            if (File(candidate).existsSync()) {
              javaExe = candidate;
            }
          }
        }
      }

      final cp = 'bin;lib/*';
      _spawnedBackendProcess = await Process.start(
        javaExe,
        ['-cp', cp, 'com.lakbaylokal.Main', '--headless'],
        workingDirectory: rootDir.path,
      );
    } catch (_) {
      // Ignore if backend is managed externally
    }
  }

  void disposeBackend() {
    _spawnedBackendProcess?.kill();
    _spawnedBackendProcess = null;
  }

  // ---------------------------------------------------------------------------
  // CORE HTTP TRANSPORT (dart:io HttpClient)
  // ---------------------------------------------------------------------------

  Future<dynamic> _request(
    String method,
    String path, {
    Map<String, String>? queryParams,
    Map<String, dynamic>? body,
    int timeoutSeconds = 10,
  }) async {
    final client = HttpClient();
    client.connectionTimeout = Duration(seconds: timeoutSeconds);
    try {
      Uri uri = Uri.parse('$_baseUrl$path');
      if (queryParams != null && queryParams.isNotEmpty) {
        final filtered = <String, String>{};
        queryParams.forEach((k, v) {
          if (v.isNotEmpty && v != 'ALL') {
            filtered[k] = v;
          }
        });
        if (filtered.isNotEmpty) {
          uri = uri.replace(queryParameters: filtered);
        }
      }

      final HttpClientRequest request = await client.openUrl(method, uri);
      request.headers.set(HttpHeaders.acceptHeader, 'application/json');
      request.headers.set(HttpHeaders.contentTypeHeader, 'application/json; charset=utf-8');
      if (_authToken != null && _authToken!.isNotEmpty) {
        request.headers.set('X-Auth-Token', _authToken!);
        request.headers.set(HttpHeaders.authorizationHeader, 'Bearer $_authToken');
      }

      if (body != null && (method == 'POST' || method == 'PUT')) {
        final encoded = utf8.encode(jsonEncode(body));
        request.contentLength = encoded.length;
        request.add(encoded);
      }

      final HttpClientResponse response =
          await request.close().timeout(Duration(seconds: timeoutSeconds));
      final String responseText = await response.transform(utf8.decoder).join();
      if (responseText.trim().isEmpty) {
        return <String, dynamic>{};
      }
      return jsonDecode(responseText);
    } finally {
      client.close(force: false);
    }
  }

  // ---------------------------------------------------------------------------
  // AUTHENTICATION & SESSION ENDPOINTS
  // ---------------------------------------------------------------------------

  Future<Map<String, dynamic>> login(String username, String password) async {
    final res = await _request(
      'POST',
      '/api/auth/login',
      body: {'username': username.trim(), 'password': password},
    );
    if (res is Map<String, dynamic>) {
      if (res['success'] == true && res['user'] is Map<String, dynamic>) {
        _authToken = res['token']?.toString();
        _currentUser = UserAccount.fromJson(res['user'] as Map<String, dynamic>);
        // Enrich session with profile IDs from /api/auth/me
        try {
          final me = await _request('GET', '/api/auth/me');
          if (me is Map<String, dynamic> && me.containsKey('userId')) {
            _currentUser = UserAccount.fromJson(me);
          }
        } catch (_) {}
      }
      return res;
    }
    return {'success': false, 'message': 'Invalid response from server.'};
  }

  Future<Map<String, dynamic>> registerTourist({
    required String username,
    required String password,
    required String email,
    required String fullName,
    required String phone,
    required String originCity,
  }) async {
    final res = await _request(
      'POST',
      '/api/auth/register',
      body: {
        'username': username.trim(),
        'password': password,
        'email': email.trim(),
        'fullName': fullName.trim(),
        'phone': phone.trim(),
        'originCity': originCity.trim(),
      },
    );
    return (res is Map<String, dynamic>)
        ? res
        : {'success': false, 'message': 'Registration failed.'};
  }

  Future<Map<String, dynamic>> forgotPassword({
    required String usernameOrEmail,
    required String newPassword,
  }) async {
    final res = await _request(
      'POST',
      '/api/auth/forgot-password',
      body: {
        'usernameOrEmail': usernameOrEmail.trim(),
        'newPassword': newPassword,
      },
    );
    return (res is Map<String, dynamic>)
        ? res
        : {'success': false, 'message': 'Password reset failed.'};
  }

  Future<void> logout() async {
    try {
      await _request('POST', '/api/auth/logout');
    } catch (_) {}
    _authToken = null;
    _currentUser = null;
  }

  // ---------------------------------------------------------------------------
  // DESTINATIONS & BARANGAYS
  // ---------------------------------------------------------------------------

  Future<List<DestinationModel>> getDestinations({
    String? category,
    int? barangayId,
    String status = 'ACTIVE',
  }) async {
    final res = await _request(
      'GET',
      '/api/destinations',
      queryParams: {
        if (category != null && category.isNotEmpty) 'category': category,
        if (barangayId != null && barangayId > 0) 'barangayId': barangayId.toString(),
        'status': status,
      },
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(DestinationModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> saveDestination(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/destinations', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> validateDestination(int destinationId) async {
    final res = await _request(
      'POST',
      '/api/destinations/validate',
      body: {'destinationId': destinationId},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> deleteDestination(int destinationId) async {
    final res = await _request(
      'POST',
      '/api/destinations/delete',
      body: {'destinationId': destinationId},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<List<BarangayModel>> getBarangays() async {
    final res = await _request('GET', '/api/barangays');
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(BarangayModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> updateBarangayRecord({
    required int barangayId,
    required String contactPerson,
    required String contactNumber,
  }) async {
    final res = await _request(
      'POST',
      '/api/barangays',
      body: {
        'barangayId': barangayId,
        'contactPerson': contactPerson,
        'contactNumber': contactNumber,
      },
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  // ---------------------------------------------------------------------------
  // PACKAGES & ANNOUNCEMENTS
  // ---------------------------------------------------------------------------

  Future<List<TourismPackageModel>> getPackages({String status = 'ACTIVE'}) async {
    final res = await _request(
      'GET',
      '/api/packages',
      queryParams: {'status': status},
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(TourismPackageModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> savePackage(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/packages', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> deletePackage(int packageId) async {
    final res = await _request(
      'POST',
      '/api/packages/delete',
      body: {'packageId': packageId},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<List<AnnouncementModel>> getAnnouncements({String? category}) async {
    final res = await _request(
      'GET',
      '/api/announcements',
      queryParams: {
        if (category != null && category.isNotEmpty) 'category': category,
      },
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(AnnouncementModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> saveAnnouncement(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/announcements', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> deleteAnnouncement(int announcementId) async {
    final res = await _request(
      'POST',
      '/api/announcements/delete',
      body: {'announcementId': announcementId},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  // ---------------------------------------------------------------------------
  // STAKEHOLDERS, SERVICES & VERIFICATIONS
  // ---------------------------------------------------------------------------

  Future<List<StakeholderModel>> getStakeholders({
    String? category,
    int? barangayId,
    String? verificationStatus,
    bool jurisdictionOnly = false,
  }) async {
    final res = await _request(
      'GET',
      '/api/stakeholders',
      queryParams: {
        if (category != null && category.isNotEmpty) 'category': category,
        if (barangayId != null && barangayId > 0) 'barangayId': barangayId.toString(),
        if (verificationStatus != null && verificationStatus.isNotEmpty)
          'verificationStatus': verificationStatus,
        if (jurisdictionOnly) 'jurisdictionOnly': 'true',
      },
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(StakeholderModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<StakeholderModel?> getMyStakeholderProfile() async {
    final res = await _request('GET', '/api/stakeholders/me');
    if (res is Map<String, dynamic> && res.containsKey('profileId')) {
      return StakeholderModel.fromJson(res);
    }
    return null;
  }

  Future<Map<String, dynamic>> saveStakeholderProfile(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/stakeholders', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<List<TourismServiceModel>> getServices({
    String? category,
    bool includeUnavailable = true,
  }) async {
    final res = await _request(
      'GET',
      '/api/stakeholders/services',
      queryParams: {
        if (category != null && category.isNotEmpty) 'category': category,
        if (includeUnavailable) 'all': 'true',
      },
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(TourismServiceModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> saveService(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/stakeholders/services', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> deleteService(int serviceId) async {
    final res = await _request(
      'POST',
      '/api/stakeholders/services/delete',
      body: {'serviceId': serviceId},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> processVerification({
    required int stakeholderId,
    required int barangayId,
    required String status,
    required String requirementsSubmitted,
    required String remarks,
  }) async {
    final res = await _request(
      'POST',
      '/api/verifications',
      body: {
        'stakeholderId': stakeholderId,
        'barangayId': barangayId,
        'status': status,
        'requirementsSubmitted': requirementsSubmitted,
        'remarks': remarks,
      },
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  // ---------------------------------------------------------------------------
  // BOOKINGS & REVIEWS
  // ---------------------------------------------------------------------------

  Future<List<BookingModel>> getBookings({String? status}) async {
    final res = await _request(
      'GET',
      '/api/bookings',
      queryParams: {
        if (status != null && status.isNotEmpty) 'status': status,
      },
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(BookingModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> createBooking(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/bookings', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> updateBookingStatus(int bookingId, String status) async {
    final res = await _request(
      'POST',
      '/api/bookings/status',
      body: {'bookingId': bookingId, 'status': status},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<List<ReviewModel>> getReviews() async {
    final res = await _request('GET', '/api/reviews');
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(ReviewModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> createReview({
    required int bookingId,
    required String targetType,
    required int targetId,
    required int rating,
    required String comment,
  }) async {
    final res = await _request(
      'POST',
      '/api/reviews',
      body: {
        'bookingId': bookingId,
        'targetType': targetType,
        'targetId': targetId,
        'rating': rating,
        'comment': comment,
      },
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  // ---------------------------------------------------------------------------
  // REPORTS, KPIs & MONITORING
  // ---------------------------------------------------------------------------

  Future<Map<String, dynamic>> getOverviewKpis() async {
    final res = await _request('GET', '/api/reports/kpis');
    return (res is Map<String, dynamic>) ? res : <String, dynamic>{};
  }

  Future<List<Map<String, dynamic>>> getFootfallReport() async {
    final res = await _request('GET', '/api/reports/footfall');
    if (res is List) {
      return res.whereType<Map<String, dynamic>>().toList();
    }
    return [];
  }

  Future<List<Map<String, dynamic>>> getBarangayReport() async {
    final res = await _request('GET', '/api/reports/barangays');
    if (res is List) {
      return res.whereType<Map<String, dynamic>>().toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> recordArrival(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/reports/arrival', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  // ---------------------------------------------------------------------------
  // SYSTEM ADMIN: USERS, SETTINGS, AUDIT LOGS & BACKUP
  // ---------------------------------------------------------------------------

  Future<List<UserAccount>> getAdminUsers() async {
    final res = await _request('GET', '/api/admin/users');
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(UserAccount.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> createAdminUser(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/admin/users', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> updateAdminUser(Map<String, dynamic> payload) async {
    final res = await _request('POST', '/api/admin/users/update', body: payload);
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> updateAdminUserStatus(int userId, String status) async {
    final res = await _request(
      'POST',
      '/api/admin/users/status',
      body: {'userId': userId, 'status': status},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<Map<String, dynamic>> resetAdminUserPassword(int userId, String newPassword) async {
    final res = await _request(
      'POST',
      '/api/admin/users/reset-password',
      body: {'userId': userId, 'newPassword': newPassword},
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<SystemSettingsModel> getSettings() async {
    final res = await _request('GET', '/api/settings');
    if (res is Map<String, dynamic>) {
      return SystemSettingsModel.fromJson(res);
    }
    return SystemSettingsModel.fromJson({});
  }

  Future<Map<String, dynamic>> saveSettings({
    required String appName,
    required String province,
    required int capacityThreshold,
    required String currency,
    required String broadcastMsg,
    List<String> updatedFields = const [],
  }) async {
    final res = await _request(
      'POST',
      '/api/settings',
      body: {
        'appName': appName,
        'province': province,
        'capacityThreshold': capacityThreshold,
        'currency': currency,
        'broadcastMsg': broadcastMsg,
        'updatedFields': updatedFields,
      },
    );
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }

  Future<List<AuditLogModel>> getAuditLogs({int limit = 60}) async {
    final res = await _request(
      'GET',
      '/api/admin/logs',
      queryParams: {'limit': limit.toString()},
    );
    if (res is List) {
      return res
          .whereType<Map<String, dynamic>>()
          .map(AuditLogModel.fromJson)
          .toList();
    }
    return [];
  }

  Future<Map<String, dynamic>> triggerDatabaseBackup() async {
    final res = await _request('POST', '/api/admin/database/backup');
    return (res is Map<String, dynamic>) ? res : {'success': false};
  }
}

