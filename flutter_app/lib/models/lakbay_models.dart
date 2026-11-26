import 'package:flutter/material.dart';

/// Strongly-typed models and role configuration for Lakbay Lokal
class UserAccount {
  final int userId;
  final String username;
  final String email;
  final String fullName;
  final String phone;
  final int roleId;
  final String roleCode;
  final String roleName;
  final int? barangayId;
  final String barangayName;
  final String municipalityName;
  final String status;
  final int? touristProfileId;
  final int? stakeholderProfileId;
  final String? verificationStatus;
  final String? businessName;

  const UserAccount({
    required this.userId,
    required this.username,
    required this.email,
    required this.fullName,
    required this.phone,
    required this.roleId,
    required this.roleCode,
    required this.roleName,
    this.barangayId,
    this.barangayName = '',
    this.municipalityName = 'Biliran',
    this.status = 'ACTIVE',
    this.touristProfileId,
    this.stakeholderProfileId,
    this.verificationStatus,
    this.businessName,
  });

  factory UserAccount.fromJson(Map<String, dynamic> json) {
    return UserAccount(
      userId: _toInt(json['userId']),
      username: (json['username'] ?? '').toString(),
      email: (json['email'] ?? '').toString(),
      fullName: (json['fullName'] ?? '').toString(),
      phone: (json['phone'] ?? '').toString(),
      roleId: _toInt(json['roleId'], 6),
      roleCode: (json['roleCode'] ?? 'TOURIST').toString(),
      roleName: (json['roleName'] ?? 'Tourist').toString(),
      barangayId: json['barangayId'] != null ? _toInt(json['barangayId']) : null,
      barangayName: (json['barangayName'] ?? '').toString(),
      municipalityName: (json['municipalityName'] ?? 'Biliran').toString(),
      status: (json['status'] ?? 'ACTIVE').toString(),
      touristProfileId: json['touristProfileId'] != null ? _toInt(json['touristProfileId']) : null,
      stakeholderProfileId: json['stakeholderProfileId'] != null ? _toInt(json['stakeholderProfileId']) : null,
      verificationStatus: json['verificationStatus']?.toString(),
      businessName: json['businessName']?.toString(),
    );
  }

  bool get isAdmin => roleCode.toUpperCase() == 'SYSTEM_ADMIN';
  bool get isTourismOffice => roleCode.toUpperCase() == 'TOURISM_OFFICE' || isAdmin;
  bool get isLguOfficer => roleCode.toUpperCase() == 'LGU_OFFICER';
  bool get isBarangayOfficial => roleCode.toUpperCase() == 'BARANGAY_OFFICIAL';
  bool get isStakeholder => roleCode.toUpperCase() == 'STAKEHOLDER';
  bool get isTourist => roleCode.toUpperCase() == 'TOURIST';
}

class NavModuleItem {
  final String id;
  final String title;
  final String subtitle;
  final IconData icon;

  const NavModuleItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.icon,
  });
}

class RoleMetadata {
  final String roleCode;
  final String displayName;
  final String badgeText;
  final String bannerTitle;
  final String bannerDescription;
  final String demoUsername;
  final List<String> allowedModules;

  const RoleMetadata({
    required this.roleCode,
    required this.displayName,
    required this.badgeText,
    required this.bannerTitle,
    required this.bannerDescription,
    required this.demoUsername,
    required this.allowedModules,
  });

  static const Map<String, NavModuleItem> allModules = {
    'overview': NavModuleItem(
      id: 'overview',
      title: 'Dashboard Overview',
      subtitle: 'Live provincial KPIs, community highlights, and recent activity',
      icon: Icons.dashboard_rounded,
    ),
    'destinations': NavModuleItem(
      id: 'destinations',
      title: 'Destinations',
      subtitle: 'Explore, search, filter, and manage Biliran tourist attractions',
      icon: Icons.landscape_rounded,
    ),
    'announcements': NavModuleItem(
      id: 'announcements',
      title: 'Tourism Bulletins',
      subtitle: 'Official provincial advisories, festivals, and eco-guidelines',
      icon: Icons.campaign_rounded,
    ),
    'stakeholders': NavModuleItem(
      id: 'stakeholders',
      title: 'Local Stakeholders',
      subtitle: 'Accredited community tour guides, boatmen, drivers, and hosts',
      icon: Icons.groups_rounded,
    ),
    'services': NavModuleItem(
      id: 'services',
      title: 'Services & Profile',
      subtitle: 'Manage stakeholder tourism services, rate cards, and availability',
      icon: Icons.storefront_rounded,
    ),
    'packages': NavModuleItem(
      id: 'packages',
      title: 'Packages & Booking',
      subtitle: 'Curated community circuits and custom itinerary builder',
      icon: Icons.card_travel_rounded,
    ),
    'bookings': NavModuleItem(
      id: 'bookings',
      title: 'Bookings & Reservations',
      subtitle: 'Track tourist reservations, stakeholder assignments, and statuses',
      icon: Icons.calendar_month_rounded,
    ),
    'verifications': NavModuleItem(
      id: 'verifications',
      title: 'Stakeholder Verification',
      subtitle: 'Barangay & Tourism Office accreditation and permit review queue',
      icon: Icons.verified_user_rounded,
    ),
    'barangay_records': NavModuleItem(
      id: 'barangay_records',
      title: 'Barangay Tourism Records',
      subtitle: 'Maintain barangay focal contacts, local destinations, and coverage',
      icon: Icons.location_city_rounded,
    ),
    'lgu_monitoring': NavModuleItem(
      id: 'lgu_monitoring',
      title: 'LGU & Eco Monitoring',
      subtitle: 'Consolidated municipal records and carrying-capacity thresholds',
      icon: Icons.analytics_rounded,
    ),
    'reviews': NavModuleItem(
      id: 'reviews',
      title: 'Reviews & Feedback',
      subtitle: 'Verified 5-star ratings and testimonials from completed bookings',
      icon: Icons.star_rate_rounded,
    ),
    'reports': NavModuleItem(
      id: 'reports',
      title: 'Reports & Statistics',
      subtitle: 'Tourist arrival logs, footfall analytics, and revenue summaries',
      icon: Icons.bar_chart_rounded,
    ),
    'admin_users': NavModuleItem(
      id: 'admin_users',
      title: 'User & Role Management',
      subtitle: 'Manage system accounts, role assignments, statuses, and passwords',
      icon: Icons.manage_accounts_rounded,
    ),
    'settings': NavModuleItem(
      id: 'settings',
      title: 'System Settings',
      subtitle: 'Configure platform parameters, carrying capacity, and currencies',
      icon: Icons.settings_rounded,
    ),
    'audit_logs': NavModuleItem(
      id: 'audit_logs',
      title: 'Security Audit Logs',
      subtitle: 'Chronological tamper-evident log of authentication & admin actions',
      icon: Icons.security_rounded,
    ),
    'backup': NavModuleItem(
      id: 'backup',
      title: 'Backup & Recovery',
      subtitle: 'Generate portable MySQL SQL dumps and inspect database health',
      icon: Icons.storage_rounded,
    ),
  };

  static const Map<String, RoleMetadata> roles = {
    'TOURIST': RoleMetadata(
      roleCode: 'TOURIST',
      displayName: 'Tourist',
      badgeText: 'TOURIST EXPLORER',
      bannerTitle: 'Biliran Visitor & Booking Workspace',
      bannerDescription:
          'Browse verified Biliran destinations, book curated packages or custom community itineraries, manage your trips, and rate completed experiences.',
      demoUsername: 'tourist_ana',
      allowedModules: [
        'overview',
        'destinations',
        'packages',
        'bookings',
        'stakeholders',
        'announcements',
        'reviews',
      ],
    ),
    'STAKEHOLDER': RoleMetadata(
      roleCode: 'STAKEHOLDER',
      displayName: 'Local Tourism Stakeholder',
      badgeText: 'COMMUNITY STAKEHOLDER',
      bannerTitle: 'Local Tourism Stakeholder Operations Hub',
      bannerDescription:
          'Manage your community business profile, publish tourism services & rates, toggle real-time availability, and fulfill assigned tourist bookings.',
      demoUsername: 'guide_pedro',
      allowedModules: [
        'overview',
        'services',
        'bookings',
        'reviews',
        'destinations',
        'announcements',
      ],
    ),
    'BARANGAY_OFFICIAL': RoleMetadata(
      roleCode: 'BARANGAY_OFFICIAL',
      displayName: 'Barangay Official',
      badgeText: 'BARANGAY TOURISM COUNCIL',
      bannerTitle: 'Barangay Jurisdiction & Verification Desk',
      bannerDescription:
          'Register and verify local stakeholders within your assigned barangay, validate local tourist attractions, and maintain barangay tourism records.',
      demoUsername: 'brgy_olog',
      allowedModules: [
        'overview',
        'verifications',
        'barangay_records',
        'stakeholders',
        'destinations',
        'reports',
        'announcements',
      ],
    ),
    'LGU_OFFICER': RoleMetadata(
      roleCode: 'LGU_OFFICER',
      displayName: 'LGU Officer',
      badgeText: 'MUNICIPAL LGU OVERSIGHT',
      bannerTitle: 'Municipal LGU Tourism & Eco-Capacity Monitor',
      bannerDescription:
          'View consolidated barangay tourism records, monitor destination carrying-capacity thresholds, publish municipal advisories, and generate reports.',
      demoUsername: 'lgu_naval',
      allowedModules: [
        'overview',
        'lgu_monitoring',
        'barangay_records',
        'reports',
        'destinations',
        'stakeholders',
        'announcements',
      ],
    ),
    'TOURISM_OFFICE': RoleMetadata(
      roleCode: 'TOURISM_OFFICE',
      displayName: 'Tourism Office',
      badgeText: 'PROVINCIAL TOURISM OFFICE',
      bannerTitle: 'Provincial Tourism Management & Promotions Center',
      bannerDescription:
          'Manage Biliran destinations, curate official tourism packages, publish provincial announcements, oversee stakeholder verifications, and analyze footfall.',
      demoUsername: 'tourismoffice',
      allowedModules: [
        'overview',
        'destinations',
        'packages',
        'announcements',
        'stakeholders',
        'verifications',
        'lgu_monitoring',
        'reports',
        'bookings',
        'reviews',
      ],
    ),
    'SYSTEM_ADMIN': RoleMetadata(
      roleCode: 'SYSTEM_ADMIN',
      displayName: 'System Administrator',
      badgeText: 'SYSTEM ADMINISTRATOR',
      bannerTitle: 'Enterprise Administration & Security Command Center',
      bannerDescription:
          'Full multi-role supervision across user accounts, RBAC permissions, system settings, security audit trails, database backups, and all tourism modules.',
      demoUsername: 'admin',
      allowedModules: [
        'overview',
        'admin_users',
        'settings',
        'audit_logs',
        'backup',
        'destinations',
        'packages',
        'announcements',
        'stakeholders',
        'services',
        'bookings',
        'verifications',
        'barangay_records',
        'lgu_monitoring',
        'reviews',
        'reports',
      ],
    ),
  };

  static RoleMetadata forRole(String? code) {
    final key = (code ?? 'TOURIST').toUpperCase();
    return roles[key] ?? roles['TOURIST']!;
  }
}

class DestinationModel {
  final int destinationId;
  final String name;
  final String category;
  final String description;
  final int barangayId;
  final String barangayName;
  final String municipalityName;
  final double entranceFee;
  final String operatingHours;
  final String locationCoordinates;
  final String contactInfo;
  final String imageUrl;
  final String status;
  final bool validated;
  final double averageRating;
  final int reviewCount;

  const DestinationModel({
    required this.destinationId,
    required this.name,
    required this.category,
    required this.description,
    required this.barangayId,
    required this.barangayName,
    required this.municipalityName,
    required this.entranceFee,
    required this.operatingHours,
    required this.locationCoordinates,
    required this.contactInfo,
    required this.imageUrl,
    required this.status,
    required this.validated,
    required this.averageRating,
    required this.reviewCount,
  });

  factory DestinationModel.fromJson(Map<String, dynamic> json) {
    return DestinationModel(
      destinationId: _toInt(json['destinationId']),
      name: (json['name'] ?? '').toString(),
      category: (json['category'] ?? 'ISLAND_BEACH').toString(),
      description: (json['description'] ?? '').toString(),
      barangayId: _toInt(json['barangayId'], 1),
      barangayName: (json['barangayName'] ?? 'Ol-og').toString(),
      municipalityName: (json['municipalityName'] ?? 'Maripipi').toString(),
      entranceFee: _toDouble(json['entranceFee']),
      operatingHours: (json['operatingHours'] ?? '6:00 AM - 5:00 PM').toString(),
      locationCoordinates: (json['locationCoordinates'] ?? '').toString(),
      contactInfo: (json['contactInfo'] ?? '').toString(),
      imageUrl: (json['imageUrl'] ?? '').toString(),
      status: (json['status'] ?? 'ACTIVE').toString(),
      validated: json['validated'] == true || json['isValidated'] == true,
      averageRating: _toDouble(json['averageRating'], 4.8),
      reviewCount: _toInt(json['reviewCount']),
    );
  }
}

class TourismPackageModel {
  final int packageId;
  final String name;
  final String description;
  final String packageType;
  final double pricePerPax;
  final int durationDays;
  final int minPax;
  final int maxPax;
  final String inclusions;
  final String exclusions;
  final String status;
  final String imageUrl;
  final List<DestinationModel> destinations;
  final List<TourismServiceModel> services;

  const TourismPackageModel({
    required this.packageId,
    required this.name,
    required this.description,
    required this.packageType,
    required this.pricePerPax,
    required this.durationDays,
    required this.minPax,
    required this.maxPax,
    required this.inclusions,
    required this.exclusions,
    required this.status,
    required this.imageUrl,
    this.destinations = const [],
    this.services = const [],
  });

  factory TourismPackageModel.fromJson(Map<String, dynamic> json) {
    final destList = <DestinationModel>[];
    if (json['destinations'] is List) {
      for (final item in json['destinations']) {
        if (item is Map<String, dynamic>) {
          destList.add(DestinationModel.fromJson(item));
        }
      }
    }
    final svcList = <TourismServiceModel>[];
    if (json['services'] is List) {
      for (final item in json['services']) {
        if (item is Map<String, dynamic>) {
          svcList.add(TourismServiceModel.fromJson(item));
        }
      }
    }
    return TourismPackageModel(
      packageId: _toInt(json['packageId']),
      name: (json['name'] ?? '').toString(),
      description: (json['description'] ?? '').toString(),
      packageType: (json['packageType'] ?? 'PRE_DESIGNED').toString(),
      pricePerPax: _toDouble(json['pricePerPax']),
      durationDays: _toInt(json['durationDays'], 1),
      minPax: _toInt(json['minPax'], 1),
      maxPax: _toInt(json['maxPax'], 15),
      inclusions: (json['inclusions'] ?? '').toString(),
      exclusions: (json['exclusions'] ?? '').toString(),
      status: (json['status'] ?? 'ACTIVE').toString(),
      imageUrl: (json['imageUrl'] ?? '').toString(),
      destinations: destList,
      services: svcList,
    );
  }
}

class StakeholderModel {
  final int profileId;
  final int userId;
  final String fullName;
  final String businessName;
  final String category;
  final int barangayId;
  final String barangayName;
  final String municipalityName;
  final String accreditationNo;
  final String contactNumber;
  final int yearsExperience;
  final String verificationStatus;
  final double averageRating;

  const StakeholderModel({
    required this.profileId,
    required this.userId,
    required this.fullName,
    required this.businessName,
    required this.category,
    required this.barangayId,
    required this.barangayName,
    required this.municipalityName,
    required this.accreditationNo,
    required this.contactNumber,
    required this.yearsExperience,
    required this.verificationStatus,
    required this.averageRating,
  });

  factory StakeholderModel.fromJson(Map<String, dynamic> json) {
    return StakeholderModel(
      profileId: _toInt(json['profileId']),
      userId: _toInt(json['userId']),
      fullName: (json['fullName'] ?? json['ownerName'] ?? '').toString(),
      businessName: (json['businessName'] ?? '').toString(),
      category: (json['category'] ?? 'TOUR_GUIDE').toString(),
      barangayId: _toInt(json['barangayId'], 1),
      barangayName: (json['barangayName'] ?? '').toString(),
      municipalityName: (json['municipalityName'] ?? 'Biliran').toString(),
      accreditationNo: (json['accreditationNo'] ?? '').toString(),
      contactNumber: (json['contactNumber'] ?? '').toString(),
      yearsExperience: _toInt(json['yearsExperience'], 1),
      verificationStatus: (json['verificationStatus'] ?? 'PENDING').toString(),
      averageRating: _toDouble(json['averageRating'], 4.8),
    );
  }
}

class TourismServiceModel {
  final int serviceId;
  final int stakeholderId;
  final String stakeholderName;
  final String serviceName;
  final String serviceCategory;
  final String description;
  final double basePrice;
  final String pricingUnit;
  final int maxCapacity;
  final bool available;

  const TourismServiceModel({
    required this.serviceId,
    required this.stakeholderId,
    required this.stakeholderName,
    required this.serviceName,
    required this.serviceCategory,
    required this.description,
    required this.basePrice,
    required this.pricingUnit,
    required this.maxCapacity,
    required this.available,
  });

  factory TourismServiceModel.fromJson(Map<String, dynamic> json) {
    return TourismServiceModel(
      serviceId: _toInt(json['serviceId']),
      stakeholderId: _toInt(json['stakeholderId']),
      stakeholderName: (json['stakeholderName'] ?? json['businessName'] ?? 'Community Provider').toString(),
      serviceName: (json['serviceName'] ?? '').toString(),
      serviceCategory: (json['serviceCategory'] ?? 'GUIDE_SERVICE').toString(),
      description: (json['description'] ?? '').toString(),
      basePrice: _toDouble(json['basePrice']),
      pricingUnit: (json['pricingUnit'] ?? 'PER_PERSON').toString(),
      maxCapacity: _toInt(json['maxCapacity'], 10),
      available: json['available'] == null ? (json['isAvailable'] != false) : (json['available'] == true),
    );
  }
}

class BookingItemModel {
  final int itemId;
  final int bookingId;
  final int serviceId;
  final String serviceName;
  final int stakeholderId;
  final String stakeholderName;
  final double unitPrice;
  final int quantity;
  final double subtotal;
  final String status;

  const BookingItemModel({
    required this.itemId,
    required this.bookingId,
    required this.serviceId,
    required this.serviceName,
    required this.stakeholderId,
    required this.stakeholderName,
    required this.unitPrice,
    required this.quantity,
    required this.subtotal,
    required this.status,
  });

  factory BookingItemModel.fromJson(Map<String, dynamic> json) {
    return BookingItemModel(
      itemId: _toInt(json['itemId']),
      bookingId: _toInt(json['bookingId']),
      serviceId: _toInt(json['serviceId']),
      serviceName: (json['serviceName'] ?? 'Community Service').toString(),
      stakeholderId: _toInt(json['stakeholderId']),
      stakeholderName: (json['stakeholderName'] ?? '').toString(),
      unitPrice: _toDouble(json['unitPrice']),
      quantity: _toInt(json['quantity'], 1),
      subtotal: _toDouble(json['subtotal']),
      status: (json['status'] ?? 'PENDING').toString(),
    );
  }
}

class BookingModel {
  final int bookingId;
  final String bookingReference;
  final int touristId;
  final String touristName;
  final int? packageId;
  final String packageName;
  final String travelDateStart;
  final String travelDateEnd;
  final int numberOfPax;
  final double totalAmount;
  final String status;
  final String customNotes;
  final String createdAt;
  final List<BookingItemModel> items;

  const BookingModel({
    required this.bookingId,
    required this.bookingReference,
    required this.touristId,
    required this.touristName,
    this.packageId,
    required this.packageName,
    required this.travelDateStart,
    required this.travelDateEnd,
    required this.numberOfPax,
    required this.totalAmount,
    required this.status,
    required this.customNotes,
    required this.createdAt,
    this.items = const [],
  });

  factory BookingModel.fromJson(Map<String, dynamic> json) {
    final parsedItems = <BookingItemModel>[];
    if (json['items'] is List) {
      for (final raw in json['items']) {
        if (raw is Map<String, dynamic>) {
          parsedItems.add(BookingItemModel.fromJson(raw));
        }
      }
    }
    return BookingModel(
      bookingId: _toInt(json['bookingId']),
      bookingReference: (json['bookingReference'] ?? 'LKB-${json['bookingId'] ?? ''}').toString(),
      touristId: _toInt(json['touristId']),
      touristName: (json['touristName'] ?? 'Tourist').toString(),
      packageId: json['packageId'] != null ? _toInt(json['packageId']) : null,
      packageName: (json['packageName'] ?? 'Custom Community Itinerary').toString(),
      travelDateStart: (json['travelDateStart'] ?? '').toString(),
      travelDateEnd: (json['travelDateEnd'] ?? '').toString(),
      numberOfPax: _toInt(json['numberOfPax'], 1),
      totalAmount: _toDouble(json['totalAmount']),
      status: (json['status'] ?? 'PENDING').toString(),
      customNotes: (json['customNotes'] ?? '').toString(),
      createdAt: (json['createdAt'] ?? '').toString(),
      items: parsedItems,
    );
  }
}

class AnnouncementModel {
  final int announcementId;
  final String title;
  final String content;
  final String category;
  final String publishDate;
  final String? expiryDate;
  final String status;
  final String authorName;

  const AnnouncementModel({
    required this.announcementId,
    required this.title,
    required this.content,
    required this.category,
    required this.publishDate,
    this.expiryDate,
    required this.status,
    required this.authorName,
  });

  factory AnnouncementModel.fromJson(Map<String, dynamic> json) {
    return AnnouncementModel(
      announcementId: _toInt(json['announcementId']),
      title: (json['title'] ?? '').toString(),
      content: (json['content'] ?? '').toString(),
      category: (json['category'] ?? 'ANNOUNCEMENT').toString(),
      publishDate: (json['publishDate'] ?? '').toString(),
      expiryDate: json['expiryDate']?.toString(),
      status: (json['status'] ?? 'PUBLISHED').toString(),
      authorName: (json['authorName'] ?? 'Provincial Tourism Office').toString(),
    );
  }
}

class ReviewModel {
  final int reviewId;
  final int bookingId;
  final int touristId;
  final String touristName;
  final String targetType;
  final int targetId;
  final String targetName;
  final int rating;
  final String comment;
  final String createdAt;

  const ReviewModel({
    required this.reviewId,
    required this.bookingId,
    required this.touristId,
    required this.touristName,
    required this.targetType,
    required this.targetId,
    required this.targetName,
    required this.rating,
    required this.comment,
    required this.createdAt,
  });

  factory ReviewModel.fromJson(Map<String, dynamic> json) {
    return ReviewModel(
      reviewId: _toInt(json['reviewId']),
      bookingId: _toInt(json['bookingId']),
      touristId: _toInt(json['touristId']),
      touristName: (json['touristName'] ?? 'Verified Tourist').toString(),
      targetType: (json['targetType'] ?? 'DESTINATION').toString(),
      targetId: _toInt(json['targetId']),
      targetName: (json['targetName'] ?? 'Biliran Experience').toString(),
      rating: _toInt(json['rating'], 5),
      comment: (json['comment'] ?? '').toString(),
      createdAt: (json['createdAt'] ?? '').toString(),
    );
  }
}

class BarangayModel {
  final int barangayId;
  final String name;
  final String municipality;
  final String contactPerson;
  final String contactNumber;

  const BarangayModel({
    required this.barangayId,
    required this.name,
    required this.municipality,
    required this.contactPerson,
    required this.contactNumber,
  });

  factory BarangayModel.fromJson(Map<String, dynamic> json) {
    return BarangayModel(
      barangayId: _toInt(json['barangayId']),
      name: (json['name'] ?? json['barangayName'] ?? '').toString(),
      municipality: (json['municipality'] ?? json['municipalityName'] ?? 'Biliran').toString(),
      contactPerson: (json['contactPerson'] ?? '').toString(),
      contactNumber: (json['contactNumber'] ?? '').toString(),
    );
  }
}

class AuditLogModel {
  final int logId;
  final String username;
  final String action;
  final String entityType;
  final String ipAddress;
  final String details;
  final String timestamp;

  const AuditLogModel({
    required this.logId,
    required this.username,
    required this.action,
    required this.entityType,
    required this.ipAddress,
    required this.details,
    required this.timestamp,
  });

  factory AuditLogModel.fromJson(Map<String, dynamic> json) {
    return AuditLogModel(
      logId: _toInt(json['logId']),
      username: (json['username'] ?? 'system').toString(),
      action: (json['action'] ?? '').toString(),
      entityType: (json['entityType'] ?? '').toString(),
      ipAddress: (json['ipAddress'] ?? '127.0.0.1').toString(),
      details: (json['details'] ?? '').toString(),
      timestamp: (json['timestamp'] ?? json['createdAt'] ?? '').toString(),
    );
  }
}

class SystemSettingsModel {
  final String appName;
  final String province;
  final int capacityThreshold;
  final String currency;
  final String broadcastMsg;

  const SystemSettingsModel({
    required this.appName,
    required this.province,
    required this.capacityThreshold,
    required this.currency,
    required this.broadcastMsg,
  });

  factory SystemSettingsModel.fromJson(Map<String, dynamic> json) {
    return SystemSettingsModel(
      appName: (json['appName'] ?? 'Lakbay Lokal - Biliran Tourism Information System').toString(),
      province: (json['province'] ?? 'Biliran Province, Region VIII, Philippines').toString(),
      capacityThreshold: _toInt(json['capacityThreshold'], 85),
      currency: (json['currency'] ?? 'PHP (Philippine Peso ₱)').toString(),
      broadcastMsg: (json['broadcastMsg'] ??
              'Welcome to Lakbay Lokal! Experience Sambawan Island, Tinago Falls, and community-guided circuits in Biliran Province.')
          .toString(),
    );
  }

  static const List<String> currencyOptions = [
    'PHP (Philippine Peso ₱)',
    'USD (US Dollar \$)',
    'EUR (Euro €)',
    'JPY (Japanese Yen ¥)',
    'SGD (Singapore Dollar S\$)',
    'AUD (Australian Dollar A\$)',
  ];
}

int _toInt(dynamic value, [int fallback = 0]) {
  if (value == null) return fallback;
  if (value is int) return value;
  if (value is num) return value.toInt();
  return int.tryParse(value.toString()) ?? fallback;
}

double _toDouble(dynamic value, [double fallback = 0.0]) {
  if (value == null) return fallback;
  if (value is double) return value;
  if (value is num) return value.toDouble();
  return double.tryParse(value.toString()) ?? fallback;
}
