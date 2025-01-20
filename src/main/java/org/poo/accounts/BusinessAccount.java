package org.poo.accounts;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;
import org.poo.cards.Card;
import org.poo.cards.OneTimeCard;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

import java.util.ArrayList;
import java.util.Comparator;

@Getter
@Setter
public final class BusinessAccount extends Account {
    private static final double INITIAL_LIMIT_IN_RON = 500;

    private User owner;
    private ArrayList<User> employees;
    private ArrayList<User> managers;
    private double spendingLimit;
    private double depositLimit;
    // the list of commerciants added by associates for the business report
    private ArrayList<Commerciant> commerciantsAddedByAssociates;

    public BusinessAccount(final String currency, final String type, final int timestamp,
                           final User user, final Bank bank) {
        super(currency, type, timestamp);
        this.employees = new ArrayList<>();
        this.managers = new ArrayList<>();
        this.owner = user;
        this.commerciantsAddedByAssociates = new ArrayList<>();

        double exchangeRate;
        try {
            exchangeRate = bank.getExchangeRate("RON", currency);
        } catch (Exception e) {
            return;
        }

        this.spendingLimit = INITIAL_LIMIT_IN_RON * exchangeRate;
        this.depositLimit = INITIAL_LIMIT_IN_RON * exchangeRate;
    }

    /**
     * Add commerciant added by associate ordered alphabetically by name
     * @param commerciant commerciant to be added to which the associate made transactions
     */
    public void addCommerciantAddedByAssociate(final Commerciant commerciant) {
        commerciantsAddedByAssociates.add(commerciant);
        commerciantsAddedByAssociates.sort(Comparator.comparing(Commerciant::getName));
    }

    /**
     * Add amount received by commerciant to its total amount received
     * @param amount amount received by the commerciant
     * @param commerciant commerciant which received the amount
     */
    public void addAmountReceivedByCommerciant(final double amount, final Commerciant commerciant) {
        commerciant.addAmountReceived(amount);
    }

    /**
     * Checks if the commerciant was added by an associate
     * @param commerciant commerciant to be checked
     * @return true if the commerciant was added by an associate, false otherwise
     */
    public boolean hasCommerciantAddedByAssociate(final Commerciant commerciant) {
        return commerciantsAddedByAssociates.contains(commerciant);
    }

    /**
     * Add associate to commerciant's list of employees or managers
     * @param user associate to be added
     * @param commerciant commerciant to which the associate is added
     */
    public void addAssociateToCommerciant(final User user, final Commerciant commerciant) {
        if (isEmployee(user)) {
            commerciant.addEmployee(user);
        } else if (isManager(user)) {
            commerciant.addManager(user);
        }
    }

    /**
     * @param user user to be checked
     * @return true if the user is the owner of the business account, false otherwise
     */
    public boolean isOwner(final User user) {
        return owner == user;
    }

    /**
     * @param user user to be checked
     * @return true if the user is an employee of the business account, false otherwise
     */
    public boolean isEmployee(final User user) {
        return employees.contains(user);
    }

    /**
     * @param user user to be checked
     * @return true if the user is a manager of the business account, false otherwise
     */
    public boolean isManager(final User user) {
        return managers.contains(user);
    }

    /**
     * @param user user to be checked
     * @return true if the user is an associate of the business account, false otherwise
     */
    public boolean isAssociate(final User user) {
        return isManager(user) || isEmployee(user);
    }

    /**
     * Add employee to the list of employees
     * @param user employee to be added
     */
    public void addEmployee(final User user) {
        employees.add(user);
    }

    /**
     * Add manager to the list of managers
     * @param user manager to be added
     */
    public void addManager(final User user) {
        managers.add(user);
    }

    /**
     * Add associate to the business account
     * @param associate associate to be added
     * @param role role of the associate (employee or manager)
     */
    public void addAssociate(final User associate, final String role) {
        // if it s already an associate, don't add it again
        if (isAssociate(associate) || isOwner(associate)) {
            return;
        }
        if (role.equals("employee")) {
            addEmployee(associate);
        } else if (role.equals("manager")) {
            addManager(associate);
        }
    }

    /**
     * @param newSpendingLimit new spending limit to be set
     * @param user user who wants to set the spending limit
     * @throws Exception if the user is not the owner of the business account
     */
    public void setSpendingLimit(final double newSpendingLimit, final User user) throws Exception {
        if (this.isOwner(user)) {
            this.spendingLimit = newSpendingLimit;
        } else {
            throw new Exception("You must be owner in order to change spending limit.");
        }
    }

    /**
     * @param newDepositLimit new deposit limit to be set
     * @param user user who wants to set the deposit limit
     * @throws Exception if the user is not the owner of the business account
     */
    public void setDepositLimit(final double newDepositLimit, final User user) throws Exception {
        if (this.isOwner(user)) {
            this.depositLimit = newDepositLimit;
        } else {
            throw new Exception("You must be owner in order to change deposit limit.");
        }
    }

    /**
     * Creates a card with the given card number
     * @param cardNumber the card number of the card to be created
     * @param user user who wants to create the card
     * @throws Exception if the user is not the owner or an associate of the business account
     */
    @Override
    public void createCard(final String cardNumber, final User user) throws Exception {
        if (!isOwner(user) && !isAssociate(user)) {
            throw new Exception("You are not associated with this account.");
        }
        Card card = new Card(cardNumber);
        addCard(card);

        // if the one creating the card is an employee, add it to his list of
        // cards added to business accounts
        if (isEmployee(user)) {
            user.addCardToCardsAddedToBusinessAccount(card);
        }
    }

    /**
     * Creates a one-time card with the given card number
     * @param cardNumber the card number of the one-time card to be created
     * @param user user who wants to create the card
     * @throws Exception if the user is not the owner or an associate of the business account
     */
    @Override
    public void createOneTimeCard(final String cardNumber, final User user) throws Exception {
        if (!isOwner(user) && !isAssociate(user)) {
            throw new Exception("You are not associated with this account.");
        }
        OneTimeCard card = new OneTimeCard(cardNumber);
        addCard(card);

        // if the one creating the card is an employee, add it to his list of
        // cards added to business accounts
        if (isEmployee(user)) {
            user.addCardToCardsAddedToBusinessAccount(card);
        }
    }

    /**
     * Delete card from the list of cards
     * @param card card to be deleted
     * @param user user who wants to delete the card
     */
    @Override
    public void deleteCard(final Card card, final User user) {
        if (card != null) {
            // if the one deleting the card is an employee (he can delete only the cards he added)
            if (isEmployee(user)) {
                // if it wasn't added by him, don't delete it
                if (!user.hasCardAddedToBusinessAccount(card)) {
                    return;
                }

                // if it was added by him, remove it from his list
                user.removeCardFromCardsAddedToBusinessAccount(card);
                getCards().remove(card);
            } else if (isOwner(user) || isManager(user)) {
                getCards().remove(card);
            }
        }
    }

    /**
     * Add funds to the account
     * @param amount amount to be added
     * @param user user who wants to add funds
     */
    @Override
    public void addFunds(final double amount, final User user) {
        if (!isOwner(user) && !isAssociate(user)) {
            return;
        }

        // add to the associated user's amount deposited on the business account
        try {
            increaseAmountDepositedByUser(amount, user);
        } catch (Exception e) {
            return;
        }

        // add funds to the balance of the account
        this.deposit(amount);
    }

    /**
     * Increase the amount deposited by the user to this business account
     * @param amount amount to be deposited
     * @param user user who wants to deposit the amount
     * @throws Exception if the deposit limit is exceeded
     */
    public void increaseAmountDepositedByUser(final double amount,
                                              final User user) throws Exception {
        if (isEmployee(user) && amount > depositLimit) {
            throw new Exception("Deposit limit exceeded");
        }
        user.increaseAmountDepositedOnBusinessAccount(this, amount);
    }

    /**
     * Increase the amount spent by the user from this business account
     * @param amount amount spent
     * @param user user who wants to spend the amount
     * @throws Exception if the spending limit is exceeded
     */
    public void increaseAmountSpentByUser(final double amount,
                                          final User user) throws Exception {
        if (isEmployee(user)) {
            if (amount > spendingLimit) {
                throw new Exception("Spending limit exceeded");
            }
        }
        user.increaseAmountSpentOnBusinessAccount(this, amount);
    }

    /**
     * @return the total amount deposited by all associates of the business account
     */
    public double getTotalAmountDeposited() {
        double amountDeposited = 0;
        for (User manager : managers) {
            amountDeposited += manager.getAmountDepositedOnBusinessAccount(this);
        }

        for (User employee : employees) {
            amountDeposited += employee.getAmountDepositedOnBusinessAccount(this);
        }
        return amountDeposited;
    }

    /**
     * @return the total amount spent by all associates of the business account
     */
    public double getTotalAmountSpent() {
        double amountSpent = 0;
        for (User manager : managers) {
            amountSpent += manager.getAmountSpentOnBusinessAccount(this);
        }

        for (User employee : employees) {
            amountSpent += employee.getAmountSpentOnBusinessAccount(this);
        }
        return amountSpent;
    }

    /**
     * Sets an alias to the account
     * @param alias alias to be set
     * @param user user who wants to set the alias
     */
    @Override
    public void setAlias(final String alias, final User user) {
        if (isOwner(user)) {
            this.setAlias(alias);
        }
    }

    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    @Override
    public boolean supportsReport() {
        return true;
    }

    @Override
    public boolean isClassicAccount() {
        return false;
    }

    @Override
    public boolean isSavingAccount() {
        return false;
    }
}
