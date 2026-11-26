import 'package:flutter_test/flutter_test.dart';
import 'package:lakbay_lokal_app/main.dart';
import 'package:lakbay_lokal_app/models/lakbay_models.dart';
import 'package:lakbay_lokal_app/theme/lakbay_theme.dart';

void main() {
  test('RoleMetadata defines all 6 Lakbay Lokal user roles and 16 modules', () {
    expect(RoleMetadata.roles.length, 6);
    expect(RoleMetadata.roles.containsKey('SYSTEM_ADMIN'), isTrue);
    expect(RoleMetadata.roles.containsKey('TOURISM_OFFICE'), isTrue);
    expect(RoleMetadata.roles.containsKey('LGU_OFFICER'), isTrue);
    expect(RoleMetadata.roles.containsKey('BARANGAY_OFFICIAL'), isTrue);
    expect(RoleMetadata.roles.containsKey('STAKEHOLDER'), isTrue);
    expect(RoleMetadata.roles.containsKey('TOURIST'), isTrue);
    expect(RoleMetadata.allModules.length, 16);
  });

  test('LakbayTheme preserves the original Philippine Nature & Ocean color palette', () {
    expect(LakbayTheme.primaryEmerald.value, 0xFF047857);
    expect(LakbayTheme.emeraldDark.value, 0xFF064E3B);
    expect(LakbayTheme.emeraldBright.value, 0xFF10B981);
    expect(LakbayTheme.secondaryTeal.value, 0xFF0D9488);
    expect(LakbayTheme.accentAmber.value, 0xFFD97706);
    expect(LakbayTheme.bgMain.value, 0xFFF3F6F9);
  });

  testWidgets('LakbayLokalApp builds without error', (WidgetTester tester) async {
    await tester.pumpWidget(const LakbayLokalApp());
    expect(find.text('LAKBAY LOKAL'), findsOneWidget);
  });
}

