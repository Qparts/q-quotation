package q.rest.quotation.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_assignment")
@Entity
public class Assignment implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_assignment_id_seq_gen", sequenceName = "qut_assignment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_assignment_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="status")
    private char status;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="completed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="assignee")
    private int assignee;

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

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getAssignee() {
        return assignee;
    }

    public void setAssignee(int assignee) {
        this.assignee = assignee;
    }
}
