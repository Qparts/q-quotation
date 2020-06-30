package q.rest.quotation.helper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class Helper {

    public List<Date> getAllDatesBetween(Date from, Date to){
        from = new Date(from.getTime() - (1000*60*60*24));
        to = new Date(to.getTime() + (1000*60*60*24));
        LocalDate fromLocal = convertToLocalDate(from);
        LocalDate toLocal = convertToLocalDate(to);

        List<LocalDate> localDates = fromLocal.datesUntil(toLocal)
                .collect(Collectors.toList());

        List<Date> dates = new ArrayList<>();
        for(LocalDate ld : localDates){
            dates.add(convertToDate(ld));
        }
        return dates;
    }

    public static Date addDays(Date original, long days) {
        return new Date(original.getTime() + (1000L * 60 * 60 * 24 * days));
    }

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
       YearMonth ym = YearMonth.of(year,month);
       LocalDate to = ym.atEndOfMonth();
       return convertToDate(to);
    }

    public static Date getFromDate(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        return convertToDate(from);
    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(
                dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    public static Date convertToDate(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
