import 'dart:async';
import 'dart:io';

/// Native Dart Windows Desktop Launcher for Lakbay Lokal.
/// Compiled to `LakbayLokalLauncher.exe` via `dart compile exe`.
/// Automatically verifies MySQL (port 3306), starts the Java REST API backend
/// (`com.lakbaylokal.Main --headless` on port 8080), and opens the standalone
/// Lakbay Lokal Windows Desktop Application window without opening a web browser.
Future<void> main(List<String> args) async {
  stdout.writeln('==============================================================');
  stdout.writeln('  LAKBAY LOKAL: Community-Based Tourism Information System');
  stdout.writeln('  Native Dart & Flutter Desktop Launcher (Biliran, PH)');
  stdout.writeln('==============================================================');

  final bool backendOnly = args.contains('--backend-only') || args.contains('--headless');
  final Directory rootDir = _resolveProjectRoot();
  stdout.writeln('[Launcher] Project Root: ${rootDir.path}');

  // 1. Ensure MySQL is listening on 127.0.0.1:3306
  await _ensureMySqlRunning();

  // 2. Ensure Java REST Backend is listening on 127.0.0.1:8080
  await _ensureJavaBackendRunning(rootDir);

  if (backendOnly) {
    stdout.writeln('[Launcher] Backend-only mode complete. REST API active on http://127.0.0.1:8080');
    return;
  }

  // 3. Launch Standalone Windows Desktop Application Window
  await _launchDesktopWindow(rootDir);
}

Directory _resolveProjectRoot() {
  final cwd = Directory.current;
  if (File('${cwd.path}${Platform.pathSeparator}bin${Platform.pathSeparator}com${Platform.pathSeparator}lakbaylokal${Platform.pathSeparator}Main.class').existsSync()) {
    return cwd;
  }
  final parent = cwd.parent;
  if (File('${parent.path}${Platform.pathSeparator}bin${Platform.pathSeparator}com${Platform.pathSeparator}lakbaylokal${Platform.pathSeparator}Main.class').existsSync()) {
    return parent;
  }
  return cwd;
}

Future<bool> _isPortOpen(int port, {String host = '127.0.0.1'}) async {
  try {
    final socket = await Socket.connect(host, port, timeout: const Duration(milliseconds: 900));
    await socket.close();
    return true;
  } catch (_) {
    return false;
  }
}

Future<bool> _isBackendHealthy() async {
  final client = HttpClient()..connectionTimeout = const Duration(milliseconds: 1200);
  try {
    final req = await client.getUrl(Uri.parse('http://127.0.0.1:8080/api/destinations'));
    final res = await req.close();
    await res.drain<void>();
    return res.statusCode == 200;
  } catch (_) {
    return false;
  } finally {
    client.close(force: true);
  }
}

Future<void> _ensureMySqlRunning() async {
  if (await _isPortOpen(3306)) {
    stdout.writeln('[MySQL] Port 3306 is active (lakbay_lokal_db ready).');
    return;
  }
  final candidates = [
    r'C:\wamp64\bin\mysql\mysql8.4.7\bin\mysqld.exe',
    r'C:\xampp\mysql\bin\mysqld.exe',
  ];
  for (final path in candidates) {
    final file = File(path);
    if (file.existsSync()) {
      stdout.writeln('[MySQL] Starting local MySQL server: $path');
      try {
        await Process.start(
          path,
          const [],
          workingDirectory: file.parent.path,
          mode: ProcessStartMode.detached,
        );
        for (int i = 0; i < 10; i++) {
          await Future<void>.delayed(const Duration(milliseconds: 450));
          if (await _isPortOpen(3306)) {
            stdout.writeln('[MySQL] MySQL started successfully on port 3306.');
            return;
          }
        }
      } catch (e) {
        stderr.writeln('[MySQL] Warning starting mysqld: $e');
      }
    }
  }
}

String _resolveJavaExe() {
  final javaHome = Platform.environment['JAVA_HOME'];
  if (javaHome != null && javaHome.isNotEmpty) {
    final candidate = File('$javaHome\\bin\\java.exe');
    if (candidate.existsSync()) return candidate.path;
  }
  final localAppData = Platform.environment['LOCALAPPDATA'] ?? '';
  final bases = [
    '$localAppData\\Programs\\Eclipse Adoptium',
    r'C:\Program Files\Eclipse Adoptium',
    r'C:\Program Files\Java',
  ];
  for (final base in bases) {
    final dir = Directory(base);
    if (dir.existsSync()) {
      final subdirs = dir.listSync().whereType<Directory>().toList()
        ..sort((a, b) => b.path.compareTo(a.path));
      for (final sub in subdirs) {
        final exe = File('${sub.path}\\bin\\java.exe');
        if (exe.existsSync()) return exe.path;
      }
    }
  }
  return 'java';
}

Future<void> _ensureJavaBackendRunning(Directory rootDir) async {
  if (await _isBackendHealthy()) {
    stdout.writeln('[Backend] Java REST API server already active on http://127.0.0.1:8080');
    return;
  }

  final javaExe = _resolveJavaExe();
  final cp = '${rootDir.path}\\bin;${rootDir.path}\\lib\\*';
  stdout.writeln('[Backend] Starting headless Java REST server ($javaExe)...');

  await Process.start(
    javaExe,
    ['-cp', cp, 'com.lakbaylokal.Main', '--headless'],
    workingDirectory: rootDir.path,
    mode: ProcessStartMode.detached,
  );

  for (int i = 0; i < 15; i++) {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    if (await _isBackendHealthy()) {
      stdout.writeln('[Backend] Java REST server online at http://127.0.0.1:8080');
      return;
    }
  }
  stdout.writeln('[Backend] Proceeding while Java backend finishes initialization...');
}

Future<void> _launchDesktopWindow(Directory rootDir) async {
  final candidates = [
    File('${rootDir.path}\\flutter_app\\build\\windows\\x64\\runner\\Release\\lakbay_lokal_app.exe'),
    File('${rootDir.path}\\dist\\win-unpacked\\Lakbay Lokal.exe'),
    File('${rootDir.path}\\node_modules\\electron\\dist\\electron.exe'),
  ];

  for (final exe in candidates) {
    if (exe.existsSync()) {
      final isElectronRuntime = exe.path.toLowerCase().endsWith('electron.exe');
      final launchArgs = isElectronRuntime ? [rootDir.path] : const <String>[];
      stdout.writeln('[Desktop] Launching standalone desktop application: ${exe.path}');
      await Process.start(
        exe.path,
        launchArgs,
        workingDirectory: rootDir.path,
        mode: ProcessStartMode.detached,
      );
      stdout.writeln('[Desktop] Lakbay Lokal Desktop Application window launched.');
      return;
    }
  }

  stderr.writeln('[Desktop] Could not find desktop executable in dist/win-unpacked or node_modules/electron.');
}

