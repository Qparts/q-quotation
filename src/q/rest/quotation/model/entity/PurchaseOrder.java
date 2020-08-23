package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "qut_purchase_order")
public class PurchaseOrder implements Serializable {
    @Id
    @SequenceGenerator(name = "qut_purchase_order_id_seq_gen", sequenceName = "qut_purchase_order_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_purchase_order_id_seq_gen")
    private long id;
    private int companyId;
    private int subscriberId;
    private int targetCompanyId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private char status;//N = new, R = approved, X = rejected
    private String notes;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="purchase_order_id")
    @OrderBy("created")
    private Set<PurchaseOrderItem> items = new HashSet<>();


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public int getTargetCompanyId() {
        return targetCompanyId;
    }

    public void setTargetCompanyId(int targetCompanyId) {
        this.targetCompanyId = targetCompanyId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String note) {
        this.notes = note;
    }

    public Set<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(Set<PurchaseOrderItem> items) {
        this.items = items;
    }
}
