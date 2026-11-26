import 'dart:ui';
import 'package:flutter/material.dart';

import '../models/lakbay_models.dart';
import '../services/api_service.dart';
import '../theme/lakbay_theme.dart';

/// True Glassmorphism Login, Registration & Password Recovery Portal
/// Preserves the exact visual identity of #login-portal-view in style.css
class AuthScreen extends StatefulWidget {
  final bool startOnRegister;
  final ValueChanged<UserAccount> onAuthenticated;
  final VoidCallback onBackToPublic;

  const AuthScreen({
    super.key,
    this.startOnRegister = false,
    required this.onAuthenticated,
    required this.onBackToPublic,
  });

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  late bool _isRegisterMode;
  bool _obscurePassword = true;
  bool _submitting = false;
  String? _errorMsg;
  String? _successMsg;

  // Login controllers
  final TextEditingController _loginUserCtrl = TextEditingController();
  final TextEditingController _loginPassCtrl = TextEditingController();

  // Registration controllers
  final TextEditingController _regFullNameCtrl = TextEditingController();
  final TextEditingController _regUsernameCtrl = TextEditingController();
  final TextEditingController _regEmailCtrl = TextEditingController();
  final TextEditingController _regPhoneCtrl = TextEditingController();
  final TextEditingController _regOriginCtrl = TextEditingController();
  final TextEditingController _regPassCtrl = TextEditingController();

  static const List<Map<String, String>> _demoRoles = [
    {'role': 'System Admin', 'user': 'admin', 'color': 'admin'},
    {'role': 'Tourism Office', 'user': 'tourismoffice', 'color': 'tourism'},
    {'role': 'LGU Officer', 'user': 'lgu_naval', 'color': 'lgu'},
    {'role': 'Barangay Official', 'user': 'brgy_olog', 'color': 'barangay'},
    {'role': 'Stakeholder', 'user': 'guide_pedro', 'color': 'stakeholder'},
    {'role': 'Tourist', 'user': 'tourist_ana', 'color': 'tourist'},
  ];

  @override
  void initState() {
    super.initState();
    _isRegisterMode = widget.startOnRegister;
  }

  Future<void> _submitLogin([String? quickUser, String? quickPass]) async {
    final username = (quickUser ?? _loginUserCtrl.text).trim();
    final password = quickPass ?? _loginPassCtrl.text;

    if (username.isEmpty || password.isEmpty) {
      setState(() {
        _errorMsg = 'Please enter both username and password.';
        _successMsg = null;
      });
      return;
    }

    setState(() {
      _submitting = true;
      _errorMsg = null;
      _successMsg = null;
    });

    try {
      final res = await LakbayApiService.instance.login(username, password);
      if (!mounted) return;
      if (res['success'] == true && LakbayApiService.instance.currentUser != null) {
        widget.onAuthenticated(LakbayApiService.instance.currentUser!);
      } else {
        setState(() {
          _errorMsg = (res['message'] ?? 'Invalid username or password.').toString();
          _submitting = false;
        });
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _errorMsg = 'Cannot reach Lakbay Lokal Java Backend (${LakbayApiService.instance.baseUrl}).';
        _submitting = false;
      });
    }
  }

  Future<void> _submitRegister() async {
    final fullName = _regFullNameCtrl.text.trim();
    final username = _regUsernameCtrl.text.trim();
    final email = _regEmailCtrl.text.trim();
    final phone = _regPhoneCtrl.text.trim();
    final origin = _regOriginCtrl.text.trim();
    final password = _regPassCtrl.text;

    if (fullName.isEmpty || username.isEmpty || email.isEmpty || password.length < 6) {
      setState(() {
        _errorMsg = 'Full name, username, email, and a password (min. 6 chars) are required.';
        _successMsg = null;
      });
      return;
    }

    setState(() {
      _submitting = true;
      _errorMsg = null;
      _successMsg = null;
    });

    try {
      final res = await LakbayApiService.instance.registerTourist(
        username: username,
        password: password,
        email: email,
        fullName: fullName,
        phone: phone,
        originCity: origin.isEmpty ? 'Cebu City' : origin,
      );
      if (!mounted) return;
      if (res['success'] == true) {
        _loginUserCtrl.text = username;
        _loginPassCtrl.text = password;
        setState(() {
          _isRegisterMode = false;
          _submitting = false;
          _successMsg = 'Registration successful! You can now sign in.';
        });
      } else {
        setState(() {
          _errorMsg = (res['message'] ?? 'Registration failed.').toString();
          _submitting = false;
        });
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _errorMsg = 'Error communicating with backend server.';
        _submitting = false;
      });
    }
  }

  Future<void> _openForgotPasswordDialog() async {
    final idCtrl = TextEditingController();
    final passCtrl = TextEditingController();
    String? dialogMsg;
    bool isSuccess = false;

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          title: const Row(
            children: [
              Icon(Icons.key_rounded, color: LakbayTheme.primary700),
              SizedBox(width: 8),
              Text('Account Password Recovery'),
            ],
          ),
          content: SizedBox(
            width: 420,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Enter your registered username or email address and your new password to update your credentials in lakbay_lokal_db.',
                  style: TextStyle(fontSize: 13, color: LakbayTheme.slate600),
                ),
                const SizedBox(height: 14),
                if (dialogMsg != null)
                  Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: isSuccess ? LakbayTheme.primary50 : const Color(0xFFFEE2E2),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      dialogMsg!,
                      style: TextStyle(
                        color: isSuccess ? LakbayTheme.primary800 : const Color(0xFF991B1B),
                        fontWeight: FontWeight.w600,
                        fontSize: 12.5,
                      ),
                    ),
                  ),
                TextField(
                  controller: idCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Username or Email',
                    hintText: 'e.g. tourist_ana or guide_pedro',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: passCtrl,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'New Password (min. 6 characters)',
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Close'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (idCtrl.text.trim().isEmpty || passCtrl.text.length < 6) {
                  setDialogState(() {
                    isSuccess = false;
                    dialogMsg = 'Enter a valid username/email and at least 6-character password.';
                  });
                  return;
                }
                final res = await LakbayApiService.instance.forgotPassword(
                  usernameOrEmail: idCtrl.text,
                  newPassword: passCtrl.text,
                );
                setDialogState(() {
                  isSuccess = res['success'] == true;
                  dialogMsg = (res['message'] ?? 'Password updated.').toString();
                });
              },
              child: const Text('Reset Password'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    final isWide = width >= 880;

    return Scaffold(
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: const BoxDecoration(
          gradient: RadialGradient(
            center: Alignment(0, -0.6),
            radius: 1.2,
            colors: [
              LakbayTheme.primary900,
              LakbayTheme.primary950,
              Color(0xFF021A13),
            ],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 980),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(28),
                  child: BackdropFilter(
                    filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(28),
                        border: Border.all(color: Colors.white.withOpacity(0.22)),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.5),
                            blurRadius: 48,
                            offset: const Offset(0, 20),
                          ),
                        ],
                      ),
                      child: isWide
                          ? IntrinsicHeight(
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  Expanded(flex: 11, child: _buildLeftPanel()),
                                  Expanded(flex: 12, child: _buildRightPanel()),
                                ],
                              ),
                            )
                          : Column(
                              children: [
                                _buildLeftPanel(compact: true),
                                _buildRightPanel(),
                              ],
                            ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLeftPanel({bool compact = false}) {
    return Container(
      padding: EdgeInsets.all(compact ? 24 : 38),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            const Color(0xFF032219).withOpacity(0.92),
            LakbayTheme.primary900.withOpacity(0.82),
          ],
        ),
        border: Border(
          right: compact
              ? BorderSide.none
              : BorderSide(color: Colors.white.withOpacity(0.15)),
          bottom: compact
              ? BorderSide(color: Colors.white.withOpacity(0.15))
              : BorderSide.none,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.16),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: Colors.white.withOpacity(0.3)),
                    ),
                    child: const Icon(
                      Icons.explore_rounded,
                      color: LakbayTheme.primary300,
                      size: 26,
                    ),
                  ),
                  const SizedBox(width: 12),
                  const Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'LAKBAY LOKAL',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 20,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      Text(
                        'Biliran Community Tourism Portal',
                        style: TextStyle(
                          color: LakbayTheme.primary200,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              IconButton(
                tooltip: 'Back to Public Homepage',
                onPressed: widget.onBackToPublic,
                icon: const Icon(Icons.arrow_back_rounded, color: Colors.white),
              ),
            ],
          ),
          if (!compact) ...[
            const SizedBox(height: 28),
            const Text(
              'Empowering Biliran Communities Through Sustainable Tourism',
              style: TextStyle(
                color: Colors.white,
                fontSize: 26,
                fontWeight: FontWeight.w800,
                height: 1.25,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'Unified platform connecting Tourists, Local Tourism Stakeholders, Barangay Officials, Municipal LGUs, the Provincial Tourism Office, and System Administrators.',
              style: TextStyle(
                color: Colors.white.withOpacity(0.88),
                fontSize: 14,
                height: 1.55,
              ),
            ),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.1),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.white.withOpacity(0.22)),
              ),
              child: const Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(Icons.shield_rounded, color: LakbayTheme.primary300, size: 22),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Role-Based Access Control (RBAC) verified server-side against MySQL lakbay_lokal_db with PBKDF2/SHA-256 salted password authentication.',
                      style: TextStyle(
                        color: LakbayTheme.slate200,
                        fontSize: 12.5,
                        height: 1.45,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Province of Biliran • Region VIII (Eastern Visayas)',
              style: TextStyle(color: LakbayTheme.slate400, fontSize: 12),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildRightPanel() {
    return Container(
      padding: const EdgeInsets.all(34),
      color: LakbayTheme.slate900.withOpacity(0.42),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            _isRegisterMode ? 'Create Tourist Account' : 'Sign In to Your Portal',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 25,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            _isRegisterMode
                ? 'Register to book Biliran tour packages and community services.'
                : 'Enter your credentials to open your role-specific dashboard.',
            style: const TextStyle(color: LakbayTheme.slate400, fontSize: 13.5),
          ),
          const SizedBox(height: 18),
          if (_errorMsg != null)
            Container(
              margin: const EdgeInsets.only(bottom: 14),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              decoration: BoxDecoration(
                color: LakbayTheme.statusDanger.withOpacity(0.2),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: LakbayTheme.statusDanger.withOpacity(0.5)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.error_outline_rounded, color: Color(0xFFFCA5A5), size: 18),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _errorMsg!,
                      style: const TextStyle(color: Color(0xFFFCA5A5), fontSize: 12.5),
                    ),
                  ),
                ],
              ),
            ),
          if (_successMsg != null)
            Container(
              margin: const EdgeInsets.only(bottom: 14),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              decoration: BoxDecoration(
                color: LakbayTheme.primary500.withOpacity(0.2),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: LakbayTheme.primary400.withOpacity(0.5)),
              ),
              child: Text(
                _successMsg!,
                style: const TextStyle(color: LakbayTheme.primary200, fontSize: 12.5),
              ),
            ),
          if (!_isRegisterMode) ...[
            _buildGlassInput(
              controller: _loginUserCtrl,
              label: 'Username or Email',
              hint: 'e.g. admin, tourismoffice, tourist_ana',
              icon: Icons.person_outline_rounded,
            ),
            const SizedBox(height: 14),
            _buildGlassInput(
              controller: _loginPassCtrl,
              label: 'Password',
              hint: 'Enter your password',
              icon: Icons.lock_outline_rounded,
              obscure: _obscurePassword,
              suffix: IconButton(
                onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                icon: Icon(
                  _obscurePassword ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                  color: LakbayTheme.slate400,
                  size: 19,
                ),
              ),
              onSubmitted: (_) => _submitLogin(),
            ),
            const SizedBox(height: 10),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton(
                onPressed: _openForgotPasswordDialog,
                child: const Text(
                  'Forgot Password?',
                  style: TextStyle(
                    color: LakbayTheme.primary300,
                    fontWeight: FontWeight.w600,
                    fontSize: 13,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 6),
            SizedBox(
              height: 48,
              child: ElevatedButton.icon(
                onPressed: _submitting ? null : () => _submitLogin(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: LakbayTheme.primary600,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                icon: _submitting
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Icon(Icons.login_rounded),
                label: Text(_submitting ? 'Authenticating...' : 'Sign In to Dashboard'),
              ),
            ),
            const SizedBox(height: 20),
            const Divider(color: Colors.white24),
            const SizedBox(height: 8),
            const Text(
              'QUICK 1-CLICK ROLE VERIFICATION LOGINS (PASSWORD: password123)',
              style: TextStyle(
                color: LakbayTheme.slate400,
                fontSize: 11,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.5,
              ),
            ),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _demoRoles.map((r) {
                return InkWell(
                  onTap: _submitting
                      ? null
                      : () {
                          _loginUserCtrl.text = r['user']!;
                          _loginPassCtrl.text = 'password123';
                          _submitLogin(r['user']!, 'password123');
                        },
                  borderRadius: BorderRadius.circular(8),
                  child: Container(
                    width: 132,
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.07),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.white.withOpacity(0.14)),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          r['role']!,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            color: LakbayTheme.primary300,
                            fontWeight: FontWeight.w700,
                            fontSize: 11.5,
                          ),
                        ),
                        Text(
                          '@${r['user']}',
                          style: const TextStyle(
                            color: LakbayTheme.slate300,
                            fontSize: 11,
                            fontFamily: 'Consolas',
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              }).toList(),
            ),
          ] else ...[
            _buildGlassInput(
              controller: _regFullNameCtrl,
              label: 'Full Name',
              hint: 'e.g. Maria Clara Santos',
              icon: Icons.badge_outlined,
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _buildGlassInput(
                    controller: _regUsernameCtrl,
                    label: 'Username',
                    hint: 'tourist_maria',
                    icon: Icons.alternate_email_rounded,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildGlassInput(
                    controller: _regPhoneCtrl,
                    label: 'Phone Number',
                    hint: '09171234567',
                    icon: Icons.phone_outlined,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _buildGlassInput(
                    controller: _regEmailCtrl,
                    label: 'Email Address',
                    hint: 'maria@example.com',
                    icon: Icons.email_outlined,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildGlassInput(
                    controller: _regOriginCtrl,
                    label: 'Origin City / Province',
                    hint: 'Tacloban / Cebu',
                    icon: Icons.location_city_outlined,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            _buildGlassInput(
              controller: _regPassCtrl,
              label: 'Password (min. 6 characters)',
              hint: 'Create a strong password',
              icon: Icons.lock_outline_rounded,
              obscure: true,
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 48,
              child: ElevatedButton.icon(
                onPressed: _submitting ? null : _submitRegister,
                style: ElevatedButton.styleFrom(
                  backgroundColor: LakbayTheme.primary600,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                icon: const Icon(Icons.person_add_alt_1_rounded),
                label: Text(_submitting ? 'Registering...' : 'Complete Tourist Registration'),
              ),
            ),
          ],
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _isRegisterMode ? 'Already have an account?' : 'Visiting Biliran for the first time?',
                style: const TextStyle(color: LakbayTheme.slate400, fontSize: 13),
              ),
              TextButton(
                onPressed: () {
                  setState(() {
                    _isRegisterMode = !_isRegisterMode;
                    _errorMsg = null;
                    _successMsg = null;
                  });
                },
                child: Text(
                  _isRegisterMode ? 'Sign In' : 'Register as Tourist',
                  style: const TextStyle(
                    color: LakbayTheme.primary300,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildGlassInput({
    required TextEditingController controller,
    required String label,
    required String hint,
    required IconData icon,
    bool obscure = false,
    Widget? suffix,
    ValueChanged<String>? onSubmitted,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            color: LakbayTheme.slate300,
            fontSize: 12.5,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 5),
        TextField(
          controller: controller,
          obscureText: obscure,
          onSubmitted: onSubmitted,
          style: const TextStyle(color: Colors.white, fontSize: 14),
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: const TextStyle(color: LakbayTheme.slate500, fontSize: 13),
            filled: true,
            fillColor: Colors.white.withOpacity(0.08),
            prefixIcon: Icon(icon, color: LakbayTheme.slate400, size: 19),
            suffixIcon: suffix,
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.white.withOpacity(0.2)),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(color: LakbayTheme.primary500, width: 1.6),
            ),
          ),
        ),
      ],
    );
  }
}

