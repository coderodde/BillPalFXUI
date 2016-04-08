package net.coderodde.billpal;

import java.util.Calendar;
import java.util.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

class ExpirationDateCellFactory implements Callback<TableColumn<Bill, Date>, 
                                           TableCell<Bill, Date>> {
    
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;

    @Override
    public TableCell<Bill, Date> call(TableColumn<Bill, Date> column) {
        TextFieldTableCell<Bill, Date> cell = 
                new TextFieldTableCell<Bill, Date>(new DateStringConverter()) {

                    @Override
                    public void updateItem(Date date, boolean empty) {
                        super.updateItem(date, empty);
                        Bill bill = (Bill) this.getTableRow().getItem();

                        if (bill == null) {
                            return;
                        }

                        Date expirationDate = bill.getExpirationDate();

                        if (expirationDate == null) {
                            this.setStyle("");
                            return;
                        }

                        Date paymentDate = bill.getPaymentDate();

                        if (paymentDate == null) {
                            long now = new Date().getTime();
                            long expirationMoment = expirationDate.getTime();
                            long millisecondsLeft = expirationMoment - now;

                            String cellStyle = getCellStyle(millisecondsLeft);
                            this.setStyle(cellStyle);
                        } else {
                            // paymentDate not null here.
                            Calendar cPayment = Calendar.getInstance();
                            Calendar cExpiration = Calendar.getInstance();
                            cPayment.setTime(paymentDate);
                            cExpiration.setTime(expirationDate);

                            int paymentYear = cPayment.get(Calendar.YEAR);
                            int expirationYear = cExpiration.get(Calendar.YEAR);

                            if (paymentYear > expirationYear) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                                return;
                            } else if (paymentYear < expirationYear) {
                                this.setStyle("-fx-background-color: #0be633");
                                return;
                            }

                            int paymentMonth = cPayment.get(Calendar.MONTH);
                            int expirationMonth
                            = cExpiration.get(Calendar.MONTH);

                            if (paymentMonth > expirationMonth) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                                return;
                            } else if (paymentMonth < expirationMonth) {
                                this.setStyle("-fx-background-color: #0be633");
                                return;
                            }

                            int paymentDay
                            = cPayment.get(Calendar.DAY_OF_MONTH);
                            int expirationDay
                            = cExpiration.get(Calendar.DAY_OF_MONTH);

                            if (paymentDay > expirationDay) {
                                this.setStyle("-fx-background-color: #2db6e3;");
                            } else {
                                this.setStyle("-fx-background-color: #0be633");
                            }
                        }
                    }
                };

        return cell;
    }
    
    private String getCellStyle(long millisecondsLeft) {
        if (millisecondsLeft <= 0L) {
            return "-fx-background-color: red; -fx-text-fill: black; " +
                   "-fx-font-weight: bold;";
            
        }
        
        float f = (1.0f * millisecondsLeft) / (MILLISECONDS_PER_WEEK);
        
        if (f >= 1.0f) {
            // "" means clear the table cell style, thus using default colors.
            return "";
        }
        
        int r = 255;
        int g = (int)(255 * f);
        int b = (int)(255 * f);
        
        StringBuilder sb = new StringBuilder("-fx-background-color: #");
        sb.append(Integer.toHexString(r));
        sb.append(handleLeadingZero(Integer.toHexString(g)));
        sb.append(handleLeadingZero(Integer.toHexString(b)));
        sb.append("; -fx-text-fill: black; -fx-font-weight: bold;");
        return sb.toString(); 
    }
    
    private String handleLeadingZero(String s) {
        switch (s.length()) {
            case 1:
                return "0" + s;
                
            case 2:
                return s;
                
            default:
                throw new IllegalStateException(
                        "Should not get here. Please debug.");
        }
    }
}
