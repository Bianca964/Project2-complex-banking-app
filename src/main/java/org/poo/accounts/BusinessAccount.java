package org.poo.accounts;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;
import org.poo.cards.Card;
import org.poo.cards.OneTimeCard;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

import java.util.ArrayList;
import java.util.Comparator;

@Getter
@Setter
public class BusinessAccount extends Account {
    private User owner;
    private ArrayList<User> employees;
    private ArrayList<User> managers;

    // lista de comercianti care ar trebui sa apara in businessReport(cei care au fost adaugati de asociati, aia de owner nu se adauga)
    private ArrayList<Commerciant> commerciantsAddedByAssociates;

    // spending limit for employees
    private double spendingLimitForEmployees;
    private double depositLimit;


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

        double spendingLimitForEmployeesInRon = 500;
        this.spendingLimitForEmployees = spendingLimitForEmployeesInRon * exchangeRate;

        double depositLimitInRon = 500;
        this.depositLimit = depositLimitInRon * exchangeRate;
    }


    // tb sa l adaug in ordine alfabetica dupa numele comerciantului - pt business Report
    public void addCommerciantAddedByAssociate(final Commerciant commerciant) {
        commerciantsAddedByAssociates.add(commerciant);
        commerciantsAddedByAssociates.sort(Comparator.comparing(Commerciant::getName));
    }











    public void addAmountReceivedByCommerciant(final double amount, final Commerciant commerciant) {
        commerciant.addAmountReceived(amount);
    }


    public boolean hasCommerciantAddedByAssociate(final Commerciant commerciant) {
        return commerciantsAddedByAssociates.contains(commerciant);
    }

    public void addAssociateToCommerciant(final User user, final Commerciant commerciant) {
        if (isEmployee(user)) {
            commerciant.addEmployee(user);
        } else if (isManager(user)) {
            commerciant.addManager(user);
        }
    }




    public boolean isOwner(final User user) {
        return owner == user;
    }

    public boolean isEmployee(final User user) {
        return employees.contains(user);
    }

    public boolean isManager(final User user) {
        return managers.contains(user);
    }

    public boolean isAssociate(final User user) {
        return isManager(user) || isEmployee(user);
    }

    public void addEmployee(final User user) {
        employees.add(user);
    }

    public void addManager(final User user) {
        managers.add(user);
    }

    public void removeEmployee(final User user) {
        employees.remove(user);
    }

    public void removeManager(final User user) {
        managers.remove(user);
    }

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

    public void removeAssociate(final User associate) {
        if (isEmployee(associate)) {
            removeEmployee(associate);
        } else if (isManager(associate)) {
            removeManager(associate);
        }
    }


    public void setSpendingLimitForEmployees(final double newSpendingLimit, final User userToSetTheSpendingLimit) throws Exception {
        if (this.isOwner(userToSetTheSpendingLimit)) {
            this.spendingLimitForEmployees = newSpendingLimit;
        } else {
            throw new Exception("You must be owner in order to change spending limit.");
        }
    }

    public void setMinBalance(final double minBalance, final User user) throws Exception {
        if (this.isOwner(user)) {
            this.setMinBalance(minBalance);
        } else {
            throw new Exception("User is not the owner of the account and cant change the min balance");
        }
    }

    public void setDepositLimit(final double depositLimit, final User user) throws Exception {
        if (this.isOwner(user)) {
            this.depositLimit = depositLimit;
        } else {
            throw new Exception("You must be owner in order to change deposit limit.");
        }
    }







    @Override
    public void createCard(final String cardNumber, final User user) {
        Card card = new Card(cardNumber);

        // tb sa stiu ce fel de rol are cel care l a creat (daca e employee tb sa l adaug
        // la lista lui de carduri adaugate la acest cont de business)
        if (isEmployee(user)) {
            user.addCardToCardsAddedToBusinessAccount(card);
        }

        // add card to account
        //this.addCard(card);
        addCard(card);
    }


    @Override
    public void createOneTimeCard(final String cardNumber, final User user) {
        OneTimeCard card = new OneTimeCard(cardNumber);

        // tb sa stiu ce fel de rol are cel care l a creat (daca e employee tb sa l adaug
        // la lista lui de carduri adaugate la acest cont de business)
        if (isEmployee(user)) {
            user.addCardToCardsAddedToBusinessAccount(card);
        }

        addCard(card);
    }


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




    @Override
    public void addFunds(final double amount, final User user) {
        if (!isOwner(user) && !isAssociate(user)) {
            return;
        }

        // add to the associated user's amount deposited on the business account
        try {
            increaseAmountDepositedOnBusinessAccountByUser(amount, user);
        } catch (Exception e) {
            return;
        }

        // add funds to the balance of the account
        this.deposit(amount);
    }

    public void increaseAmountDepositedOnBusinessAccountByUser(final double amount, final User user) throws Exception {

        if (isEmployee(user) && amount > depositLimit) {
            throw new Exception("Deposit limit exceeded");
        }
        user.increaseAmountDepositedOnBusinessAccount(this, amount);
    }

    public void increaseAmountSpentOnBusinessAccountByUser(final double amount, final User user) throws Exception {
        if (isEmployee(user)) {
            if (amount > spendingLimitForEmployees) {
                throw new Exception("Spending limit exceeded");
            }
        }
        user.increaseAmountSpentOnBusinessAccount(this, amount);
    }

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
