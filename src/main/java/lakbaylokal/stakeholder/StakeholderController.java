package lakbaylokal.stakeholder;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.service.StakeholderService;

public class StakeholderController {
    private final StakeholderService stakeholderService;
    private final BookingService bookingService;
    private StakeholderDashboard view;

    public StakeholderController() {
        this.stakeholderService = new StakeholderService();
        this.bookingService = new BookingService();
    }

    public void setView(StakeholderDashboard view) {
        this.view = view;
    }

    public StakeholderProfile getCurrentStakeholder() {
        User user = SessionManager.getCurrentUser();
        int userId = (user != null) ? user.getUserId() : 2;
        return stakeholderService.getStakeholderByUserId(userId);
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
