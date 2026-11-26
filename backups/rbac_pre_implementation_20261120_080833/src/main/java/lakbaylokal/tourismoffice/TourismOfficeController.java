package lakbaylokal.tourismoffice;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.service.AnnouncementService;
import lakbaylokal.service.DestinationService;
import lakbaylokal.service.ReportService;

public class TourismOfficeController {
    private final DestinationService destinationService;
    private final AnnouncementService announcementService;
    private final ReportService reportService;
    private TourismOfficeDashboard view;

    public TourismOfficeController() {
        this.destinationService = new DestinationService();
        this.announcementService = new AnnouncementService();
        this.reportService = new ReportService();
    }

    public void setView(TourismOfficeDashboard view) {
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
