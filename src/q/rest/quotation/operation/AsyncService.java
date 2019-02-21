package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.model.contract.CreateQuotationItemRequest;
import q.rest.quotation.model.contract.CreateQuotationRequest;
import q.rest.quotation.model.entity.*;
import q.rest.quotation.operation.sockets.QuotationsEndPoint;
import q.rest.quotation.operation.sockets.QuotingEndpoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Quota;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;


@Stateless
public class AsyncService {

    @EJB
    private DAO dao;


    @Asynchronous
    public void completeQuotationCreation(Quotation quotation, CreateQuotationRequest qr, String header) {
        createBill(quotation);
        broadcastToQuotations("new quotation," + quotation.getId());
        sendQuotationCreationEmail(quotation.getId());
        sendQuotationCreateionSms(quotation.getId());
    }


    @Asynchronous
    public void createBill(Quotation quotation) {
        Bill bill = new Bill();
        bill.setQuotationId(quotation.getId());
        bill.setCreated(new Date());
        bill.setCreatedBy(quotation.getCreatedBy());
        bill.setStatus('W');
        dao.persist(bill);
        for (QuotationItem qitem : quotation.getQuotationItems()) {
            BillItem bi = new BillItem();
            bi.setQuotationId(quotation.getId());
            bi.setCreated(bill.getCreated());
            bi.setBillId(bill.getId());
            bi.setCreatedBy(bill.getCreatedBy());
            bi.setItemDesc(qitem.getName());
            bi.setQuantity(qitem.getQuantity());
            bi.setStatus('W');
            dao.persist(bi);
        }
        quotation.setStatus('W');
        dao.update(quotation);
    }

    @Asynchronous
    public void createFinderScore(BillItemResponse qir, String desc, String stage, String authHeader, int score) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("quotationId", qir.getQuotationId());
        map.put("billResponseId", qir.getId());
        map.put("stage", stage);
        map.put("createdBy", qir.getCreatedBy());
        map.put("score", score);
        map.put("desc", desc);
        postSecuredRequest(AppConstants.POST_QUOTING_SCORE, map, authHeader);
    }

    @Asynchronous
    public void sendQuotationCreationEmail(long quotationId) {
        //   QuotationsEndpoint.broadcast(message);
    }

    @Asynchronous
    public void sendQuotationCompletionEmail(String authHeader, Quotation quotation){
        //send some email
    }

    @Asynchronous
    public void sendQuotationCompletionSms(String authHeader, Quotation quotation){
        //send some email
    }

    @Asynchronous
    public void sendQuotationCreateionSms(long quotationId) {
        //   QuotationsEndpoint.broadcast(message);
    }

    @Asynchronous
    public void broadcastToQuotations(String message) {
        QuotationsEndPoint.broadcast(message);
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;

    }



    @Asynchronous
    public void sendToQuotingUser(String message, int userId) {
        QuotingEndpoint.sendToUser(message, userId);
    }


}
