package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;
import org.poo.fileio.UserInput;
import org.poo.transactions.Transaction;

import java.util.ArrayList;

import static org.poo.utils.Utils.generateCardNumber;

public class User {
    private final UserInput userInfo;
    @Getter
    @Setter
    private ArrayList<Account> accounts;
    @Setter
    @Getter
    private ServicePlan servicePlan;
    private int min300payments;

    private final ArrayList<Transaction> transactions;
    private int nrClassicAccounts;

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

    public void upgradePlan(Account account, final Bank bank, final int timestamp, final String newPlanType) throws Exception {

        // if user has silver plan, make the automatic upgrade to gold plan (without fee)
        if (min300payments >= 5 && this.servicePlan.getName().equals("Silver")) {
            this.servicePlan = new GoldPlan(bank);
            return;
        }

        String currentPlanName = this.servicePlan.getName();
        if (newPlanType.equals(currentPlanName)) {
            throw new Exception("The user already has the " + newPlanType + " plan");
        }

        ServicePlan newServicePlan = switch (newPlanType) {
            case "student" -> new StudentPlan(bank);
            case "standard" -> new StandardPlan(bank);
            case "gold" -> new GoldPlan(bank);
            case "silver" -> new SilverPlan(bank);
            default -> throw new Exception("Invalid plan type");
        };

        if (newServicePlan.getUpgradeLevel() < this.servicePlan.getUpgradeLevel()) {
            throw new Exception("You cannot downgrade your plan.");
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
            throw new Exception("Insufficient funds");
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
        account.createCard(cardNumber);

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
     * @param transaction the transaction to be added to the user's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
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
            if (!account.hasInterest()) {
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
}
