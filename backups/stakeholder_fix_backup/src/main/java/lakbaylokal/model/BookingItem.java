package lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class BookingItem {
    private int itemId;
    private int bookingId;
    private int serviceId;
    private int stakeholderId;
    private Timestamp scheduledTime;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private String status;

    private String serviceName;
    private String businessName;
    private String stakeholderName;

    public BookingItem() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getStakeholderId() { return stakeholderId; }
    public void setStakeholderId(int stakeholderId) { this.stakeholderId = stakeholderId; }

    public Timestamp getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Timestamp scheduledTime) { this.scheduledTime = scheduledTime; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getStakeholderName() { return stakeholderName; }
    public void setStakeholderName(String stakeholderName) { this.stakeholderName = stakeholderName; }
}
