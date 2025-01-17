package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class TransactionHistory {
    private ArrayList<Transaction> transactions;

    public TransactionHistory() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Adds a transaction to the account's list of transactions (ordered by timestamp)
     * @param transaction the transaction to be added to the account's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        int index = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTimestamp() > transaction.getTimestamp()) {
                index = i;
                break;
            }
            index = i + 1; // Dacă ajunge la final, se inserează la sfârșit
        }
        transactions.add(index, transaction); // Inserăm tranzacția la poziția calculat
    }



    /**
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ArrayNode representing the user's transactions
     */
    public ArrayNode transactionsTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode transactionsArray = objectMapper.createArrayNode();
        if (!transactions.isEmpty()) {
            for (Transaction transaction : transactions) {
                transactionsArray.add(transaction.transformToAnObjectNode(objectMapper));
            }
        }
        return transactionsArray;
    }
}
