package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="qut_company_price_policy")
public class CompanyPricePolicy implements Serializable {
	@Id
	@SequenceGenerator(name = "qut_company_price_policy_seq_gen", sequenceName = "qut_company_price_policy_id_seq", initialValue=1000, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_company_price_policy_id_seq_gen")
	private int id;
	private int companyId;
	private int targetCompanyId;
	@Column(name="policy_id")
	private int policyId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	private int createdBy;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getTargetCompanyId() {
		return targetCompanyId;
	}

	public void setTargetCompanyId(int targetCompanyId) {
		this.targetCompanyId = targetCompanyId;
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

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
