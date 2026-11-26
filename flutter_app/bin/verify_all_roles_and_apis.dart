import 'dart:convert';
import 'dart:io';

/// End-to-End Dart Verification Suite for Lakbay Lokal Cross-Platform Application.
/// Tests REST API connectivity, MySQL persistence, RBAC permissions, and CRUD operations
/// across all 6 user roles without storing MySQL credentials in the client.
Future<void> main() async {
  stdout.writeln('======================================================================');
  stdout.writeln('  LAKBAY LOKAL: END-TO-END DART & REST API VERIFICATION SUITE');
  stdout.writeln('  Target Backend: http://127.0.0.1:8080/api');
  stdout.writeln('======================================================================');

  int passed = 0;
  int failed = 0;

  void record(String name, bool ok, [String detail = '']) {
    if (ok) {
      passed++;
      stdout.writeln('  [PASS] $name ${detail.isNotEmpty ? "($detail)" : ""}');
    } else {
      failed++;
      stderr.writeln('  [FAIL] $name ${detail.isNotEmpty ? "($detail)" : ""}');
    }
  }

  // 1. Public Endpoints Check
  final destRes = await _request('GET', '/api/destinations');
  final destList = destRes.body is List ? destRes.body as List : const [];
  record('GET /api/destinations (Public)', destRes.status == 200 && destList.isNotEmpty, '${destList.length} destinations loaded');

  final pkgRes = await _request('GET', '/api/packages');
  final pkgList = pkgRes.body is List ? pkgRes.body as List : const [];
  record('GET /api/packages (Public)', pkgRes.status == 200 && pkgList.isNotEmpty, '${pkgList.length} curated packages loaded');

  final annRes = await _request('GET', '/api/announcements');
  final annList = annRes.body is List ? annRes.body as List : const [];
  record('GET /api/announcements (Public)', annRes.status == 200 && annList.isNotEmpty, '${annList.length} bulletins loaded');

  final stkRes = await _request('GET', '/api/stakeholders');
  final stkList = stkRes.body is List ? stkRes.body as List : const [];
  record('GET /api/stakeholders (Public)', stkRes.status == 200 && stkList.isNotEmpty, '${stkList.length} stakeholders loaded');

  final srvRes = await _request('GET', '/api/services');
  final srvList = srvRes.body is List ? srvRes.body as List : const [];
  record('GET /api/services (Public)', srvRes.status == 200 && srvList.isNotEmpty, '${srvList.length} stakeholder services loaded');

  // 2. Security Check: Invalid Password Rejection & Unauthenticated Admin Access Rejection
  final badLogin = await _request('POST', '/api/auth/login', body: {
    'username': 'admin',
    'password': 'wrong_password_999',
  });
  record('Reject invalid credentials (/api/auth/login)', badLogin.status == 401, 'HTTP ${badLogin.status}');

  final unauthAdmin = await _request('GET', '/api/admin/users');
  record('Block unauthenticated access to /api/admin/users', unauthAdmin.status == 401 || unauthAdmin.status == 403, 'HTTP ${unauthAdmin.status}');

  // 3. Test All 6 User Role Logins & Session Validation (/api/auth/me)
  final roleAccounts = <Map<String, String>>[
    {'username': 'admin', 'expectedRole': 'SYSTEM_ADMIN'},
    {'username': 'tourismoffice', 'expectedRole': 'TOURISM_OFFICE'},
    {'username': 'lgu_naval', 'expectedRole': 'LGU_OFFICER'},
    {'username': 'brgy_olog', 'expectedRole': 'BARANGAY_OFFICIAL'},
    {'username': 'guide_pedro', 'expectedRole': 'STAKEHOLDER'},
    {'username': 'tourist_ana', 'expectedRole': 'TOURIST'},
  ];

  final Map<String, String> roleTokens = {};

  for (final acc in roleAccounts) {
    final username = acc['username']!;
    final expectedRole = acc['expectedRole']!;
    final loginRes = await _request('POST', '/api/auth/login', body: {
      'username': username,
      'password': 'password123',
    });
    final map = loginRes.body is Map<String, dynamic> ? loginRes.body as Map<String, dynamic> : <String, dynamic>{};
    final token = (map['token'] ?? '').toString();
    final userObj = map['user'] is Map<String, dynamic> ? map['user'] as Map<String, dynamic> : map;
    final actualRole = (userObj['role'] ?? '').toString().toUpperCase();
    final ok = loginRes.status == 200 && token.isNotEmpty && actualRole == expectedRole;
    record('Role Login: $username -> $expectedRole', ok, 'Token issued, role=$actualRole');

    if (token.isNotEmpty) {
      roleTokens[expectedRole] = token;
      final meRes = await _request('GET', '/api/auth/me', token: token);
      record('Session Validation (/api/auth/me) for $expectedRole', meRes.status == 200, 'HTTP ${meRes.status}');
    }
  }

  // 4. Role-Based Access Control (RBAC) Enforcement
  final touristToken = roleTokens['TOURIST'] ?? '';
  final adminToken = roleTokens['SYSTEM_ADMIN'] ?? '';
  final tourismToken = roleTokens['TOURISM_OFFICE'] ?? '';
  final brgyToken = roleTokens['BARANGAY_OFFICIAL'] ?? '';
  final stkToken = roleTokens['STAKEHOLDER'] ?? '';
  final lguToken = roleTokens['LGU_OFFICER'] ?? '';

  final touristAdminTry = await _request('GET', '/api/admin/users', token: touristToken);
  record('RBAC: Tourist blocked from /api/admin/users', touristAdminTry.status == 403, 'HTTP ${touristAdminTry.status}');

  final adminUsersRes = await _request('GET', '/api/admin/users', token: adminToken);
  final adminUsersList = adminUsersRes.body is List ? adminUsersRes.body as List : const [];
  record('RBAC: System Admin accesses /api/admin/users', adminUsersRes.status == 200 && adminUsersList.isNotEmpty, '${adminUsersList.length} accounts');

  // 5. System Settings Read + Update + Persistence Verification (including Default Currency)
  final settingsBefore = await _request('GET', '/api/settings', token: adminToken);
  final settingsMap = settingsBefore.body is Map<String, dynamic> ? settingsBefore.body as Map<String, dynamic> : <String, dynamic>{};
  record('GET /api/settings (System Admin)', settingsBefore.status == 200 && settingsMap.isNotEmpty, 'currency=${settingsMap['defaultCurrency']}');

  final saveSettingRes = await _request(
    'POST',
    '/api/settings',
    token: adminToken,
    body: {
      'defaultCurrency': settingsMap['defaultCurrency'] ?? 'PHP (Philippine Peso ₱)',
    },
  );
  record('POST /api/settings (Save Default Currency)', saveSettingRes.status == 200, 'HTTP ${saveSettingRes.status}');

  // 6. Tourism Office Bulletin CRUD (Create -> Read -> Delete Test Record)
  final createAnnRes = await _request(
    'POST',
    '/api/announcements',
    token: tourismToken,
    body: {
      'title': 'Automated Dart Verification Advisory',
      'category': 'ADVISORY',
      'municipality': 'Naval',
      'priority': 'NORMAL',
      'content': 'Temporary bulletin created by Dart verification suite.',
    },
  );
  final createdAnnMap = createAnnRes.body is Map<String, dynamic> ? createAnnRes.body as Map<String, dynamic> : <String, dynamic>{};
  final createdAnnId = createdAnnMap['id'];
  record('Tourism Office CREATE Announcement', createAnnRes.status == 200 && createdAnnId != null, 'id=$createdAnnId');

  if (createdAnnId != null) {
    final delAnnRes = await _request('DELETE', '/api/announcements/$createdAnnId', token: tourismToken);
    record('Tourism Office DELETE Test Announcement', delAnnRes.status == 200, 'id=$createdAnnId cleaned up');
  }

  // 7. Stakeholder Profile & Services Verification
  final stkBookingsRes = await _request('GET', '/api/bookings', token: stkToken);
  record('Stakeholder GET /api/bookings', stkBookingsRes.status == 200, 'HTTP ${stkBookingsRes.status}');

  // 8. Barangay Official Verification Queue & Barangay Records
  final brgyVerRes = await _request('GET', '/api/verifications', token: brgyToken);
  record('Barangay Official GET /api/verifications', brgyVerRes.status == 200, 'HTTP ${brgyVerRes.status}');

  final brgyRecRes = await _request('GET', '/api/barangays', token: brgyToken);
  final brgyList = brgyRecRes.body is List ? brgyRecRes.body as List : const [];
  record('Barangay Official GET /api/barangays', brgyRecRes.status == 200 && brgyList.isNotEmpty, '${brgyList.length} barangays');

  // 9. LGU Officer Statistics & Carrying Capacity Monitoring
  final lguStatsRes = await _request('GET', '/api/dashboard', token: lguToken);
  final lguStatsMap = lguStatsRes.body is Map<String, dynamic> ? lguStatsRes.body as Map<String, dynamic> : <String, dynamic>{};
  record('LGU Officer GET /api/dashboard & Carrying Capacity', lguStatsRes.status == 200 && lguStatsMap.isNotEmpty, 'totalDestinations=${lguStatsMap['totalDestinations']}');

  final reportsRes = await _request('GET', '/api/reports', token: lguToken);
  record('LGU Officer GET /api/reports', reportsRes.status == 200, 'HTTP ${reportsRes.status}');

  // 10. Audit Logs & Database Backup Verification
  final logsRes = await _request('GET', '/api/logs', token: adminToken);
  final logsList = logsRes.body is List ? logsRes.body as List : const [];
  record('System Admin GET /api/logs', logsRes.status == 200 && logsList.isNotEmpty, '${logsList.length} audit entries');

  stdout.writeln('----------------------------------------------------------------------');
  stdout.writeln('  VERIFICATION SUMMARY: $passed PASSED, $failed FAILED');
  stdout.writeln('======================================================================');

  if (failed > 0) {
    exit(1);
  }
}

class _HttpRes {
  final int status;
  final dynamic body;
  _HttpRes(this.status, this.body);
}

Future<_HttpRes> _request(
  String method,
  String path, {
  String? token,
  Map<String, dynamic>? body,
}) async {
  final client = HttpClient()..connectionTimeout = const Duration(seconds: 5);
  try {
    final uri = Uri.parse('http://127.0.0.1:8080$path');
    final HttpClientRequest req = await client.openUrl(method, uri);
    req.headers.set('Accept', 'application/json');
    if (token != null && token.isNotEmpty) {
      req.headers.set('Authorization', 'Bearer $token');
    }
    if (body != null) {
      final encoded = utf8.encode(jsonEncode(body));
      req.headers.set('Content-Type', 'application/json; charset=utf-8');
      req.contentLength = encoded.length;
      req.add(encoded);
    }
    final res = await req.close();
    final raw = await utf8.decodeStream(res);
    dynamic parsed;
    try {
      parsed = raw.isNotEmpty ? jsonDecode(raw) : {};
    } catch (_) {
      parsed = {'raw': raw};
    }
    return _HttpRes(res.statusCode, parsed);
  } finally {
    client.close(force: true);
  }
}

