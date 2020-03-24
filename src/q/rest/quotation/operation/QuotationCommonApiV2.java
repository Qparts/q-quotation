package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.contract.CreateQuotationItemRequest;
import q.rest.quotation.model.contract.CreateQuotationRequest;
import q.rest.quotation.model.entity.Quotation;
import q.rest.quotation.model.entity.QuotationItem;
import q.rest.quotation.model.entity.WebApp;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

@Stateless
public class QuotationCommonApiV2 {


    @EJB
    private DAO dao;


    public Quotation createQuotation(CreateQuotationRequest qr, WebApp wa, String header){
        Quotation quotation = new Quotation();
        quotation.setAppCode(wa.getAppCode());
        quotation.setCityId(qr.getCityId());
        quotation.setCreated(new Date());
        quotation.setCreatedBy(0);
        quotation.setMobile(qr.getMobile());
        quotation.setCustomerId(qr.getCustomerId());
        qr.setCustomerVehicleNewlyCreated(false);
        if(qr.getCustomerVehicleId() == null){
            Map<String,Object> map = new HashMap<>();
            if(qr.getVin() == null){
                qr.setVin("");
            }
            map.put("vehicleYearId", qr.getVehicleYearId());
            map.put("vin", qr.getVin());
            map.put("imageAttached", qr.getImageAttached());
            map.put("customerId", qr.getCustomerId());
            System.out.println("=======================");
            System.out.println("vehicle year id = " +qr.getVehicleYearId());
            System.out.println("vin = " +qr.getVin());
            System.out.println("image attached = " +qr.getImageAttached());
            System.out.println("customer Id = " +qr.getCustomerId());
            Response r = postSecuredRequest(AppConstants.POST_CUSTOMER_VEHICLE_IF_AVAILABLE, map , header);
            System.out.println(r.getStatus());
            System.out.println("=======================");
            if(r.getStatus() == 200){
                Long customerVehicleId = r.readEntity(Long.class);
                qr.setCustomerVehicleId(customerVehicleId);
                qr.setCustomerVehicleNewlyCreated(true);
            }
            else if(r.getStatus() == 409){
                Long customerVehicleId = r.readEntity(Long.class);
                qr.setCustomerVehicleId(customerVehicleId);
                qr.setCustomerVehicleNewlyCreated(false);
            }
        }
        quotation.setMobile(qr.getMobile());
        quotation.setCustomerVehicleId(qr.getCustomerVehicleId());
        quotation.setMakeId(qr.getMakeId());
        if(wa.getAppCode() == 3){
            if(qr.getPaymentMethod() != null){
                if(qr.getPaymentMethod() == 'W'){
                    quotation.setStatus('T');
                }
                else if (qr.getPaymentMethod() == 'V' || qr.getPaymentMethod() == 'M'){
                    quotation.setStatus('I');
                }
                if(qr.getPaymentMethod() == 'F'){
                    quotation.setStatus('N');
                }
            }
        }
        else {
            quotation.setStatus('N');
        }
        quotation.setVinImageAttached(false);
        dao.persist(quotation);
        return quotation;
    }

    //for moyasssar
    public Map<String,Object> createQuotationPaymentObject(Quotation quotation, CreateQuotationRequest qr){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("customerId", qr.getCustomerId());
        map.put("quotationId", quotation.getId());
        map.put("paymentMethod", qr.getPaymentMethod());
        map.put("amount", 15);
        map.put("cardHolder", qr.getCardHolder());
        return map;
    }


    public void createQuotationItems(Quotation quotation, List<CreateQuotationItemRequest> qir) {
        quotation.setQuotationItems(new ArrayList<>());
        for (CreateQuotationItemRequest qritem : qir) {
            QuotationItem quotationItem = new QuotationItem();
            quotationItem.setQuotationId(quotation.getId());
            quotationItem.setName(qritem.getItemName());
            quotationItem.setQuantity(qritem.getQuantity());
            quotationItem.setImageAttached(qritem.isHasImage());
            dao.persist(quotationItem);
            quotation.getQuotationItems().add(quotationItem);
            qritem.setItemName(quotationItem.getId() + ".png");
        }
    }



    // check idempotency of a cart
    public boolean isQuotationRedudant(long customerId, Date created) {
        // if a cart was created less than n seconds ago, then do not do
        String jpql = "select b from Quotation b where b.customerId = :value0 and b.created between :value1 and :value2";
        Date previous = Helper.addSeconds(created, -10);
        List<Quotation> carts = dao.getJPQLParams(Quotation.class, jpql, customerId, previous, created);
        return carts.size() > 0;
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));
        return r;
    }


    public Response getSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.get();
        return r;
    }

    public Response deleteSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.delete();
        return r;
    }

}
