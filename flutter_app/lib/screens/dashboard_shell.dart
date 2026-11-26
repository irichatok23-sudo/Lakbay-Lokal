import 'package:flutter/material.dart';

import '../models/lakbay_models.dart';
import '../services/api_service.dart';
import '../theme/lakbay_theme.dart';
import 'modules/governance_admin_modules.dart';
import 'modules/tourism_modules.dart';

/// Responsive Role-Based Application Shell for Windows Desktop & Android Mobile
class DashboardShell extends StatefulWidget {
  final UserAccount user;
  final VoidCallback onLoggedOut;

  const DashboardShell({
    super.key,
    required this.user,
    required this.onLoggedOut,
  });

  @override
  State<DashboardShell> createState() => _DashboardShellState();
}

class _DashboardShellState extends State<DashboardShell> {
  late String _activeModuleId;
  SystemSettingsModel _settings = SystemSettingsModel.fromJson({});

  static const Set<String> _tourismModules = {
    'overview',
    'destinations',
    'announcements',
    'stakeholders',
    'services',
    'packages',
    'bookings',
    'reviews',
  };

  @override
  void initState() {
    super.initState();
    final meta = RoleMetadata.forRole(widget.user.roleCode);
    _activeModuleId = meta.allowedModules.first;
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    try {
      final s = await LakbayApiService.instance.getSettings();
      if (!mounted) return;
      setState(() => _settings = s);
    } catch (_) {}
  }

  Future<void> _confirmLogout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Sign Out of Lakbay Lokal?'),
        content: Text(
          'You are currently signed in as ${widget.user.fullName} (${widget.user.roleName}). Do you want to end your session?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Stay Signed In'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: LakbayTheme.statusDanger,
            ),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Sign Out'),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await LakbayApiService.instance.logout();
      widget.onLoggedOut();
    }
  }

  @override
  Widget build(BuildContext context) {
    final roleMeta = RoleMetadata.forRole(widget.user.roleCode);
    final allowedNavItems = roleMeta.allowedModules
        .map((id) => RoleMetadata.allModules[id])
        .whereType<NavModuleItem>()
        .toList();
    final activeNav =
        RoleMetadata.allModules[_activeModuleId] ?? allowedNavItems.first;

    return LayoutBuilder(
      builder: (context, constraints) {
        final isDesktop = constraints.maxWidth >= 920;

        return Scaffold(
          backgroundColor: LakbayTheme.bgMain,
          appBar: PreferredSize(
            preferredSize: const Size.fromHeight(70),
            child: Container(
              decoration: const BoxDecoration(
                color: Colors.white,
                border: Border(bottom: BorderSide(color: LakbayTheme.slate200)),
              ),
              padding: EdgeInsets.symmetric(horizontal: isDesktop ? 26 : 12),
              child: SafeArea(
                child: Row(
                  children: [
                    if (!isDesktop)
                      Builder(
                        builder: (ctx) => IconButton(
                          onPressed: () => Scaffold.of(ctx).openDrawer(),
                          icon: const Icon(Icons.menu_rounded),
                        ),
                      ),
                    Container(
                      width: 42,
                      height: 42,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [LakbayTheme.primary800, LakbayTheme.primary600],
                        ),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(
                        Icons.explore_rounded,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            _settings.appName,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w800,
                              color: LakbayTheme.primary900,
                            ),
                          ),
                          Text(
                            _settings.province,
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
                    // User Profile Badge
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: LakbayTheme.slate50,
                        borderRadius: BorderRadius.circular(999),
                        border: Border.all(color: LakbayTheme.slate200),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircleAvatar(
                            radius: 15,
                            backgroundColor: LakbayTheme.primary100,
                            child: Text(
                              widget.user.fullName.isNotEmpty
                                  ? widget.user.fullName[0].toUpperCase()
                                  : 'U',
                              style: const TextStyle(
                                color: LakbayTheme.primary800,
                                fontWeight: FontWeight.w800,
                                fontSize: 13,
                              ),
                            ),
                          ),
                          if (isDesktop) ...[
                            const SizedBox(width: 10),
                            Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  widget.user.fullName,
                                  style: const TextStyle(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w700,
                                    color: LakbayTheme.slate900,
                                  ),
                                ),
                                Text(
                                  roleMeta.badgeText,
                                  style: const TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.w800,
                                    color: LakbayTheme.primary700,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ],
                      ),
                    ),
                    const SizedBox(width: 10),
                    OutlinedButton.icon(
                      onPressed: _confirmLogout,
                      icon: const Icon(
                        Icons.logout_rounded,
                        size: 16,
                        color: LakbayTheme.statusDanger,
                      ),
                      label: Text(
                        isDesktop ? 'Logout' : 'Exit',
                        style: const TextStyle(color: LakbayTheme.statusDanger),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          drawer: isDesktop
              ? null
              : Drawer(
                  backgroundColor: Colors.white,
                  child: _buildSidebarNavigation(allowedNavItems, isDrawer: true),
                ),
          bottomNavigationBar: isDesktop
              ? null
              : NavigationBar(
                  backgroundColor: Colors.white,
                  selectedIndex: allowedNavItems
                      .take(4)
                      .toList()
                      .indexWhere((m) => m.id == _activeModuleId)
                      .clamp(0, 3),
                  onDestinationSelected: (idx) {
                    if (idx < allowedNavItems.length) {
                      setState(() => _activeModuleId = allowedNavItems[idx].id);
                    }
                  },
                  destinations: allowedNavItems
                      .take(4)
                      .map((m) => NavigationDestination(
                            icon: Icon(m.icon),
                            label: m.title.split(' ').first,
                          ))
                      .toList(),
                ),
          body: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (isDesktop)
                Container(
                  width: 270,
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    border: Border(right: BorderSide(color: LakbayTheme.slate200)),
                  ),
                  child: _buildSidebarNavigation(allowedNavItems),
                ),
              Expanded(
                child: SingleChildScrollView(
                  padding: EdgeInsets.all(isDesktop ? 28 : 16),
                  child: Center(
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 1380),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          // Live Broadcast Notice Bar (.notice-bar in style.css)
                          Container(
                            margin: const EdgeInsets.only(bottom: 18),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 11,
                            ),
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [
                                  LakbayTheme.primary50,
                                  LakbayTheme.accent100,
                                ],
                              ),
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(
                                color: LakbayTheme.primary500.withOpacity(0.3),
                              ),
                            ),
                            child: Row(
                              children: [
                                const Icon(
                                  Icons.campaign_rounded,
                                  color: LakbayTheme.accent600,
                                  size: 20,
                                ),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    _settings.broadcastMsg,
                                    style: const TextStyle(
                                      color: LakbayTheme.primary900,
                                      fontWeight: FontWeight.w600,
                                      fontSize: 13,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          // Role Workspace Banner (.role-workspace-banner in style.css)
                          Container(
                            margin: const EdgeInsets.only(bottom: 22),
                            padding: const EdgeInsets.all(22),
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [
                                  LakbayTheme.primary900,
                                  Color(0xFF022C22),
                                ],
                              ),
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(
                                color: LakbayTheme.primary500.withOpacity(0.25),
                              ),
                            ),
                            child: Wrap(
                              alignment: WrapAlignment.spaceBetween,
                              crossAxisAlignment: WrapCrossAlignment.center,
                              runSpacing: 12,
                              children: [
                                ConstrainedBox(
                                  constraints: const BoxConstraints(maxWidth: 780),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                          horizontal: 10,
                                          vertical: 4,
                                        ),
                                        decoration: BoxDecoration(
                                          color: LakbayTheme.primary500.withOpacity(0.2),
                                          borderRadius: BorderRadius.circular(999),
                                          border: Border.all(
                                            color: LakbayTheme.primary500.withOpacity(0.4),
                                          ),
                                        ),
                                        child: Text(
                                          roleMeta.badgeText,
                                          style: const TextStyle(
                                            color: LakbayTheme.primary300,
                                            fontSize: 11,
                                            fontWeight: FontWeight.w700,
                                          ),
                                        ),
                                      ),
                                      const SizedBox(height: 8),
                                      Text(
                                        roleMeta.bannerTitle,
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontSize: 21,
                                          fontWeight: FontWeight.w800,
                                        ),
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        roleMeta.bannerDescription,
                                        style: const TextStyle(
                                          color: LakbayTheme.primary200,
                                          fontSize: 13.5,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 14,
                                    vertical: 8,
                                  ),
                                  decoration: BoxDecoration(
                                    color: Colors.white.withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(999),
                                    border: Border.all(color: Colors.white24),
                                  ),
                                  child: Text(
                                    widget.user.barangayName.isNotEmpty
                                        ? 'Brgy. ${widget.user.barangayName}, ${widget.user.municipalityName}'
                                        : 'Province of Biliran (${_settings.currency.split(' ').first})',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontWeight: FontWeight.w700,
                                      fontSize: 12.5,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          // Active Module Header
                          Padding(
                            padding: const EdgeInsets.only(bottom: 16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  activeNav.title,
                                  style: const TextStyle(
                                    fontSize: 22,
                                    fontWeight: FontWeight.w800,
                                    color: LakbayTheme.slate900,
                                  ),
                                ),
                                const SizedBox(height: 3),
                                Text(
                                  activeNav.subtitle,
                                  style: const TextStyle(
                                    fontSize: 13.5,
                                    color: LakbayTheme.slate500,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          // Active Module Content
                          if (_tourismModules.contains(_activeModuleId))
                            TourismModulesView(
                              activeModuleId: _activeModuleId,
                              currentUser: widget.user,
                              settings: _settings,
                              onNavigateModule: (mod) =>
                                  setState(() => _activeModuleId = mod),
                              onRefreshGlobal: _loadSettings,
                            )
                          else
                            GovernanceAdminModulesView(
                              activeModuleId: _activeModuleId,
                              currentUser: widget.user,
                              settings: _settings,
                              onSettingsUpdated: _loadSettings,
                            ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildSidebarNavigation(
    List<NavModuleItem> items, {
    bool isDrawer = false,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Expanded(
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
            itemCount: items.length,
            itemBuilder: (context, index) {
              final item = items[index];
              final isSelected = item.id == _activeModuleId;
              return Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: InkWell(
                  borderRadius: BorderRadius.circular(10),
                  onTap: () {
                    setState(() => _activeModuleId = item.id);
                    if (isDrawer) Navigator.pop(context);
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 12,
                    ),
                    decoration: BoxDecoration(
                      gradient: isSelected
                          ? const LinearGradient(
                              colors: [
                                LakbayTheme.primary700,
                                LakbayTheme.primary800,
                              ],
                            )
                          : null,
                      borderRadius: BorderRadius.circular(10),
                      boxShadow: isSelected
                          ? [
                              BoxShadow(
                                color: LakbayTheme.primary800.withOpacity(0.25),
                                blurRadius: 8,
                                offset: const Offset(0, 3),
                              ),
                            ]
                          : null,
                    ),
                    child: Row(
                      children: [
                        Icon(
                          item.icon,
                          size: 20,
                          color: isSelected
                              ? Colors.white
                              : LakbayTheme.slate600,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            item.title,
                            style: TextStyle(
                              fontSize: 13.8,
                              fontWeight: isSelected
                                  ? FontWeight.w700
                                  : FontWeight.w600,
                              color: isSelected
                                  ? Colors.white
                                  : LakbayTheme.slate700,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: const BoxDecoration(
            border: Border(top: BorderSide(color: LakbayTheme.slate200)),
          ),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: LakbayTheme.primary50,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                Container(
                  width: 8,
                  height: 8,
                  decoration: const BoxDecoration(
                    color: LakbayTheme.statusSuccess,
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 8),
                const Expanded(
                  child: Text(
                    'MySQL lakbay_lokal_db Online',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: LakbayTheme.primary800,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

