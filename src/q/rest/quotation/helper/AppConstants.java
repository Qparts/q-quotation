package q.rest.quotation.helper;

public class AppConstants {

    private static final String USER_SERVICE = SysProps.getValue("userService");
    private static final String IMAGE_SERVICE = "http://localhost:8081/q-images/rest/internal/api/v2/";
    private static final String CUSTOMER_SERVICE = SysProps.getValue("customerService");
    private final static String PUBLIC_PRODUCT_SERVICE= SysProps.getValue("productPublicService");

    public static final String CUSTOMER_MATCH_TOKEN = CUSTOMER_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN_WS = USER_SERVICE + "match-token/ws";
    public static final String POST_QUOTATION_VIN_IMAGE = IMAGE_SERVICE + "quotation-vin";
    public static final String POST_QUOTATION_ITEM_IMAGE = IMAGE_SERVICE + "quotation-item";


    public static final String POST_QUOTING_SCORE = USER_SERVICE + "quoting-score";

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
