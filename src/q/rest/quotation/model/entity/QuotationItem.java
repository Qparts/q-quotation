package q.rest.quotation.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import q.rest.quotation.model.contract.PublicQuotationItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_quotation_item")
@Entity
public class QuotationItem implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_quotation_item_id_seq_gen", sequenceName = "qut_quotation_item_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_quotation_item_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="name")
    private String name;

    @Column(name="quantity")
    private int quantity;

    @Column(name="image_attached")
    private boolean imageAttached;


    @JsonIgnore
    @Transient
    public PublicQuotationItem getContract(){
        PublicQuotationItem pqi = new PublicQuotationItem();
        pqi.setName(this.name);
        pqi.setQuantity(this.quantity);
        pqi.setId(this.id);
        return pqi;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isImageAttached() {
        return imageAttached;
    }

    public void setImageAttached(boolean imageAttached) {
        this.imageAttached = imageAttached;
    }
}
