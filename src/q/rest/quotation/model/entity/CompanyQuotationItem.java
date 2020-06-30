package q.rest.quotation.model.entity;

import q.rest.quotation.model.contract.QuotationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_company_quotation_item")
@Entity
public class CompanyQuotationItem implements Serializable {
    @Id
    @SequenceGenerator(name = "qut_company_quotation_item_id_seq_gen", sequenceName = "qut_company_quotation_item_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_company_quotation_item_id_seq_gen")
    private long id;
    @Column(name="quotation_id")
    private long quotationId;
    private String itemNumber;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private char status;
    private String brand;
    private double retailPrice;
    private double wholesalesPrice;
    private double factor;
    private int policyId;
    private boolean specialOffer;
    private double specialOfferPrice;

    public CompanyQuotationItem() {
    }

    public CompanyQuotationItem(QuotationModel model, PricePolicy pp) {
        this.status = 'N';
        this.created = new Date();
        this.brand = model.getBrand();
        this.itemNumber = model.getItemNumber();
        this.retailPrice = model.getRetailPrice();
        this.wholesalesPrice = model.getWholesalesPrice();
        if(model.isSpecialOffer()){
            this.specialOffer = true;
            this.specialOfferPrice = model.getSpecialOfferPrice();

        }
        else if(pp != null){
            this.policyId = pp.getId();
            this.factor = pp.getFactor();
        }
    }

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

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
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
