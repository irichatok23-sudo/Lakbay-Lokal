package lakbaylokal.barangay;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.service.DestinationService;
import lakbaylokal.service.ReportService;
import lakbaylokal.service.VerificationService;

public class BarangayController {
    private final VerificationService verificationService;
    private final DestinationService destinationService;
    private final ReportService reportService;
    private BarangayDashboard view;

    public BarangayController() {
        this.verificationService = new VerificationService();
        this.destinationService = new DestinationService();
        this.reportService = new ReportService();
    }

    public void setView(BarangayDashboard view) {
        this.view = view;
    }

    public void handleLogout() {
        SessionManager.logout();
        if (view != null) {
            view.dispose();
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                Class<?> loginCls = Class.forName("lakbaylokal.authentication.LoginFrame");
                javax.swing.JFrame frame = (javax.swing.JFrame) loginCls.getDeclaredConstructor().newInstance();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
