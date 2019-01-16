package q.rest.quotation.helper;

import q.rest.quotation.model.contract.PublicComment;
import q.rest.quotation.model.contract.PublicQuotationItem;
import q.rest.quotation.model.entity.Comment;
import q.rest.quotation.model.entity.QuotationItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Helper {

    public static int getRandomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static Date addSeconds(Date original, int seconds) {
        return new Date(original.getTime() + (1000L * seconds));
    }

    public static Date addMinutes(Date original, int minutes) {
        return new Date(original.getTime() + (1000L * 60 * minutes));
    }

    public String getDateFormat(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");
        return sdf.format(date);
    }

    public String getDateFormat(Date date, String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }




    public static List<PublicQuotationItem> convertQuotationItemsToContract(List<QuotationItem> quotationItems){
        List<PublicQuotationItem> publicQuotationItems = new ArrayList<>();
        for(QuotationItem qi : quotationItems){
            publicQuotationItems.add(qi.getContract());
        }
        return publicQuotationItems;
    }


    public static List<PublicComment> convertCommentsToContract(List<Comment> comments){
        List<PublicComment> publicComments = new ArrayList<>();
        for(Comment comment : comments){
            publicComments.add(comment.getContract());
        }
        return publicComments;
    }
}
