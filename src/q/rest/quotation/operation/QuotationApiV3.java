package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.filter.annotation.SubscriberJwt;
import q.rest.quotation.filter.annotation.UserJwt;
import q.rest.quotation.filter.annotation.UserSubscriberJwt;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.contract.QuotationModel;
import q.rest.quotation.model.contract.QuotationsSummary;
import q.rest.quotation.model.entity.CompanyQuotation;
import q.rest.quotation.model.entity.CompanyQuotationItem;
import q.rest.quotation.model.entity.PricePolicy;
import q.rest.quotation.model.entity.PurchaseOrder;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/internal/api/v3/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QuotationApiV3 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;

    @SubscriberJwt
    @POST
    @Path("price-policy")
    public Response createNewVendorPolicy(PricePolicy pricePolicy) {
        PricePolicy pp = dao.findTwoConditions(PricePolicy.class, "companyId", "policyName", pricePolicy.getCompanyId(), pricePolicy.getPolicyName());
        if (pp != null) {
            return Response.status(409).build();
        }
        pricePolicy.setCreated(new Date());
        dao.persist(pricePolicy);
        return Response.status(200).entity(pricePolicy).build();
    }

    @UserSubscriberJwt
    @GET
    @Path("policies/company/{id}")
    public Response getCompanyPolicies(@PathParam(value = "id") int id) {
        List<PricePolicy> policies = dao.getCondition(PricePolicy.class, "companyId", id);
        return Response.ok().entity(policies).build();
    }

    @SubscriberJwt
    @POST
    @Path("quotation")
    public Response requestQuotation(QuotationModel model) {
        String sql = "select b from CompanyQuotation b where b.companyId =:value0 and b.targetCompanyId = :value1 and cast(b.created as date) =:value2";
        CompanyQuotation cq = dao.findJPQLParams(CompanyQuotation.class, sql, model.getCompanyId(), model.getTargetCompanyId(), new Date());
        String jpql = "select b from PricePolicy b where b.id in (select c.policyId from CompanyPricePolicy c where c.companyId = :value0 and c.targetCompanyId = :value1)";
        PricePolicy pp = dao.findJPQLParams(PricePolicy.class, jpql, model.getTargetCompanyId(), model.getCompanyId());
        if (cq == null) {
            cq = new CompanyQuotation(model, pp);
            dao.persist(cq);
        } else {
            CompanyQuotationItem item = cq.getItemFromModel(model);
            if (item == null) {
                item = new CompanyQuotationItem(model, pp);
                cq.getQuotationItems().add(item);
                dao.update(cq);
            }
        }
        String sql2 = "select b from CompanyQuotation b where b.companyId =:value0 and b.targetCompanyId = :value1 and cast(b.created as date) =:value2";
        CompanyQuotation cq2 = dao.findJPQLParams(CompanyQuotation.class, sql2, model.getCompanyId(), model.getTargetCompanyId(), new Date());
        return Response.ok().entity(cq2).build();
    }

    @UserSubscriberJwt
    @GET
    @Path("quotations/target/{targetId}/last/{max}")
    public Response getTargetCompanyLatest(@PathParam(value = "max") int max, @PathParam(value = "targetId") int targetId){
        String sql = "select b from CompanyQuotation b where b.targetCompanyId = :value0 order by b.created desc";
        List<CompanyQuotation> vqs = dao.getJPQLParamsMax(CompanyQuotation.class, sql, max, targetId);
        return Response.status(200).entity(vqs).build();
    }


    @UserSubscriberJwt
    @GET
    @Path("quotations/target/{targetId}/from/{from}/to/{to}")
    public Response getTargetCompanyQuotations(@PathParam(value = "targetId") int targetId, @PathParam(value = "from") long from, @PathParam(value = "to") long to) {
        String sql = "select b from CompanyQuotation b where b.targetCompanyId = :value0 and cast(b.created as date) between cast(:value1 as date) and cast(:value2 as date) order by b.created desc";
        List<CompanyQuotation> vqs = dao.getJPQLParams(CompanyQuotation.class, sql, targetId, new Date(from), new Date(to));
        return Response.status(200).entity(vqs).build();
    }

    @UserSubscriberJwt
    @GET
    @Path("quotations/company/{companyId}/from/{from}/to/{to}")
    public Response getCompanyQuotations(@PathParam(value = "companyId") int companyId, @PathParam(value = "from") long from, @PathParam(value = "to") long to) {
        String sql = "select b from CompanyQuotation b where b.companyId = :value0 and cast(b.created as date) between cast(:value1 as date) and cast(:value2 as date) order by b.created desc";
        List<CompanyQuotation> vqs = dao.getJPQLParams(CompanyQuotation.class, sql, companyId, new Date(from), new Date(to));
        return Response.status(200).entity(vqs).build();
    }


    @UserSubscriberJwt
    @GET
    @Path("quotations/target/{targetId}/year/{year}/month/{month}")
    public Response getTargetVendorQuotations(@PathParam(value = "targetId") int targetId, @PathParam(value = "year") int year, @PathParam(value = "month") int month) {
        Date from = Helper.getFromDate(month, year);
        Date to = Helper.getToDate(month, year);
        String sql = "select b from CompanyQuotation b where b.targetCompanyId = :value0 and cast(b.created as date) between cast(:value1 as date) and cast(:value2 as date) order by b.created desc";
        List<CompanyQuotation> vqs = dao.getJPQLParams(CompanyQuotation.class, sql, targetId, from, to);
        return Response.ok().entity(vqs).build();
    }

    @SubscriberJwt
    @Path("purchase-order")
    @POST
    public Response createPurchaseOrder(PurchaseOrder po) {
        po.setCreated(new Date());
        po.getItems().forEach(item -> item.setCreated(new Date()));
        dao.persist(po);
        //send email notification to target company
        async.sendPurchaseOrderNotification(po.getSubscriberId(), po.getTargetCompanyId());
        return Response.status(200).build();
    }

    @SubscriberJwt
    @Path("purchase-order")
    @PUT
    public Response updatePurchaseOrder(PurchaseOrder po) {
        try {
            dao.update(po);
            if(po.getStatus() == 'A')
                async.sendUpdatePurchaseOrderNotification(po.getSubscriberId(), po.getTargetCompanyId(), "Accepted");
            if(po.getStatus() == 'R')
                async.sendUpdatePurchaseOrderNotification(po.getSubscriberId(), po.getTargetCompanyId(), "Refused");
            //send email notification to company;
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @SubscriberJwt
    @Path("purchase-order/target/{companyId}")
    @GET
    public Response getPendingTargetPurchaseOrders(@PathParam(value = "companyId") int targetId) {
        try {
            String sql = "select b from PurchaseOrder b where b.targetCompanyId = :value0 order by created desc";
            List<PurchaseOrder> pos = dao.getJPQLParams(PurchaseOrder.class, sql, targetId);
            return Response.status(200).entity(pos).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @UserJwt
    @GET
    @Path("summary-report/company/{id}")
    public Response getCompanySummaryReport(@PathParam(value = "id") int id) {
        String sql = "select count(*) from CompanyQuotationItem b where b.quotationId in (select c.id from CompanyQuotation c where c.companyId = :value0)" ;
        int quotationsSubmitted = dao.findJPQLParams(Number.class, sql, id).intValue();
        sql = "select count(*) from CompanyQuotationItem b where b.quotationId in (select c.id from CompanyQuotation c where c.targetCompanyId = :value0)" ;
        int quotationReceived = dao.findJPQLParams(Number.class, sql, id).intValue();
        sql = "select count(*) from PurchaseOrderItem b where b.purchaseOrderId in (select c.id from PurchaseOrder c where c.companyId = :value0)";
        int posSubmitted = dao.findJPQLParams(Number.class, sql, id).intValue();
        sql = "select count(*) from PurchaseOrderItem b where b.purchaseOrderId in (select c.id from PurchaseOrder c where c.targetCompanyId = :value0)";
        int posReceived = dao.findJPQLParams(Number.class, sql, id).intValue();
        QuotationsSummary report = new QuotationsSummary();
        report.setQuotationsSubmitted(quotationsSubmitted);
        report.setQuotationsReceived(quotationReceived);
        report.setPosSubmitted(posSubmitted);
        report.setPosReceived(posReceived);
        return Response.ok().entity(report).build();
    }

    @UserJwt
    @GET
    @Path("quotations-activity/from/{from}/to/{to}")
    public Response getQuotationsItemsDailyReport(@PathParam(value = "from") long fromLong, @PathParam(value = "to") long toLong) {
        try {
            Helper h = new Helper();
            List<Date> dates = h.getAllDatesBetween(new Date(fromLong), new Date(toLong));
            List<Map> kgs = new ArrayList<>();
            for (Date date : dates) {
                String sql = "select count(*) from CompanyQuotationItem b where cast(b.created as date) = cast(:value0 as date)";
                Number n = dao.findJPQLParams(Number.class, sql, date);
                Map<String, Object> map = new HashMap<>();
                map.put("count", n.intValue());
                map.put("date", date.getTime());
                kgs.add(map);
            }
            return Response.status(200).entity(kgs).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @UserJwt
    @GET
    @Path("purchase-order-activity/from/{from}/to/{to}")
    public Response getPurchaseOrderItemsDailyReport(@PathParam(value = "from") long fromLong, @PathParam(value = "to") long toLong) {
        try {
            Helper h = new Helper();
            List<Date> dates = h.getAllDatesBetween(new Date(fromLong), new Date(toLong));
            List<Map> kgs = new ArrayList<>();
            for (Date date : dates) {
                String sql = "select count(*) from PurchaseOrder b where cast(b.created as date) = cast(:value0 as date)";
                Number n = dao.findJPQLParams(Number.class, sql, date);
                Map<String, Object> map = new HashMap<>();
                map.put("count", n.intValue());
                map.put("date", date.getTime());
                kgs.add(map);
            }
            return Response.status(200).entity(kgs).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @SubscriberJwt
    @Path("purchase-order/company/{vendorId}")
    @GET
    public Response getVendorPurchaseOrders(@PathParam(value = "vendorId") int vendorId) {
        try {
            String sql = "select b from PurchaseOrder b where b.companyId = :value0 order by created desc";
            List<PurchaseOrder> pos = dao.getJPQLParams(PurchaseOrder.class, sql, vendorId);
            return Response.status(200).entity(pos).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @UserJwt
    @Path("purchase-orders-report")
    @POST
    public Response generatePurchaseOrderReport(Map<String, Object> map) {
        int year = ((Number) map.get("year")).intValue();
        int month = ((Number) map.get("month")).intValue();
        Date from = Helper.getFromDate(month, year);
        Date to = Helper.getToDate(month, year);
        String sql = "select b from PurchaseOrder b where cast(b.created as date) between :value0 and :value1 order by b.created asc";
        List<PurchaseOrder> orders = dao.getJPQLParams(PurchaseOrder.class, sql, from, to);
        return Response.ok().entity(orders).build();
    }


}
