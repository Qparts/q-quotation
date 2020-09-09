package q.rest.quotation.helper;

public class AppConstants {

    private final static String SUBSCRIBER_SERVICE = SysProps.getValue("subscriberService");
    public final static String INTERNAL_APP_SECRET = "INTERNAL_APP";
    public final static String POST_PURCHASE_ORDER_NOTIFICATION = SUBSCRIBER_SERVICE +  "send-purchase-order";
    public final static String POST_UPDATE_PURCHASE_ORDER_NOTIFICATION = SUBSCRIBER_SERVICE +  "update-purchase-order";

}
