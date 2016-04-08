package net.coderodde.billpal.fileio;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import net.coderodde.billpal.Bill;

/**
 * This class is responsible for writing the list of bills into a text file.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 22, 2016)
 */
public class BillListWriter {
    
    private static final String COMMA = ", ";
    
    private final PrintWriter printWriter;
    
    public BillListWriter(OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "The output stream is null.");
        this.printWriter = new PrintWriter(outputStream);
    }
    
    public void write(List<Bill> billList) {
        billList.stream().forEach((bill) -> {
            printWriter.println(convertBillToCSV(bill));
        });
        
        printWriter.close();
    }
    
    private String filter(Date date) {
        return date == null ? "" : Long.toString(date.getTime());
    }
    
    private String convertBillToCSV(Bill bill) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(bill.getAmount())                .append(COMMA);
        sb.append(filter(bill.getDateReceived()))  .append(COMMA);
        sb.append(filter(bill.getExpirationDate())).append(COMMA);
        sb.append(filter(bill.getPaymentDate()))   .append(COMMA);
        sb.append(bill.getReceiver())              .append(COMMA);
        sb.append(bill.getReceiverIban())          .append(COMMA);
        sb.append(bill.getReferenceNumber())       .append(COMMA);
        sb.append(bill.getBillNumber())            .append(COMMA);
        sb.append(bill.getComment());
        
        return sb.toString();
    }
}
