package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Table(name="qut_vendor_quotation")
@Entity
public class VendorQuotation implements Serializable {
    @Id
    @SequenceGenerator(name = "qut_vendor_quotation_id_seq_gen", sequenceName = "qut_vendor_quotation_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_vendor_quotation_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="vendor_id")
    private int vendorId;

    @Column(name="created_by_vendor_user")
    private int createdByVendorUser;

    @Column(name="target_vendor_id")
    private int targetVendorId;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="status")
    private char status;

    @Transient
    private List<VendorQuotationItem> vendorQuotationItems;

    public List<VendorQuotationItem> getVendorQuotationItems() {
        return vendorQuotationItems;
    }

    public void setVendorQuotationItems(List<VendorQuotationItem> vendorQuotationItems) {
        this.vendorQuotationItems = vendorQuotationItems;
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

    public int getCreatedByVendorUser() {
        return createdByVendorUser;
    }

    public void setCreatedByVendorUser(int createdByVendorUser) {
        this.createdByVendorUser = createdByVendorUser;
    }

    public int getTargetVendorId() {
        return targetVendorId;
    }

    public void setTargetVendorId(int targetVendorId) {
        this.targetVendorId = targetVendorId;
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
