package q.rest.quotation.model.contract;

public class QuotationsSummary {
    private int quotationsSubmitted;
    private int quotationsReceived;
    private int posSubmitted;
    private int posReceived;


    public int getQuotationsSubmitted() {
        return quotationsSubmitted;
    }

    public void setQuotationsSubmitted(int quotationsSubmitted) {
        this.quotationsSubmitted = quotationsSubmitted;
    }

    public int getQuotationsReceived() {
        return quotationsReceived;
    }

    public void setQuotationsReceived(int quotationsReceived) {
        this.quotationsReceived = quotationsReceived;
    }

    public int getPosSubmitted() {
        return posSubmitted;
    }

    public void setPosSubmitted(int posSubmitted) {
        this.posSubmitted = posSubmitted;
    }

    public int getPosReceived() {
        return posReceived;
    }

    public void setPosReceived(int posRecived) {
        this.posReceived = posRecived;
    }
}
