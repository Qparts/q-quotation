package q.rest.quotation.model.contract;


import java.io.Serializable;

public class CardHolder implements Serializable {

    private Integer ccMonth;
    private Integer ccYear;
    private String ccName;
    private String ccNumber;
    private String ccCvc;

    public Integer getCcMonth() {
        return ccMonth;
    }

    public void setCcMonth(Integer ccMonth) {
        this.ccMonth = ccMonth;
    }

    public Integer getCcYear() {
        return ccYear;
    }

    public void setCcYear(Integer ccYear) {
        this.ccYear = ccYear;
    }

    public String getCcName() {
        return ccName;
    }

    public void setCcName(String ccName) {
        this.ccName = ccName;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public void setCcNumber(String ccNumber) {
        this.ccNumber = ccNumber;
    }

    public String getCcCvc() {
        return ccCvc;
    }

    public void setCcCvc(String ccCvc) {
        this.ccCvc = ccCvc;
    }
}
