package org.poo.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.serviceplans.ServicePlan;
import org.poo.serviceplans.StandardPlan;
import org.poo.serviceplans.StudentPlan;
import org.poo.serviceplans.GoldPlan;
import org.poo.serviceplans.SilverPlan;
import org.poo.transactions.TransactionHistory;
import org.poo.cards.Card;
import org.poo.fileio.UserInput;
import org.poo.transactions.splitpayments.SplitPayment;
import org.poo.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.poo.utils.Utils.generateCardNumber;

@Getter
@Setter
public class User {
    private final UserInput userInfo;
    private ArrayList<Account> accounts;
    private ServicePlan servicePlan;
    private int min300payments;
    private final TransactionHistory transactionHistory;
    private int nrClassicAccounts;
    private ArrayList<SplitPayment> splitPayments;
    private Map<BusinessAccount, Double> amountsDepositedOnBusinessAccounts;
    private Map<BusinessAccount, Double> amountsSpentOnBusinessAccounts;
    private ArrayList<Card> cardsAddedToBusinessAccount; // for employees

    private static final int MIN_300_PAYMENTS_REQUIRED = 5;
    private static final int CURRENT_YEAR = 2024;
    private static final int BIRTH_YEAR_POSITION = 4;

    public User(final UserInput userInfo, final Bank bank) {
        this.userInfo = userInfo;
        if (userInfo.getOccupation().equals("student")) {
            this.servicePlan = new StudentPlan(bank);
        } else {
            this.servicePlan = new StandardPlan(bank);
        }
        this.accounts = new ArrayList<>();
        this.transactionHistory = new TransactionHistory();
        this.nrClassicAccounts = 0;
        this.min300payments = 0;
        this.splitPayments = new ArrayList<>();
        this.amountsDepositedOnBusinessAccounts = new HashMap<>();
        this.amountsSpentOnBusinessAccounts = new HashMap<>();
        this.cardsAddedToBusinessAccount = new ArrayList<>();
    }

    /**
     * Adds the card to the list of cards added to a business account by this user
     * @param card the card to be added
     */
    public void addCardToCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.add(card);
    }

    /**
     * Removes the card from the list of cards added to a business account by this user
     * @param card the card to be removed
     */
    public void removeCardFromCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.remove(card);
    }

    /**
     * Increases the amount spent on the business account by this user
     * @param account the business account on which the amount is spent
     * @param amount the amount spent
     */
    public void increaseAmountSpentOnBusinessAccount(final BusinessAccount account,
                                                     final double amount) {
        double currentAmount = amountsSpentOnBusinessAccounts.getOrDefault(account, 0.0);
        amountsSpentOnBusinessAccounts.put(account, currentAmount + amount);
    }

    /**
     * @param account the business account to be checked
     * @return the amount spent on the business account by this user
     */
    public double getAmountSpentOnBusinessAccount(final BusinessAccount account) {
        return amountsSpentOnBusinessAccounts.getOrDefault(account, 0.0);
    }

    /**
     * Increases the amount deposited on the business account by this user
     * @param account the business account on which the amount is deposited
     * @param amount the amount deposited
     */
    public void increaseAmountDepositedOnBusinessAccount(final BusinessAccount account,
                                                         final double amount) {
        double currentAmount = amountsDepositedOnBusinessAccounts.getOrDefault(account, 0.0);
        amountsDepositedOnBusinessAccounts.put(account, currentAmount + amount);
    }

    /**
     * @param account the business account to be checked
     * @return the amount deposited on the business account by this user
     */
    public double getAmountDepositedOnBusinessAccount(final BusinessAccount account) {
        return amountsDepositedOnBusinessAccounts.getOrDefault(account, 0.0);
    }

    /**
     * Adds a split payment to the user's list of split payments
     * @param splitPayment the split payment to be added
     */
    public void addSplitPayment(final SplitPayment splitPayment) {
        splitPayments.add(splitPayment);
    }

    /**
     * Removes the split payment from the user's list of split payments
     * @param splitPayment the split payment to be removed
     */
    public void removeSplitPayment(final SplitPayment splitPayment) {
        splitPayments.remove(splitPayment);
    }

    /**
     * Accepts the first split payment of the given type
     * @param type the type of the split payment to be accepted
     * @return the split payment accepted, null if it doesn't exist
     */
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

    /**
     * @param type the type of the split payment wanted
     * @return the first split payment of the given type, null if it doesn't exist
     */
    public SplitPayment getFirstSplitTransactionOfType(final String type) {
        for (SplitPayment splitPayment : splitPayments) {
            if (splitPayment.getType().equals(type)) {
                return splitPayment;
            }
        }
        return null;
    }

    /**
     * Increases the number of classic accounts of the user
     */
    public void incrementNrClassicAccounts() {
        nrClassicAccounts++;
    }

    /**
     * @return true if the user has at least one classic account, false otherwise
     */
    public boolean hasClassicAccount() {
        return nrClassicAccounts >= 1;
    }

    /**
     * Increases the number of payments of at least 300 RON
     */
    public void increaseMin300payments() {
        min300payments++;
    }

    /**
     * Checks if the user has made at least 5 payments of at least 300 RON
     * If he did, the user is automatically upgraded to the gold plan
     * @param account the account used to add transactions
     * @param bank the bank used to get the exchange rate and create the service plans
     * @param timestamp the timestamp of the command
     * @return true if the user was upgraded to the gold plan, false otherwise
     */
    public boolean checkForUpgradeToGoldPlan(final Account account, final Bank bank,
                                             final int timestamp) {
        if (min300payments >= MIN_300_PAYMENTS_REQUIRED && this.servicePlan.isSilverPlan()) {
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

    /**
     * Upgrades the user's plan to the new plan type
     * @param account used to withdraw the fee for the upgrade and add transactions
     * @param bank used to get the exchange rate and create the service plans
     * @param timestamp the timestamp of the command
     * @param newPlanType the new plan type to which the user will be upgraded
     * @throws Exception if an error occurs during the upgrade
     */
    public void upgradePlan(final Account account, final Bank bank, final int timestamp,
                            final String newPlanType) throws Exception {
        // if user has silver plan, make the automatic upgrade to gold plan (without fee)
        if (checkForUpgradeToGoldPlan(account, bank, timestamp)) {
            return;
        }

        String currentPlanName = this.servicePlan.getName();
        if (newPlanType.equals(currentPlanName)) {
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

        // can't downgrade the plan
        if (newServicePlan.getUpgradeLevel() < this.servicePlan.getUpgradeLevel()) {
            return;
        }

        // upgrade the plan
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
    }

    /**
     * If the user is an employee in a business account need to be kept evidence of the cards added
     * @param card the card to be checked
     * @return true if the card was added to a business account by this user, false otherwise
     */
    public boolean hasCardAddedToBusinessAccount(final Card card) {
        return cardsAddedToBusinessAccount.contains(card);
    }

    /**
     * @param cardNumber the card number which will be deleted
     * @param timestamp the timestamp of the command
     * @param bank the bank used to find the account with the card
     */
    public void deleteCard(final String cardNumber, final int timestamp, final Bank bank) {
        for (Account account : accounts) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    // if the account still has money in it, don't delete the card
                    if (account.hasMoneyInAccount()) {
                        return;
                    }

                    account.deleteCard(card, this);

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

        // if reached here, the one deleting the card doesn't have the card in his accounts,
        // but wants to delete it from a business account in which he is an employee or a manager
        Account account = bank.getAccountWithCard(cardNumber);
        if (account == null || account.hasMoneyInAccount()) {
            return;
        }

        Card card = account.getCardWithCardNumber(cardNumber);
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount =  (BusinessAccount) account;
            businessAccount.deleteCard(card, this);
        }
    }

    /**
     * @param iban the IBAN of the account where the card will be created
     * @param timestamp the timestamp of the command
     * @param bank the bank used to find the account
     */
    public void createCard(final String iban, final int timestamp, final Bank bank) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            // if the account is not found among the user's accounts, maybe the
            // one creating the card is an employee or manager of a business account
            account = bank.getAccountWithIBAN(iban);
            if (account == null) {
                return;
            }
            if (!account.isBusinessAccount()) {
                return;
            }
        }

        try {
            account.createCard(cardNumber, this);
        } catch (Exception e) {
            return;
        }

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
     * @param bank the bank used to find the account
     */
    public void createOneTimeCard(final String iban, final int timestamp, final Bank bank) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            // if the account is not found among the user's accounts, maybe the
            // one creating the card is an employee or manager of a business account
            account = bank.getAccountWithIBAN(iban);
            if (account == null) {
                return;
            }
            if (!account.isBusinessAccount()) {
                return;
            }
        }

        try {
            account.createOneTimeCard(cardNumber, this);
        } catch (Exception e) {
            return;
        }

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
        transactionHistory.addTransaction(transaction);
    }

    /**
     * @return the user's full name
     */
    public String getUsername() {
        return userInfo.getLastName() + " " + userInfo.getFirstName();
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

    /**
     * @return the user's birth date
     */
    public String getBirthDate() {
        return userInfo.getBirthDate();
    }

    /**
     * @return the user's occupation
     */
    public String getOccupation() {
        return userInfo.getOccupation();
    }

    /**
     * @return the user's age
     */
    public int getAge() {
        String birthDate = userInfo.getBirthDate();
        int birthYear = Integer.parseInt(birthDate.substring(0, BIRTH_YEAR_POSITION));
        return CURRENT_YEAR - birthYear;
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
        if (account == null) {
            return;
        }

        // if the account is business, only the owner can delete de account
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().getEmail().equals(this.getEmail())) {
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setTimestamp(timestamp)
                        .setDescription("Account couldn't be deleted - only the"
                                + " owner can delete the account")
                        .build();
                this.addTransaction(transaction);
                throw new Exception("Account couldn't be deleted - see org.poo.transactions "
                        + "for details");
            }
        }

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
        return transactionHistory.transactionsTransformToArrayNode(objectMapper);
    }

    /**
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @param businessAccount the business account to which the user is associated
     * @return an ObjectNode representing the user's attributes associated to a business account
     */
    public ObjectNode associateTransformToAnObjNode(final ObjectMapper objectMapper,
                                                    final BusinessAccount businessAccount) {
        ObjectNode userNode = objectMapper.createObjectNode();

        userNode.put("username", getUsername());
        userNode.put("spent", getAmountSpentOnBusinessAccount(businessAccount));
        userNode.put("deposited", getAmountDepositedOnBusinessAccount(businessAccount));

        return userNode;
    }
}
