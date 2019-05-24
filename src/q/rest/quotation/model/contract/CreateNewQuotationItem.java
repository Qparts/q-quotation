package q.rest.quotation.model.contract;

import q.rest.quotation.model.entity.QuotationItem;

import java.io.Serializable;

public class CreateNewQuotationItem implements Serializable {
    private QuotationItem quotationItem;
    private int createdBy;

    public QuotationItem getQuotationItem() {
        return quotationItem;
    }

    public void setQuotationItem(QuotationItem quotationItem) {
        this.quotationItem = quotationItem;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
