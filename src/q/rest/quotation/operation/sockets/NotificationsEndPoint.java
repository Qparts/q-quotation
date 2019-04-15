package q.rest.quotation.operation.sockets;

import q.rest.quotation.dao.DAO;
import q.rest.quotation.helper.AppConstants;
import q.rest.quotation.model.entity.Quotation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/notifications/user/{username}/token/{token}")
@Stateless
public class NotificationsEndPoint {

    @EJB
    private DAO dao;

    private Session session;
    private int userId;
    private String token;
    private static Set<NotificationsEndPoint> notificationsEndPoints = new CopyOnWriteArraySet<>();

    @OnMessage
    public String onMessage(String message) {
        return (message);
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("username") Integer userId, @PathParam("token") String token) throws IOException {
        System.out.println("Connected");
        this.session = session;
        this.userId = userId;
        this.token = token;
        if(this.tokenMatched()) {
            notificationsEndPoints.add(this);
            String jpql = "select count(b) from Quotation b where b.status in (:value0, :value1, :value2, :value3)";
            Number number = dao.findJPQLParams(Number.class, jpql, 'N', 'W', 'R', 'A');
            if(number == null)
                number = 0;
            this.broadcast("pendingQuotations," + number.intValue());
        }
        else {
            session.close();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        notificationsEndPoints.remove(this);
    }


    public static void sendToUser(String message, int userId) {
        notificationsEndPoints.forEach(endpoint -> {
            synchronized (endpoint) {
                if (endpoint.session.isOpen()) {
                    if(endpoint.userId == userId) {
                        endpoint.session.getAsyncRemote().sendText(message);
                    }
                }
            }
        });
    }

    public static void broadcast(String message) {
        notificationsEndPoints.forEach(endpoint -> {
            synchronized (endpoint) {
                if (endpoint.session.isOpen()) {
                    endpoint.session.getAsyncRemote().sendText(message);
                }
            }
        });
    }


    private boolean tokenMatched() {
        Map<String,Object> map = new HashMap<>();
        map.put("token", token);
        map.put("userId", userId);
        Response r = this.postNoneSecuredRequest(AppConstants.USER_MATCH_TOKEN_WS, map);
        if(r.getStatus() == 200) {
            return true;
        }
        else {
            return false;
        }
    }

    public <T> Response postNoneSecuredRequest(String link, T t) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;
    }



}
