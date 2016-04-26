package net.coderodde.billpal;

import java.util.Date;
import java.util.Objects;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class implements the data type for representing bills.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 22, 2016)
 */
public class Bill {
    
    private final DoubleProperty amount          = new SimpleDoubleProperty();
    
    private final ObjectProperty<Date> dateReceived 
      = new SimpleObjectProperty<>();
    
    private final ObjectProperty<Date> expirationDate 
      = new SimpleObjectProperty<>();
    
    private final ObjectProperty<Date> paymentDate
      = new SimpleObjectProperty<>();
    
    private final StringProperty receiver        = new SimpleStringProperty();
    private final StringProperty receiverIban    = new SimpleStringProperty();
    private final StringProperty referenceNumber = new SimpleStringProperty();
    private final StringProperty billNumber      = new SimpleStringProperty();
    private final StringProperty comment         = new SimpleStringProperty();
    
    public Bill() {
        this(0.0, null, null, null, "", "", "", "", "");
    }
    
    public Bill(Double amount,
                Date dateReceived,
                Date expirationDate,
                Date paymentDate,
                String receiver,
                String receiverIban,
                String referenceNumber,
                String billNumber,
                String comment) {
        setAmount(amount);
        setDateReceived(dateReceived);
        setExpirationDate(expirationDate);
        
        setPaymentDate(paymentDate);
        setReceiver(receiver);
        setReceiverIban(receiverIban);
        
        setReferenceNumber(referenceNumber);
        setBillNumber(billNumber);
        setComment(comment);
    }
    
    public Bill(Bill copy) {
        Objects.requireNonNull(copy, "The bill to copy is null.");
        set(copy);
    }
    
    public Double getAmount() {
        return amount.get();
    }
    
    public void setAmount(Double amount) {
        this.amount.set(amount);
    }
    
    public Date getDateReceived() {
        return dateReceived.get();
    }
    
    public void setDateReceived(Date dateReceived) {
        this.dateReceived.set(dateReceived);
    }
    
    public Date getExpirationDate() {
        return expirationDate.get();
    }
    
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate.set(expirationDate);
    }
    
    public Date getPaymentDate() {
        return paymentDate.get();
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate.set(paymentDate);
    }
    
    public String getReceiver() {
        return receiver.get();
    }
    
    public void setReceiver(String receiver) {
        this.receiver.set(receiver);
    }
    
    public String getReceiverIban() {
        return receiverIban.get();
    }
    
    public void setReceiverIban(String receiverIban) {
        this.receiverIban.set(receiverIban);
    }
    
    public String getReferenceNumber() {
        return referenceNumber.get();
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber.set(referenceNumber);
    }
    
    public String getBillNumber() {
        return billNumber.get();
    }
    
    public void setBillNumber(String billNumber) {
        this.billNumber.set(billNumber);
    }
    
    public String getComment() {
        return comment.get();
    }
    
    public void setComment(String comment) {
        this.comment.set(comment);
    }
    
    public DoubleProperty amountProperty() {
        return amount;
    }
    
    public ObjectProperty<Date> dateReceivedProperty() {
        return dateReceived;
    }
    
    public ObjectProperty<Date> expirationDateProperty() {
        return expirationDate;
    }
    
    public ObjectProperty<Date> paymentDateProperty() {
        return paymentDate;
    }
    
    public StringProperty receiverProperty() {
        return receiver;
    }
    
    public StringProperty receiverIbanProperty() {
        return receiverIban;
    }
    
    public StringProperty referenceNumberProperty() {
        return referenceNumber;
    }
    
    public StringProperty billNumberProperty() {
        return billNumber;
    }
    
    public StringProperty commentProperty() {
        return comment;
    }
    
    public void set(Bill bill) {
        setAmount         (bill.getAmount());
        setDateReceived   (bill.getDateReceived());
        setExpirationDate (bill.getExpirationDate());
        setPaymentDate    (bill.getPaymentDate());
        setReceiver       (bill.getReceiver());
        setReceiverIban   (bill.getReceiverIban());
        setReferenceNumber(bill.getReferenceNumber());
        setBillNumber     (bill.getBillNumber());
        setComment        (bill.getComment());
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        if (!getClass().equals(o.getClass())) {
            return false;
        }
        
        final Bill other = (Bill) o;
        
        return 
              Objects.equals(getAmount(),          other.getAmount())
           && Objects.equals(getBillNumber(),      other.getBillNumber())
           && Objects.equals(getComment(),         other.getComment())
           && Objects.equals(getDateReceived(),    other.getDateReceived())
           && Objects.equals(getExpirationDate(),  other.getExpirationDate())
           && Objects.equals(getPaymentDate(),     other.getPaymentDate())
           && Objects.equals(getReceiver(),        other.getReceiver())
           && Objects.equals(getReceiverIban(),    other.getReceiverIban())
           && Objects.equals(getReferenceNumber(), other.getReferenceNumber());
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
