package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_bill_item_response")
@Entity
public class BillItemResponse implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_bill_item_response_id_seq_gen", sequenceName = "qut_bill_item_response_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_bill_item_response_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="bill_id")
    private long billId;

    @Column(name="bill_item_id")
    private long billItemId;

    @Column(name="product_id")
    private long productId;

    @Column(name="item_desc")
    private String itemDesc;

    @Column(name="quantity")
    private int quantity;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="status")
    private char status;


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

    public long getBillId() {
        return billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public long getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(long billItemId) {
        this.billItemId = billItemId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}
