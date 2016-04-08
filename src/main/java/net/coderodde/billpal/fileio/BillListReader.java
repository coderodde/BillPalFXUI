package net.coderodde.billpal.fileio;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import net.coderodde.billpal.Bill;

/**
 * This class is responsible for reading the list of bills from 
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 22, 2016)
 */
public class BillListReader {
    
    private final Scanner scanner;
    
    public BillListReader(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "The input stream is null.");
        this.scanner = new Scanner(inputStream);
    }
    
    public List<Bill> read() {
        List<Bill> billList = new ArrayList<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            String[] tokens = line.split("\\s*,\\s*");
            Bill bill = parseBill(tokens);
            
            if (bill != null) {
                billList.add(bill);
            }
        }
        
        scanner.close();
        return billList;
    }
    
    private Bill parseBill(String[] tokens) {
        Bill bill = new Bill();
        
        if (tokens.length >= 1) {
            bill.setAmount(parseAmount(tokens[0]));
        } 
        
        if (tokens.length >= 2) {
            bill.setDateReceived(parseDate(tokens[1]));
        }
        
        if (tokens.length >= 3) {
            bill.setExpirationDate(parseDate(tokens[2]));
        }
        
        if (tokens.length >= 4) {
            bill.setPaymentDate(parseDate(tokens[3]));
        }
        
        if (tokens.length >= 5) {
            bill.setReceiver(tokens[4]);
        }
        
        if (tokens.length >= 6) {
            bill.setReceiverIban(tokens[5]);
        }
        
        if (tokens.length >= 7) {
            bill.setReferenceNumber(tokens[6]);
        }
        
        if (tokens.length >= 8) {
            bill.setBillNumber(tokens[7]);
        }
        
        if (tokens.length >= 9) {
            bill.setBillNumber(tokens[8]);
        }
        
        return bill;
    }
    
    private double parseAmount(String amountString) {
        try {
            return Double.parseDouble(amountString);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }
    
    // dateString must be a number of milliseconds since Unix epoch.
    private Date parseDate(String dateString) {
        try {
            long milliseconds = Long.parseLong(dateString);
            return new Date(milliseconds);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
