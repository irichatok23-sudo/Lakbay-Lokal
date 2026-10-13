package lakbaylokal.lgu;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.service.ReportService;

public class LGUController {
    private final ReportService reportService;
    private LGUDashboard view;

    public LGUController() {
        this.reportService = new ReportService();
    }

    public void setView(LGUDashboard view) {
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
