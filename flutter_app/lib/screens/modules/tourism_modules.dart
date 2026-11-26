import 'package:flutter/material.dart';

import '../../models/lakbay_models.dart';
import '../../services/api_service.dart';
import '../../theme/lakbay_theme.dart';

/// Modules 1, 2, 3, 4, 5, 6, 7, and 11:
/// Overview, Destinations, Announcements, Stakeholders, Services/Profile,
/// Packages & Booking Builder, Bookings Management, and Reviews.
class TourismModulesView extends StatefulWidget {
  final String activeModuleId;
  final UserAccount currentUser;
  final SystemSettingsModel settings;
  final ValueChanged<String> onNavigateModule;
  final VoidCallback onRefreshGlobal;

  const TourismModulesView({
    super.key,
    required this.activeModuleId,
    required this.currentUser,
    required this.settings,
    required this.onNavigateModule,
    required this.onRefreshGlobal,
  });

  @override
  State<TourismModulesView> createState() => _TourismModulesViewState();
}

class _TourismModulesViewState extends State<TourismModulesView> {
  bool _loading = true;
  Map<String, dynamic> _kpis = {};
  List<DestinationModel> _destinations = [];
  List<TourismPackageModel> _packages = [];
  List<AnnouncementModel> _announcements = [];
  List<StakeholderModel> _stakeholders = [];
  List<TourismServiceModel> _services = [];
  List<BookingModel> _bookings = [];
  List<ReviewModel> _reviews = [];
  List<BarangayModel> _barangays = [];
  StakeholderModel? _myProfile;

  // Filters
  String _destSearch = '';
  String _destCategory = 'ALL';
  String _stkCategory = 'ALL';
  String _bookingStatusFilter = 'ALL';

  // Custom itinerary builder state
  final Set<int> _selectedServiceIds = {};
  int _builderPax = 2;
  String _builderStartDate = '2026-11-20';
  String _builderEndDate = '2026-11-22';
  final TextEditingController _builderNotesCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  @override
  void didUpdateWidget(covariant TourismModulesView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.activeModuleId != widget.activeModuleId) {
      _loadData();
    }
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final api = LakbayApiService.instance;
      final results = await Future.wait([
        api.getOverviewKpis(),
        api.getDestinations(),
        api.getPackages(),
        api.getAnnouncements(),
        api.getStakeholders(
          jurisdictionOnly: widget.currentUser.isBarangayOfficial,
        ),
        api.getServices(includeUnavailable: true),
        api.getBookings(),
        api.getReviews(),
        api.getBarangays(),
      ]);

      StakeholderModel? myProf;
      if (widget.currentUser.isStakeholder) {
        myProf = await api.getMyStakeholderProfile();
      }

      if (!mounted) return;
      setState(() {
        _kpis = results[0] as Map<String, dynamic>;
        _destinations = results[1] as List<DestinationModel>;
        _packages = results[2] as List<TourismPackageModel>;
        _announcements = results[3] as List<AnnouncementModel>;
        _stakeholders = results[4] as List<StakeholderModel>;
        _services = results[5] as List<TourismServiceModel>;
        _bookings = results[6] as List<BookingModel>;
        _reviews = results[7] as List<ReviewModel>;
        _barangays = results[8] as List<BarangayModel>;
        _myProfile = myProf;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _loading = false);
    }
  }

  void _showToast(String message, {bool isError = false}) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? const Color(0xFF991B1B) : LakbayTheme.primary800,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(48),
          child: CircularProgressIndicator(),
        ),
      );
    }

    switch (widget.activeModuleId) {
      case 'overview':
        return _buildOverviewPane();
      case 'destinations':
        return _buildDestinationsPane();
      case 'announcements':
        return _buildAnnouncementsPane();
      case 'stakeholders':
        return _buildStakeholdersPane();
      case 'services':
        return _buildServicesAndProfilePane();
      case 'packages':
        return _buildPackagesAndBuilderPane();
      case 'bookings':
        return _buildBookingsPane();
      case 'reviews':
        return _buildReviewsPane();
      default:
        return _buildOverviewPane();
    }
  }

  // ===========================================================================
  // MODULE 1: OVERVIEW DASHBOARD
  // ===========================================================================
  Widget _buildOverviewPane() {
    final totalDest = _kpis['activeDestinations'] ?? _destinations.length;
    final totalStk = _kpis['verifiedStakeholders'] ?? _stakeholders.length;
    final totalBookings = _kpis['totalBookings'] ?? _bookings.length;
    final totalVisitors = _kpis['totalTourists'] ?? 1420;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // KPI Cards Row
        Wrap(
          spacing: 16,
          runSpacing: 16,
          children: [
            _buildKpiCard(
              title: 'Active Attractions',
              value: '$totalDest',
              subtext: 'Validated across 8 municipalities',
              icon: Icons.landscape_rounded,
              tintBg: LakbayTheme.primary100,
              tintFg: LakbayTheme.primary800,
            ),
            _buildKpiCard(
              title: 'Community Stakeholders',
              value: '$totalStk',
              subtext: 'Guides, boatmen, drivers & hosts',
              icon: Icons.groups_rounded,
              tintBg: const Color(0xFFCCFBF1),
              tintFg: const Color(0xFF0F766E),
            ),
            _buildKpiCard(
              title: 'Recorded Bookings',
              value: '$totalBookings',
              subtext: 'Curated & custom itineraries',
              icon: Icons.calendar_month_rounded,
              tintBg: LakbayTheme.accent100,
              tintFg: LakbayTheme.accent600,
            ),
            _buildKpiCard(
              title: 'Tourist Arrivals',
              value: '$totalVisitors',
              subtext: 'Cumulative provincial footfall',
              icon: Icons.trending_up_rounded,
              tintBg: const Color(0xFFDBEAFE),
              tintFg: const Color(0xFF1D4ED8),
            ),
          ],
        ),
        const SizedBox(height: 20),
        // Hero Showcase Card
        Container(
          height: 230,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            gradient: const LinearGradient(
              colors: [LakbayTheme.primary950, LakbayTheme.primary800, LakbayTheme.secondary700],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            boxShadow: const [
              BoxShadow(
                color: Colors.black12,
                blurRadius: 16,
                offset: Offset(0, 4),
              ),
            ],
          ),
          padding: const EdgeInsets.all(24),
          alignment: Alignment.bottomLeft,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                decoration: BoxDecoration(
                  color: LakbayTheme.primary500.withOpacity(0.35),
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(color: Colors.white38),
                ),
                child: const Text(
                  'BILIRAN PROVINCE ECO-CIRCUIT',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Sambawan Island, Tinago Cascades & Higatangan Sandbar',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: 4),
              const Text(
                'Real-time MySQL synchronization between Tourists, Community Stakeholders, Barangay Officials, and LGUs.',
                style: TextStyle(color: Colors.white70, fontSize: 13.5),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),
        // Dual Overview Grid: Announcements + Recent Bookings
        LayoutBuilder(
          builder: (context, constraints) {
            final isWide = constraints.maxWidth >= 820;
            final leftCard = Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Latest Tourism Advisories & Bulletins',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                        ),
                        TextButton(
                          onPressed: () => widget.onNavigateModule('announcements'),
                          child: const Text('View All'),
                        ),
                      ],
                    ),
                    const Divider(),
                    ..._announcements.take(4).map(
                          (a) => ListTile(
                            contentPadding: EdgeInsets.zero,
                            leading: CircleAvatar(
                              backgroundColor: LakbayTheme.primary50,
                              child: const Icon(
                                Icons.campaign_rounded,
                                color: LakbayTheme.primary700,
                              ),
                            ),
                            title: Text(
                              a.title,
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                fontSize: 13.5,
                              ),
                            ),
                            subtitle: Text(
                              a.content,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 12.5),
                            ),
                            trailing: LakbayTheme.statusBadge(a.category),
                          ),
                        ),
                  ],
                ),
              ),
            );

            final rightCard = Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Recent Bookings & Community Trips',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                        ),
                        if (RoleMetadata.forRole(widget.currentUser.roleCode)
                            .allowedModules
                            .contains('bookings'))
                          TextButton(
                            onPressed: () => widget.onNavigateModule('bookings'),
                            child: const Text('Manage'),
                          ),
                      ],
                    ),
                    const Divider(),
                    if (_bookings.isEmpty)
                      const Padding(
                        padding: EdgeInsets.all(16),
                        child: Text('No bookings recorded yet.'),
                      )
                    else
                      ..._bookings.take(4).map(
                            (b) => ListTile(
                              contentPadding: EdgeInsets.zero,
                              leading: CircleAvatar(
                                backgroundColor: LakbayTheme.accent50,
                                child: const Icon(
                                  Icons.luggage_rounded,
                                  color: LakbayTheme.accent600,
                                ),
                              ),
                              title: Text(
                                '${b.bookingReference} • ${b.packageName}',
                                style: const TextStyle(
                                  fontWeight: FontWeight.w700,
                                  fontSize: 13.5,
                                ),
                              ),
                              subtitle: Text(
                                '${b.touristName} • ${b.travelDateStart} (${b.numberOfPax} pax) • ₱${b.totalAmount.toStringAsFixed(2)}',
                                style: const TextStyle(fontSize: 12),
                              ),
                              trailing: LakbayTheme.statusBadge(b.status),
                            ),
                          ),
                  ],
                ),
              ),
            );

            if (isWide) {
              return Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(child: leftCard),
                  const SizedBox(width: 18),
                  Expanded(child: rightCard),
                ],
              );
            }
            return Column(
              children: [
                leftCard,
                const SizedBox(height: 16),
                rightCard,
              ],
            );
          },
        ),
      ],
    );
  }

  Widget _buildKpiCard({
    required String title,
    required String value,
    required String subtext,
    required IconData icon,
    required Color tintBg,
    required Color tintFg,
  }) {
    return SizedBox(
      width: 265,
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  color: tintBg,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: tintFg, size: 26),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title.toUpperCase(),
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        color: LakbayTheme.slate500,
                      ),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      value,
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.w800,
                        color: LakbayTheme.slate900,
                      ),
                    ),
                    Text(
                      subtext,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 11.5,
                        color: LakbayTheme.slate500,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 2: DESTINATIONS BROWSER & MANAGEMENT
  // ===========================================================================
  Widget _buildDestinationsPane() {
    final canManage = widget.currentUser.isBarangayOfficial ||
        widget.currentUser.isLguOfficer ||
        widget.currentUser.isTourismOffice ||
        widget.currentUser.isAdmin;
    final canDelete = widget.currentUser.isTourismOffice || widget.currentUser.isAdmin;

    final filtered = _destinations.where((d) {
      final matchesCat = _destCategory == 'ALL' || d.category == _destCategory;
      final q = _destSearch.trim().toLowerCase();
      final matchesQ = q.isEmpty ||
          d.name.toLowerCase().contains(q) ||
          d.barangayName.toLowerCase().contains(q) ||
          d.municipalityName.toLowerCase().contains(q);
      return matchesCat && matchesQ;
    }).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Wrap(
          spacing: 12,
          runSpacing: 12,
          alignment: WrapAlignment.spaceBetween,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            SizedBox(
              width: 340,
              child: TextField(
                onChanged: (v) => setState(() => _destSearch = v),
                decoration: const InputDecoration(
                  hintText: 'Search destination name or barangay...',
                  prefixIcon: Icon(Icons.search_rounded),
                ),
              ),
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                DropdownButton<String>(
                  value: _destCategory,
                  items: const [
                    DropdownMenuItem(value: 'ALL', child: Text('All Categories')),
                    DropdownMenuItem(value: 'ISLAND_BEACH', child: Text('Islands & Beaches')),
                    DropdownMenuItem(value: 'WATERFALL', child: Text('Waterfalls')),
                    DropdownMenuItem(value: 'MOUNTAIN_ECO', child: Text('Mountain & Eco')),
                    DropdownMenuItem(value: 'HERITAGE_CULTURAL', child: Text('Heritage & Cultural')),
                  ],
                  onChanged: (v) {
                    if (v != null) setState(() => _destCategory = v);
                  },
                ),
                if (canManage) ...[
                  const SizedBox(width: 12),
                  ElevatedButton.icon(
                    onPressed: () => _openDestinationDialog(),
                    icon: const Icon(Icons.add_location_alt_rounded, size: 18),
                    label: const Text('Add Destination'),
                  ),
                ],
              ],
            ),
          ],
        ),
        const SizedBox(height: 18),
        LayoutBuilder(
          builder: (context, constraints) {
            final cols = constraints.maxWidth >= 1050
                ? 3
                : (constraints.maxWidth >= 660 ? 2 : 1);
            return GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: filtered.length,
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: cols,
                crossAxisSpacing: 18,
                mainAxisSpacing: 18,
                mainAxisExtent: canManage ? 390 : 340,
              ),
              itemBuilder: (context, idx) {
                final d = filtered[idx];
                return Card(
                  clipBehavior: Clip.antiAlias,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      SizedBox(
                        height: 120,
                        child: Stack(
                          fit: StackFit.expand,
                          children: [
                            Container(
                              decoration: const BoxDecoration(
                                gradient: LinearGradient(
                                  colors: [LakbayTheme.primary700, LakbayTheme.primary900],
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                ),
                              ),
                              child: Center(
                                child: Icon(
                                  _destinationCategoryIcon(d.category),
                                  size: 46,
                                  color: Colors.white.withOpacity(0.85),
                                ),
                              ),
                            ),
                            Positioned(
                              top: 10,
                              left: 10,
                              child: LakbayTheme.statusBadge(d.category.replaceAll('_', ' ')),
                            ),
                            Positioned(
                              top: 10,
                              right: 10,
                              child: Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                decoration: BoxDecoration(
                                  color: Colors.white.withOpacity(0.92),
                                  borderRadius: BorderRadius.circular(999),
                                ),
                                child: Row(
                                  children: [
                                    const Icon(
                                      Icons.star_rounded,
                                      size: 15,
                                      color: LakbayTheme.accent600,
                                    ),
                                    const SizedBox(width: 3),
                                    Text(
                                      d.averageRating.toStringAsFixed(1),
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w700,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                d.name,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              const SizedBox(height: 3),
                              Text(
                                'Brgy. ${d.barangayName}, ${d.municipalityName} • ${d.operatingHours}',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: LakbayTheme.slate500,
                                ),
                              ),
                              const SizedBox(height: 8),
                              Expanded(
                                child: Text(
                                  d.description,
                                  maxLines: 3,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 12.8,
                                    color: LakbayTheme.slate600,
                                  ),
                                ),
                              ),
                              const Divider(height: 16),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    d.entranceFee > 0
                                        ? '₱${d.entranceFee.toStringAsFixed(2)} / pax'
                                        : 'Free Entry',
                                    style: const TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.w800,
                                      color: LakbayTheme.primary800,
                                    ),
                                  ),
                                  if (widget.currentUser.isTourist)
                                    ElevatedButton(
                                      onPressed: () => widget.onNavigateModule('packages'),
                                      style: ElevatedButton.styleFrom(
                                        padding: const EdgeInsets.symmetric(
                                          horizontal: 12,
                                          vertical: 8,
                                        ),
                                      ),
                                      child: const Text('Book Trip'),
                                    ),
                                ],
                              ),
                              if (canManage) ...[
                                const SizedBox(height: 8),
                                Row(
                                  children: [
                                    Expanded(
                                      child: OutlinedButton.icon(
                                        onPressed: () => _openDestinationDialog(existing: d),
                                        icon: const Icon(Icons.edit_outlined, size: 15),
                                        label: const Text('Edit'),
                                      ),
                                    ),
                                    const SizedBox(width: 6),
                                    Expanded(
                                      child: OutlinedButton.icon(
                                        onPressed: () async {
                                          final res = await LakbayApiService.instance
                                              .validateDestination(d.destinationId);
                                          _showToast(
                                            (res['message'] ?? 'Validated').toString(),
                                            isError: res['success'] != true,
                                          );
                                          _loadData();
                                        },
                                        icon: const Icon(
                                          Icons.verified_outlined,
                                          size: 15,
                                          color: LakbayTheme.primary700,
                                        ),
                                        label: const Text('Validate'),
                                      ),
                                    ),
                                    if (canDelete) ...[
                                      const SizedBox(width: 6),
                                      IconButton(
                                        tooltip: 'Delete Destination',
                                        onPressed: () async {
                                          final res = await LakbayApiService.instance
                                              .deleteDestination(d.destinationId);
                                          _showToast(
                                            (res['message'] ?? 'Deleted').toString(),
                                            isError: res['success'] != true,
                                          );
                                          _loadData();
                                        },
                                        icon: const Icon(
                                          Icons.delete_outline_rounded,
                                          color: LakbayTheme.statusDanger,
                                        ),
                                      ),
                                    ],
                                  ],
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              },
            );
          },
        ),
      ],
    );
  }

  IconData _destinationCategoryIcon(String category) {
    final upper = category.toUpperCase();
    if (upper.contains('WATERFALL')) return Icons.water_drop_rounded;
    if (upper.contains('ISLAND') || upper.contains('BEACH')) return Icons.beach_access_rounded;
    if (upper.contains('SPRING') || upper.contains('HOT')) return Icons.hot_tub_rounded;
    if (upper.contains('CULTURAL') || upper.contains('HERITAGE') || upper.contains('POTTERY')) {
      return Icons.account_balance_rounded;
    }
    return Icons.landscape_rounded;
  }

  Future<void> _openDestinationDialog({DestinationModel? existing}) async {
    final nameCtrl = TextEditingController(text: existing?.name ?? '');
    final descCtrl = TextEditingController(text: existing?.description ?? '');
    final feeCtrl = TextEditingController(
      text: existing != null ? existing.entranceFee.toStringAsFixed(0) : '50',
    );
    final hoursCtrl = TextEditingController(
      text: existing?.operatingHours ?? '6:00 AM - 5:00 PM',
    );
    String category = existing?.category ?? 'ISLAND_BEACH';
    int barangayId = existing?.barangayId ??
        widget.currentUser.barangayId ??
        (_barangays.isNotEmpty ? _barangays.first.barangayId : 1);

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: Text(existing == null ? 'Register New Destination' : 'Edit Destination'),
          content: SizedBox(
            width: 480,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(labelText: 'Destination Name'),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<String>(
                    value: category,
                    decoration: const InputDecoration(labelText: 'Category'),
                    items: const [
                      DropdownMenuItem(value: 'ISLAND_BEACH', child: Text('Island & Beach')),
                      DropdownMenuItem(value: 'WATERFALL', child: Text('Waterfall & Spring')),
                      DropdownMenuItem(value: 'MOUNTAIN_ECO', child: Text('Mountain & Eco-Park')),
                      DropdownMenuItem(value: 'HERITAGE_CULTURAL', child: Text('Cultural & Heritage')),
                    ],
                    onChanged: (v) {
                      if (v != null) setModal(() => category = v);
                    },
                  ),
                  const SizedBox(height: 12),
                  if (_barangays.isNotEmpty)
                    DropdownButtonFormField<int>(
                      value: _barangays.any((b) => b.barangayId == barangayId)
                          ? barangayId
                          : _barangays.first.barangayId,
                      decoration: const InputDecoration(labelText: 'Barangay Jurisdiction'),
                      items: _barangays
                          .map((b) => DropdownMenuItem(
                                value: b.barangayId,
                                child: Text('Brgy. ${b.name} (${b.municipality})'),
                              ))
                          .toList(),
                      onChanged: widget.currentUser.isBarangayOfficial
                          ? null
                          : (v) {
                              if (v != null) setModal(() => barangayId = v);
                            },
                    ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: feeCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Entrance Fee (PHP)'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: TextField(
                          controller: hoursCtrl,
                          decoration: const InputDecoration(labelText: 'Operating Hours'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: descCtrl,
                    maxLines: 3,
                    decoration: const InputDecoration(labelText: 'Description & Highlights'),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                if (nameCtrl.text.trim().isEmpty) return;
                final res = await LakbayApiService.instance.saveDestination({
                  if (existing != null) 'destinationId': existing.destinationId,
                  'name': nameCtrl.text.trim(),
                  'category': category,
                  'barangayId': barangayId,
                  'entranceFee': double.tryParse(feeCtrl.text) ?? 0,
                  'operatingHours': hoursCtrl.text.trim(),
                  'description': descCtrl.text.trim(),
                  'status': 'ACTIVE',
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Destination saved.').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Save Destination'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 3: TOURISM ANNOUNCEMENTS & ADVISORIES
  // ===========================================================================
  Widget _buildAnnouncementsPane() {
    final canEdit = widget.currentUser.isLguOfficer ||
        widget.currentUser.isTourismOffice ||
        widget.currentUser.isAdmin;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (canEdit)
          Align(
            alignment: Alignment.centerRight,
            child: ElevatedButton.icon(
              onPressed: () => _openAnnouncementDialog(),
              icon: const Icon(Icons.add_alert_rounded, size: 18),
              label: const Text('Publish Announcement'),
            ),
          ),
        const SizedBox(height: 14),
        ..._announcements.map(
          (a) => Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        LakbayTheme.statusBadge(a.category),
                        Text(
                          'Published: ${a.publishDate} • By ${a.authorName}',
                          style: const TextStyle(fontSize: 12, color: LakbayTheme.slate500),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    Text(
                      a.title,
                      style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      a.content,
                      style: const TextStyle(color: LakbayTheme.slate600, height: 1.45),
                    ),
                    if (canEdit) ...[
                      const Divider(height: 22),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          OutlinedButton.icon(
                            onPressed: () => _openAnnouncementDialog(existing: a),
                            icon: const Icon(Icons.edit_outlined, size: 15),
                            label: const Text('Edit'),
                          ),
                          const SizedBox(width: 8),
                          OutlinedButton.icon(
                            onPressed: () async {
                              final res = await LakbayApiService.instance
                                  .deleteAnnouncement(a.announcementId);
                              _showToast(
                                (res['message'] ?? 'Announcement deleted.').toString(),
                                isError: res['success'] != true,
                              );
                              _loadData();
                            },
                            icon: const Icon(
                              Icons.delete_outline_rounded,
                              size: 15,
                              color: LakbayTheme.statusDanger,
                            ),
                            label: const Text('Delete'),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openAnnouncementDialog({AnnouncementModel? existing}) async {
    final titleCtrl = TextEditingController(text: existing?.title ?? '');
    final contentCtrl = TextEditingController(text: existing?.content ?? '');
    String category = existing?.category ?? 'ANNOUNCEMENT';

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: Text(existing == null ? 'Publish Tourism Bulletin' : 'Edit Bulletin'),
          content: SizedBox(
            width: 460,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: titleCtrl,
                  decoration: const InputDecoration(labelText: 'Headline / Title'),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  value: ['ANNOUNCEMENT', 'EVENT', 'FESTIVAL', 'TRAVEL_GUIDELINE']
                          .contains(category)
                      ? category
                      : 'ANNOUNCEMENT',
                  decoration: const InputDecoration(labelText: 'Bulletin Category'),
                  items: const [
                    DropdownMenuItem(value: 'ANNOUNCEMENT', child: Text('General Announcement')),
                    DropdownMenuItem(value: 'TRAVEL_GUIDELINE', child: Text('Travel & Eco Guideline')),
                    DropdownMenuItem(value: 'FESTIVAL', child: Text('Festival & Cultural Event')),
                    DropdownMenuItem(value: 'EVENT', child: Text('Tourism Event')),
                  ],
                  onChanged: (v) {
                    if (v != null) setModal(() => category = v);
                  },
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: contentCtrl,
                  maxLines: 4,
                  decoration: const InputDecoration(labelText: 'Bulletin Content'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                if (titleCtrl.text.trim().isEmpty || contentCtrl.text.trim().isEmpty) return;
                final res = await LakbayApiService.instance.saveAnnouncement({
                  if (existing != null) 'announcementId': existing.announcementId,
                  'title': titleCtrl.text.trim(),
                  'category': category,
                  'content': contentCtrl.text.trim(),
                  'status': 'PUBLISHED',
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Saved').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Save Bulletin'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 4: LOCAL STAKEHOLDERS DIRECTORY & REGISTRATION
  // ===========================================================================
  Widget _buildStakeholdersPane() {
    final canRegister = widget.currentUser.isBarangayOfficial ||
        widget.currentUser.isTourismOffice ||
        widget.currentUser.isAdmin;

    final filtered = _stakeholders.where((s) {
      return _stkCategory == 'ALL' || s.category == _stkCategory;
    }).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Wrap(
          alignment: WrapAlignment.spaceBetween,
          crossAxisAlignment: WrapCrossAlignment.center,
          spacing: 12,
          runSpacing: 12,
          children: [
            DropdownButton<String>(
              value: _stkCategory,
              items: const [
                DropdownMenuItem(value: 'ALL', child: Text('All Stakeholder Categories')),
                DropdownMenuItem(value: 'TOUR_GUIDE', child: Text('Eco-Trek & Island Guides')),
                DropdownMenuItem(value: 'BOAT_OPERATOR', child: Text('Boat Operators')),
                DropdownMenuItem(value: 'TRANSPORT', child: Text('Habal-Habal / Transport')),
                DropdownMenuItem(value: 'HOMESTAY', child: Text('Community Homestays')),
              ],
              onChanged: (v) {
                if (v != null) setState(() => _stkCategory = v);
              },
            ),
            if (canRegister)
              ElevatedButton.icon(
                onPressed: _openRegisterStakeholderDialog,
                icon: const Icon(Icons.person_add_alt_1_rounded, size: 18),
                label: const Text('Register Community Stakeholder'),
              ),
          ],
        ),
        const SizedBox(height: 16),
        Card(
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              columns: const [
                DataColumn(label: Text('BUSINESS / SERVICE NAME')),
                DataColumn(label: Text('OWNER')),
                DataColumn(label: Text('CATEGORY')),
                DataColumn(label: Text('BARANGAY / LGU')),
                DataColumn(label: Text('CONTACT')),
                DataColumn(label: Text('ACCREDITATION')),
                DataColumn(label: Text('STATUS')),
              ],
              rows: filtered.map((s) {
                return DataRow(
                  cells: [
                    DataCell(Text(
                      s.businessName,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    )),
                    DataCell(Text(s.fullName)),
                    DataCell(Text(s.category.replaceAll('_', ' '))),
                    DataCell(Text('Brgy. ${s.barangayName}, ${s.municipalityName}')),
                    DataCell(Text(s.contactNumber)),
                    DataCell(Text(s.accreditationNo.isEmpty ? 'Pending' : s.accreditationNo)),
                    DataCell(LakbayTheme.statusBadge(s.verificationStatus)),
                  ],
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openRegisterStakeholderDialog() async {
    final ownerCtrl = TextEditingController();
    final unameCtrl = TextEditingController();
    final emailCtrl = TextEditingController();
    final bizCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final accredCtrl = TextEditingController(text: 'BLR-ACC-2026');
    String category = 'TOUR_GUIDE';
    int brgyId = widget.currentUser.barangayId ??
        (_barangays.isNotEmpty ? _barangays.first.barangayId : 1);

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: const Text('Register Community Stakeholder'),
          content: SizedBox(
            width: 460,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: ownerCtrl,
                    decoration: const InputDecoration(labelText: 'Stakeholder Full Name'),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: unameCtrl,
                          decoration: const InputDecoration(labelText: 'Login Username'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: TextField(
                          controller: emailCtrl,
                          decoration: const InputDecoration(labelText: 'Email Address'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: bizCtrl,
                    decoration: const InputDecoration(labelText: 'Business / Service Title'),
                  ),
                  const SizedBox(height: 10),
                  DropdownButtonFormField<String>(
                    value: category,
                    decoration: const InputDecoration(labelText: 'Service Category'),
                    items: const [
                      DropdownMenuItem(value: 'TOUR_GUIDE', child: Text('Tour Guide')),
                      DropdownMenuItem(value: 'BOAT_OPERATOR', child: Text('Boat Operator')),
                      DropdownMenuItem(value: 'TRANSPORT', child: Text('Local Transport')),
                      DropdownMenuItem(value: 'HOMESTAY', child: Text('Homestay Host')),
                    ],
                    onChanged: (v) {
                      if (v != null) setModal(() => category = v);
                    },
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: phoneCtrl,
                          decoration: const InputDecoration(labelText: 'Contact Number'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: TextField(
                          controller: accredCtrl,
                          decoration: const InputDecoration(labelText: 'Accreditation No.'),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                final res = await LakbayApiService.instance.saveStakeholderProfile({
                  'ownerName': ownerCtrl.text.trim(),
                  'username': unameCtrl.text.trim(),
                  'email': emailCtrl.text.trim(),
                  'businessName': bizCtrl.text.trim(),
                  'category': category,
                  'barangayId': brgyId,
                  'contactNumber': phoneCtrl.text.trim(),
                  'accreditationNo': accredCtrl.text.trim(),
                  'yearsExperience': 3,
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Stakeholder registered.').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Register Stakeholder'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 5: STAKEHOLDER SERVICES, AVAILABILITY & PROFILE MANAGEMENT
  // ===========================================================================
  Widget _buildServicesAndProfilePane() {
    final myStkId = _myProfile?.profileId ?? widget.currentUser.stakeholderProfileId;
    final displayedServices = widget.currentUser.isStakeholder && myStkId != null
        ? _services.where((s) => s.stakeholderId == myStkId).toList()
        : _services;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (_myProfile != null) ...[
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 28,
                    backgroundColor: LakbayTheme.primary100,
                    child: const Icon(
                      Icons.storefront_rounded,
                      color: LakbayTheme.primary800,
                      size: 28,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Text(
                              _myProfile!.businessName,
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                            const SizedBox(width: 10),
                            LakbayTheme.statusBadge(_myProfile!.verificationStatus),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Category: ${_myProfile!.category} • Accreditation: ${_myProfile!.accreditationNo} • Contact: ${_myProfile!.contactNumber}',
                          style: const TextStyle(color: LakbayTheme.slate600, fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                  OutlinedButton.icon(
                    onPressed: _openEditMyProfileDialog,
                    icon: const Icon(Icons.edit_outlined, size: 16),
                    label: const Text('Edit Profile'),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 18),
        ],
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              'Tourism Services, Rate Cards & Schedule Availability',
              style: TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
            ),
            ElevatedButton.icon(
              onPressed: () => _openServiceDialog(),
              icon: const Icon(Icons.add_rounded, size: 18),
              label: const Text('Add Service Listing'),
            ),
          ],
        ),
        const SizedBox(height: 14),
        Card(
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              columns: const [
                DataColumn(label: Text('SERVICE NAME')),
                DataColumn(label: Text('CATEGORY')),
                DataColumn(label: Text('PROVIDER')),
                DataColumn(label: Text('RATE (PHP)')),
                DataColumn(label: Text('CAPACITY')),
                DataColumn(label: Text('AVAILABILITY')),
                DataColumn(label: Text('ACTIONS')),
              ],
              rows: displayedServices.map((svc) {
                return DataRow(
                  cells: [
                    DataCell(Text(
                      svc.serviceName,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    )),
                    DataCell(Text(svc.serviceCategory.replaceAll('_', ' '))),
                    DataCell(Text(svc.stakeholderName)),
                    DataCell(Text(
                      '₱${svc.basePrice.toStringAsFixed(2)} / ${svc.pricingUnit}',
                      style: const TextStyle(
                        fontWeight: FontWeight.w700,
                        color: LakbayTheme.primary800,
                      ),
                    )),
                    DataCell(Text('${svc.maxCapacity} pax')),
                    DataCell(
                      Switch(
                        value: svc.available,
                        activeColor: LakbayTheme.primary600,
                        onChanged: (val) async {
                          final res = await LakbayApiService.instance.saveService({
                            'serviceId': svc.serviceId,
                            'stakeholderId': svc.stakeholderId,
                            'serviceName': svc.serviceName,
                            'serviceCategory': svc.serviceCategory,
                            'description': svc.description,
                            'basePrice': svc.basePrice,
                            'pricingUnit': svc.pricingUnit,
                            'maxCapacity': svc.maxCapacity,
                            'available': val,
                          });
                          _showToast(
                            val
                                ? 'Service marked AVAILABLE.'
                                : 'Service marked UNAVAILABLE.',
                            isError: res['success'] != true,
                          );
                          _loadData();
                        },
                      ),
                    ),
                    DataCell(
                      Row(
                        children: [
                          IconButton(
                            tooltip: 'Edit Service',
                            onPressed: () => _openServiceDialog(existing: svc),
                            icon: const Icon(Icons.edit_outlined, size: 18),
                          ),
                          IconButton(
                            tooltip: 'Delete Service',
                            onPressed: () async {
                              final res = await LakbayApiService.instance
                                  .deleteService(svc.serviceId);
                              _showToast(
                                (res['message'] ?? 'Deleted').toString(),
                                isError: res['success'] != true,
                              );
                              _loadData();
                            },
                            icon: const Icon(
                              Icons.delete_outline_rounded,
                              size: 18,
                              color: LakbayTheme.statusDanger,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openEditMyProfileDialog() async {
    if (_myProfile == null) return;
    final bizCtrl = TextEditingController(text: _myProfile!.businessName);
    final phoneCtrl = TextEditingController(text: _myProfile!.contactNumber);
    final accredCtrl = TextEditingController(text: _myProfile!.accreditationNo);
    final expCtrl = TextEditingController(text: '${_myProfile!.yearsExperience}');

    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Update Stakeholder Business Profile'),
        content: SizedBox(
          width: 440,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: bizCtrl,
                decoration: const InputDecoration(labelText: 'Business / Service Title'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: phoneCtrl,
                decoration: const InputDecoration(labelText: 'Contact Number'),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: accredCtrl,
                      decoration: const InputDecoration(labelText: 'Accreditation No.'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      controller: expCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(labelText: 'Years Experience'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              final res = await LakbayApiService.instance.saveStakeholderProfile({
                'profileId': _myProfile!.profileId,
                'businessName': bizCtrl.text.trim(),
                'contactNumber': phoneCtrl.text.trim(),
                'accreditationNo': accredCtrl.text.trim(),
                'yearsExperience': int.tryParse(expCtrl.text) ?? 1,
              });
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              _showToast(
                (res['message'] ?? 'Profile updated.').toString(),
                isError: res['success'] != true,
              );
              _loadData();
            },
            child: const Text('Save Profile'),
          ),
        ],
      ),
    );
  }

  Future<void> _openServiceDialog({TourismServiceModel? existing}) async {
    final nameCtrl = TextEditingController(text: existing?.serviceName ?? '');
    final descCtrl = TextEditingController(text: existing?.description ?? '');
    final priceCtrl = TextEditingController(
      text: existing != null ? existing.basePrice.toStringAsFixed(0) : '500',
    );
    final capCtrl = TextEditingController(
      text: existing != null ? '${existing.maxCapacity}' : '8',
    );
    String cat = existing?.serviceCategory ?? 'GUIDE_SERVICE';
    String unit = existing?.pricingUnit ?? 'PER_PERSON';

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: Text(existing == null ? 'Add Tourism Service' : 'Edit Tourism Service'),
          content: SizedBox(
            width: 460,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(labelText: 'Service Name'),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<String>(
                    value: ['GUIDE_SERVICE', 'BOAT_RENTAL', 'TRANSPORT', 'HOMESTAY']
                            .contains(cat)
                        ? cat
                        : 'GUIDE_SERVICE',
                    decoration: const InputDecoration(labelText: 'Service Category'),
                    items: const [
                      DropdownMenuItem(value: 'GUIDE_SERVICE', child: Text('Eco-Tour Guiding')),
                      DropdownMenuItem(value: 'BOAT_RENTAL', child: Text('Island Boat Charter')),
                      DropdownMenuItem(value: 'TRANSPORT', child: Text('Land / Habal-Habal Transfer')),
                      DropdownMenuItem(value: 'HOMESTAY', child: Text('Community Homestay')),
                    ],
                    onChanged: (v) {
                      if (v != null) setModal(() => cat = v);
                    },
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: priceCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Base Price (PHP)'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: ['PER_PERSON', 'PER_TRIP', 'PER_NIGHT'].contains(unit)
                              ? unit
                              : 'PER_PERSON',
                          decoration: const InputDecoration(labelText: 'Pricing Unit'),
                          items: const [
                            DropdownMenuItem(value: 'PER_PERSON', child: Text('Per Person')),
                            DropdownMenuItem(value: 'PER_TRIP', child: Text('Per Trip / Group')),
                            DropdownMenuItem(value: 'PER_NIGHT', child: Text('Per Night')),
                          ],
                          onChanged: (v) {
                            if (v != null) setModal(() => unit = v);
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: capCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: 'Maximum Capacity (Pax)'),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: descCtrl,
                    maxLines: 3,
                    decoration: const InputDecoration(labelText: 'Service Description'),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                if (nameCtrl.text.trim().isEmpty) return;
                final res = await LakbayApiService.instance.saveService({
                  if (existing != null) 'serviceId': existing.serviceId,
                  'stakeholderId': existing?.stakeholderId ??
                      _myProfile?.profileId ??
                      (_stakeholders.isNotEmpty ? _stakeholders.first.profileId : 1),
                  'serviceName': nameCtrl.text.trim(),
                  'serviceCategory': cat,
                  'basePrice': double.tryParse(priceCtrl.text) ?? 500,
                  'pricingUnit': unit,
                  'maxCapacity': int.tryParse(capCtrl.text) ?? 8,
                  'description': descCtrl.text.trim(),
                  'available': existing?.available ?? true,
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Service saved.').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Save Service'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 6: CURATED PACKAGES & CUSTOM ITINERARY BUILDER
  // ===========================================================================
  Widget _buildPackagesAndBuilderPane() {
    final canManagePkg = widget.currentUser.isTourismOffice || widget.currentUser.isAdmin;
    final availableServices = _services.where((s) => s.available).toList();

    double customTotal = 0;
    for (final s in availableServices) {
      if (_selectedServiceIds.contains(s.serviceId)) {
        final qty = s.pricingUnit == 'PER_PERSON' ? _builderPax : 1;
        customTotal += s.basePrice * qty;
      }
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              'Curated Biliran Community Tourism Packages',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
            ),
            if (canManagePkg)
              ElevatedButton.icon(
                onPressed: () => _openPackageDialog(),
                icon: const Icon(Icons.add_box_rounded, size: 18),
                label: const Text('Create Curated Package'),
              ),
          ],
        ),
        const SizedBox(height: 14),
        LayoutBuilder(
          builder: (context, constraints) {
            final cols = constraints.maxWidth >= 980
                ? 3
                : (constraints.maxWidth >= 640 ? 2 : 1);
            return GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: _packages.length,
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: cols,
                crossAxisSpacing: 18,
                mainAxisSpacing: 18,
                mainAxisExtent: canManagePkg ? 380 : 330,
              ),
              itemBuilder: (context, index) {
                final pkg = _packages[index];
                return Card(
                  clipBehavior: Clip.antiAlias,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      SizedBox(
                        height: 115,
                        child: Stack(
                          fit: StackFit.expand,
                          children: [
                            Container(
                              decoration: const BoxDecoration(
                                gradient: LinearGradient(
                                  colors: [LakbayTheme.primary800, LakbayTheme.secondary700],
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                ),
                              ),
                              child: const Center(
                                child: Icon(
                                  Icons.card_travel_rounded,
                                  size: 44,
                                  color: Colors.white70,
                                ),
                              ),
                            ),
                            Positioned(
                              top: 10,
                              left: 10,
                              child: LakbayTheme.statusBadge('${pkg.durationDays} Day Circuit'),
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                pkg.name,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                'Group Size: ${pkg.minPax}–${pkg.maxPax} pax • Includes: ${pkg.inclusions}',
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: LakbayTheme.slate500,
                                ),
                              ),
                              const SizedBox(height: 6),
                              Expanded(
                                child: Text(
                                  pkg.description,
                                  maxLines: 3,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 12.5,
                                    color: LakbayTheme.slate600,
                                  ),
                                ),
                              ),
                              const Divider(height: 16),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    '₱${pkg.pricePerPax.toStringAsFixed(0)} / pax',
                                    style: const TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w800,
                                      color: LakbayTheme.primary800,
                                    ),
                                  ),
                                  ElevatedButton.icon(
                                    onPressed: () => _openBookPackageDialog(pkg),
                                    icon: const Icon(Icons.check_circle_outline, size: 16),
                                    label: const Text('Book Package'),
                                  ),
                                ],
                              ),
                              if (canManagePkg) ...[
                                const SizedBox(height: 8),
                                Row(
                                  children: [
                                    Expanded(
                                      child: OutlinedButton(
                                        onPressed: () => _openPackageDialog(existing: pkg),
                                        child: const Text('Edit'),
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Expanded(
                                      child: OutlinedButton(
                                        onPressed: () async {
                                          final res = await LakbayApiService.instance
                                              .deletePackage(pkg.packageId);
                                          _showToast(
                                            (res['message'] ?? 'Package deleted').toString(),
                                            isError: res['success'] != true,
                                          );
                                          _loadData();
                                        },
                                        child: const Text(
                                          'Delete',
                                          style: TextStyle(color: LakbayTheme.statusDanger),
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              },
            );
          },
        ),
        const SizedBox(height: 28),
        // Custom Itinerary Builder
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Custom Community Itinerary Builder',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Select accredited local guides, boat operators, and homestays to build a custom Biliran booking.',
                  style: TextStyle(color: LakbayTheme.slate600, fontSize: 13),
                ),
                const SizedBox(height: 16),
                ...availableServices.map((svc) {
                  final selected = _selectedServiceIds.contains(svc.serviceId);
                  return CheckboxListTile(
                    value: selected,
                    activeColor: LakbayTheme.primary700,
                    onChanged: (val) {
                      setState(() {
                        if (val == true) {
                          _selectedServiceIds.add(svc.serviceId);
                        } else {
                          _selectedServiceIds.remove(svc.serviceId);
                        }
                      });
                    },
                    title: Text(
                      '${svc.serviceName} (${svc.stakeholderName})',
                      style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                    ),
                    subtitle: Text(
                      '${svc.serviceCategory.replaceAll('_', ' ')} • ₱${svc.basePrice.toStringAsFixed(0)} / ${svc.pricingUnit}',
                    ),
                  );
                }),
                const Divider(height: 24),
                Wrap(
                  spacing: 16,
                  runSpacing: 12,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  alignment: WrapAlignment.spaceBetween,
                  children: [
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Text('Travelers (Pax): ', style: TextStyle(fontWeight: FontWeight.w700)),
                        IconButton(
                          onPressed: _builderPax > 1 ? () => setState(() => _builderPax--) : null,
                          icon: const Icon(Icons.remove_circle_outline),
                        ),
                        Text('$_builderPax', style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
                        IconButton(
                          onPressed: () => setState(() => _builderPax++),
                          icon: const Icon(Icons.add_circle_outline),
                        ),
                      ],
                    ),
                    Text(
                      'Estimated Total: ₱${customTotal.toStringAsFixed(2)}',
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        color: LakbayTheme.primary800,
                      ),
                    ),
                    ElevatedButton.icon(
                      onPressed: _selectedServiceIds.isEmpty ? null : () => _submitCustomBooking(customTotal),
                      icon: const Icon(Icons.send_rounded, size: 17),
                      label: const Text('Submit Custom Booking'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openBookPackageDialog(TourismPackageModel pkg) async {
    int pax = pkg.minPax > 0 ? pkg.minPax : 2;
    final startCtrl = TextEditingController(text: '2026-11-25');
    final endCtrl = TextEditingController(text: '2026-11-26');
    final notesCtrl = TextEditingController();

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: Text('Book Package: ${pkg.name}'),
          content: SizedBox(
            width: 440,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Rate: ₱${pkg.pricePerPax.toStringAsFixed(2)} / pax • Total: ₱${(pkg.pricePerPax * pax).toStringAsFixed(2)}',
                  style: const TextStyle(
                    fontWeight: FontWeight.w800,
                    color: LakbayTheme.primary800,
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: startCtrl,
                        decoration: const InputDecoration(labelText: 'Start Date (YYYY-MM-DD)'),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextField(
                        controller: endCtrl,
                        decoration: const InputDecoration(labelText: 'End Date (YYYY-MM-DD)'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    const Text('Number of Pax: ', style: TextStyle(fontWeight: FontWeight.w600)),
                    IconButton(
                      onPressed: pax > 1 ? () => setModal(() => pax--) : null,
                      icon: const Icon(Icons.remove_circle_outline),
                    ),
                    Text('$pax', style: const TextStyle(fontWeight: FontWeight.w800)),
                    IconButton(
                      onPressed: () => setModal(() => pax++),
                      icon: const Icon(Icons.add_circle_outline),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: notesCtrl,
                  maxLines: 2,
                  decoration: const InputDecoration(labelText: 'Special Requests / Notes'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                final res = await LakbayApiService.instance.createBooking({
                  'packageId': pkg.packageId,
                  'travelDateStart': startCtrl.text.trim(),
                  'travelDateEnd': endCtrl.text.trim(),
                  'numberOfPax': pax,
                  'totalAmount': pkg.pricePerPax * pax,
                  'customNotes': notesCtrl.text.trim(),
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Booking submitted!').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Confirm Booking'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _submitCustomBooking(double totalAmount) async {
    final items = <Map<String, dynamic>>[];
    for (final svc in _services) {
      if (_selectedServiceIds.contains(svc.serviceId)) {
        final qty = svc.pricingUnit == 'PER_PERSON' ? _builderPax : 1;
        items.add({
          'serviceId': svc.serviceId,
          'stakeholderId': svc.stakeholderId,
          'unitPrice': svc.basePrice,
          'quantity': qty,
          'subtotal': svc.basePrice * qty,
        });
      }
    }

    final res = await LakbayApiService.instance.createBooking({
      'travelDateStart': _builderStartDate,
      'travelDateEnd': _builderEndDate,
      'numberOfPax': _builderPax,
      'totalAmount': totalAmount,
      'customNotes': _builderNotesCtrl.text.trim(),
      'items': items,
    });
    _showToast(
      (res['message'] ?? 'Custom booking created!').toString(),
      isError: res['success'] != true,
    );
    if (res['success'] == true) {
      setState(() => _selectedServiceIds.clear());
      _loadData();
    }
  }

  Future<void> _openPackageDialog({TourismPackageModel? existing}) async {
    final nameCtrl = TextEditingController(text: existing?.name ?? '');
    final descCtrl = TextEditingController(text: existing?.description ?? '');
    final priceCtrl = TextEditingController(
      text: existing != null ? existing.pricePerPax.toStringAsFixed(0) : '1850',
    );
    final daysCtrl = TextEditingController(
      text: existing != null ? '${existing.durationDays}' : '2',
    );
    final incCtrl = TextEditingController(
      text: existing?.inclusions ?? 'Accredited Tour Guide, Boat Transfer, Entrance Fees',
    );

    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(existing == null ? 'Create Curated Package' : 'Edit Tourism Package'),
        content: SizedBox(
          width: 460,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(labelText: 'Package Name'),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: priceCtrl,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: 'Price Per Pax (PHP)'),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextField(
                        controller: daysCtrl,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: 'Duration (Days)'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: incCtrl,
                  decoration: const InputDecoration(labelText: 'Package Inclusions'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: descCtrl,
                  maxLines: 3,
                  decoration: const InputDecoration(labelText: 'Itinerary Description'),
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              if (nameCtrl.text.trim().isEmpty) return;
              final res = await LakbayApiService.instance.savePackage({
                if (existing != null) 'packageId': existing.packageId,
                'name': nameCtrl.text.trim(),
                'description': descCtrl.text.trim(),
                'pricePerPax': double.tryParse(priceCtrl.text) ?? 1500,
                'durationDays': int.tryParse(daysCtrl.text) ?? 1,
                'minPax': 2,
                'maxPax': 15,
                'inclusions': incCtrl.text.trim(),
                'status': 'ACTIVE',
                'destinationIds': _destinations.take(2).map((d) => d.destinationId).toList(),
                'serviceIds': _services.take(2).map((s) => s.serviceId).toList(),
              });
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              _showToast(
                (res['message'] ?? 'Package saved.').toString(),
                isError: res['success'] != true,
              );
              _loadData();
            },
            child: const Text('Save Package'),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // MODULE 7: BOOKINGS & RESERVATIONS MANAGEMENT
  // ===========================================================================
  Widget _buildBookingsPane() {
    final filtered = _bookings.where((b) {
      return _bookingStatusFilter == 'ALL' || b.status.toUpperCase() == _bookingStatusFilter;
    }).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            DropdownButton<String>(
              value: _bookingStatusFilter,
              items: const [
                DropdownMenuItem(value: 'ALL', child: Text('All Booking Statuses')),
                DropdownMenuItem(value: 'PENDING', child: Text('Pending Confirmation')),
                DropdownMenuItem(value: 'CONFIRMED', child: Text('Confirmed')),
                DropdownMenuItem(value: 'COMPLETED', child: Text('Completed')),
                DropdownMenuItem(value: 'CANCELLED', child: Text('Cancelled')),
              ],
              onChanged: (v) {
                if (v != null) setState(() => _bookingStatusFilter = v);
              },
            ),
            OutlinedButton.icon(
              onPressed: _loadData,
              icon: const Icon(Icons.refresh_rounded, size: 17),
              label: const Text('Refresh Bookings'),
            ),
          ],
        ),
        const SizedBox(height: 14),
        Card(
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              columns: const [
                DataColumn(label: Text('REFERENCE')),
                DataColumn(label: Text('TOURIST')),
                DataColumn(label: Text('PACKAGE / ITINERARY')),
                DataColumn(label: Text('TRAVEL DATES')),
                DataColumn(label: Text('PAX')),
                DataColumn(label: Text('TOTAL (PHP)')),
                DataColumn(label: Text('STATUS')),
                DataColumn(label: Text('ACTIONS')),
              ],
              rows: filtered.map((b) {
                return DataRow(
                  cells: [
                    DataCell(Text(
                      b.bookingReference,
                      style: const TextStyle(fontWeight: FontWeight.w700, fontFamily: 'Consolas'),
                    )),
                    DataCell(Text(b.touristName)),
                    DataCell(Text(b.packageName)),
                    DataCell(Text('${b.travelDateStart} → ${b.travelDateEnd}')),
                    DataCell(Text('${b.numberOfPax}')),
                    DataCell(Text(
                      '₱${b.totalAmount.toStringAsFixed(2)}',
                      style: const TextStyle(
                        fontWeight: FontWeight.w800,
                        color: LakbayTheme.primary800,
                      ),
                    )),
                    DataCell(LakbayTheme.statusBadge(b.status)),
                    DataCell(
                      Row(
                        children: [
                          if (widget.currentUser.isTourist && b.status == 'PENDING')
                            OutlinedButton(
                              onPressed: () => _changeBookingStatus(b.bookingId, 'CANCELLED'),
                              child: const Text(
                                'Cancel',
                                style: TextStyle(color: LakbayTheme.statusDanger),
                              ),
                            ),
                          if (widget.currentUser.isTourist && b.status == 'COMPLETED')
                            ElevatedButton(
                              onPressed: () => _openSubmitReviewDialog(b),
                              child: const Text('Leave Review'),
                            ),
                          if (!widget.currentUser.isTourist) ...[
                            if (b.status == 'PENDING')
                              ElevatedButton(
                                onPressed: () => _changeBookingStatus(b.bookingId, 'CONFIRMED'),
                                style: ElevatedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                                ),
                                child: const Text('Confirm'),
                              ),
                            if (b.status == 'CONFIRMED') ...[
                              const SizedBox(width: 6),
                              ElevatedButton(
                                onPressed: () => _changeBookingStatus(b.bookingId, 'COMPLETED'),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: LakbayTheme.primary600,
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                                ),
                                child: const Text('Complete'),
                              ),
                            ],
                          ],
                        ],
                      ),
                    ),
                  ],
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _changeBookingStatus(int bookingId, String status) async {
    final res = await LakbayApiService.instance.updateBookingStatus(bookingId, status);
    _showToast(
      (res['message'] ?? 'Booking updated to $status').toString(),
      isError: res['success'] != true,
    );
    _loadData();
  }

  Future<void> _openSubmitReviewDialog(BookingModel booking) async {
    int rating = 5;
    final commentCtrl = TextEditingController();
    int targetId = _destinations.isNotEmpty ? _destinations.first.destinationId : 1;

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: Text('Rate Completed Experience (${booking.bookingReference})'),
          content: SizedBox(
            width: 420,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (_destinations.isNotEmpty)
                  DropdownButtonFormField<int>(
                    value: targetId,
                    decoration: const InputDecoration(labelText: 'Destination Visited'),
                    items: _destinations
                        .map((d) => DropdownMenuItem(
                              value: d.destinationId,
                              child: Text(d.name),
                            ))
                        .toList(),
                    onChanged: (v) {
                      if (v != null) setModal(() => targetId = v);
                    },
                  ),
                const SizedBox(height: 14),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(5, (i) {
                    final star = i + 1;
                    return IconButton(
                      onPressed: () => setModal(() => rating = star),
                      icon: Icon(
                        Icons.star_rounded,
                        size: 34,
                        color: star <= rating ? LakbayTheme.accent500 : LakbayTheme.slate300,
                      ),
                    );
                  }),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: commentCtrl,
                  maxLines: 3,
                  decoration: const InputDecoration(
                    labelText: 'Share your feedback with the local community',
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () async {
                final res = await LakbayApiService.instance.createReview(
                  bookingId: booking.bookingId,
                  targetType: 'DESTINATION',
                  targetId: targetId,
                  rating: rating,
                  comment: commentCtrl.text.trim(),
                );
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Review submitted!').toString(),
                  isError: res['success'] != true,
                );
                _loadData();
              },
              child: const Text('Submit Review'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 11: COMMUNITY REVIEWS & TOURIST FEEDBACK
  // ===========================================================================
  Widget _buildReviewsPane() {
    final completedTouristBookings = _bookings
        .where((b) => b.status.toUpperCase() == 'COMPLETED')
        .toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (widget.currentUser.isTourist && completedTouristBookings.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child: Align(
              alignment: Alignment.centerRight,
              child: ElevatedButton.icon(
                onPressed: () => _openSubmitReviewDialog(completedTouristBookings.first),
                icon: const Icon(Icons.rate_review_rounded, size: 18),
                label: const Text('Submit Review for Completed Booking'),
              ),
            ),
          ),
        ..._reviews.map(
          (r) => Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          '${r.touristName} → ${r.targetName}',
                          style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
                        ),
                        Row(
                          children: List.generate(
                            5,
                            (idx) => Icon(
                              Icons.star_rounded,
                              size: 18,
                              color: idx < r.rating
                                  ? LakbayTheme.accent500
                                  : LakbayTheme.slate300,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      r.comment,
                      style: const TextStyle(color: LakbayTheme.slate700, height: 1.45),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Target: ${r.targetType} • Recorded: ${r.createdAt}',
                      style: const TextStyle(fontSize: 11.5, color: LakbayTheme.slate400),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

