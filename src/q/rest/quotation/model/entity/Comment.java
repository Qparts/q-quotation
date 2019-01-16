package q.rest.quotation.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import q.rest.quotation.model.contract.PublicComment;
import q.rest.quotation.model.contract.PublicQuotationItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="qut_comment")
@Entity
public class Comment implements Serializable {

    @Id
    @SequenceGenerator(name = "qut_comment_id_seq_gen", sequenceName = "qut_comment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qut_comment_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="status")
    private char status;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="comment_text")
    private String text;

    @Column(name="visible_to_customer")
    private boolean visibleToCustomer;


    @Transient
    @JsonIgnore
    public PublicComment getContract(){
        PublicComment comment = new PublicComment();
        comment.setCreated(this.created);
        comment.setId(this.id);
        comment.setText(this.text);
        return comment;
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

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVisibleToCustomer() {
        return visibleToCustomer;
    }

    public void setVisibleToCustomer(boolean visibleToCustomer) {
        this.visibleToCustomer = visibleToCustomer;
    }
}
