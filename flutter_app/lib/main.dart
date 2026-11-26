import 'package:flutter/material.dart';

import 'models/lakbay_models.dart';
import 'screens/auth_screen.dart';
import 'screens/dashboard_shell.dart';
import 'screens/public_home_screen.dart';
import 'services/api_service.dart';
import 'theme/lakbay_theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const LakbayLokalApp());
}

class LakbayLokalApp extends StatefulWidget {
  const LakbayLokalApp({super.key});

  @override
  State<LakbayLokalApp> createState() => _LakbayLokalAppState();
}

enum _AppScreenState {
  splash,
  publicPortal,
  authLogin,
  authRegister,
  dashboard,
}

class _LakbayLokalAppState extends State<LakbayLokalApp> {
  _AppScreenState _screenState = _AppScreenState.splash;
  UserAccount? _authenticatedUser;
  String _bootStatus = 'Initializing Lakbay Lokal Desktop & Mobile Engine...';

  @override
  void initState() {
    super.initState();
    _bootstrapApplication();
  }

  Future<void> _bootstrapApplication() async {
    setState(() {
      _bootStatus = 'Connecting to Lakbay Lokal Java REST Backend & MySQL (lakbay_lokal_db)...';
    });
    await LakbayApiService.instance.ensureBackendReady();
    await Future.delayed(const Duration(milliseconds: 600));
    if (!mounted) return;
    setState(() {
      _screenState = _AppScreenState.publicPortal;
    });
  }

  @override
  void dispose() {
    LakbayApiService.instance.disposeBackend();
    super.dispose();
  }

  void _openServerConfigDialog(BuildContext context) {
    final urlCtrl = TextEditingController(
      text: LakbayApiService.instance.baseUrl,
    );
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Java Backend REST API Endpoint'),
        content: SizedBox(
          width: 420,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Windows Desktop uses http://127.0.0.1:8080.\nAndroid Emulator uses http://10.0.2.2:8080, or enter your PC LAN IP for a physical Android phone.',
                style: TextStyle(fontSize: 12.5, color: LakbayTheme.slate600),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: urlCtrl,
                decoration: const InputDecoration(
                  labelText: 'Backend Base URL',
                  hintText: 'http://127.0.0.1:8080',
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
            onPressed: () {
              LakbayApiService.instance.setBaseUrl(urlCtrl.text);
              Navigator.pop(ctx);
            },
            child: const Text('Save Endpoint'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Lakbay Lokal: Biliran Community Tourism System',
      debugShowCheckedModeBanner: false,
      theme: LakbayTheme.lightTheme,
      home: Builder(
        builder: (innerContext) {
          switch (_screenState) {
            case _AppScreenState.splash:
              return _buildSplashScreen();
            case _AppScreenState.publicPortal:
              return PublicHomeScreen(
                onOpenLogin: () =>
                    setState(() => _screenState = _AppScreenState.authLogin),
                onOpenRegister: () =>
                    setState(() => _screenState = _AppScreenState.authRegister),
                onConfigureServer: () => _openServerConfigDialog(innerContext),
              );
            case _AppScreenState.authLogin:
            case _AppScreenState.authRegister:
              return AuthScreen(
                startOnRegister: _screenState == _AppScreenState.authRegister,
                onBackToPublic: () =>
                    setState(() => _screenState = _AppScreenState.publicPortal),
                onAuthenticated: (user) {
                  setState(() {
                    _authenticatedUser = user;
                    _screenState = _AppScreenState.dashboard;
                  });
                },
              );
            case _AppScreenState.dashboard:
              if (_authenticatedUser == null) {
                return PublicHomeScreen(
                  onOpenLogin: () =>
                      setState(() => _screenState = _AppScreenState.authLogin),
                  onOpenRegister: () =>
                      setState(() => _screenState = _AppScreenState.authRegister),
                  onConfigureServer: () => _openServerConfigDialog(innerContext),
                );
              }
              return DashboardShell(
                user: _authenticatedUser!,
                onLoggedOut: () {
                  setState(() {
                    _authenticatedUser = null;
                    _screenState = _AppScreenState.publicPortal;
                  });
                },
              );
          }
        },
      ),
    );
  }

  Widget _buildSplashScreen() {
    return Scaffold(
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              LakbayTheme.primary950,
              LakbayTheme.primary900,
              LakbayTheme.primary700,
            ],
          ),
        ),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 84,
                height: 84,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.14),
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: Colors.white30, width: 1.5),
                ),
                child: const Icon(
                  Icons.explore_rounded,
                  color: LakbayTheme.primary300,
                  size: 48,
                ),
              ),
              const SizedBox(height: 22),
              const Text(
                'LAKBAY LOKAL',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 30,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 1.2,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Community-Based Tourism Information System • Biliran, Philippines',
                style: TextStyle(
                  color: LakbayTheme.primary200,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 32),
              const SizedBox(
                width: 34,
                height: 34,
                child: CircularProgressIndicator(
                  color: LakbayTheme.primary300,
                  strokeWidth: 3,
                ),
              ),
              const SizedBox(height: 16),
              Text(
                _bootStatus,
                style: const TextStyle(
                  color: Colors.white70,
                  fontSize: 13,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

