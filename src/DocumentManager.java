
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * The bridge between Java's Document and Operations.
 * 
 * It turns document events into Operations.
 * 
 * @author fazo
 */
public class DocumentManager implements DocumentListener {
    
    private Operation latest = null;
    private Document d;
    
    @Override
    public void insertUpdate(DocumentEvent de) {
        if(de.getType() == DocumentEvent.EventType.INSERT){
            String what = "";
            try {
                what = de.getDocument().getText(de.getOffset(), de.getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            latest = new Operation(de.getOffset(), what, latest);
        } else if(de.getType() == DocumentEvent.EventType.REMOVE){
            latest = new Operation(de.getOffset(), de.getLength()+de.getOffset(), latest);
        }
        System.out.println(latest.evaluate());
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if(de.getType() == DocumentEvent.EventType.REMOVE){
            latest = new Operation(de.getOffset(), de.getLength()+de.getOffset(), latest);
        }
        System.out.println(latest.evaluate());
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // Java's PlainDocument doesn't emit changedUpdate events
    }
    
    public DocumentManager(Document d){
        this.d = d;
        d.addDocumentListener(this);
    }
    
    public void apply(Operation o){
        // Todo implement this
        // make sure user doesn't touch document while operations are being applied
    }
    
    public Operation getLatest(){
        return latest;
    }
    
}
