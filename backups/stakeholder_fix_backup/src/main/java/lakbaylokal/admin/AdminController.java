package lakbaylokal.admin;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.barangay.BarangayDashboard;
import lakbaylokal.lgu.LGUDashboard;
import lakbaylokal.stakeholder.StakeholderDashboard;
import lakbaylokal.tourist.TouristDashboard;
import lakbaylokal.tourismoffice.TourismOfficeDashboard;

import javax.swing.*;

public class AdminController {
    private AdminDashboard view;

    public void setView(AdminDashboard view) {
        this.view = view;
    }

    public void launchTouristView() {
        SwingUtilities.invokeLater(() -> {
            TouristDashboard frame = new TouristDashboard();
            frame.setTitle("Lakbay Lokal — Tourist View (Opened by Administrator)");
            frame.setVisible(true);
        });
    }

    public void launchStakeholderView() {
        SwingUtilities.invokeLater(() -> {
            StakeholderDashboard frame = new StakeholderDashboard();
            frame.setTitle("Lakbay Lokal — Stakeholder View (Opened by Administrator)");
            frame.setVisible(true);
        });
    }

    public void launchBarangayView() {
        SwingUtilities.invokeLater(() -> {
            BarangayDashboard frame = new BarangayDashboard();
            frame.setTitle("Lakbay Lokal — Barangay View (Opened by Administrator)");
            frame.setVisible(true);
        });
    }

    public void launchLGUView() {
        SwingUtilities.invokeLater(() -> {
            LGUDashboard frame = new LGUDashboard();
            frame.setTitle("Lakbay Lokal — LGU View (Opened by Administrator)");
            frame.setVisible(true);
        });
    }

    public void launchTourismOfficeView() {
        SwingUtilities.invokeLater(() -> {
            TourismOfficeDashboard frame = new TourismOfficeDashboard();
            frame.setTitle("Lakbay Lokal — Tourism Office View (Opened by Administrator)");
            frame.setVisible(true);
        });
    }

    public void handleLogout() {
        SessionManager.logout();
        if (view != null) {
            view.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> loginCls = Class.forName("lakbaylokal.authentication.LoginFrame");
                JFrame frame = (JFrame) loginCls.getDeclaredConstructor().newInstance();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
