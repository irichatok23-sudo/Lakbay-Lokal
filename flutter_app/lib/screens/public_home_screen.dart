import 'package:flutter/material.dart';

import '../models/lakbay_models.dart';
import '../services/api_service.dart';
import '../theme/lakbay_theme.dart';

/// Preserved Lakbay Lokal Public Discovery Portal (Desktop & Mobile responsive)
class PublicHomeScreen extends StatefulWidget {
  final VoidCallback onOpenLogin;
  final VoidCallback onOpenRegister;
  final VoidCallback onConfigureServer;

  const PublicHomeScreen({
    super.key,
    required this.onOpenLogin,
    required this.onOpenRegister,
    required this.onConfigureServer,
  });

  @override
  State<PublicHomeScreen> createState() => _PublicHomeScreenState();
}

class _PublicHomeScreenState extends State<PublicHomeScreen> {
  final TextEditingController _searchCtrl = TextEditingController();
  String _selectedCategory = 'ALL';
  bool _loading = true;
  List<DestinationModel> _destinations = [];
  List<StakeholderModel> _stakeholders = [];
  SystemSettingsModel? _settings;

  static const List<Map<String, String>> _categories = [
    {'code': 'ALL', 'label': 'All Wonders'},
    {'code': 'ISLAND_BEACH', 'label': 'Islands & Beaches'},
    {'code': 'WATERFALL', 'label': 'Waterfalls & Springs'},
    {'code': 'MOUNTAIN_ECO', 'label': 'Mountain & Eco-Parks'},
    {'code': 'HERITAGE_CULTURAL', 'label': 'Cultural & Heritage'},
  ];

  @override
  void initState() {
    super.initState();
    _loadPortalData();
  }

  Future<void> _loadPortalData() async {
    setState(() => _loading = true);
    try {
      final results = await Future.wait([
        LakbayApiService.instance.getDestinations(),
        LakbayApiService.instance.getStakeholders(verificationStatus: 'APPROVED'),
        LakbayApiService.instance.getSettings(),
      ]);
      if (!mounted) return;
      setState(() {
        _destinations = results[0] as List<DestinationModel>;
        _stakeholders = results[1] as List<StakeholderModel>;
        _settings = results[2] as SystemSettingsModel;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _loading = false);
    }
  }

  List<DestinationModel> get _filteredDestinations {
    final q = _searchCtrl.text.trim().toLowerCase();
    return _destinations.where((d) {
      final matchesCat = _selectedCategory == 'ALL' ||
          d.category.toUpperCase() == _selectedCategory;
      final matchesQuery = q.isEmpty ||
          d.name.toLowerCase().contains(q) ||
          d.description.toLowerCase().contains(q) ||
          d.municipalityName.toLowerCase().contains(q) ||
          d.barangayName.toLowerCase().contains(q);
      return matchesCat && matchesQuery;
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    final isMobile = width < 820;

    return Scaffold(
      backgroundColor: const Color(0xFFFDFEFE),
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(72),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.96),
            border: const Border(bottom: BorderSide(color: LakbayTheme.slate200)),
            boxShadow: [
              BoxShadow(
                color: LakbayTheme.primary900.withOpacity(0.05),
                blurRadius: 14,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          padding: EdgeInsets.symmetric(horizontal: isMobile ? 16 : 40),
          child: SafeArea(
            child: Row(
              children: [
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [LakbayTheme.primary800, LakbayTheme.primary600],
                    ),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.explore_rounded, color: Colors.white, size: 26),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'LAKBAY LOKAL',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                          color: LakbayTheme.primary900,
                          letterSpacing: -0.3,
                        ),
                      ),
                      Text(
                        _settings?.province ?? 'Biliran Province, Region VIII, Philippines',
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontSize: 11.5,
                          color: LakbayTheme.slate500,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  tooltip: 'Configure Backend API Server (${LakbayApiService.instance.baseUrl})',
                  onPressed: widget.onConfigureServer,
                  icon: const Icon(Icons.dns_rounded, color: LakbayTheme.slate600),
                ),
                const SizedBox(width: 6),
                if (!isMobile) ...[
                  OutlinedButton.icon(
                    onPressed: widget.onOpenRegister,
                    icon: const Icon(Icons.person_add_alt_1_rounded, size: 17),
                    label: const Text('Register Tourist'),
                  ),
                  const SizedBox(width: 10),
                ],
                ElevatedButton.icon(
                  onPressed: widget.onOpenLogin,
                  icon: const Icon(Icons.login_rounded, size: 18),
                  label: Text(isMobile ? 'Sign In' : 'Sign In / Role Portal'),
                ),
              ],
            ),
          ),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: _loadPortalData,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _buildHeroSection(isMobile),
              _buildDestinationsSection(isMobile, width),
              _buildStakeholdersSection(isMobile, width),
              _buildFooter(isMobile),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeroSection(bool isMobile) {
    return Container(
      constraints: BoxConstraints(minHeight: isMobile ? 480 : 540),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [LakbayTheme.primary950, LakbayTheme.primary900, LakbayTheme.secondary900],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      padding: EdgeInsets.symmetric(
        horizontal: isMobile ? 20 : 48,
        vertical: isMobile ? 40 : 64,
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 960),
          child: Column(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.14),
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(color: Colors.white.withOpacity(0.3)),
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.eco_rounded, color: LakbayTheme.primary300, size: 16),
                    SizedBox(width: 8),
                    Text(
                      'COMMUNITY-BASED TOURISM INFORMATION SYSTEM',
                      style: TextStyle(
                        color: LakbayTheme.primary300,
                        fontWeight: FontWeight.w700,
                        fontSize: 12,
                        letterSpacing: 0.7,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              Text(
                'Discover the Untouched Island & Waterfall Wonders of Biliran',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white,
                  fontSize: isMobile ? 28 : 44,
                  fontWeight: FontWeight.w800,
                  height: 1.18,
                ),
              ),
              const SizedBox(height: 14),
              Text(
                'Connect directly with barangay-verified island guides, boat captains, habal-habal drivers, and homestay hosts across all 8 municipalities of Biliran Province.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white.withOpacity(0.9),
                  fontSize: isMobile ? 14.5 : 17,
                  height: 1.55,
                ),
              ),
              const SizedBox(height: 28),
              // Search & Filter Bar
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.97),
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.28),
                      blurRadius: 28,
                      offset: const Offset(0, 12),
                    ),
                  ],
                ),
                child: isMobile
                    ? Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          TextField(
                            controller: _searchCtrl,
                            onChanged: (_) => setState(() {}),
                            decoration: const InputDecoration(
                              hintText: 'Search Sambawan, Tinago Falls, Higatangan...',
                              prefixIcon: Icon(Icons.search_rounded),
                              border: InputBorder.none,
                              enabledBorder: InputBorder.none,
                              focusedBorder: InputBorder.none,
                            ),
                          ),
                          const SizedBox(height: 8),
                          ElevatedButton.icon(
                            onPressed: widget.onOpenLogin,
                            icon: const Icon(Icons.flight_takeoff_rounded),
                            label: const Text('Book a Community Experience'),
                          ),
                        ],
                      )
                    : Row(
                        children: [
                          const SizedBox(width: 8),
                          const Icon(Icons.search_rounded, color: LakbayTheme.slate500),
                          const SizedBox(width: 10),
                          Expanded(
                            flex: 3,
                            child: TextField(
                              controller: _searchCtrl,
                              onChanged: (_) => setState(() {}),
                              decoration: const InputDecoration(
                                hintText:
                                    'Search destinations, waterfalls, islands, or municipalities...',
                                border: InputBorder.none,
                                enabledBorder: InputBorder.none,
                                focusedBorder: InputBorder.none,
                              ),
                            ),
                          ),
                          Container(width: 1, height: 32, color: LakbayTheme.slate200),
                          const SizedBox(width: 12),
                          DropdownButtonHideUnderline(
                            child: DropdownButton<String>(
                              value: _selectedCategory,
                              items: _categories
                                  .map((c) => DropdownMenuItem(
                                        value: c['code'],
                                        child: Text(
                                          c['label']!,
                                          style: const TextStyle(
                                            fontWeight: FontWeight.w600,
                                            fontSize: 13.5,
                                          ),
                                        ),
                                      ))
                                  .toList(),
                              onChanged: (val) {
                                if (val != null) setState(() => _selectedCategory = val);
                              },
                            ),
                          ),
                          const SizedBox(width: 12),
                          ElevatedButton.icon(
                            onPressed: widget.onOpenLogin,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: LakbayTheme.primary600,
                              padding: const EdgeInsets.symmetric(
                                horizontal: 22,
                                vertical: 16,
                              ),
                            ),
                            icon: const Icon(Icons.calendar_today_rounded, size: 17),
                            label: const Text('Book Experience'),
                          ),
                        ],
                      ),
              ),
              const SizedBox(height: 28),
              // Fast Stats Row
              Wrap(
                spacing: 14,
                runSpacing: 14,
                alignment: WrapAlignment.center,
                children: [
                  _buildHeroStat('${_destinations.isNotEmpty ? _destinations.length : 8}+', 'Verified Attractions'),
                  _buildHeroStat('8', 'Biliran Municipalities'),
                  _buildHeroStat('${_stakeholders.isNotEmpty ? _stakeholders.length : 12}+', 'Accredited Stakeholders'),
                  _buildHeroStat('6', 'Role-Based Portals'),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeroStat(String numText, String label) {
    return Container(
      width: 195,
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.12),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.white.withOpacity(0.22)),
      ),
      child: Column(
        children: [
          Text(
            numText,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 24,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label.toUpperCase(),
            textAlign: TextAlign.center,
            style: const TextStyle(
              color: LakbayTheme.primary100,
              fontSize: 11,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDestinationsSection(bool isMobile, double width) {
    final items = _filteredDestinations;
    final crossAxisCount = width >= 1150 ? 3 : (width >= 720 ? 2 : 1);

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isMobile ? 16 : 48,
        vertical: 48,
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1320),
          child: Column(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
                decoration: BoxDecoration(
                  color: LakbayTheme.primary50,
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(color: LakbayTheme.primary100),
                ),
                child: const Text(
                  'EXPLORE BILIRAN',
                  style: TextStyle(
                    color: LakbayTheme.primary600,
                    fontWeight: FontWeight.w700,
                    fontSize: 11.5,
                    letterSpacing: 0.8,
                  ),
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                'Featured Eco-Tourism Destinations',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w800,
                  color: LakbayTheme.slate900,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Live database records from lakbay_lokal_db — validated by Barangay Officials and the Provincial Tourism Office.',
                textAlign: TextAlign.center,
                style: TextStyle(color: LakbayTheme.slate600, fontSize: 14.5),
              ),
              const SizedBox(height: 22),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                alignment: WrapAlignment.center,
                children: _categories.map((cat) {
                  final active = _selectedCategory == cat['code'];
                  return ChoiceChip(
                    label: Text(cat['label']!),
                    selected: active,
                    selectedColor: LakbayTheme.primary700,
                    backgroundColor: Colors.white,
                    labelStyle: TextStyle(
                      color: active ? Colors.white : LakbayTheme.slate700,
                      fontWeight: FontWeight.w600,
                    ),
                    onSelected: (_) => setState(() => _selectedCategory = cat['code']!),
                  );
                }).toList(),
              ),
              const SizedBox(height: 28),
              if (_loading)
                const Padding(
                  padding: EdgeInsets.all(40),
                  child: CircularProgressIndicator(),
                )
              else if (items.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(32),
                  child: Text('No destinations match your filter criteria.'),
                )
              else
                GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: items.length,
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: crossAxisCount,
                    crossAxisSpacing: 20,
                    mainAxisSpacing: 20,
                    mainAxisExtent: 330,
                  ),
                  itemBuilder: (context, index) {
                    final d = items[index];
                    return Card(
                      clipBehavior: Clip.antiAlias,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          SizedBox(
                            height: 125,
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
                                  top: 12,
                                  left: 12,
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 4,
                                    ),
                                    decoration: BoxDecoration(
                                      color: LakbayTheme.primary950.withOpacity(0.85),
                                      borderRadius: BorderRadius.circular(999),
                                    ),
                                    child: Text(
                                      d.category.replaceAll('_', ' '),
                                      style: const TextStyle(
                                        color: LakbayTheme.primary300,
                                        fontSize: 11,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ),
                                ),
                                Positioned(
                                  bottom: 12,
                                  right: 12,
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 5,
                                    ),
                                    decoration: BoxDecoration(
                                      color: Colors.white.withOpacity(0.95),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: Text(
                                      d.entranceFee > 0
                                          ? '₱${d.entranceFee.toStringAsFixed(0)} / pax'
                                          : 'Free Entry',
                                      style: const TextStyle(
                                        color: LakbayTheme.primary800,
                                        fontWeight: FontWeight.w800,
                                        fontSize: 12.5,
                                      ),
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Expanded(
                            child: Padding(
                              padding: const EdgeInsets.all(16),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    d.name,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: const TextStyle(
                                      fontSize: 17,
                                      fontWeight: FontWeight.w700,
                                      color: LakbayTheme.slate900,
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Row(
                                    children: [
                                      const Icon(
                                        Icons.location_on_rounded,
                                        size: 14,
                                        color: LakbayTheme.primary600,
                                      ),
                                      const SizedBox(width: 4),
                                      Expanded(
                                        child: Text(
                                          'Brgy. ${d.barangayName}, ${d.municipalityName}',
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: const TextStyle(
                                            fontSize: 12.5,
                                            color: LakbayTheme.slate500,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: 8),
                                  Expanded(
                                    child: Text(
                                      d.description,
                                      maxLines: 3,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: LakbayTheme.slate600,
                                        height: 1.4,
                                      ),
                                    ),
                                  ),
                                  const Divider(height: 16),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                    children: [
                                      Row(
                                        children: [
                                          const Icon(
                                            Icons.star_rounded,
                                            color: LakbayTheme.accent500,
                                            size: 18,
                                          ),
                                          const SizedBox(width: 4),
                                          Text(
                                            d.averageRating.toStringAsFixed(1),
                                            style: const TextStyle(
                                              fontWeight: FontWeight.w700,
                                              fontSize: 13,
                                            ),
                                          ),
                                        ],
                                      ),
                                      TextButton.icon(
                                        onPressed: widget.onOpenLogin,
                                        icon: const Icon(Icons.arrow_forward_rounded, size: 16),
                                        label: const Text('Book Visit'),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStakeholdersSection(bool isMobile, double width) {
    final crossAxisCount = width >= 1100 ? 4 : (width >= 700 ? 2 : 1);
    final list = _stakeholders.take(8).toList();

    return Container(
      color: LakbayTheme.slate50,
      padding: EdgeInsets.symmetric(
        horizontal: isMobile ? 16 : 48,
        vertical: 48,
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1320),
          child: Column(
            children: [
              const Text(
                'Barangay-Verified Community Stakeholders',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  color: LakbayTheme.slate900,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Directly supporting local livelihoods across Biliran island-hopping, trekking, and homestay circuits.',
                textAlign: TextAlign.center,
                style: TextStyle(color: LakbayTheme.slate600, fontSize: 14),
              ),
              const SizedBox(height: 28),
              if (list.isNotEmpty)
                GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: list.length,
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: crossAxisCount,
                    crossAxisSpacing: 16,
                    mainAxisSpacing: 16,
                    mainAxisExtent: 210,
                  ),
                  itemBuilder: (context, index) {
                    final s = list[index];
                    return Card(
                      child: Padding(
                        padding: const EdgeInsets.all(18),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            CircleAvatar(
                              radius: 26,
                              backgroundColor: LakbayTheme.primary50,
                              child: const Icon(
                                Icons.verified_user_rounded,
                                color: LakbayTheme.primary700,
                                size: 26,
                              ),
                            ),
                            const SizedBox(height: 10),
                            Text(
                              s.businessName,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                fontSize: 15,
                                color: LakbayTheme.slate900,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              s.category.replaceAll('_', ' '),
                              style: const TextStyle(
                                color: LakbayTheme.primary600,
                                fontWeight: FontWeight.w700,
                                fontSize: 11.5,
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              'Brgy. ${s.barangayName} • ${s.yearsExperience} yrs exp.',
                              style: const TextStyle(
                                color: LakbayTheme.slate500,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
            ],
          ),
        ),
      ),
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

  Widget _buildFooter(bool isMobile) {
    return Container(
      color: LakbayTheme.titlebarDark,
      padding: EdgeInsets.symmetric(
        horizontal: isMobile ? 20 : 48,
        vertical: 32,
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1320),
          child: Wrap(
            alignment: WrapAlignment.spaceBetween,
            runSpacing: 12,
            children: [
              const Text(
                '© 2026 Lakbay Lokal: Biliran Community-Based Tourism Information System',
                style: TextStyle(color: LakbayTheme.slate400, fontSize: 12.5),
              ),
              Text(
                'Connected to Java REST API (${LakbayApiService.instance.baseUrl}) & MySQL lakbay_lokal_db',
                style: const TextStyle(color: LakbayTheme.primary300, fontSize: 12.5),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

