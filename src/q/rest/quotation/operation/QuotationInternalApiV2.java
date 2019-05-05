package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.filter.SecuredUser;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.Helper;
import q.rest.quotation.model.entity.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/internal/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QuotationInternalApiV2 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;


    @SecuredUser
    @POST
    @Path("assign-to-user")
    public Response assignToUser(@HeaderParam("Authorization") String authHeader, Assignment assignment) {
        try {
            String jpql = "select b from Assignment b where b.quotationId = :value0 and b.status = :value1";
            List<Assignment> list = dao.getJPQLParams(Assignment.class, jpql, assignment.getQuotationId(), 'A');
            if (!list.isEmpty()) {
                return Response.status(409).build();
            }
            assignment.setCreated(new Date());
            assignment.setStatus('A');
            dao.persist(assignment);
            async.broadcastToQuotations("assignment changed," + assignment.getQuotationId());
            async.sendToQuotingUser("newly assigned," + assignment.getQuotationId(), assignment.getAssignee());
            async.broadcastToNotification("quotingQuotations," + async.getAssinedQuotations(assignment.getAssignee()));
            return Response.status(200).entity(assignment).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("assigned-quotations/user/{param}")
    public Response getUserAssignedQuotations(@PathParam(value = "param") int userId,
                                              @HeaderParam("Authorization") String authHeader) {
        try {
            String jpql = "select b from Quotation b where b.status = :value0 and b.id in ("
                    + "select c.quotationId from Assignment c where c.status = :value1 and c.assignee = :value2)";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, jpql, 'W', 'A', userId);
            for (Quotation quotation : quotations) {
                prepareQuotation(quotation);
            }
            return Response.status(200).entity(quotations).build();

        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("assigned-quotations/user/{userId}/quotation/{quotationId}")
    public Response getUserAssignedCart(@PathParam(value = "userId") int userId, @PathParam(value = "quotationId") long quotationId, @HeaderParam("Authorization") String authHeader) {
        try {
            String jpql = "select b from Quotation b where b.status = :value0 and b.id in ("
                    + "select c.quotationId from Assignment c where c.status = :value1) and b.id = :value2";
            Quotation quotation = dao.findJPQLParams(Quotation.class, jpql, 'W', 'A', quotationId);
            if (quotation == null) {
                return Response.status(404).build();
            }
            prepareQuotation(quotation);
            return Response.status(200).entity(quotation).build();

        } catch (Exception ex) {
            return Response.status(500).build();
        }

    }


    @SecuredUser
    @POST
    @Path("assign")
    public Response requestAssignment(@HeaderParam("Authorization") String authHeader, int userId) {
        try {
            List<Integer> makeids = getQuotingMakeIds(userId, authHeader);
            String sql = "select * from qut_quotation where status = 'W' and make_id in (0";
            for (int makeId : makeids) {
                sql = sql + "," + makeId;
            }
            sql = sql + ")";
            sql = sql + " and id not in (select a.quotation_id from qut_assignment a where a.status = 'A')";
            sql = sql + " and id not in (select c.quotation_id from qut_assignment c where c.assignee = " + userId
                    + " and c.status = 'D') ";
            sql = sql + " order by created asc";
            List<Quotation> quotations = dao.getNative(Quotation.class, sql);
            if (quotations.isEmpty()) {
                sql = "select * from qut_quotation where status = 'W' and id not in "
                        + "(select a.quotation_id from qut_assignment a where a.status = 'A') order by created asc";
                quotations = dao.getNative(Quotation.class, sql);
                if (quotations.isEmpty()) {
                    return Response.status(404).build();
                }
            }
            Quotation randomQuotation = quotations.get(Helper.getRandomInteger(0, quotations.size() - 1));
            assignQuotation(randomQuotation, userId, userId);
            async.broadcastToQuotations("assignment changed," + randomQuotation.getId());
            async.broadcastToNotification("quotingQuotations," + async.getAssinedQuotations(userId));

            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @PUT
    @Path("unassign")
    public Response unassignQuotation(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
        try {
            Long quotationId = ((Number) map.get("quotationId")).longValue();
            Integer assignee = ((Number) map.get("assignee")).intValue();
            deactivateActiveAssignment(quotationId, assignee);
            async.broadcastToQuotations("assignment changed," + quotationId);
            async.sendToQuotingUser("unassigned quotation," + quotationId, assignee);
            async.broadcastToNotification("quotingQuotations," + async.getAssinedQuotations(assignee));
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("quotation/{param}")
    public Response getQuotation(@HeaderParam("Authorization") String authHeader, @PathParam(value = "param") long quotationId) {
        try {
            Quotation quotation = dao.find(Quotation.class, quotationId);
            if (quotation == null) {
                return Response.status(404).build();
            }
            this.prepareQuotation(quotation);
            return Response.status(200).entity(quotation).build();
        } catch (Exception ex) {
            ex.getMessage();
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("quotations/customer/{param}")
    public Response getCustomerQuotations(@PathParam(value = "param") long customerId) {
        try {
            String sql = "select b from Quotation b where b.customerId = :value0 order by created desc";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, sql, customerId);
            for(Quotation quotation : quotations){
                this.prepareQuotation(quotation);
            }
            return Response.status(200).entity(quotations).build();
        } catch (Exception ex) {
            ex.getMessage();
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("quotations/pending")
    public Response getPendingQuotations() {
        try {
            String jpql = "select b from Quotation b where b.status in (:value0, :value1, :value2, :value3) order by b.id";
            List<Quotation> quotations = dao.getJPQLParams(Quotation.class, jpql, 'N', 'W', 'R', 'A');
            for (Quotation quotation : quotations) {
                prepareQuotation(quotation);
            }
            return Response.status(200).entity(quotations).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @POST
    @Path("comment")
    public Response createComment(Comment comment) {
        try {
            comment.setCreated(new Date());
            dao.persist(comment);
            if (comment.getStatus() == 'X') {
                archiveQuotation(comment.getQuotationId());
            }
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @DELETE
    @Path("bill/{param}")
    public Response deleteBill(@HeaderParam("Authorization") String authHeader,
                               @PathParam(value = "param") long billId) {
        try {
            Bill bill = dao.find(Bill.class, billId);
            // delete quotation items,
            List<BillItem> billItems = dao.getCondition(BillItem.class, "billId", bill.getId());
            for (BillItem billItem : billItems) {
                billItem.setStatus('X');
                dao.update(billItem);
            }

            // archive  bill item responses
            String jpql = "select b from BillItemResponse b where billId = :value0 and status != :value1";
            List<BillItemResponse> qirs = dao.getJPQLParams(BillItemResponse.class, jpql, bill.getId(), 'X');
            for (BillItemResponse bir : qirs) {
                if (bir.getStatus() == 'C') {
                    async.createFinderScore(bir, "Quotation deleted", "revising", authHeader, -3);
                } else if (bir.getStatus() == 'I') {
                    async.createFinderScore(bir, "Quotation deleted", "revising", authHeader, -2);
                } else if (bir.getStatus() == 'N') {
                    async.createFinderScore(bir, "Quotation deleted", "revising", authHeader, 0);
                }
                bir.setStatus('X');
                dao.update(bir);
            }
            // archive quotation
            bill.setStatus('X');
            dao.update(bill);

            List<Bill> bills = dao.getCondition(Bill.class, "quotationId", bill.getQuotationId());
            if (bills.isEmpty()) {
                Quotation q = dao.find(Quotation.class, bill.getQuotationId());
                q.setStatus('W');
                dao.update(q);
            }
//            verifyQuotationCompletion(qo.getCartId());
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @DELETE
    @Path("bill-item/{param}")
    public Response deleteBillItem(@HeaderParam("Authorization") String authHeader,
                                   @PathParam(value = "param") long billItemId) {
        try {
            BillItem billItem = dao.find(BillItem.class, billItemId);
            // delete vendor quotation items,

            String jpql = "select b from BillItemResponse b where billItemId = :value0 and status != :value1";
            List<BillItemResponse> birs = dao.getJPQLParams(BillItemResponse.class, jpql, billItem.getId(), 'X');
            for (BillItemResponse biResponse : birs) {
                if (biResponse.getStatus() == 'C') {
                    async.createFinderScore(biResponse, "Quotation item deleted", "revising", authHeader, -3);
                } else if (biResponse.getStatus() == 'I') {
                    async.createFinderScore(biResponse, "Quotation item deleted", "revising", authHeader, -2);
                } else if (biResponse.getStatus() == 'N') {
                    async.createFinderScore(biResponse, "Quotation item deleted", "revising", authHeader, 0);
                }
                biResponse.setStatus('X');
                dao.update(biResponse);
            }

            billItem.setStatus('X');
            dao.update(billItem);
//            verifyQuotationCompletion(qi.getCartId());
            return Response.status(201).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @POST
    @Path("bill")
    public Response createBill(@HeaderParam("Authorization") String authHeader, Bill bill) {
        try {
            bill.setCreated(new Date());
            // check idempotency
            if (isBillRedudant(bill.getCreatedBy(), bill.getQuotationId(), bill.getCreated())) {
                return Response.status(409).build();
            }
            bill.setStatus('W');
            dao.persist(bill);
            for (BillItem billItem : bill.getBillItems()) {
                billItem.setCreated(bill.getCreated());
                billItem.setStatus('W');
                billItem.setBillId(bill.getId());
                billItem.setCreatedBy(bill.getCreatedBy());
                billItem.setQuotationId(bill.getQuotationId());
                dao.persist(billItem);
            }
            Quotation quotation = dao.find(Quotation.class, bill.getQuotationId());
            quotation.setStatus('W');
            dao.update(quotation);
            async.broadcastToQuotations("update quotation," + bill.getQuotationId());
            return Response.status(200).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @POST
    @Path("bill-item")
    public Response createBillIteam(BillItem billItem) {
        try {
            Bill bill = new Bill();
            bill.setQuotationId(billItem.getQuotationId());
            bill.setStatus('W');
            bill.setCreated(new Date());
            bill.setCreatedBy(billItem.getCreatedBy());
            bill.setBillItems(new ArrayList<>());
            dao.persist(bill);
            billItem.setBillId(bill.getId());
            billItem.setCreated(new Date());
            billItem.setStatus('W');
            dao.persist(billItem);
            bill.getBillItems().add(billItem);
            return Response.status(200).entity(bill).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }

    }

    @SecuredUser
    @POST
    @Path("bill-item-response")
    public Response createQuotationItemResponse(@HeaderParam("Authorization") String authHeader, BillItem billItem) {
        try {
            if (billItem.getStatus() == 'N') {
                dao.update(billItem);
            }
            for (BillItemResponse bir : billItem.getBillItemResponses()) {
                if (bir.getId() == 0) {
                    // new quotaiton item
                    String jpql = "select b from BillItemResponse b where b.billItemId =:value0 and b.productId = :value1";
                    List<BillItemResponse> checkResponses = dao.getJPQLParams(BillItemResponse.class, jpql,
                            bir.getBillItemId(), bir.getProductId());
                    if (!checkResponses.isEmpty()) {
                        return Response.status(429).build();
                    }
                    bir.setCreated(new Date());
                    dao.persist(bir);
                    if (bir.getStatus() == 'C') {
                        this.async.createFinderScore(bir, "Product number and price found", "quoting", authHeader, 3);
                    } else if (bir.getStatus() == 'I') {
                        this.async.createFinderScore(bir, "Product number found", "quoting", authHeader, 2);
                    } else if (bir.getStatus() == 'N') {
                        this.async.createFinderScore(bir, "Product not available", "quoting", authHeader, 0);
                    }
                } else {
                    // already submitted before
                    dao.update(bir);
                    if (bir.getStatus() == 'C') {
                        this.async.createFinderScore(bir, "Price found", "quoting", authHeader, 1);
                    } else if (bir.getStatus() == 'N') {
                        this.async.createFinderScore(bir, "Product Found but price is not available", "quoting",
                                authHeader, 1);
                    }

                }
            }
            // check for completion
            checkForBillItemCompletion(authHeader, billItem.getId());
            return Response.status(201).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }

    // check if quotation item is responded
    private void checkForBillItemCompletion(String authHeader, long billItemId) {
        // check for completed
        String jpql = "select b from BillItemResponse b where b.billItemId =:value0 and b.status =:value1";
        List<BillItemResponse> completed = dao.getJPQLParams(BillItemResponse.class, jpql, billItemId, 'C');
        if (!completed.isEmpty()) {
            BillItem billItem = dao.find(BillItem.class, billItemId);
            billItem.setStatus('C');
            dao.update(billItem);
            checkForBillCompletion(authHeader, billItem.getBillId());
        } else {
            // item not completed, check if unavailable response exists
            String jpql2 = "select b from BillItemResponse b where b.billItemId =:value0 and b.status =:value1";
            List<BillItemResponse> notAvailable = dao.getJPQLParams(BillItemResponse.class, jpql2, billItemId, 'N');

            if (!notAvailable.isEmpty()) {
                BillItem billItem = dao.find(BillItem.class, billItemId);
                billItem.setStatus('N');// not available
                dao.update(billItem);
                checkForBillCompletion(authHeader, billItem.getBillId());
            }

        }
    }


    // check if all quotation items completed or not available
    private void checkForBillCompletion(String authHeader, long billId) {
        List<BillItem> billItem = dao.getTwoConditions(BillItem.class, "billId", "status", billId, 'W');
        if (billItem.isEmpty()) {
            Bill bill = dao.find(Bill.class, billId);
            bill.setStatus('C');
            dao.update(bill);
            checkForQuotationReadyForSubmission(authHeader, bill.getQuotationId());
        }
    }

    private void checkForQuotationReadyForSubmission(String authHeader, long quotationId) {
        List<Bill> allBills = dao.getCondition(Bill.class, "quotationId", quotationId);
        String sql = "select b from Bill b where b.quotationId =:value0 and b.status in (:value1, :value2)";
        List<Bill> completedBills = dao.getJPQLParams(Bill.class, sql, quotationId, 'C', 'X');
        //List<Bill> completedBills = dao.getTwoConditions(Bill.class, "quotationId", "status", quotationId, 'C');
        if (allBills.size() == completedBills.size()) {
            List<BillItemResponse> birs = dao.getTwoConditions(BillItemResponse.class, "status", "quotationId", 'C', quotationId);
            Quotation quotation = dao.find(Quotation.class, quotationId);
            if(!birs.isEmpty()){
                completeQuotationAssignment(quotationId);
                quotation.setStatus('S');
                dao.update(quotation);
                async.sendQuotationCompletionEmail(authHeader, quotation);
                async.sendQuotationCompletionSms(authHeader, quotation);
                async.broadcastToQuotations("submit quotation," + quotation.getId());
                async.broadcastToNotification("pendingQuotations," + async.getPendingQuotations());
                async.sendToCustomerNotification("quotationComplete" , quotation.getCustomerId());
            }
            else{
                // quotation completed but cart does not have items! set as ready for submission
                // for archiving!
                quotation.setStatus('R');// all items not available
                dao.update(quotation);
                async.broadcastToQuotations("not available quotation," + quotationId);
            }
        }
    }



    private void completeQuotationAssignment(long quotationId) {
        Assignment ca = dao.findTwoConditions(Assignment.class, "quotationId", "status", quotationId, 'A');
        if (ca != null) {
            Quotation c = dao.find(Quotation.class, quotationId);
            ca.setCompleted(new Date());
            ca.setStatus('C');
            dao.update(ca);
        }
    }

    @SecuredUser
    @PUT
    @Path("bill-item")
    public Response updateBillItem(BillItem billItem) {
        try {
            dao.update(billItem);
            List<BillItemResponse> billItemResponses = dao.getCondition(BillItemResponse.class, "billItemId", billItem.getId());
            for (BillItemResponse bir : billItemResponses) {
                if (bir.getStatus() != 'N') {
                    bir.setQuantity(billItem.getQuantity());
                    dao.update(bir);
                }
            }
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @PUT
    @Path("merge-quotations")
    public Response mergeQuotations(Map<String, Object> map) {
        try {
            long mainId = ((Number) map.get("mainId")).longValue();
            long slaveId = ((Number) map.get("slaveId")).longValue();
            int userId = ((Number) map.get("userId")).intValue();
            Quotation main = dao.find(Quotation.class, mainId);
            Quotation slave = dao.find(Quotation.class, slaveId);
            List<QuotationItem> slaveItems = dao.getCondition(QuotationItem.class, "quotationId", slave.getId());
            for (QuotationItem qi : slaveItems) {
                qi.setQuotationId(main.getId());
                dao.update(qi);
            }
            List<Bill> bills = dao.getCondition(Bill.class, "quotationId", slave.getId());
            for (Bill bill : bills) {
                bill.setQuotationId(main.getId());
                dao.update(bill);
            }
            List<BillItem> billItems = dao.getCondition(BillItem.class, "quotationId", slave.getId());
            for (BillItem bi : billItems) {
                bi.setQuotationId(main.getId());
                dao.update(bi);
            }
            List<BillItemResponse> billItemResponses = dao.getCondition(BillItemResponse.class, "quotationId",
                    slave.getId());
            for (BillItemResponse bir : billItemResponses) {
                bir.setQuotationId(main.getId());
                dao.update(bir);
            }

            Comment comment = new Comment();
            comment.setStatus('X');
            comment.setQuotationId(slave.getId());
            comment.setCreated(new Date());
            comment.setCreatedBy(userId);
            comment.setText("Merged to: " + main.getId());
            dao.persist(comment);

            closeActiveComments(slave.getId());

            slave.setStatus('X');
            dao.update(slave);
            return Response.status(201).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }


    private void archiveQuotation(long quotationId) {
        Quotation quotation = dao.find(Quotation.class, quotationId);
        if (quotation != null) {
            quotation.setStatus('X');
            dao.update(quotation);
            async.broadcastToQuotations("archive quotation," + quotation.getId());
            async.broadcastToNotification("pendingQuotations," + async.getPendingQuotations());
        }

    }

    private void closeActiveComments(long quotationId) {
        String jpql = "select b from Comment b where b.quotationId = :value0 and b.status in ( :value1 , :value2)";
        List<Comment> pastReviews = dao.getJPQLParams(Comment.class, jpql, quotationId, 'P', 'A');
        for (Comment r : pastReviews) {
            r.setStatus('C');
            dao.update(r);
        }
    }


    private Map<String, Object> getCustomer(long customerId, String authHeader) {
        Response r = this.getSecuredRequest(AppConstants.getCustomer(customerId), authHeader);
        if (r.getStatus() == 200) {
            return r.readEntity(Map.class);
        } else {
            return null;
        }
    }


    // check idempotency of a quotation
    private boolean isBillRedudant(int userId, long quotationId, Date created) {
        // if a quotation was created less than 30 seconds ago, then do not do
        String jpql = "select b from Bill b where b.createdBy = :value0 " +
                "and b.quotationId = :value1 " +
                "and b.created between :value2 and :value3";
        Date previous = Helper.addSeconds(created, -20);
        List<Bill> bills = dao.getJPQLParams(Bill.class, jpql, userId, quotationId, previous, created);
        return bills.size() > 0;

    }

    private void deactivateActiveAssignment(long quotationId, int assignee) {
        String sql = "select b from Assignment b where b.quotationId =:value0 and b.assignee =:value1 and b.status =:value2";
        Assignment assignment = dao.findJPQLParams(Assignment.class, sql, quotationId, assignee, 'A');
        if (assignment != null) {
            assignment.setCompleted(new Date());
            assignment.setStatus('D');
            dao.update(assignment);
        }
    }

    private void assignQuotation(Quotation quotation, int createdBy, int assignee) {
        Assignment assignment = new Assignment();
        assignment.setCreatedBy(createdBy);
        assignment.setCreated(new Date());
        assignment.setAssignee(assignee);
        assignment.setQuotationId(quotation.getId());
        assignment.setCompleted(null);
        assignment.setStatus('A');
        dao.persist(assignment);
    }

    private List<Integer> getQuotingMakeIds(int finderId, String authHeader) {
        Response r = this.getSecuredRequest(AppConstants.getQuotingMakeIds(finderId), authHeader);
        if (r.getStatus() == 200) {
            List<Integer> finders = r.readEntity(new GenericType<List<Integer>>() {
            });
            return finders;
        }
        return new ArrayList<>();
    }

    private void appendComments(Quotation quotation) {
        //get comments
        String sql2 = "select b from Comment b where b.quotationId = :value0 order by b.created";
        List<Comment> comments = dao.getJPQLParams(Comment.class, sql2, quotation.getId());
        quotation.setComments(comments);
    }

    private void appendLastAssignment(Quotation quotation) {
        //get last assignment
        String sql = "select b from Assignment b where b.quotationId = :value0 and b.status = :value1 order by b.created desc";// newest
        List<Assignment> cas = dao.getJPQLParams(Assignment.class, sql, quotation.getId(), 'A');
        if (!cas.isEmpty()) {
            quotation.setActiveAssignment(cas.get(0));
        }
    }

    private void appendQuotationItems(Quotation quotation) {
        //get quotation items
        List<QuotationItem> items = dao.getCondition(QuotationItem.class, "quotationId", quotation.getId());
        quotation.setQuotationItems(items);
    }

    private void appendBills(Quotation quotation) {
        //get bills
        String sql3 = "select b from Bill b where b.quotationId = :value0 and b.status != :value1 order by b.created";
        List<Bill> bills = dao.getJPQLParams(Bill.class, sql3, quotation.getId(), 'X');
        for (Bill bill : bills) {
            appendBillItems(bill);

        }
        quotation.setBills(bills);
    }

    private void appendBillItems(Bill bill) {
        String sql4 = "select b from BillItem b where b.billId =:value0 and b.status != :value1 order by b.created";
        List<BillItem> billItems = dao.getJPQLParams(BillItem.class, sql4, bill.getId(), 'X');
        for (BillItem billItem : billItems) {
            appendBillItemResponses(billItem);
        }
        bill.setBillItems(billItems);
    }

    private void appendBillItemResponses(BillItem billItem) {
        String sql5 = "select b from BillItemResponse b where b.billItemId = :value0 and b.status != :value1";
        List<BillItemResponse> responses = dao.getJPQLParams(BillItemResponse.class, sql5, billItem.getId(), 'X');
        billItem.setBillItemResponses(responses);
    }

    private void prepareQuotation(Quotation quotation) {
        try {
            appendQuotationItems(quotation);
            appendLastAssignment(quotation);
            appendComments(quotation);
            appendBills(quotation);

        } catch (Exception ex) {

        }
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
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;
    }

    public Response deleteSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.delete();
        return r;
    }


}
