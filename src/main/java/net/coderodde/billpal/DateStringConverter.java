package net.coderodde.billpal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.util.StringConverter;

/**
 * This class is responsible for converting between double values and dates.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 28, 2016)
 */
public class DateStringConverter extends StringConverter<Date> {

    private static final String DATE_FORMAT_STRING = "yyyy.MM.dd";
    private static final SimpleDateFormat DATE_FORMAT =
                     new SimpleDateFormat(DATE_FORMAT_STRING);
    
    @Override
    public String toString(Date object) {
        if (object == null) {
            return "";
        }
        
        return DATE_FORMAT.format(object);
    }

    @Override
    public Date fromString(String string) {
        try {
            return DATE_FORMAT.parse(string);
        } catch (ParseException ex) {
            return new Date();
        }
    }
    
}
