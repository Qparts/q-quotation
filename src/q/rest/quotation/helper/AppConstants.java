package q.rest.quotation.helper;

public class AppConstants {

    private static final String USER_SERVICE = SysProps.getValue("userService");
    private static final String CUSTOMER_SERVICE = SysProps.getValue("customerService");
    private static final String PUBLIC_CUSTOMER_SERVICE = SysProps.getValue("customerPublicService");
    private static final String PUBLIC_PRODUCT_SERVICE= SysProps.getValue("productPublicService");
    private static final String PUBLIC_CART_SERVICE = SysProps.getValue("cartPublicService");
    private static final String VENDOR_SERVICE = SysProps.getValue("vendorService");
    private static final String INVOICE_SERVICE = SysProps.getValue("invoiceService");

    public static final String CUSTOMER_MATCH_TOKEN = CUSTOMER_SERVICE + "match-token";
    public static final String CUSTOMER_MATCH_TOKEN_WS = CUSTOMER_SERVICE + "match-token/ws";
    public static final String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
    public static final String VENDOR_MATCH_TOKEN = VENDOR_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN_WS = USER_SERVICE + "match-token/ws";


    public static final String POST_QUOTING_SCORE = USER_SERVICE + "quoting-score";
    public static final String POST_CUSTOMER_VEHICLE_IF_AVAILABLE = PUBLIC_CUSTOMER_SERVICE + "vehicle-if-available";
    public static final String POST_QUOTATION_COMPLETTION_EMAIL = CUSTOMER_SERVICE + "quotation-ready";
    public static final String POST_QUOTATION_SUBMITTED_EMAIL = PUBLIC_CUSTOMER_SERVICE + "email/quotation-submitted";
    public static final String POST_QUOTATION_PAYMENT_CC = PUBLIC_CART_SERVICE + "quotation/credit-card";
    public static final String POST_QUOTATION_PAYMENT = INVOICE_SERVICE + "payment-order";
    public static final String POST_QUOTATION_PAYMENT_WIRE = PUBLIC_CART_SERVICE + "quotation/wire-transfer";

    public static String getCustomer(long customerId) {
        return CUSTOMER_SERVICE + "customer/"+ customerId;
    }

    public static String getPublicProduct(long id){
        return PUBLIC_PRODUCT_SERVICE + "product/" + id;
    }

    public static String getQuotingMakeIds(int userId) {
        return USER_SERVICE + "quoting-make-ids/user/" + userId;
    }

}
