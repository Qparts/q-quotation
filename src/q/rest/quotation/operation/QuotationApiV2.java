package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.filter.SecuredCustomer;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.contract.*;
import q.rest.quotation.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
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
    @POST
    @Path("quotation")
    public Response createQuotationRequest(@HeaderParam("Authorization") String header, CreateQuotationRequest qr){
        try {
            if (isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }
            WebApp wa = this.getWebAppFromAuthHeader(header);
            Quotation quotation = createQuotation(qr, wa, header);
            createQuotationItems(quotation, qr.getQuotationItems());
            async.completeQuotationCreation(quotation, qr, header);
            CreateQuotationResponse res = new CreateQuotationResponse();
            res.setQuotationId(quotation.getId());
            res.setItems(new ArrayList<>());
            for(CreateQuotationItemRequest req : qr.getQuotationItems()){
                Map<String,Object> map = new HashMap<>();
                map.put("tempId", req.getTempId());
                map.put("imageName" , req.getItemName());
                res.getItems().add(map);
                res.setVehicleImageName(qr.getCustomerVehicleId() + ".png");
                res.setUploadImage(qr.getCustomerVehicleNewlyCreated() && qr.getImageAttached());
            }
            return Response.status(200).entity(res).build();
        }catch(Exception ex){
            ex.printStackTrace();
            return getServerErrorResponse();
        }
    }


    @SecuredCustomer
    @GET
    @Path("quotations/customer/{customerId}/pending")
    public Response getCustomerQuotations(@PathParam(value="customerId") long customerId){
        try{
            String sql = "select b from Quotation b where b.customerId= :value0 and b.status in (:value1, :value2, :value3)";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId, 'W', 'R', 'A');
            List<PublicQuotation> publicQuotations = new ArrayList<>();
            for(Quotation quotation : quotations){
                PublicQuotation publicQuotation = quotation.getContract();
                //add quotation items
                List<QuotationItem> quotationItems = dao.getCondition(QuotationItem.class, "quotationId", quotation.getId());
                publicQuotation.setQuotationItems(Helper.convertQuotationItemsToContract(quotationItems));
                //add public comments
                List<Comment> comments = dao.getTwoConditions(Comment.class, "quotationId" , "visibleToCustomer" , quotation.getId(), true);
                publicQuotation.setComments(Helper.convertCommentsToContract(comments));
                //add to arraylist
                publicQuotations.add(publicQuotation);
            }
            return Response.status(200).entity(publicQuotations).build();

        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }

    @SecuredCustomer
    @GET
    @Path("quotations/customer/{customerId}/closed")
    public Response getCustomerCloseddQuotations(@PathParam(value="customerId") long customerId){
        try{
            String sql = "select b from Quotation b where b.customerId= :value0 and b.status in (:value1, :value2)";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId, 'Y', 'X');
            List<PublicQuotation> publicQuotations = new ArrayList<>();
            for(Quotation quotation : quotations){
                PublicQuotation publicQuotation = quotation.getContract();
                //add quotation items
                List<QuotationItem> quotationItems = dao.getCondition(QuotationItem.class, "quotationId", quotation.getId());
                publicQuotation.setQuotationItems(Helper.convertQuotationItemsToContract(quotationItems));
                //add public comments
                List<Comment> comments = dao.getTwoConditions(Comment.class, "quotationId" , "visibleToCustomer" , quotation.getId(), true);
                publicQuotation.setComments(Helper.convertCommentsToContract(comments));
                //add to arraylist
                publicQuotations.add(publicQuotation);
            }
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
                sql = "select b from BillItem b where b.id in (select c.billItemId from BillItemResponse c where c.status = :value0 and c.quotationId = :value1) ";
                List<BillItem> billItems = dao.getJPQLParams(BillItem.class, sql, 'C', quotation.getId());
                publicQuotation.setQuotationItems(new ArrayList<>());
                for(BillItem billItem : billItems){
                    PublicQuotationItem pqi = billItem.getContract();
                    sql = "select b from BillItemResponse b where b.billItemId = :value0 and b.status = :value1";
                    List<BillItemResponse> billItemResponses = dao.getJPQLParams(BillItemResponse.class, sql, billItem.getId(), 'C');

                    for(BillItemResponse bir : billItemResponses){
                        Response r = this.getSecuredRequest(AppConstants.getPublicProduct(bir.getProductId()), header);
                        if(r.getStatus() == 200){
                            Map map = r.readEntity(Map.class);
                            pqi.setProducts(map);
                        }
                    }

                    publicQuotation.getQuotationItems().add(pqi);
                }
                List<Comment> comments = dao.getTwoConditions(Comment.class, "quotationId" , "visibleToCustomer" , quotation.getId(), true);
                publicQuotation.setComments(Helper.convertCommentsToContract(comments));
                publicQuotations.add(publicQuotation);
            }

            return Response.status(200).entity(publicQuotations).build();

        }catch (Exception ex){
            return getServerErrorResponse();
        }
    }





    // check idempotency of a cart
    private boolean isQuotationRedudant(long customerId, Date created) {
        // if a cart was created less than n seconds ago, then do not do
        String jpql = "select b from Quotation b where b.customerId = :value0 and b.created between :value1 and :value2";
        Date previous = Helper.addSeconds(created, -15);
        List<Quotation> carts = dao.getJPQLParams(Quotation.class, jpql, customerId, previous, created);
        return carts.size() > 0;

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


    public Response getSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.get();
        return r;
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));
        return r;
    }


        private Quotation createQuotation(CreateQuotationRequest qr, WebApp wa, String header){
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
        quotation.setStatus('N');
        quotation.setVinImageAttached(false);
        dao.persist(quotation);
        return quotation;
    }


    private void createQuotationItems(Quotation quotation, List<CreateQuotationItemRequest> qir) {
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

}
