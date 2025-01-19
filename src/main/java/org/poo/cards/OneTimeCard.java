package org.poo.cards;

import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.users.User;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Transaction;

import static org.poo.utils.Utils.generateCardNumber;

@Getter
@Setter
public class OneTimeCard extends Card {
    private boolean used;

    public OneTimeCard(final String cardNumber) {
        super(cardNumber);
        this.used = false;
    }

    /**
     * Resets the card number and resets the used status to false.
     */
    public void resetCardNumber() {
        this.setCardNumber(generateCardNumber());
        this.used = false;
    }

    /**
     * Handles the post payment for the OneTimeCard for the payOnline command (resets the
     * card number, creates the transactions for the destroyed card and the new card and
     * adds them to the user and account).
     * @param account the account of the user
     * @param user the user that owns the account
     * @param command the command input
     */
    @Override
    public void handlePostPayment(final Account account, final User user,
                                  final CommandInput command,
                                  final double convertedAmount) {
        // the payment transaction
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Card payment")
                .setTimestamp(command.getTimestamp())
                .setAmountPayOnline(convertedAmount)
                .setCommerciant(command.getCommerciant())
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);

        // the transaction for the destroyed card
        Transaction transactionOneTimeCard = new Transaction.TransactionBuilder()
                .setDescription("The card has been destroyed")
                .setTimestamp(command.getTimestamp())
                .setAccountIBAN(account.getIban())
                .setCardHolderEmail(user.getEmail())
                .setCardNumber(this.getCardNumber())
                .build();

        user.addTransaction(transactionOneTimeCard);
        account.addTransaction(transactionOneTimeCard);

        // reset the card number of the OneTimeCard
        this.resetCardNumber();

        // the transaction for the new card
        Transaction transactionNewOneTimeCard = new Transaction.TransactionBuilder()
                .setDescription("New card created")
                .setTimestamp(command.getTimestamp())
                .setAccountIBAN(account.getIban())
                .setCardHolderEmail(user.getEmail())
                .setCardNumber(this.getCardNumber())
                .build();

        user.addTransaction(transactionNewOneTimeCard);
        account.addTransaction(transactionNewOneTimeCard);
    }
}
