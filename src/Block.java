import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot; // It is a hash of all transactions in the block.
    public ArrayList<Transaction> transactions = new ArrayList<>(); // Our data will be a simple message.

    private long timeStamp; // As number of milliseconds since 1/1/1970.

    private int nonce;

    // Block Constructor.
    public Block(String previousHash) {

        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySha256(
                previousHash + timeStamp + nonce + merkleRoot
        );
    }

    // Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
            if (nonce % 10000 == 0) {
                System.out.println("Hash: " + hash);
            }
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    // Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((previousHash != "0")) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}