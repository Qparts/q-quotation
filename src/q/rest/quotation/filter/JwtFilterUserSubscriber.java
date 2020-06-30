package q.rest.quotation.filter;



import q.rest.quotation.filter.annotation.UserSubscriberJwt;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

@Provider
@UserSubscriberJwt
@Priority(Priorities.AUTHENTICATION)
public class JwtFilterUserSubscriber extends JwtFilter {

    @Override
    public void validateType(Object type) throws Exception{
        if(!(type.toString().equals("S") || type.toString().equals("U"))){
            throw new Exception();
        }
    }

}
