package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table(name="qut_price_policy")
@Entity
public class PricePolicy implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_price_policy_id_seq_gen", sequenceName = "qut_price_policy_id_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "qut_price_policy_id_seq_gen")
    private int id;
    private int companyId;
    private String policyName;
    private double factor;
    private int createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="policy_id")
    @OrderBy("created")
    private Set<CompanyPricePolicy> companyPolicies = new HashSet<>();



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }


    public Set<CompanyPricePolicy> getCompanyPolicies() {
        return companyPolicies;
    }

    public void setCompanyPolicies(Set<CompanyPricePolicy> companyPolicies) {
        this.companyPolicies = companyPolicies;
    }
}
