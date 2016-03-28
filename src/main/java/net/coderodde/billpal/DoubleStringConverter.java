package net.coderodde.billpal;

import javafx.util.StringConverter;

/**
 * This class implements a converter for converting between double values and
 * strings.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 28, 2016)
 */
public class DoubleStringConverter extends StringConverter<Double> {

    @Override
    public String toString(Double object) {
        if (object == null) {
            return "0.0";
        }
        
        return object.toString();
    }

    @Override
    public Double fromString(String string) {
        try {
            string = string.replace(',', '.');
            return Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
