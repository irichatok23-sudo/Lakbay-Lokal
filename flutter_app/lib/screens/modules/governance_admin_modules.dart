import 'package:flutter/material.dart';

import '../../models/lakbay_models.dart';
import '../../services/api_service.dart';
import '../../theme/lakbay_theme.dart';

/// Modules 8, 9, 10, 12, 13, 14, 15, and 16:
/// Stakeholder Verifications, Barangay Records, LGU Monitoring, Consolidated Reports,
/// Admin User Management, System Settings, Audit Logs, and Database Backup.
class GovernanceAdminModulesView extends StatefulWidget {
  final String activeModuleId;
  final UserAccount currentUser;
  final SystemSettingsModel settings;
  final VoidCallback onSettingsUpdated;

  const GovernanceAdminModulesView({
    super.key,
    required this.activeModuleId,
    required this.currentUser,
    required this.settings,
    required this.onSettingsUpdated,
  });

  @override
  State<GovernanceAdminModulesView> createState() =>
      _GovernanceAdminModulesViewState();
}

class _GovernanceAdminModulesViewState
    extends State<GovernanceAdminModulesView> {
  bool _loading = true;
  List<StakeholderModel> _stakeholders = [];
  List<BarangayModel> _barangays = [];
  List<DestinationModel> _destinations = [];
  List<Map<String, dynamic>> _footfallRows = [];
  List<Map<String, dynamic>> _barangayCoverageRows = [];
  List<UserAccount> _adminUsers = [];
  List<AuditLogModel> _auditLogs = [];
  String? _backupResultText;

  // System Settings form controllers
  late TextEditingController _appNameCtrl;
  late TextEditingController _provinceCtrl;
  late TextEditingController _thresholdCtrl;
  late TextEditingController _broadcastCtrl;
  late String _selectedCurrency;
  final Set<String> _modifiedSettingKeys = {};

  @override
  void initState() {
    super.initState();
    _initSettingsControllers(widget.settings);
    _loadModuleData();
  }

  void _initSettingsControllers(SystemSettingsModel s) {
    _appNameCtrl = TextEditingController(text: s.appName);
    _provinceCtrl = TextEditingController(text: s.province);
    _thresholdCtrl = TextEditingController(text: '${s.capacityThreshold}');
    _broadcastCtrl = TextEditingController(text: s.broadcastMsg);
    _selectedCurrency =
        SystemSettingsModel.currencyOptions.contains(s.currency)
            ? s.currency
            : SystemSettingsModel.currencyOptions.first;
    _modifiedSettingKeys.clear();
  }

  @override
  void didUpdateWidget(covariant GovernanceAdminModulesView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.activeModuleId != widget.activeModuleId) {
      _loadModuleData();
    }
  }

  Future<void> _loadModuleData() async {
    setState(() => _loading = true);
    try {
      final api = LakbayApiService.instance;
      final stkFuture = api.getStakeholders(
        jurisdictionOnly: widget.currentUser.isBarangayOfficial,
      );
      final brgyFuture = api.getBarangays();
      final destFuture = api.getDestinations();
      final footfallFuture = api.getFootfallReport();
      final brgyRepFuture = api.getBarangayReport();
      final settingsFuture = api.getSettings();

      final results = await Future.wait([
        stkFuture,
        brgyFuture,
        destFuture,
        footfallFuture,
        brgyRepFuture,
        settingsFuture,
      ]);

      List<UserAccount> users = [];
      List<AuditLogModel> logs = [];
      if (widget.currentUser.isAdmin) {
        users = await api.getAdminUsers();
        logs = await api.getAuditLogs(limit: 60);
      }

      if (!mounted) return;
      final freshSettings = results[5] as SystemSettingsModel;
      setState(() {
        _stakeholders = results[0] as List<StakeholderModel>;
        _barangays = results[1] as List<BarangayModel>;
        _destinations = results[2] as List<DestinationModel>;
        _footfallRows = results[3] as List<Map<String, dynamic>>;
        _barangayCoverageRows = results[4] as List<Map<String, dynamic>>;
        _adminUsers = users;
        _auditLogs = logs;
        if (widget.activeModuleId == 'settings') {
          _initSettingsControllers(freshSettings);
        }
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
        backgroundColor:
            isError ? const Color(0xFF991B1B) : LakbayTheme.primary800,
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
      case 'verifications':
        return _buildVerificationQueuePane();
      case 'barangay_records':
        return _buildBarangayRecordsPane();
      case 'lgu_monitoring':
        return _buildLguMonitoringPane();
      case 'reports':
        return _buildConsolidatedReportsPane();
      case 'admin_users':
        return _buildAdminUsersPane();
      case 'settings':
        return _buildSystemSettingsPane();
      case 'audit_logs':
        return _buildAuditLogsPane();
      case 'backup':
        return _buildDatabaseBackupPane();
      default:
        return _buildConsolidatedReportsPane();
    }
  }

  // ===========================================================================
  // MODULE 8: STAKEHOLDER VERIFICATION QUEUE
  // ===========================================================================
  Widget _buildVerificationQueuePane() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  widget.currentUser.isBarangayOfficial
                      ? 'Barangay Jurisdiction Verification Queue (Brgy. ${widget.currentUser.barangayName})'
                      : 'Provincial Stakeholder Verification & Accreditation Queue',
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Verify community tourism operators before their services are listed as accredited in the public directory.',
                  style: TextStyle(color: LakbayTheme.slate600, fontSize: 13),
                ),
                const SizedBox(height: 14),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: DataTable(
                    columns: const [
                      DataColumn(label: Text('STAKEHOLDER / BUSINESS')),
                      DataColumn(label: Text('OWNER')),
                      DataColumn(label: Text('CATEGORY')),
                      DataColumn(label: Text('BARANGAY')),
                      DataColumn(label: Text('ACCREDITATION')),
                      DataColumn(label: Text('STATUS')),
                      DataColumn(label: Text('DECISION ACTIONS')),
                    ],
                    rows: _stakeholders.map((s) {
                      return DataRow(
                        cells: [
                          DataCell(Text(
                            s.businessName,
                            style: const TextStyle(fontWeight: FontWeight.w700),
                          )),
                          DataCell(Text(s.fullName)),
                          DataCell(Text(s.category.replaceAll('_', ' '))),
                          DataCell(Text('Brgy. ${s.barangayName}')),
                          DataCell(Text(s.accreditationNo.isEmpty
                              ? 'BLR-PENDING'
                              : s.accreditationNo)),
                          DataCell(LakbayTheme.statusBadge(s.verificationStatus)),
                          DataCell(
                            Row(
                              children: [
                                ElevatedButton(
                                  onPressed: () => _openVerificationDecisionModal(
                                    s,
                                    'APPROVED',
                                  ),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: LakbayTheme.statusSuccess,
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 6,
                                    ),
                                  ),
                                  child: const Text('Approve'),
                                ),
                                const SizedBox(width: 6),
                                OutlinedButton(
                                  onPressed: () => _openVerificationDecisionModal(
                                    s,
                                    'RETURNED',
                                  ),
                                  child: const Text('Return'),
                                ),
                                const SizedBox(width: 6),
                                OutlinedButton(
                                  onPressed: () => _openVerificationDecisionModal(
                                    s,
                                    'REJECTED',
                                  ),
                                  child: const Text(
                                    'Reject',
                                    style: TextStyle(color: LakbayTheme.statusDanger),
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
              ],
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openVerificationDecisionModal(
    StakeholderModel stk,
    String initialDecision,
  ) async {
    final reqCtrl = TextEditingController(
      text: 'Barangay Clearance, Municipal Permit, Safety Briefing Certificate',
    );
    final remarksCtrl = TextEditingController(
      text: initialDecision == 'APPROVED'
          ? 'Verified in good standing by Barangay & Tourism Council.'
          : 'Please submit updated clearance documents.',
    );

    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('$initialDecision Stakeholder: ${stk.businessName}'),
        content: SizedBox(
          width: 440,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: reqCtrl,
                decoration: const InputDecoration(
                  labelText: 'Submitted Requirements',
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: remarksCtrl,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Official Verification Remarks',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              final res = await LakbayApiService.instance.processVerification(
                stakeholderId: stk.profileId,
                barangayId: stk.barangayId,
                status: initialDecision,
                requirementsSubmitted: reqCtrl.text.trim(),
                remarks: remarksCtrl.text.trim(),
              );
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              _showToast(
                (res['message'] ?? 'Verification updated.').toString(),
                isError: res['success'] != true,
              );
              _loadModuleData();
            },
            child: Text('Confirm $initialDecision'),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // MODULE 9: BARANGAY TOURISM RECORDS
  // ===========================================================================
  Widget _buildBarangayRecordsPane() {
    final visibleBarangays = widget.currentUser.isBarangayOfficial &&
            widget.currentUser.barangayId != null
        ? _barangays
            .where((b) => b.barangayId == widget.currentUser.barangayId)
            .toList()
        : _barangays;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Barangay Tourism Focal Records & Jurisdiction Directory',
              style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: DataTable(
                columns: const [
                  DataColumn(label: Text('BARANGAY')),
                  DataColumn(label: Text('MUNICIPALITY')),
                  DataColumn(label: Text('FOCAL OFFICIAL')),
                  DataColumn(label: Text('CONTACT PHONE')),
                  DataColumn(label: Text('ACTIONS')),
                ],
                rows: visibleBarangays.map((b) {
                  return DataRow(
                    cells: [
                      DataCell(Text(
                        'Brgy. ${b.name}',
                        style: const TextStyle(fontWeight: FontWeight.w700),
                      )),
                      DataCell(Text(b.municipality)),
                      DataCell(Text(b.contactPerson)),
                      DataCell(Text(b.contactNumber)),
                      DataCell(
                        OutlinedButton.icon(
                          onPressed: () => _openEditBarangayDialog(b),
                          icon: const Icon(Icons.edit_outlined, size: 15),
                          label: const Text('Update Record'),
                        ),
                      ),
                    ],
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _openEditBarangayDialog(BarangayModel b) async {
    final personCtrl = TextEditingController(text: b.contactPerson);
    final phoneCtrl = TextEditingController(text: b.contactNumber);

    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Update Barangay Record — Brgy. ${b.name}'),
        content: SizedBox(
          width: 420,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: personCtrl,
                decoration: const InputDecoration(
                  labelText: 'Barangay Tourism Focal Official',
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: phoneCtrl,
                decoration: const InputDecoration(
                  labelText: 'Official Contact Number',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              final res = await LakbayApiService.instance.updateBarangayRecord(
                barangayId: b.barangayId,
                contactPerson: personCtrl.text.trim(),
                contactNumber: phoneCtrl.text.trim(),
              );
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              _showToast(
                (res['message'] ?? 'Barangay record updated.').toString(),
                isError: res['success'] != true,
              );
              _loadModuleData();
            },
            child: const Text('Save Record'),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // MODULE 10: LGU & ECO CARRYING-CAPACITY MONITORING
  // ===========================================================================
  Widget _buildLguMonitoringPane() {
    final alertThreshold = widget.settings.capacityThreshold;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Carrying Capacity & Environmental Thresholds (Alert at $alertThreshold%)',
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Real-time utilization computed from recorded tourist arrivals in lakbay_lokal_db versus municipal eco-thresholds.',
                  style: TextStyle(color: LakbayTheme.slate600, fontSize: 13),
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: _footfallRows.take(6).map((row) {
                    final name = (row['destination_name'] ?? 'Destination').toString();
                    final lower = name.toLowerCase();
                    int cap = 200;
                    if (lower.contains('sambawan')) cap = 250;
                    if (lower.contains('tinago')) cap = 400;
                    if (lower.contains('higatangan')) cap = 180;

                    final visitors =
                        int.tryParse('${row['grand_total_visitors'] ?? 0}') ?? 0;
                    final pct = ((visitors / cap) * 100).round().clamp(0, 100);

                    // Restored exact previous color palette for Safe / Moderate / Critical
                    Color bg = const Color(0xFFF0FDF4);
                    Color border = const Color(0xFFBBF7D0);
                    Color fg = const Color(0xFF166534);
                    String statusLabel = 'Safe';
                    IconData icon = Icons.check_circle_rounded;

                    if (pct >= alertThreshold) {
                      bg = const Color(0xFFFEF2F2);
                      border = const Color(0xFFFECACA);
                      fg = const Color(0xFF991B1B);
                      statusLabel = 'Critical';
                      icon = Icons.error_rounded;
                    } else if (pct >= 65) {
                      bg = const Color(0xFFFFFBEB);
                      border = const Color(0xFFFDE68A);
                      fg = const Color(0xFF92400E);
                      statusLabel = 'Moderate';
                      icon = Icons.warning_amber_rounded;
                    }

                    return Container(
                      width: 320,
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: bg,
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(color: border),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Icon(icon, color: fg, size: 20),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  name,
                                  style: TextStyle(
                                    color: fg,
                                    fontWeight: FontWeight.w800,
                                    fontSize: 15,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'Recorded Footfall: $visitors / $cap pax | Utilization: $pct% ($statusLabel)',
                            style: const TextStyle(
                              fontSize: 13,
                              color: LakbayTheme.slate800,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        _buildBarangayRecordsPane(),
      ],
    );
  }

  // ===========================================================================
  // MODULE 12: CONSOLIDATED REPORTS & ARRIVAL FOOTFALL LOGGING
  // ===========================================================================
  Widget _buildConsolidatedReportsPane() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: ElevatedButton.icon(
            onPressed: _openRecordArrivalDialog,
            icon: const Icon(Icons.edit_calendar_rounded, size: 18),
            label: const Text('Record Tourist Footfall Arrival'),
          ),
        ),
        const SizedBox(height: 14),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Destination Footfall & Revenue Summary (vw_destination_footfall_summary)',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: DataTable(
                    columns: const [
                      DataColumn(label: Text('DESTINATION')),
                      DataColumn(label: Text('MUNICIPALITY / BRGY')),
                      DataColumn(label: Text('LOCAL')),
                      DataColumn(label: Text('DOMESTIC')),
                      DataColumn(label: Text('FOREIGN')),
                      DataColumn(label: Text('TOTAL VISITORS')),
                      DataColumn(label: Text('REVENUE (PHP)')),
                    ],
                    rows: _footfallRows.map((r) {
                      return DataRow(
                        cells: [
                          DataCell(Text(
                            (r['destination_name'] ?? '').toString(),
                            style: const TextStyle(fontWeight: FontWeight.w700),
                          )),
                          DataCell(Text(
                            'Brgy. ${r['barangay_name'] ?? ''}, ${r['municipality'] ?? 'Biliran'}',
                          )),
                          DataCell(Text('${r['total_local'] ?? 0}')),
                          DataCell(Text('${r['total_domestic'] ?? 0}')),
                          DataCell(Text('${r['total_foreign'] ?? 0}')),
                          DataCell(Text(
                            '${r['grand_total_visitors'] ?? 0}',
                            style: const TextStyle(fontWeight: FontWeight.w800),
                          )),
                          DataCell(Text(
                            '₱${r['total_revenue_generated'] ?? 0}',
                            style: const TextStyle(
                              fontWeight: FontWeight.w800,
                              color: LakbayTheme.primary800,
                            ),
                          )),
                        ],
                      );
                    }).toList(),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 18),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Barangay Tourism Coverage Summary (vw_barangay_tourism_summary)',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: DataTable(
                    columns: const [
                      DataColumn(label: Text('BARANGAY')),
                      DataColumn(label: Text('MUNICIPALITY')),
                      DataColumn(label: Text('CONTACT OFFICIAL')),
                      DataColumn(label: Text('ATTRACTIONS')),
                      DataColumn(label: Text('STAKEHOLDERS')),
                      DataColumn(label: Text('VERIFIED')),
                    ],
                    rows: _barangayCoverageRows.map((b) {
                      return DataRow(
                        cells: [
                          DataCell(Text(
                            'Brgy. ${b['barangay_name'] ?? ''}',
                            style: const TextStyle(fontWeight: FontWeight.w700),
                          )),
                          DataCell(Text('${b['municipality'] ?? 'Biliran'}')),
                          DataCell(Text('${b['contact_person'] ?? ''}')),
                          DataCell(Text('${b['total_destinations'] ?? 0}')),
                          DataCell(Text('${b['total_stakeholders'] ?? 0}')),
                          DataCell(LakbayTheme.statusBadge(
                              '${b['verified_stakeholders'] ?? 0} Verified')),
                        ],
                      );
                    }).toList(),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _openRecordArrivalDialog() async {
    int destId = _destinations.isNotEmpty ? _destinations.first.destinationId : 1;
    final dateCtrl = TextEditingController(text: '2026-11-12');
    final localCtrl = TextEditingController(text: '15');
    final domCtrl = TextEditingController(text: '25');
    final forCtrl = TextEditingController(text: '4');
    final revCtrl = TextEditingController(text: '2200');

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: const Text('Record Destination Tourist Arrival Log'),
          content: SizedBox(
            width: 440,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (_destinations.isNotEmpty)
                    DropdownButtonFormField<int>(
                      value: destId,
                      decoration: const InputDecoration(labelText: 'Destination'),
                      items: _destinations
                          .map((d) => DropdownMenuItem(
                                value: d.destinationId,
                                child: Text(d.name),
                              ))
                          .toList(),
                      onChanged: (v) {
                        if (v != null) setModal(() => destId = v);
                      },
                    ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: dateCtrl,
                    decoration: const InputDecoration(labelText: 'Log Date (YYYY-MM-DD)'),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: localCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Local'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: TextField(
                          controller: domCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Domestic'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: TextField(
                          controller: forCtrl,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Foreign'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: revCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Total Revenue Collected (PHP)',
                    ),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                final res = await LakbayApiService.instance.recordArrival({
                  'destinationId': destId,
                  'logDate': dateCtrl.text.trim(),
                  'localTouristsCount': int.tryParse(localCtrl.text) ?? 0,
                  'domesticTouristsCount': int.tryParse(domCtrl.text) ?? 0,
                  'foreignTouristsCount': int.tryParse(forCtrl.text) ?? 0,
                  'totalRevenue': double.tryParse(revCtrl.text) ?? 0,
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'Arrival log recorded.').toString(),
                  isError: res['success'] != true,
                );
                _loadModuleData();
              },
              child: const Text('Save Arrival Log'),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 13: SYSTEM ADMIN USER & ROLE MANAGEMENT
  // ===========================================================================
  Widget _buildAdminUsersPane() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: ElevatedButton.icon(
            onPressed: _openCreateUserDialog,
            icon: const Icon(Icons.person_add_alt_1_rounded, size: 18),
            label: const Text('Create User Account'),
          ),
        ),
        const SizedBox(height: 14),
        Card(
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              columns: const [
                DataColumn(label: Text('USERNAME')),
                DataColumn(label: Text('FULL NAME')),
                DataColumn(label: Text('EMAIL')),
                DataColumn(label: Text('ROLE')),
                DataColumn(label: Text('JURISDICTION')),
                DataColumn(label: Text('STATUS')),
                DataColumn(label: Text('ACTIONS')),
              ],
              rows: _adminUsers.map((u) {
                final isActive = u.status.toUpperCase() == 'ACTIVE';
                return DataRow(
                  cells: [
                    DataCell(Text(
                      u.username,
                      style: const TextStyle(
                        fontWeight: FontWeight.w700,
                        fontFamily: 'Consolas',
                      ),
                    )),
                    DataCell(Text(u.fullName)),
                    DataCell(Text(u.email)),
                    DataCell(LakbayTheme.statusBadge(u.roleName)),
                    DataCell(Text(
                      u.barangayName.isNotEmpty
                          ? 'Brgy. ${u.barangayName}'
                          : 'Provincial',
                    )),
                    DataCell(LakbayTheme.statusBadge(u.status)),
                    DataCell(
                      Row(
                        children: [
                          OutlinedButton(
                            onPressed: () async {
                              final nextStatus = isActive ? 'SUSPENDED' : 'ACTIVE';
                              final res = await LakbayApiService.instance
                                  .updateAdminUserStatus(u.userId, nextStatus);
                              _showToast(
                                (res['message'] ?? 'Status updated').toString(),
                                isError: res['success'] != true,
                              );
                              _loadModuleData();
                            },
                            child: Text(isActive ? 'Suspend' : 'Activate'),
                          ),
                          const SizedBox(width: 6),
                          OutlinedButton.icon(
                            onPressed: () => _openResetPasswordDialog(u),
                            icon: const Icon(Icons.key_rounded, size: 15),
                            label: const Text('Reset Password'),
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

  Future<void> _openCreateUserDialog() async {
    final unameCtrl = TextEditingController();
    final nameCtrl = TextEditingController();
    final emailCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final passCtrl = TextEditingController(text: 'password123');
    int roleId = 6; // Tourist default

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModal) => AlertDialog(
          title: const Text('Create New System User Account'),
          content: SizedBox(
            width: 440,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(labelText: 'Full Name'),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: unameCtrl,
                    decoration: const InputDecoration(labelText: 'Username'),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: emailCtrl,
                    decoration: const InputDecoration(labelText: 'Email Address'),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: phoneCtrl,
                    decoration: const InputDecoration(labelText: 'Phone Number'),
                  ),
                  const SizedBox(height: 10),
                  DropdownButtonFormField<int>(
                    value: roleId,
                    decoration: const InputDecoration(labelText: 'Assigned Role'),
                    items: const [
                      DropdownMenuItem(value: 1, child: Text('System Administrator')),
                      DropdownMenuItem(value: 2, child: Text('Tourism Office')),
                      DropdownMenuItem(value: 3, child: Text('LGU Officer')),
                      DropdownMenuItem(value: 4, child: Text('Barangay Official')),
                      DropdownMenuItem(value: 5, child: Text('Local Stakeholder')),
                      DropdownMenuItem(value: 6, child: Text('Tourist')),
                    ],
                    onChanged: (v) {
                      if (v != null) setModal(() => roleId = v);
                    },
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: passCtrl,
                    decoration: const InputDecoration(labelText: 'Initial Password'),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                final res = await LakbayApiService.instance.createAdminUser({
                  'username': unameCtrl.text.trim(),
                  'fullName': nameCtrl.text.trim(),
                  'email': emailCtrl.text.trim(),
                  'phone': phoneCtrl.text.trim(),
                  'roleId': roleId,
                  'password': passCtrl.text,
                  'status': 'ACTIVE',
                });
                if (!ctx.mounted) return;
                Navigator.pop(ctx);
                _showToast(
                  (res['message'] ?? 'User created.').toString(),
                  isError: res['success'] != true,
                );
                _loadModuleData();
              },
              child: const Text('Create User'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _openResetPasswordDialog(UserAccount user) async {
    final passCtrl = TextEditingController(text: 'password123');
    await showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Reset Password for @${user.username}'),
        content: TextField(
          controller: passCtrl,
          decoration: const InputDecoration(
            labelText: 'New Password (min. 6 characters)',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              final res = await LakbayApiService.instance.resetAdminUserPassword(
                user.userId,
                passCtrl.text,
              );
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              _showToast(
                (res['message'] ?? 'Password reset.').toString(),
                isError: res['success'] != true,
              );
            },
            child: const Text('Reset Password'),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // MODULE 14: SYSTEM SETTINGS (WITH EDITABLE CURRENCY & SPECIFIC NOTIFICATIONS)
  // ===========================================================================
  Widget _buildSystemSettingsPane() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'System Settings & Provincial Parameters',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
                    ),
                    SizedBox(height: 4),
                    Text(
                      'Persisted in MySQL lakbay_lokal_db.system_settings and backups/system_settings.properties.',
                      style: TextStyle(color: LakbayTheme.slate500, fontSize: 13),
                    ),
                  ],
                ),
                ElevatedButton.icon(
                  onPressed: _saveSystemSettings,
                  icon: const Icon(Icons.save_rounded, size: 18),
                  label: const Text('Save Settings'),
                ),
              ],
            ),
            const Divider(height: 30),
            TextField(
              controller: _appNameCtrl,
              onChanged: (_) => _modifiedSettingKeys.add('appName'),
              decoration: const InputDecoration(labelText: 'Platform Name'),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _provinceCtrl,
              onChanged: (_) => _modifiedSettingKeys.add('province'),
              decoration: const InputDecoration(labelText: 'Provincial Jurisdiction'),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _thresholdCtrl,
                    keyboardType: TextInputType.number,
                    onChanged: (_) => _modifiedSettingKeys.add('capacityThreshold'),
                    decoration: const InputDecoration(
                      labelText: 'Carrying Capacity Alert Threshold (%)',
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: DropdownButtonFormField<String>(
                    value: _selectedCurrency,
                    decoration: const InputDecoration(labelText: 'Default Currency'),
                    items: SystemSettingsModel.currencyOptions
                        .map((c) => DropdownMenuItem(
                              value: c,
                              child: Text(c),
                            ))
                        .toList(),
                    onChanged: (v) {
                      if (v != null) {
                        setState(() {
                          _selectedCurrency = v;
                          _modifiedSettingKeys.add('currency');
                        });
                      }
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _broadcastCtrl,
              maxLines: 3,
              onChanged: (_) => _modifiedSettingKeys.add('broadcastMsg'),
              decoration: const InputDecoration(
                labelText: 'Live Broadcast Notice (Top Banner)',
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _saveSystemSettings() async {
    final threshold =
        (int.tryParse(_thresholdCtrl.text.trim()) ?? 85).clamp(50, 100);
    final res = await LakbayApiService.instance.saveSettings(
      appName: _appNameCtrl.text.trim(),
      province: _provinceCtrl.text.trim(),
      capacityThreshold: threshold,
      currency: _selectedCurrency,
      broadcastMsg: _broadcastCtrl.text.trim(),
      updatedFields: _modifiedSettingKeys.toList(),
    );
    _modifiedSettingKeys.clear();
    _showToast(
      (res['message'] ?? 'System settings saved successfully.').toString(),
      isError: res['success'] != true,
    );
    widget.onSettingsUpdated();
  }

  // ===========================================================================
  // MODULE 15: SECURITY AUDIT LOGS
  // ===========================================================================
  Widget _buildAuditLogsPane() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Tamper-Evident Security & Activity Audit Trail',
                  style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800),
                ),
                OutlinedButton.icon(
                  onPressed: _loadModuleData,
                  icon: const Icon(Icons.refresh_rounded, size: 16),
                  label: const Text('Refresh Logs'),
                ),
              ],
            ),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: DataTable(
                columns: const [
                  DataColumn(label: Text('TIMESTAMP')),
                  DataColumn(label: Text('USER')),
                  DataColumn(label: Text('ACTION')),
                  DataColumn(label: Text('ENTITY')),
                  DataColumn(label: Text('IP ADDRESS')),
                  DataColumn(label: Text('DETAILS')),
                ],
                rows: _auditLogs.map((log) {
                  return DataRow(
                    cells: [
                      DataCell(Text(log.timestamp)),
                      DataCell(Text(
                        log.username,
                        style: const TextStyle(fontWeight: FontWeight.w700),
                      )),
                      DataCell(LakbayTheme.statusBadge(log.action)),
                      DataCell(Text(log.entityType)),
                      DataCell(Text(log.ipAddress)),
                      DataCell(Text(log.details)),
                    ],
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ===========================================================================
  // MODULE 16: DATABASE BACKUP & DISASTER RECOVERY
  // ===========================================================================
  Widget _buildDatabaseBackupPane() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: LakbayTheme.primary50,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: const Icon(
                    Icons.dns_rounded,
                    color: LakbayTheme.primary600,
                    size: 30,
                  ),
                ),
                const SizedBox(width: 16),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'MySQL Relational Database State (lakbay_lokal_db)',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      SizedBox(height: 4),
                      Text(
                        'Connected to MySQL on port 3306 • 18 relational tables + system_settings + 3 analytical views.',
                        style: TextStyle(color: LakbayTheme.slate600),
                      ),
                    ],
                  ),
                ),
                ElevatedButton.icon(
                  onPressed: () async {
                    final res =
                        await LakbayApiService.instance.triggerDatabaseBackup();
                    setState(() {
                      _backupResultText =
                          (res['message'] ?? 'Backup generated.').toString();
                    });
                    _showToast(
                      _backupResultText!,
                      isError: res['success'] != true,
                    );
                  },
                  icon: const Icon(Icons.backup_rounded, size: 18),
                  label: const Text('Generate SQL Backup Dump'),
                ),
              ],
            ),
            if (_backupResultText != null) ...[
              const SizedBox(height: 18),
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: LakbayTheme.primary50,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: LakbayTheme.primary200),
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.check_circle_rounded,
                      color: LakbayTheme.primary800,
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        _backupResultText!,
                        style: const TextStyle(
                          color: LakbayTheme.primary800,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

