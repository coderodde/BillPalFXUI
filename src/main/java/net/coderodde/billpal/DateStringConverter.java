package net.coderodde.billpal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
    private boolean formatGotIt;
    
    @Override
    public String toString(Date object) {
        if (object == null) {
            return "";
        }
        
        return DATE_FORMAT.format(object);
    }

    @Override
    public Date fromString(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        
        try {
            return DATE_FORMAT.parse(string);
        } catch (ParseException ex) {
            if (!formatGotIt) {
                ButtonType buttonGotItType =
                        new ButtonType("Got it!", ButtonBar.ButtonData.YES);

                ButtonType buttonDidNotGetItType = 
                        new ButtonType("Did not get it",
                                       ButtonBar.ButtonData.NO);

                Alert alert = 
                        new Alert(AlertType.WARNING, 
                            "The format for dates is year.month.day. " +
                            "For example, today is " + todayToString() + ".",
                             buttonDidNotGetItType,
                             buttonGotItType);
                
                alert.setTitle("Date format warning");
                Optional<ButtonType> result = alert.showAndWait();
                
                if (result.get() == buttonGotItType) {
                    formatGotIt = true;
                }
            }
            
            return null;
        }
    }
    
    private String todayToString() {
        Calendar cal = Calendar.getInstance();
        return new StringBuilder(20)
                .append(cal.get(Calendar.YEAR)).append(".")
                .append(cal.get(Calendar.MONTH) + 1).append(".")
                .append(cal.get(Calendar.DAY_OF_MONTH)).toString();
    }
}
