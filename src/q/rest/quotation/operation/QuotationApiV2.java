package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.filter.SecuredCustomer;
import q.rest.quotation.filter.SecuredUser;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.contract.CreateQuotationRequest;
import q.rest.quotation.model.contract.PublicComment;
import q.rest.quotation.model.contract.PublicQuotation;
import q.rest.quotation.model.contract.PublicQuotationItem;
import q.rest.quotation.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
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
    @POST
    @Path("quotation")
    public Response createQuotation(@HeaderParam("Authorization") String header, CreateQuotationRequest qr){
        try {
            if (isQuotationRedudant(qr.getCustomerId(), new Date())) {
                return Response.status(429).build();
            }

            WebApp wa = this.getWebAppFromAuthHeader(header);
            Quotation quotation = new Quotation();
            quotation.setAppCode(wa.getAppCode());
            quotation.setCityId(qr.getCityId());
            quotation.setCreated(new Date());
            quotation.setCreatedBy(0);
            quotation.setCustomerId(qr.getCustomerId());
            quotation.setCustomerVehicleId(qr.getCustomerVehicleId());
            quotation.setMakeId(qr.getMakeId());
            quotation.setStatus('N');
            quotation.setVinImageAttached(qr.getVinImage().length() > 0);
            dao.persist(quotation);
            async.completeQuotationCreation(quotation, qr, header);
            Map<String,Object> map = new HashMap<>();
            map.put("quotationId", quotation.getId());
            return Response.status(200).entity(map).build();
        }catch(Exception ex){
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
                        System.out.println(AppConstants.getPublicProduct(bir.getProductId()));
                        Response r = this.getSecuredRequest(AppConstants.getPublicProduct(bir.getProductId()), header);
                        if(r.getStatus() == 200){
                            Map map = r.readEntity(Map.class);
                            pqi.setProducts(map);
                        }
                        System.out.println(r.getStatus());
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



}
