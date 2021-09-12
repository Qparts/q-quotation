package q.rest.quotation.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import q.rest.quotation.model.contract.QuotationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table(name="qut_company_quotation")
@Entity
public class CompanyQuotationForMigration implements Serializable {
    @Id
    private long id;
    private int companyId;
    private int subscriberId;
    private int targetCompanyId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private char status;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="quotation_id")
    @OrderBy("created")
    private Set<CompanyQuotationItemForMigration> quotationItems = new HashSet<>();


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

    public Set<CompanyQuotationItemForMigration> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(Set<CompanyQuotationItemForMigration> quotationItems) {
        this.quotationItems = quotationItems;
    }
}
