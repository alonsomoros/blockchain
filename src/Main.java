import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class Main {

    private ArrayList<Block> blocks;

    public static void main(String[] args) {
        ArrayList<Block> blockchain = new ArrayList<>();
        // Add blocks to the blockchain
        blockchain.add(new Block("First block data", "0"));
        blockchain.add(new Block("Second block data", blockchain.get(blockchain.size() - 1).hash));
        blockchain.add(new Block("Third block data", blockchain.get(blockchain.size() - 1).hash));

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainJson);
    }
}
