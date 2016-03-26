package net.coderodde.billpal;

import java.util.Date;

/**
 * This class implements the data type for representing bills.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 22, 2016)
 */
public class Bill {
    
    private double amount;
    private Date dateReceived;
    private Date expirationDate;
    private Date paymentDate;
    private String receiverIban;
    private String referenceNumber;
    private String billNumber;
    private String comment;
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public Date getDateReceived() {
        return dateReceived;
    }
    
    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }
    
    public Date getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getReceiverIban() {
        return receiverIban;
    }
    
    public void setReceiverIban(String receiverIban) {
        this.receiverIban = receiverIban;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public String getBillNumber() {
        return billNumber;
    }
    
    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("[Bill: amount = ")
                                  .append(amount)
                                  .append(", date received = \"")
                                  .append(dateReceived)
                                  .append("\", expiration date = \"")
                                  .append(expirationDate)
                                  .append("\", payment date = \"")
                                  .append(paymentDate)
                                  .append("\", receiver IBAN = \"")
                                  .append(receiverIban)
                                  .append("\", reference number = \"")
                                  .append(referenceNumber)
                                  .append("\", bill number = \"")
                                  .append(billNumber)
                                  .append("\", comment = \"")
                                  .append(comment)
                                  .append("\"]")
                                  .toString();
    }
}
