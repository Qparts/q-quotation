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
public class CompanyQuotation implements Serializable {
    @Id
    @SequenceGenerator(name = "qut_company_quotation_id_seq_gen", sequenceName = "qut_company_quotation_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_company_quotation_id_seq_gen")
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
    private Set<CompanyQuotationItem> quotationItems = new HashSet<>();


    public CompanyQuotation() {

    }

    public CompanyQuotation(QuotationModel model, PricePolicy pp) {
        this.status = 'N';
        this.companyId = model.getCompanyId();
        this.created = new Date();
        this.targetCompanyId = model.getTargetCompanyId();
        this.subscriberId = model.getSubscriberId();
        CompanyQuotationItem cqi = new CompanyQuotationItem(model, pp);
        this.quotationItems.add(cqi);

    }

    @JsonIgnore
    public CompanyQuotationItem getItemFromModel(QuotationModel model){
        for(var item : quotationItems){
            if(model.getBrand().equals(item.getBrand()) &&
            model.getItemNumber().equals(item.getItemNumber())){
                return item;
            }
        }
        return null;
    }


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

    public Set<CompanyQuotationItem> getQuotationItems() {
        return quotationItems;
    }

    public void setQuotationItems(Set<CompanyQuotationItem> quotationItems) {
        this.quotationItems = quotationItems;
    }
}
