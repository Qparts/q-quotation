package q.rest.quotation.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import q.rest.quotation.model.contract.PublicQuotation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Table(name="qut_quotation")
@Entity
public class Quotation implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_quotation_id_seq_gen", sequenceName = "qut_quotation_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_quotation_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="status")
    private char status;

    @Column(name="app_code")
    private int appCode;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="city_id")
    private int cityId;

    @Column(name="make_id")
    private int makeId;

    @Column(name="vin_image")
    private boolean vinImageAttached;

    @Column(name="customer_vehicle_id")
    private Long customerVehicleId;

    @Transient
    private List<QuotationItem> quotationItems;
    @Transient
    private Assignment activeAssignment;
    @Transient
    private List<Comment> comments;
    @Transient
    private List<Bill> bills;

    @JsonIgnore
    @Transient
    public PublicQuotation getContract(){
        PublicQuotation pq = new PublicQuotation();
        pq.setCreated(this.getCreated());
        pq.setCustomerId(this.getCustomerId());
        pq.setCustomerVehicleId(this.getCustomerVehicleId());
        return pq;
    }


    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<QuotationItem> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(List<QuotationItem> quotationItems) {
        this.quotationItems = quotationItems;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public int getAppCode() {
        return appCode;
    }

    public void setAppCode(int appCode) {
        this.appCode = appCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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

    public boolean isVinImageAttached() {
        return vinImageAttached;
    }

    public void setVinImageAttached(boolean vinImageAttached) {
        this.vinImageAttached = vinImageAttached;
    }

    public Long getCustomerVehicleId() {
        return customerVehicleId;
    }

    public void setCustomerVehicleId(Long customerVehicleId) {
        this.customerVehicleId = customerVehicleId;
    }

    public Assignment getActiveAssignment() {
        return activeAssignment;
    }

    public void setActiveAssignment(Assignment activeAssignment) {
        this.activeAssignment = activeAssignment;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }
}
