package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.filter.Secured;
import q.rest.quotation.filter.SecuredCustomer;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.contract.*;
import q.rest.quotation.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QuotationApiV2 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;

    @EJB
    private QuotationCommonApiV2 common;

    @SecuredCustomer
    @PUT
    @Path("quotation/read")
    public Response readQuotation(Map<String,Object> map){
        try{
            Long id = ((Number) map.get("quotationId")).longValue();
            Long customerId = ((Number) map.get("customerId")).longValue();
            Quotation quotation = dao.findTwoConditions(Quotation.class, "id", "customerId", id, customerId);
            quotation.setRead(true);
            quotation.setReadOn(new Date());
            dao.update(quotation);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredCustomer
    @PUT
    @Path("quotation/payment")
    public Response updateQuotationAfterPayment(@HeaderParam("Authorization") String header, Map<String,Object> map){
        try{
            Long quotationId = ((Number) map.get("quotationId")).longValue();
            String paymentStatus = (String) map.get("paymentStatus");
            Quotation quotation = dao.find(Quotation.class, quotationId);
            if(paymentStatus.equals("failed")){
                quotation.setStatus('F');
            }
            else{
                quotation.setStatus('W');
            }
            dao.update(quotation);
            async.notifyCustomerOfQuotationCreation(header, quotation);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @Secured
    @POST
    @Path("quotation/wire-transfer")
    public Response createWireTransferQuotation(@HeaderParam("Authorization") String header, CreateQuotationRequest qr){
        try{
            if (common.isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }
            WebApp wa;
            if (qr.getAppCode() == null) {
                wa = this.getWebAppFromAuthHeader(header);
            }
            else{
                wa = dao.find(WebApp.class, qr.getAppCode());
            }
            Quotation quotation = common.createQuotation(qr, wa, header);
            common.createQuotationItems(quotation, qr.getQuotationItems());
            async.createBill(quotation);
            Map<String,Object> map = common.createQuotationPaymentObject(quotation, qr);
            Response r = common.postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_WIRE, map, header);
            if(r.getStatus() != 201){
                throw new Exception();
            }

            CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
            return Response.status(200).entity(res).build();

        }catch (Exception ex){
            return Response.status(500).build();
        }
    }



    @SecuredCustomer
    @POST
    @Path("quotation/credit-card")
    public Response createCreditCardQuotation(@HeaderParam("Authorization") String header, CreateQuotationRequest qr){
        try{
            if (common.isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }
            WebApp wa = this.getWebAppFromAuthHeader(header);
            Quotation quotation = common.createQuotation(qr, wa, header);
            common.createQuotationItems(quotation, qr.getQuotationItems());
            async.createBill(quotation);
            Map<String,Object> map = common.createQuotationPaymentObject(quotation, qr);
            //credit card
            Response r = common.postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_CC, map, header);
            //possible outcomes
            if(r.getStatus() == 400){
                //bad credit card request
                return Response.status(400).entity("bad request from gateway").build();
            }
            if(r.getStatus() == 401){
                String reason = r.readEntity(String.class);
                return Response.status(401).entity(reason).build();
            }
            if(r.getStatus() == 202){
                Map<String, Object> resmap = r.readEntity(Map.class);
                String transactionUrl = (String) resmap.get("transactionUrl");
                CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                res.setTransactionUrl(transactionUrl);
                return Response.status(202).entity(res).build();
            }
            if(r.getStatus() == 200){
                //success
                quotation.setStatus('W');
                dao.update(quotation);
                async.notifyCustomerOfQuotationCreation(header, quotation);
                CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                return Response.status(200).entity(res).build();
            }
            throw new Exception();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }
/*
    @Secured
    @POST
    @Path("quotation")
    public Response createQuotationRequest(@HeaderParam("Authorization") String header, CreateQuotationRequest qr) {
        try {
            if (isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }
            WebApp wa;
            if (qr.getAppCode() == null) {
                wa = this.getWebAppFromAuthHeader(header);
            }
            else{
                wa = dao.find(WebApp.class, qr.getAppCode());
            }
            Quotation quotation = common.createQuotation(qr, wa, header);
            common.createQuotationItems(quotation, qr.getQuotationItems());
            async.createBill(quotation);
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("customerId", qr.getCustomerId());
            map.put("quotationId", quotation.getId());
            map.put("paymentMethod", qr.getPaymentMethood());
            map.put("amount", 15);
            //wire transfer
            if(qr.getPaymentMethood() == 'W'){
                Response r = common.postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_WIRE, map, header);
                if(r.getStatus() == 201){
                    CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                    return Response.status(200).entity(res).build();
                }
                else{
                    throw new Exception();
                }
            }
            //mada
            else if(qr.getPaymentMethood() == 'M' || qr.getPaymentMethood() == 'V'){
                map.put("cardHolder", qr.getCardHolder());
                Response r = common.postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_CC, map, header);
                if(r.getStatus() == 400){
                    //bad credit card request
                    return Response.status(400).entity("bad request from gateway").build();
                }
                if(r.getStatus() == 401){
                    String reason = r.readEntity(String.class);
                    return Response.status(401).entity(reason).build();
                }
                if(r.getStatus() == 202){
                    Map<String, Object> resmap = r.readEntity(Map.class);
                    String transactionUrl = (String) resmap.get("transactionUrl");
                    CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                    res.setTransactionUrl(transactionUrl);
                    return Response.status(202).entity(res).build();
                }
                if(r.getStatus() == 200){
                    //success
                    quotation.setStatus('W');
                    dao.update(quotation);
                }
            }else if(qr.getPaymentMethood() == 'F'){
                quotation.setStatus('W');
                dao.update(quotation);
            }
            async.notifyCustomerOfQuotationCreation(header, quotation);
            CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
            return Response.status(200).entity(res).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    /*
    @Secured
    @POST
    @Path("quotation")
     public Response createQuotationRequest(@HeaderParam("Authorization") String header, CreateQuotationRequest qr) {
        try {
            if (isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }
            WebApp wa;
            if (qr.getAppCode() == null) {
                wa = this.getWebAppFromAuthHeader(header);
            }
            else{
                wa = dao.find(WebApp.class, qr.getAppCode());
            }
            Quotation quotation = createQuotation(qr, wa, header);
            createQuotationItems(quotation, qr.getQuotationItems());
            async.createBill(quotation);
            if(wa.getAppCode() == 3){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put("customerId", qr.getCustomerId());
                map.put("quotationId", quotation.getId());
                map.put("paymentMethod", qr.getPaymentMethood());
                map.put("amount", 15);
                if(qr.getPaymentMethood() == 'W'){
                    Response r = postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_WIRE, map, header);
                    if(r.getStatus() == 201){
                        CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                        return Response.status(200).entity(res).build();
                    }
                    else{
                        throw new Exception();
                    }
                }
                else if(qr.getPaymentMethood() == 'M' || qr.getPaymentMethood() == 'V'){
                    map.put("cardHolder", qr.getCardHolder());
                    Response r = postSecuredRequest(AppConstants.POST_QUOTATION_PAYMENT_CC, map, header);
                    if(r.getStatus() == 400){
                        //bad credit card request
                        return Response.status(400).entity("bad request from gateway").build();
                    }
                    if(r.getStatus() == 401){
                        String reason = r.readEntity(String.class);
                        return Response.status(401).entity(reason).build();
                    }
                    if(r.getStatus() == 202){
                        Map<String, Object> resmap = r.readEntity(Map.class);
                        String transactionUrl = (String) resmap.get("transactionUrl");
                        CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
                        res.setTransactionUrl(transactionUrl);
                        return Response.status(202).entity(res).build();
                    }
                    if(r.getStatus() == 200){
                        //success
                        quotation.setStatus('W');
                        dao.update(quotation);
                    }
                }else if(qr.getPaymentMethood() == 'F'){
                    quotation.setStatus('W');
                    dao.update(quotation);
                }
            }
            else{
                //this is q.parts
                quotation.setStatus('W');
                dao.update(quotation);
            }
            async.notifyCustomerOfQuotationCreation(header, quotation);
            CreateQuotationResponse res = prepareCreateQuotationResponse(quotation, qr);
            return Response.status(200).entity(res).build();
        }catch(Exception ex){
            return getServerErrorResponse();
        }
    }

    */

    private CreateQuotationResponse prepareCreateQuotationResponse(Quotation quotation, CreateQuotationRequest qr){
        CreateQuotationResponse res = new CreateQuotationResponse();
        res.setQuotationId(quotation.getId());
        res.setItems(new ArrayList<>());
        for(CreateQuotationItemRequest req : qr.getQuotationItems()){
            Map<String,Object> map = new HashMap<>();
            map.put("tempId", req.getTempId());
            map.put("imageName" , req.getItemName());
            res.getItems().add(map);
        }
        res.setVehicleImageName(qr.getCustomerVehicleId() + ".png");
        res.setUploadImage(qr.getCustomerVehicleNewlyCreated() && qr.getImageAttached());
        return res;
    }

    private List<PublicQuotation> getPublicQuotations(List<Quotation> quotations){
        List<PublicQuotation> publicQuotations = new ArrayList<>();
        for(Quotation quotation : quotations){
            PublicQuotation publicQuotation = getPublicQuotation(quotation);
            publicQuotations.add(publicQuotation);
        }
        return publicQuotations;
    }


    @SecuredCustomer
    @PUT
    @Path("close-quotation")
    public Response closeQuotation(Map<String,Object> map){
        try{
            long quotaitonId = ((Number) map.get("quotationId")).longValue();

            Quotation q = dao.find(Quotation.class, quotaitonId);
            q.setStatus('C');
            dao.update(q);
            Comment comment = new Comment();
            comment.setCreated(new Date());
            comment.setText("Closed by customer");
            comment.setCreatedBy(0);
            comment.setQuotationId(quotaitonId);
            comment.setStatus('Y');
            comment.setVisibleToCustomer(false);
            dao.persist(comment);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredCustomer
    @GET
    @Path("quotations/customer/{customerId}/pending")
    public Response getCustomerQuotations(@PathParam(value="customerId") long customerId){
        try{
            String sql = "select b from Quotation b where b.customerId= :value0 and b.status in (:value1, :value2, :value3)";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId, 'W', 'R', 'A');
            List<PublicQuotation> publicQuotations = getPublicQuotations(quotations);
            return Response.status(200).entity(publicQuotations).build();

        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }

    private PublicQuotation getPublicQuotation(Quotation quotation){
        PublicQuotation publicQuotation = quotation.getContract();
        //add quotation items
        List<QuotationItem> quotationItems = dao.getCondition(QuotationItem.class, "quotationId", quotation.getId());
        publicQuotation.setQuotationItems(Helper.convertQuotationItemsToContract(quotationItems));
        //add public comments
        List<Comment> comments = dao.getTwoConditions(Comment.class, "quotationId" , "visibleToCustomer" , quotation.getId(), true);
        publicQuotation.setComments(Helper.convertCommentsToContract(comments));
        return publicQuotation;
    }


    private void initProductsToQuotationItems(PublicQuotation publicQuotation, String header){
        String sql = "select b from BillItem b where b.id in (select c.billItemId from BillItemResponse c where c.status = :value0 and c.quotationId = :value1) ";
        List<BillItem> billItems = dao.getJPQLParams(BillItem.class, sql, 'C', publicQuotation.getId());
        publicQuotation.setQuotationItems(new ArrayList<>());
        for(BillItem billItem : billItems){
            PublicQuotationItem pqi = billItem.getContract();
            sql = "select b from BillItemResponse b where b.billItemId = :value0 and b.status = :value1";
            List<BillItemResponse> billItemResponses = dao.getJPQLParams(BillItemResponse.class, sql, billItem.getId(), 'C');

            for(BillItemResponse bir : billItemResponses){
                Response r = common.getSecuredRequest(AppConstants.getPublicProduct(bir.getProductId()), header);
                if(r.getStatus() == 200){
                    Map map = r.readEntity(Map.class);
                    pqi.setProducts(map);
                }
            }

            publicQuotation.getQuotationItems().add(pqi);
        }
    }



    @SecuredCustomer
    @GET
    @Path("quotation/{quotationId}")
    public Response getQuotation(@HeaderParam("Authorization") String header, @PathParam(value = "quotationId") long quotationId){
        try{
            long customerId = getCustomerIdFromHeader(header);
            Quotation quotation = dao.findTwoConditions(Quotation.class, "id", "customerId",  quotationId, customerId);
            if(quotation == null){
                return Response.status(404).build();
            }
            PublicQuotation publicQuotation = getPublicQuotation(quotation);
            initProductsToQuotationItems(publicQuotation, header);
            return Response.status(200).entity(publicQuotation).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }





    @SecuredCustomer
    @GET
    @Path("quotations/customer/{customerId}/closed")
    public Response getCustomerCloseddQuotations(@PathParam(value="customerId") long customerId){
        try{
            String sql = "select b from Quotation b where b.customerId= :value0 and b.status in (:value1, :value2)";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId, 'Y', 'X');
            List<PublicQuotation> publicQuotations = getPublicQuotations(quotations);
            return Response.status(200).entity(publicQuotations).build();

        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }

    @SecuredCustomer
    @GET
    @Path("quotations/customer/{customerId}/completed")
    public Response getCustomerQuotations(@HeaderParam("Authorization") String header, @PathParam(value="customerId") long customerId){
        try{
            String sql = "select b from Quotation b where b.customerId= :value0 and b.status = :value1";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId, 'S');
            List<PublicQuotation> publicQuotations = new ArrayList<>();
            for(Quotation quotation : quotations){
                PublicQuotation publicQuotation = quotation.getContract();
                initProductsToQuotationItems(publicQuotation, header);
                List<Comment> comments = dao.getTwoConditions(Comment.class, "quotationId" , "visibleToCustomer" , quotation.getId(), true);
                publicQuotation.setComments(Helper.convertCommentsToContract(comments));
                publicQuotations.add(publicQuotation);
            }

            return Response.status(200).entity(publicQuotations).build();

        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }


    private long getCustomerIdFromHeader(String header){
        try{
            String[] values = header.split("&&");
            String customerId = values[1].trim();
            return Long.parseLong(customerId);
        } catch (Exception ex ){
            return 0;
        }
    }



    private WebApp getWebAppFromAuthHeader(String authHeader) {
        try {
            String[] values = authHeader.split("&&");
            String appSecret = values[2].trim();
            // Validate app secret
            return getWebAppFromSecret(appSecret);
        } catch (Exception ex) {
            return null;
        }
    }

    // retrieves app object from app secret
    private WebApp getWebAppFromSecret(String secret) throws Exception {
        // verify web app secret
        WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
        if (webApp == null) {
            throw new Exception();
        }
        return webApp;
    }



    private static Response getServerErrorResponse(){
        return Response.status(500).entity("Server Error").build();
    }


}
