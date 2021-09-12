package q.rest.quotation.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import q.rest.quotation.model.contract.QuotationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_company_quotation_item")
@Entity
public class CompanyQuotationItemForMigration implements Serializable {
    @Id
    private long id;
    @Column(name="quotation_id")
    private long searchListId;
    @JsonProperty("productNumber")
    private String itemNumber;
    private String brand;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private char status;
    private double retailPrice;
    private boolean specialOffer;
    private double specialOfferPrice;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSearchListId() {
        return searchListId;
    }

    public void setSearchListId(long searchListId) {
        this.searchListId = searchListId;
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

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
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
}
