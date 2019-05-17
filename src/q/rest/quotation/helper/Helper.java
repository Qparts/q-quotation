package q.rest.quotation.helper;

import q.rest.quotation.model.contract.PublicComment;
import q.rest.quotation.model.contract.PublicQuotationItem;
import q.rest.quotation.model.entity.Comment;
import q.rest.quotation.model.entity.QuotationItem;

import java.text.SimpleDateFormat;
import java.util.*;

public class Helper {

    public static String getFullMobile(String mobile, String countryCode){
        String mobileFull = mobile;
        mobileFull = mobileFull.replaceFirst("^0+(?!$)", "");
        mobileFull = countryCode + mobileFull;
        return mobileFull;
    }

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

    public static Date getToDate(int month, int year) {
        Date to = new Date();
        Calendar cTo = new GregorianCalendar();
        if (month == 12) {
            cTo.set(year, 11, 31, 0, 0, 0);
        } else {
            cTo.set(year, month, 1, 0, 0, 0);
            cTo.set(Calendar.DAY_OF_MONTH, cTo.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        cTo.set(Calendar.HOUR_OF_DAY, 23);
        cTo.set(Calendar.MINUTE, 59);
        cTo.set(Calendar.SECOND, 59);
        cTo.set(Calendar.MILLISECOND, cTo.getActualMaximum(Calendar.MILLISECOND));
        to.setTime(cTo.getTimeInMillis());
        return to;
    }



    public static Date getFromDate(int month, int year) {
        Date from = new Date();
        if (month == 12) {
            Calendar cFrom = new GregorianCalendar();
            cFrom.set(year, 0, 1, 0, 0, 0);
            cFrom.set(Calendar.MILLISECOND, 0);
            from.setTime(cFrom.getTimeInMillis());
        } else {
            Calendar cFrom = new GregorianCalendar();
            cFrom.set(year, month, 1, 0, 0, 0);
            cFrom.set(Calendar.MILLISECOND, 0);
            from.setTime(cFrom.getTimeInMillis());
        }
        return from;
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
