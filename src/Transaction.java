import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; // this is also the hash of the transaction.
    public PublicKey sender; // Senders address/public key.
    public PublicKey recipient; // Recipients address/public key.
    public float value;
    public byte[] signature; // This is to prevent anybody else from spending funds in our wallet.

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated.

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // This Calculates the transaction hash (which will be used as its ID)
    private String calculateHash() {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        value + sequence
        );
    }

    // Signs all the data we don't wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        signature = StringUtil.applyECDSASig(privateKey,data);
    }

    // Verifies the data we signed hasn't been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    // Returns true if new transaction could be created.
    public boolean processTransaction() {

        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = Main.UTXOs.get(i.transactionOutputId);
        }

        // Check if transaction is valid:
        if(getInputsValue() < Main.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs:
        float leftOver = getInputsValue() - value; // Get value of inputs then the leftover change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId)); // Send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); // Send the left over 'change' back to sender

        // Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            Main.UTXOs.put(o.id , o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // If Transaction can't be found skip it
            Main.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // Returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // If Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    // Returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}
