package org.poo.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.*;
import org.poo.cards.Card;
import org.poo.fileio.UserInput;
import org.poo.serviceplans.*;
import org.poo.transactions.Commerciant;
import org.poo.transactions.SplitPayment;
import org.poo.transactions.Transaction;

import java.util.ArrayList;

import static org.poo.utils.Utils.generateCardNumber;

@Getter
@Setter
public class User {
    private final UserInput userInfo;
    private ArrayList<Account> accounts;
    private ServicePlan servicePlan;
    private int min300payments;
    private final ArrayList<Transaction> transactions;
    private int nrClassicAccounts;

    // for splitPayment
    private ArrayList<SplitPayment> splitPayments;

    private ArrayList<Commerciant> commerciants;


    private boolean discountFood;
    private boolean discountClothes;
    private boolean discountTech;

    private boolean discountFoodWasUsed;
    private boolean discountClothesWasUsed;
    private boolean discountTechWasUsed;

    // for business accounts
    private double amountSpentOnBusinessAccount;
    private double amountDepositedOnBusinessAccount;
    private ArrayList<Card> cardsAddedToBusinessAccount;

//    enum DiscountType {
//        FOOD,
//        CLOTHES,
//        TECH
//    }


    public User(final UserInput userInfo, final Bank bank) {
        this.userInfo = userInfo;
        if (userInfo.getOccupation().equals("student")) {
            this.servicePlan = new StudentPlan(bank);
        } else {
            this.servicePlan = new StandardPlan(bank);
        }

        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.nrClassicAccounts = 0;
        this.min300payments = 0;

        this.discountFood = false;
        this.discountClothes = false;
        this.discountTech = false;

        this.discountFoodWasUsed = false;
        this.discountClothesWasUsed = false;
        this.discountTechWasUsed = false;

        this.splitPayments = new ArrayList<>();

        this.amountSpentOnBusinessAccount = 0;
        this.amountDepositedOnBusinessAccount = 0;
        this.cardsAddedToBusinessAccount = new ArrayList<>();

        this.commerciants = new ArrayList<>();

    }


    public void addCardToCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.add(card);
    }

    public void removeCardFromCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.remove(card);
    }



    public void increaseAmountSpentOnBusinessAccount(double amount) {
        this.amountSpentOnBusinessAccount += amount;
    }

    public void increaseAmountDepositedOnBusinessAccount(double amount) {
        this.amountDepositedOnBusinessAccount += amount;
    }

    public String getUsername() {
        return userInfo.getLastName() + " " + userInfo.getFirstName();
    }






    public void addSplitPayment(final SplitPayment splitPayment) {
        splitPayments.add(splitPayment);
    }

    public void removeSplitPayment(final SplitPayment splitPayment) {
        splitPayments.remove(splitPayment);
    }

    public SplitPayment acceptSplitPayment(final String type) {
        // accept the first split payment of type given (they are accepted in order)
        if (!splitPayments.isEmpty()) {
            SplitPayment splitPayment = getFirstSplitTransactionOfType(type);
            if (splitPayment == null) {
                return null;
            }
            splitPayment.incrementAccepts();
            return splitPayment;
        }
        return null;
    }

    public SplitPayment getFirstSplitTransactionOfType(final String type) {

        // nu verific daca e si acceptata deja de user ul acesta!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1

        for (SplitPayment splitPayment : splitPayments) {
            if (splitPayment.getType().equals(type)) {
                return splitPayment;
            }
        }
        return null;
    }









    // NrOfTransactions
    public boolean hasDiscountAvailable() {
        return this.discountFood || this.discountClothes || this.discountTech;
    }

    public void applyDiscount(Account account, Commerciant commerciant, double amountSpent) {
        if (commerciant.getType().equals("Food")) {
            applyFoodDiscount(account, amountSpent);
        } else if (commerciant.getType().equals("Clothes")) {
            applyClothesDiscount(account, amountSpent);
        } else if (commerciant.getType().equals("Tech")) {
            applyTechDiscount(account, amountSpent);
        }
    }

    public void applyFoodDiscount(Account account, double amountSpent) {
        if (this.discountFood && !this.discountFoodWasUsed) {
            account.deposit(amountSpent * 0.02);
            this.discountFoodWasUsed = true;
            this.discountFood = false;
        }
    }

    public void applyClothesDiscount(Account account, double amountSpent) {
        if (this.discountClothes && !this.discountClothesWasUsed) {
            account.deposit(amountSpent * 0.05);
            this.discountClothesWasUsed = true;
            this.discountClothes = false;
        }
    }

    public void applyTechDiscount(Account account, double amountSpent) {
        if (this.discountTech && !this.discountTechWasUsed) {
            account.deposit(amountSpent * 0.1);
            this.discountTechWasUsed = true;
            this.discountTech = false;
        }
    }

//    public void resetFoodDiscountForSpendingThresholdAfterUse() {
//        this.discountFood = true;
//        this.discountFoodWasUsed = false;
//    }
//
//    public void resetClothesDiscountForSpendingThresholdAfterUse() {
//        this.discountClothes = true;
//        this.discountClothesWasUsed = false;
//    }
//
//    public void resetTechDiscountForSpendingThresholdAfterUse() {
//        this.discountTech = true;
//        this.discountTechWasUsed = false;
//    }
//
//    public void resetDiscountsForSpendingThresholdAfterUse(Commerciant commerciant) {
//        resetFoodDiscountForSpendingThresholdAfterUse();
//        resetClothesDiscountForSpendingThresholdAfterUse();
//        resetTechDiscountForSpendingThresholdAfterUse();
//    }








    public Commerciant getCommerciant(final String name) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    public Commerciant getCommerciant(final Commerciant wantedCommerciant) {
        for (Commerciant c : commerciants) {
            if (c.getName().equals(wantedCommerciant.getName())) {
                return c;
            }
        }
        return null;
    }

    public void addCommerciant(final Commerciant commerciant) {
        commerciants.add(commerciant);
    }

    public void incrementNrOfTrnscForCommerciant(final Commerciant commerciant) {
        if (commerciant != null) {
            commerciant.incrementNrTransactions();
        }
    }






    public void setDiscountFood() {
        if (this.discountFoodWasUsed) {
            return;
        }
        this.discountFood = true;
    }

    public void setDiscountClothes() {
        if (this.discountClothesWasUsed) {
            return;
        }
        this.discountClothes = true;
    }

    public void setDiscountTech() {
        if (this.discountTechWasUsed) {
            return;
        }
        this.discountTech = true;
    }

    public void setDiscountFoodAsUsed() {
        this.discountFoodWasUsed = true;
    }

    public void setDiscountClothesAsUsed() {
        this.discountClothesWasUsed = true;
    }

    public void setDiscountTechAsUsed() {
        this.discountTechWasUsed = true;
    }

    public boolean isDiscountFoodUsed() {
        return this.discountFoodWasUsed;
    }

    public boolean isDiscountClothesUsed() {
        return this.discountClothesWasUsed;
    }

    public boolean isDiscountTechUsed() {
        return this.discountTechWasUsed;
    }






    // SPENDING THRESHOLD
    public void applySpendingThresholdDiscount(Account account, double amountSpent) {
        ServicePlan servicePlan = this.getServicePlan();
        // if the sender account is business, the owner's service plan is used for cashback
        if (account.isBusinessAccount()) {
            servicePlan = ((BusinessAccount) account).getOwner().getServicePlan();
        }

        double totalAmountForSpendingThreshold = account.getTotalAmountForSpendingThreshold();

        if (totalAmountForSpendingThreshold >= 100 && totalAmountForSpendingThreshold < 300) {
            if (servicePlan.getName().equals("student") || servicePlan.getName().equals("standard")) {
                account.deposit(amountSpent * 0.001);
            }
            if (servicePlan.getName().equals("silver")) {
                account.deposit(amountSpent * 0.003);
            }
            if (servicePlan.getName().equals("gold")) {
                account.deposit(amountSpent * 0.005);
            }
        }

        if (totalAmountForSpendingThreshold >= 300 && totalAmountForSpendingThreshold < 500) {
            if (servicePlan.getName().equals("student") || servicePlan.getName().equals("standard")) {
                account.deposit(amountSpent * 0.002);
            }
            if (servicePlan.getName().equals("silver")) {
                account.deposit(amountSpent * 0.004);
            }
            if (servicePlan.getName().equals("gold")) {
                account.deposit(amountSpent * 0.0055);
            }
        }

        if (totalAmountForSpendingThreshold >= 500) {
            if (servicePlan.getName().equals("student") || servicePlan.getName().equals("standard")) {
                account.deposit(amountSpent * 0.0025);
            }
            if (servicePlan.getName().equals("silver")) {
                account.deposit(amountSpent * 0.005);
            }
            if (servicePlan.getName().equals("gold")) {
                account.deposit(amountSpent * 0.007);
            }
        }
    }













    public void incrementNrClassicAccounts() {
        nrClassicAccounts++;
    }

    public boolean hasClassicAccount() {
        if (nrClassicAccounts >= 1) {
            return true;
        }
        return false;
    }

    public void increaseMin300payments() {
        min300payments++;
    }



    public boolean checkForUpgradeToGoldPlan(Account account, final Bank bank, final int timestamp) {
        if (min300payments >= 5 && this.servicePlan.getName().equals("silver")) {
            this.servicePlan = new GoldPlan(bank);

            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(timestamp)
                    .setDescription("Upgrade plan")
                    .setAccountIbanUpgradePlan(account.getIban())
                    .setNewPlanType("gold")
                    .build();
            this.addTransaction(transaction);
            account.addTransaction(transaction);
            return true;
        }
        return false;
    }


    public void upgradePlan(Account account, final Bank bank, final int timestamp, final String newPlanType) throws Exception {

        // if user has silver plan, make the automatic upgrade to gold plan (without fee)
//        if (min300payments >= 5 && this.servicePlan.getName().equals("silver")) {
//            this.servicePlan = new GoldPlan(bank);
//            return;
//        }
        if (checkForUpgradeToGoldPlan(account, bank, timestamp)) {
            return;
        }



        String currentPlanName = this.servicePlan.getName();
        if (newPlanType.equals(currentPlanName)) {
            //throw new Exception("You already have this plan");
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(timestamp)
                    .setDescription("The user already has the " + newPlanType + " plan.")
                    .build();
            this.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        ServicePlan newServicePlan = switch (newPlanType) {
            case "student" -> new StudentPlan(bank);
            case "standard" -> new StandardPlan(bank);
            case "gold" -> new GoldPlan(bank);
            case "silver" -> new SilverPlan(bank);
            default -> throw new Exception("Invalid plan type");
        };

        if (newServicePlan.getUpgradeLevel() < this.servicePlan.getUpgradeLevel()) {
            return;
            //throw new Exception("You cannot downgrade your plan.");
        }

        // upgrade the plan
        // NU VERIFIC DACA FAC UPGRADE DE LA STUDENT LA STANDARD SAU INVERS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        double exchangeRateFromRon = 0.0;
        try {
            exchangeRateFromRon = bank.getExchangeRate("RON", account.getCurrency());
        } catch (Exception e) {
            return;
        }
        double feeInRon = this.servicePlan.getUpgradeFee(newPlanType);
        double feeInAccountCurrency = feeInRon * exchangeRateFromRon;
        if (account.getBalance() < feeInAccountCurrency) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(timestamp)
                    .setDescription("Insufficient funds")
                    .build();
            this.addTransaction(transaction);
            return;
        }

        account.withdraw(feeInAccountCurrency);
        this.servicePlan = newServicePlan;

        // add successful upgrade to user's transactions
        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setDescription("Upgrade plan")
                .setAccountIbanUpgradePlan(account.getIban())
                .setNewPlanType(newPlanType)
                .build();
        this.addTransaction(transaction);
        account.addTransaction(transaction);

        // if the new plan is silver, reset the number of payments over 300
        if (newPlanType.equals("silver")) {
            setMin300payments(0);
        }
    }






    /**
     * @param cardNumber the card number which will be deleted
     * @param timestamp the timestamp of the command
     */
    public void deleteCard(final String cardNumber, final int timestamp) {
        for (Account account : accounts) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    account.getCards().remove(card);

                    Transaction transaction = new Transaction.TransactionBuilder()
                            .setTimestamp(timestamp)
                            .setDescription("The card has been destroyed")
                            .setCardNumber(cardNumber)
                            .setCardHolderEmail(userInfo.getEmail())
                            .setAccountIBAN(account.getIban())
                            .build();

                    this.addTransaction(transaction);
                    account.addTransaction(transaction);
                    return;
                }
            }
        }
    }

    /**
     * @param iban the IBAN of the account where the card will be created
     * @param timestamp the timestamp of the command
     */
    public void createCard(final String iban, final int timestamp) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            return;
        }

        account.createCard(cardNumber, this);

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setDescription("New card created")
                .setCardNumber(cardNumber)
                .setCardHolderEmail(userInfo.getEmail())
                .setAccountIBAN(iban)
                .build();

        this.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * @param iban the IBAN of the account where the one-time card will be created
     * @param timestamp the timestamp of the command
     */
    public void createOneTimeCard(final String iban, final int timestamp) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            return;
        }
        account.createOneTimeCard(cardNumber);

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setDescription("New card created")
                .setCardNumber(cardNumber)
                .setCardHolderEmail(userInfo.getEmail())
                .setAccountIBAN(iban)
                .build();

        this.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * Adds a transaction to the account's list of transactions (ordered by timestamp)
     * @param transaction the transaction to be added to the user's list of transactions
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
     * @return the user's first name
     */
    public String getFirstName() {
        return userInfo.getFirstName();
    }

    /**
     * @return the user's last name
     */
    public String getLastName() {
        return userInfo.getLastName();
    }

    /**
     * @return the user's email
     */
    public String getEmail() {
        return userInfo.getEmail();
    }

    public String getBirthDate() {
        return userInfo.getBirthDate();
    }

    public String getOccupation() {
        return userInfo.getOccupation();
    }

    public int getAge() {
        return 2024 - Integer.parseInt(userInfo.getBirthDate().substring(0,4));
    }



    /**
     * @param account the account to be added to the user's list of accounts
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * @param iban the IBAN of the account to be removed
     * @param timestamp the timestamp of the command
     * @throws Exception if the account couldn't be deleted
     */
    public void deleteAccount(final String iban, final int timestamp) throws Exception {
        Account account = getAccount(iban);

        if (account != null) {
            if (account.hasMoneyInAccount()) {
                // add transaction to user
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setTimestamp(timestamp)
                        .setDescription("Account couldn't be deleted - there are funds remaining")
                        .build();
                this.addTransaction(transaction);
                throw new Exception("Account couldn't be deleted - see org.poo.transactions "
                        + "for details");
            }

            // if account is of type classic account, decrement the number of classic accounts
            if (account.isClassicAccount()) {
                nrClassicAccounts--;
            }

            accounts.remove(account);
        }
    }

    /**
     * @param iban the IBAN of the account to be returned
     * @return the account with the given IBAN
     */
    public Account getAccount(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().equals(iban)) {
                return account;
            }
        }
        return null;
    }

    /**
     * @param cardNumber the card number to be searched for
     * @return the account that has the card with the given card number
     */
    public Account getAccountWithCard(final String cardNumber) {
        for (Account account : accounts) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Transforms the user's attributes into a JSON representation
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ObjectNode representing the user's attributes
     */
    public ObjectNode transformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();

        if (userInfo != null) {
            userNode.put("firstName", userInfo.getFirstName());
            userNode.put("lastName", userInfo.getLastName());
            userNode.put("email", userInfo.getEmail());
        }

        ArrayNode accountsArray = objectMapper.createArrayNode();
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                accountsArray.add(account.transformToObjectNode(objectMapper));
            }
        }
        userNode.set("accounts", accountsArray);
        return userNode;
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


    public ObjectNode associateTransformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();

        userNode.put("username", getUsername());
        userNode.put("spent", getAmountSpentOnBusinessAccount());
        userNode.put("deposited", getAmountDepositedOnBusinessAccount());

        return userNode;
    }
}
