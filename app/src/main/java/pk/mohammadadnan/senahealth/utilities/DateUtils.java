package pk.mohammadadnan.senahealth.utilities;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String getString(long date){
        Date dateObject = new Date(date);
        SimpleDateFormat formatDate = new SimpleDateFormat("d LLL, yyyy hh:mm aa");
        return formatDate.format(dateObject);
    }

}
