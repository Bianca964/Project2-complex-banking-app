package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.poo.accounts.Account;
import org.poo.serviceplans.ServicePlan;

import java.util.List;

@Getter
public final class Transaction {
    // createAccount + common for all
    private final String description;
    private final int timestamp;

    // sendMoney
    private final Account fromAccount;
    private final Account toAccount;
    private final String toAccountCommerciant;
    private final double amountSender;
    private final double amountReceiver;
    private final String transferType;

    // createCard + deleteCard + createOneTimeCard
    private final String cardNumber;
    private final String cardHolderEmail;
    private final String accountIBAN;

    // payOnline
    private final String commerciant;
    private final double amountPayOnline;

    // splitTransactionEqual
    private final String currency;
    private final double amountSplitted;
    private final List<String> involvedAccounts;
    private final String error;

    // splitTransactionCustom
    private final String splitPaymentType;
    private final List<Double> amountsSplitTransaction;

    // upgradePlan
    private final String newPlanType;
    private final String accountIbanUpgradePlan;

    // cashWithdrawal
    private final double amountCashWithdrawal;

    // addInterest
    private final double amountInterest;
    private final String currencyAddInterest;

    // withdraw savings
    private final String classicAccountIban;
    private final String savingsAccountIban;
    private final double amountWithdrawn;

    // private constructor for forcing the use of the Builder
    private Transaction(final TransactionBuilder builder) {
        this.description = builder.description;
        this.fromAccount = builder.fromAccount;
        this.toAccount = builder.toAccount;
        this.amountSender = builder.amountSender;
        this.amountReceiver = builder.amountReceiver;
        this.transferType = builder.transferType;
        this.timestamp = builder.timestamp;
        this.cardNumber = builder.cardNumber;
        this.cardHolderEmail = builder.cardHolderEmail;
        this.accountIBAN = builder.accountIBAN;
        this.commerciant = builder.commerciant;
        this.amountPayOnline = builder.amountPayOnline;
        this.currency = builder.currency;
        this.amountSplitted = builder.amountSplitted;
        this.involvedAccounts = builder.involvedAccounts;
        this.error = builder.error;
        this.newPlanType = builder.newPlanType;
        this.accountIbanUpgradePlan = builder.accountIbanUpgradePlan;
        this.amountCashWithdrawal = builder.amountCashWithdrawal;
        this.amountInterest = builder.amountInterest;
        this.currencyAddInterest = builder.currencyAddInterest;
        this.splitPaymentType = builder.splitPaymentType;
        this.amountsSplitTransaction = builder.amountsSplitTransaction;
        this.classicAccountIban = builder.classicAccountIban;
        this.savingsAccountIban = builder.savingsAccountIban;
        this.amountWithdrawn = builder.amountWithdrawn;
        this.toAccountCommerciant = builder.toAccountCommerciant;
    }

    public static final class TransactionBuilder {
        private String description;
        private Account fromAccount;
        private Account toAccount;
        private double amountSender;
        private double amountReceiver;
        private int timestamp;
        private String cardNumber;
        private String cardHolderEmail;
        private String accountIBAN;
        private String commerciant;
        private double amountPayOnline;
        private String currency;
        private double amountSplitted;
        private List<String> involvedAccounts;
        private String error;
        private String transferType;
        private String newPlanType;
        private String accountIbanUpgradePlan;
        private double amountCashWithdrawal;
        private double amountInterest;
        private String currencyAddInterest;
        private String splitPaymentType;
        private List<Double> amountsSplitTransaction;
        private String classicAccountIban;
        private String savingsAccountIban;
        private double amountWithdrawn;
        private String toAccountCommerciant;

        /**
         * @param argToAccountCommerciant the IBAN of the account for the sendMoney command
         */
        public TransactionBuilder setToAccountCommerciant(final String argToAccountCommerciant) {
            this.toAccountCommerciant = argToAccountCommerciant;
            return this;
        }

        /**
         * @param argClassicAccountIban the IBAN of the classic account for the savings withdrawal
         */
        public TransactionBuilder setClassicAccountIban(final String argClassicAccountIban) {
            this.classicAccountIban = argClassicAccountIban;
            return this;
        }

        /**
         * @param argSavingsAccountIban the IBAN of the savings account for the savings withdrawal
         */
        public TransactionBuilder setSavingsAccountIban(final String argSavingsAccountIban) {
            this.savingsAccountIban = argSavingsAccountIban;
            return this;
        }

        /**
         * @param argAmountWithdrawn the amount of money withdrawn in the withdrawSavings command
         */
        public TransactionBuilder setAmountWithdrawn(final double argAmountWithdrawn) {
            this.amountWithdrawn = argAmountWithdrawn;
            return this;
        }

        /**
         * @param argSplitPaymentType the type of the splitTransaction command
         */
        public TransactionBuilder setSplitPaymentType(final String argSplitPaymentType) {
            this.splitPaymentType = argSplitPaymentType;
            return this;
        }

        /**
         * @param argAmounts the list of amounts for the splitTransaction command
         */
        public TransactionBuilder setAmountsSplitTransaction(final List<Double> argAmounts) {
            this.amountsSplitTransaction = argAmounts;
            return this;
        }

        /**
         * @param argAmountInterest the amount of interest added to the savings account
         */
        public TransactionBuilder setAmountInterest(final double argAmountInterest) {
            this.amountInterest = argAmountInterest;
            return this;
        }

        /**
         * @param argCurrencyAddInterest the currency of the addInterest command
         */
        public TransactionBuilder setCurrencyAddInterest(final String argCurrencyAddInterest) {
            this.currencyAddInterest = argCurrencyAddInterest;
            return this;
        }

        /**
         * @param argAmountCashWithdrawal the amount of money withdrawn in the cashWithdrawal
         */
        public TransactionBuilder setAmountCashWithdrawal(final double argAmountCashWithdrawal) {
            this.amountCashWithdrawal = argAmountCashWithdrawal;
            return this;
        }

        /**
         * @param argIbanUpgradePlan the IBAN of the account for the upgradePlan command
         */
        public TransactionBuilder setAccountIbanUpgradePlan(final String argIbanUpgradePlan) {
            this.accountIbanUpgradePlan = argIbanUpgradePlan;
            return this;
        }

        /**
         * @param argNewPlanType the new plan type for the upgradePlan command
         */
        public TransactionBuilder setNewPlanType(final String argNewPlanType) {
            this.newPlanType = argNewPlanType;
            return this;
        }

        /**
         * @param argTransferType the type of the transaction ("sent" or "received")
         */
        public TransactionBuilder setTransferType(final String argTransferType) {
            this.transferType = argTransferType;
            return this;
        }

        /**
         * @param argError the error message for the splitTransaction command
         */
        public TransactionBuilder setError(final String argError) {
            this.error = argError;
            return this;
        }

        /**
         * @param argCurrency the currency of the splitTransaction command
         */
        public TransactionBuilder setCurrency(final String argCurrency) {
            this.currency = argCurrency;
            return this;
        }

        /**
         * @param argAmountSplitted the amount of each contributor in the splitTransaction command
         */
        public TransactionBuilder setAmountSplitted(final double argAmountSplitted) {
            this.amountSplitted = argAmountSplitted;
            return this;
        }

        /**
         * @param argInvolvedAccounts the list of accounts involved in the splitTransaction command
         */
        public TransactionBuilder setInvolvedAccounts(final List<String> argInvolvedAccounts) {
            this.involvedAccounts = argInvolvedAccounts;
            return this;
        }

        /**
         * @param argCardNumber the card number for the createCard, deleteCard and createOneTimeCard
         *                      commands
         */
        public TransactionBuilder setCardNumber(final String argCardNumber) {
            this.cardNumber = argCardNumber;
            return this;
        }

        /**
         * @param argCardHolderEmail the user's email for the createCard, deleteCard and
         *                           createOneTimeCard commands
         */
        public TransactionBuilder setCardHolderEmail(final String argCardHolderEmail) {
            this.cardHolderEmail = argCardHolderEmail;
            return this;
        }

        /**
         * @param argAccountIBAN the IBAN of the account for the createCard, deleteCard and
         *                       createOneTimeCard commands
         */
        public TransactionBuilder setAccountIBAN(final String argAccountIBAN) {
            this.accountIBAN = argAccountIBAN;
            return this;
        }

        /**
         * @param argCommerciant the name of the merchant for the payOnline command
         */
        public TransactionBuilder setCommerciant(final String argCommerciant) {
            this.commerciant = argCommerciant;
            return this;
        }

        /**
         * @param argAmountPayOnline the total amount of money for the payOnline command
         */
        public TransactionBuilder setAmountPayOnline(final double argAmountPayOnline) {
            this.amountPayOnline = argAmountPayOnline;
            return this;
        }

        /**
         * @param argDescription the description of the transaction
         */
        public TransactionBuilder setDescription(final String argDescription) {
            this.description = argDescription;
            return this;
        }

        /**
         * @param argFromAccount the account from which the money is sent in sendMoney command
         */
        public TransactionBuilder setFromAccount(final Account argFromAccount) {
            this.fromAccount = argFromAccount;
            return this;
        }

        /**
         * @param argToAccount the account to which the money is sent in sendMoney command
         */
        public TransactionBuilder setToAccount(final Account argToAccount) {
            this.toAccount = argToAccount;
            return this;
        }

        /**
         * @param argAmountSender the amount of money sent in sendMoney command (converted to
         *                        the sender's currency)
         */
        public TransactionBuilder setAmountSender(final double argAmountSender) {
            this.amountSender = argAmountSender;
            return this;
        }

        /**
         * @param argAmountReceiver the amount of money received in sendMoney command (converted
         *                          to the receiver's currency)
         */
        public TransactionBuilder setAmountReceiver(final double argAmountReceiver) {
            this.amountReceiver = argAmountReceiver;
            return this;
        }

        /**
         * @param argTimestamp the timestamp of the transaction
         */
        public TransactionBuilder setTimestamp(final int argTimestamp) {
            this.timestamp = argTimestamp;
            return this;
        }

        /**
         * @return the Transaction object
         */
        public Transaction build() {
            return new Transaction(this);
        }
    }

    /**
     * Executes the sendMoney command
     * @throws Exception if the sender does not have enough money in the account
     */
    public void doTransactionSendMoney(final ServicePlan servicePlan) throws Exception {
        if (amountSender <= 0 || fromAccount == null || toAccount == null) {
            return;
        }

        // apply comission
        double amountSenderWithComission = servicePlan.applyCommission(amountSender,
                                                                       fromAccount.getCurrency());

        if (fromAccount.getBalance() < amountSenderWithComission) {
            throw new Exception("Insufficient funds");
        }

        fromAccount.withdraw(amountSenderWithComission);
        toAccount.deposit(amountReceiver);
    }

    /**
     * Transforms the transaction into an ObjectNode
     * @param mapper the ObjectMapper used to create the ObjectNode
     * @return the ObjectNode
     */
    public ObjectNode transformToAnObjectNode(final ObjectMapper mapper) {
        ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("timestamp", timestamp);
        objectNode.put("description", description);

        // sendMoney
        if (fromAccount != null) {
            objectNode.put("senderIBAN", fromAccount.getIban());
        }
        if (toAccount != null) {
            objectNode.put("receiverIBAN", toAccount.getIban());
        }
        if (toAccountCommerciant != null) {
            objectNode.put("receiverIBAN", toAccountCommerciant);
        }
        if (transferType != null) {
            objectNode.put("transferType", transferType);
        }
        if (transferType != null && transferType.equals("sent")) {
            if (amountSender > 0 && fromAccount != null) {
                String amountString = String.format(amountSender + " " + fromAccount.getCurrency());
                objectNode.put("amount", amountString);
            }
        } else if (transferType != null && transferType.equals("received")) {
            if (amountReceiver > 0 && toAccount != null) {
                String amountString = String.format(amountReceiver + " " + toAccount.getCurrency());
                objectNode.put("amount", amountString);
            }
        }

        // createCard + deleteCard + createOneTimeCard
        if (cardNumber != null) {
            objectNode.put("card", cardNumber);
        }
        if (cardHolderEmail != null) {
            objectNode.put("cardHolder", cardHolderEmail);
        }
        if (accountIBAN != null) {
            objectNode.put("account", accountIBAN);
        }

        // payOnline
        if (amountPayOnline > 0) {
            objectNode.put("amount", amountPayOnline);
        }
        if (commerciant != null) {
            objectNode.put("commerciant", commerciant);
        }

        // splitTransaction
        if (currency != null) {
            objectNode.put("currency", currency);
        }
        if (amountSplitted > 0) {
            objectNode.put("amount", amountSplitted);
        }
        if (error != null) {
            objectNode.put("error", error);
        }
        if (involvedAccounts != null && !involvedAccounts.isEmpty()) {
            ArrayNode accountsArray = mapper.createArrayNode();
            for (String account : involvedAccounts) {
                accountsArray.add(account);
            }
            objectNode.set("involvedAccounts", accountsArray);
        }

        // upgradePlan
        if (newPlanType != null) {
            objectNode.put("newPlanType", newPlanType);
        }

        if (accountIbanUpgradePlan != null) {
            objectNode.put("accountIBAN", accountIbanUpgradePlan);
        }

        // cashWithdrawal
        if (amountCashWithdrawal > 0) {
            objectNode.put("amount", amountCashWithdrawal);
        }

        // addInterest
        if (amountInterest > 0) {
            objectNode.put("amount", amountInterest);
        }
        if (currencyAddInterest != null) {
            objectNode.put("currency", currencyAddInterest);
        }

        // splitTransactionCustom
        if (splitPaymentType != null) {
            objectNode.put("splitPaymentType", splitPaymentType);
        }

        if (amountsSplitTransaction != null && !amountsSplitTransaction.isEmpty()) {
            ArrayNode amountsArray = mapper.createArrayNode();
            for (Double amount : amountsSplitTransaction) {
                amountsArray.add(amount);
            }
            objectNode.set("amountForUsers", amountsArray);
        }

        // withdraw savings
        if (classicAccountIban != null) {
            objectNode.put("classicAccountIBAN", classicAccountIban);
        }

        if (savingsAccountIban != null) {
            objectNode.put("savingsAccountIBAN", savingsAccountIban);
        }

        if (amountWithdrawn > 0) {
            objectNode.put("amount", amountWithdrawn);
        }

        return objectNode;
    }
}
