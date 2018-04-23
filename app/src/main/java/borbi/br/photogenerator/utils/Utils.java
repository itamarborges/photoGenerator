package borbi.br.photogenerator.utils;

import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Gabriela on 07/04/2016.
 */
public class Utils {

    private static final String CLASS_NAME = Utils.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STRING, INT, FLOAT})
    public @interface DataType {}

    public static final int STRING = 0;
    public static final int INT = 1;
    public static final int
            FLOAT = 2;

    public static void positioningCursorInTheEnd(EditText et) {
        et.setSelection(et.getText().length());
    }

    public static long getTimeDifferenteInSeconds(long lastTime){
        long diff = (new Date()).getTime() - lastTime;
        return diff/1000;
    }

    public static String getFormattedDateMonth(Long dateInMillis ) {
        Date date = new Date(dateInMillis);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("dd/MM");
        return monthDayFormat.format(date);
    }

    public static String getFormattedDateMonth(Date date) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("dd/MM");
        return monthDayFormat.format(date);
    }

    public static int getDifferenceInDays(Date dateBegin, Date dateEnd){
        Long difference = dateEnd.getTime() - dateBegin.getTime();
        difference = (difference/1000)/60/60/24;
        return difference.intValue();
    }

    public static Date getDate(int year, int monthOfYear, int dayOfMonth){
        return (new GregorianCalendar(year,monthOfYear,dayOfMonth,0,0,0)).getTime();
    }

    /*
    Formata a data marcada no calendario de acordo com o formato marcado como padrao no aparelho.
     */
    public static String formatDate(int year, int monthOfYear, int dayOfMonth, DateFormat dateFormat) {
        Calendar data = new GregorianCalendar(year,monthOfYear,dayOfMonth);
        return dateFormat.format(data.getTime());
    }

    public static Date setDateToFinalHours(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
        return calendar.getTime();
    }

    public static Date setDateToInitialHours(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /*
    Returns a date <code>numberOfDays</code> from today.
     */
    public static Date getDateDaysFromToday(int numberOfDays){
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
        return calendar.getTime();
    }

    public static boolean isEmptyField(View view) throws Exception {
        boolean mEmptyField = false;

        if (view == null ) {
            throw new Exception("View is null");
        }
        if (view instanceof EditText) {
            if (TextUtils.isEmpty(((EditText) view).getText())) {
               return true;
            }
        }

        return mEmptyField;
    }

    public static boolean isEmptyString(String s) {
        return ((s == null ) || (s.equals("")));
    }

}
