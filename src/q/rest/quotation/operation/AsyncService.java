package q.rest.quotation.operation;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.operation.sockets.NotificationsEndPoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;


@Stateless
public class AsyncService {

    @EJB
    private DAO dao;

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
