package q.rest.quotation.model.contract;

import java.util.Map;

public class PublicQuotationItem {

    private long id;
    private int quantity;
    private String name;
    private Map products;

    public Map getProducts() {
        return products;
    }

    public void setProducts(Map products) {
        this.products = products;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
