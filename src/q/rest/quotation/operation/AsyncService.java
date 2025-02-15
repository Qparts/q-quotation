package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.helper.InternalAppRequester;
import q.rest.quotation.operation.sockets.NotificationsEndPoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Stateless
public class AsyncService {

    @EJB
    private DAO dao;


    @Asynchronous
    public void sendPurchaseOrderNotification(int sender, int receiver){
        Map<String,Integer> map = new HashMap<>();
        map.put("receiverCompanyId", receiver);
        map.put("senderId", sender);
        InternalAppRequester.postSecuredRequest(AppConstants.POST_PURCHASE_ORDER_NOTIFICATION, map);
    }

    @Asynchronous
    public void sendUpdatePurchaseOrderNotification(int sender, int receiver, String status){
        Map<String,Object> map = new HashMap<>();
        map.put("receiverCompanyId", receiver);
        map.put("senderId", sender);
        map.put("status", status);
        InternalAppRequester.postSecuredRequest(AppConstants.POST_UPDATE_PURCHASE_ORDER_NOTIFICATION, map);
    }

//    @Asynchronous
 //   public void notifyCustomerOfQuotationCreation(String header, Quotation quotation) {
   //     sendQuotationCreationEmail(header, quotation.getId(), quotation.getCustomerId());
     //   sendQuotationCreateionSms(quotation.getId());
      //  broadcastToQuotations("new quotation,"+quotation.getId());
        //broadcastToNotification("pendingQuotations,"+getPendingQuotations());
    //}


    @Asynchronous
    public void broadcastToNotification(String message){
        NotificationsEndPoint.broadcast(message);
    }

}
