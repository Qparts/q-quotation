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
        createCartItems(header, quotation, qr);
        createBill(quotation);
        writeQuotationVinImage(header, qr.getVinImage(), quotation.getId(), quotation.getCreated());
        broadcastToQuotations("new quotation," + quotation.getId());
        sendQuotationCreationEmail(quotation.getId());
        sendQuotationCreateionSms(quotation.getId());
    }


    private void createCartItems(String header, Quotation quotation, CreateQuotationRequest qr) {
        quotation.setQuotationItems(new ArrayList<>());
        for (CreateQuotationItemRequest qritem : qr.getQuotationItems()) {
            QuotationItem quotationItem = new QuotationItem();
            quotationItem.setQuotationId(quotation.getId());
            quotationItem.setName(qritem.getItemName());
            quotationItem.setQuantity(qritem.getQuantity());
            quotationItem.setImageAttached(qritem.getImage().length() > 0);
            dao.persist(quotationItem);
            quotation.getQuotationItems().add(quotationItem);
            writeQuotationItemImage(header, qritem.getImage(), quotation.getId(), quotationItem.getId(), quotation.getCreated());
        }
    }


    private void createBill(Quotation quotation) {
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
    private void writeQuotationItemImage(String header, String imageString, Long quotationId, Long id, Date created) {
        if (imageString != null && imageString.length() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("quotationId", quotationId);
            map.put("imageString", imageString);
            map.put("id", id);
            map.put("created", created.getTime());
            Response r = postSecuredRequest(AppConstants.POST_QUOTATION_ITEM_IMAGE, map, header);
            System.out.println("item image response "+r.getStatus());
        }
    }



    @Asynchronous
    private void writeQuotationVinImage(String header, String imageString, Long id, Date created) {
        if (imageString != null && imageString.length() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("imageString", imageString);
            map.put("id", id);
            map.put("created", created.getTime());
            Response r= postSecuredRequest(AppConstants.POST_QUOTATION_VIN_IMAGE, map, header);
            System.out.println("Vin image response " + r.getStatus());
        }
    }

    @Asynchronous
    public void sendQuotationCreationEmail(long quotationId) {
        //   QuotationsEndpoint.broadcast(message);
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
