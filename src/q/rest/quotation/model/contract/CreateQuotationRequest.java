package q.rest.quotation.model.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class CreateQuotationRequest {


    private Long customerVehicleId;
    private long customerId;
    private int cityId;
    private int makeId;
    private String mobile;
    private Integer vehicleYearId;
    private String vin;
    private Boolean imageAttached;
    private List<CreateQuotationItemRequest> quotationItems;
    private Integer appCode;
    private Character paymentMethood;
    private CardHolder cardHolder;

    @JsonIgnore
    private Boolean customerVehicleNewlyCreated;

    public Boolean getCustomerVehicleNewlyCreated() {
        return customerVehicleNewlyCreated;
    }

    public void setCustomerVehicleNewlyCreated(Boolean customerVehicleNewlyCreated) {
        this.customerVehicleNewlyCreated = customerVehicleNewlyCreated;
    }

    public Integer getVehicleYearId() {
        return vehicleYearId;
    }

    public void setVehicleYearId(Integer vehicleYearId) {
        this.vehicleYearId = vehicleYearId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Boolean getImageAttached() {
        return imageAttached;
    }

    public void setImageAttached(Boolean imageAttached) {
        this.imageAttached = imageAttached;
    }



    public List<CreateQuotationItemRequest> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(List<CreateQuotationItemRequest> quotationItems) {
        this.quotationItems = quotationItems;
    }

    public Long getCustomerVehicleId() {
        return customerVehicleId;
    }

    public void setCustomerVehicleId(Long customerVehicleId) {
        this.customerVehicleId = customerVehicleId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getAppCode() {
        return appCode;
    }

    public void setAppCode(Integer appCode) {
        this.appCode = appCode;
    }

    public Character getPaymentMethood() {
        return paymentMethood;
    }

    public void setPaymentMethood(Character paymentMethood) {
        this.paymentMethood = paymentMethood;
    }

    public CardHolder getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(CardHolder cardHolder) {
        this.cardHolder = cardHolder;
    }
}
