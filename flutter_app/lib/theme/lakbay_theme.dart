import 'package:flutter/material.dart';

/// Official Lakbay Lokal Design System — Philippine Nature & Ocean Palette
/// Preserves the exact colors from src/main/resources/web/css/style.css
class LakbayTheme {
  // Primary Palette — Forest Green & Emerald
  static const Color primary950 = Color(0xFF042F2E);
  static const Color primary900 = Color(0xFF064E3B);
  static const Color primary800 = Color(0xFF065F46);
  static const Color primary700 = Color(0xFF047857);
  static const Color primary600 = Color(0xFF059669);
  static const Color primary500 = Color(0xFF10B981);
  static const Color primary400 = Color(0xFF34D399);
  static const Color primary300 = Color(0xFF6EE7B7);
  static const Color primary200 = Color(0xFFA7F3D0);
  static const Color primary100 = Color(0xFFD1FAE5);
  static const Color primary50 = Color(0xFFECFDF5);

  // Secondary / Accent Palette — Amber Sunset & Coastal Cyan
  static const Color accent700 = Color(0xFFB45309);
  static const Color accent600 = Color(0xFFD97706);
  static const Color accent500 = Color(0xFFF59E0B);
  static const Color accent200 = Color(0xFFFDE68A);
  static const Color accent100 = Color(0xFFFEF3C7);
  static const Color accent50 = Color(0xFFFFFBEB);

  static const Color cyan700 = Color(0xFF0E7490);
  static const Color cyan600 = Color(0xFF0891B2);
  static const Color cyan500 = Color(0xFF06B6D4);
  static const Color cyan100 = Color(0xFFCFFAFE);
  static const Color cyan50 = Color(0xFFECFEFF);

  // Neutrals & Slate Base
  static const Color slate900 = Color(0xFF0F172A);
  static const Color slate800 = Color(0xFF1E293B);
  static const Color slate700 = Color(0xFF334155);
  static const Color slate600 = Color(0xFF475569);
  static const Color slate500 = Color(0xFF64748B);
  static const Color slate400 = Color(0xFF94A3B8);
  static const Color slate300 = Color(0xFFCBD5E1);
  static const Color slate200 = Color(0xFFE2E8F0);
  static const Color slate100 = Color(0xFFF1F5F9);
  static const Color slate50 = Color(0xFFF8FAFC);
  static const Color bgMain = Color(0xFFF3F6F9);
  static const Color white = Color(0xFFFFFFFF);
  static const Color titlebarDark = Color(0xFF021A13);

  // Functional Status Colors
  static const Color statusSuccess = Color(0xFF10B981);
  static const Color statusWarning = Color(0xFFF59E0B);
  static const Color statusDanger = Color(0xFFEF4444);
  static const Color statusInfo = Color(0xFF3B82F6);

  static ThemeData get lightTheme {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: primary700,
      primary: primary700,
      secondary: accent600,
      tertiary: cyan600,
      surface: white,
      error: statusDanger,
      brightness: Brightness.light,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: bgMain,
      fontFamily: 'Segoe UI',
      appBarTheme: const AppBarTheme(
        backgroundColor: white,
        foregroundColor: slate900,
        elevation: 0,
        scrolledUnderElevation: 1,
        surfaceTintColor: Colors.transparent,
      ),
      cardTheme: CardThemeData(
        color: white,
        elevation: 0,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: const BorderSide(color: slate200, width: 1),
        ),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primary700,
          foregroundColor: white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          textStyle: const TextStyle(
            fontWeight: FontWeight.w700,
            fontSize: 13.5,
          ),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: slate700,
          backgroundColor: white,
          side: const BorderSide(color: slate300),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 13),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          textStyle: const TextStyle(
            fontWeight: FontWeight.w600,
            fontSize: 13.5,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: white,
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: slate300),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: slate300),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: primary600, width: 1.8),
        ),
        labelStyle: const TextStyle(
          color: slate700,
          fontWeight: FontWeight.w600,
          fontSize: 13,
        ),
        hintStyle: const TextStyle(
          color: slate400,
          fontSize: 13,
        ),
      ),
      dividerTheme: const DividerThemeData(
        color: slate200,
        thickness: 1,
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: white,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
    );
  }

  /// Helper widget for consistent status pills matching `.badge` in style.css
  static Widget statusBadge(String text, {String? type}) {
    final upper = (type ?? text).toUpperCase();
    Color bg = const Color(0xFFE0F2FE);
    Color fg = const Color(0xFF0369A1);

    if (upper.contains('ACTIVE') ||
        upper.contains('APPROVED') ||
        upper.contains('CONFIRMED') ||
        upper.contains('COMPLETED') ||
        upper.contains('ACCEPTED') ||
        upper.contains('PUBLISHED') ||
        upper.contains('SAFE') ||
        upper.contains('SUCCESS')) {
      bg = primary100;
      fg = primary800;
    } else if (upper.contains('PENDING') ||
        upper.contains('MODERATE') ||
        upper.contains('RETURNED') ||
        upper.contains('WARNING')) {
      bg = accent100;
      fg = const Color(0xFF92400E);
    } else if (upper.contains('REJECTED') ||
        upper.contains('CANCELLED') ||
        upper.contains('INACTIVE') ||
        upper.contains('SUSPENDED') ||
        upper.contains('CRITICAL') ||
        upper.contains('DANGER')) {
      bg = const Color(0xFFFEE2E2);
      fg = const Color(0xFF991B1B);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        text.toUpperCase(),
        style: TextStyle(
          color: fg,
          fontSize: 11,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.3,
        ),
      ),
    );
  }

  /// Resolves a Biliran asset image from the backend URL or bundled Flutter assets
  static String resolveAssetPath(String? imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty) {
      return 'assets/images/sambawan_island_hero_1791699785896.jpg';
    }
    final lower = imageUrl.toLowerCase();
    if (lower.contains('tinago')) {
      return 'assets/images/tinago_falls_hero_1791699805710.jpg';
    }
    if (lower.contains('higatangan')) {
      return 'assets/images/higatangan_sandbar_hero.jpg';
    }
    if (lower.contains('ulan')) {
      return 'assets/images/ulan_ulan_falls_hero.jpg';
    }
    return 'assets/images/sambawan_island_hero_1791699785896.jpg';
  }
}

