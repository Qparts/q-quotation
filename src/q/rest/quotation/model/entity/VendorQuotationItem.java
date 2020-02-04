package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_vendor_quotation_item")
@Entity
public class VendorQuotationItem implements Serializable {
    @Id
    @SequenceGenerator(name = "qut_vendor_quotation_item_id_seq_gen", sequenceName = "qut_vendor_quotation_item_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_vendor_quotation_item_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="vendor_id")
    private int vendorId;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="item_number")
    private String itemNumber;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="status")
    private char status;

    @Column(name="brand")
    private String brand;

    @Column(name="retail_price")
    private double retailPrice;

    @Column(name="wholesales_price")
    private double wholesalesPrice;

    @Column(name="factor")
    private double factor;

    @Column(name="policy_name")
    private String policyName;

    @Column(name="special_offer")
    private boolean specialOffer;

    @Column(name="special_offer_price")
    private double specialOfferPrice;

    public boolean isSpecialOffer() {
        return specialOffer;
    }

    public void setSpecialOffer(boolean specialOffer) {
        this.specialOffer = specialOffer;
    }

    public double getSpecialOfferPrice() {
        return specialOfferPrice;
    }

    public void setSpecialOfferPrice(double specialOfferPrice) {
        this.specialOfferPrice = specialOfferPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(long quotationId) {
        this.quotationId = quotationId;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public double getWholesalesPrice() {
        return wholesalesPrice;
    }

    public void setWholesalesPrice(double wholesalesPrice) {
        this.wholesalesPrice = wholesalesPrice;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}
