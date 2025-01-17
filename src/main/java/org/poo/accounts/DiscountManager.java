package org.poo.accounts;

import org.poo.serviceplans.ServicePlan;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

public class DiscountManager {
    private boolean discountFood;
    private boolean discountClothes;
    private boolean discountTech;

    private boolean discountFoodWasUsed;
    private boolean discountClothesWasUsed;
    private boolean discountTechWasUsed;

    public DiscountManager() {
        this.discountFood = false;
        this.discountClothes = false;
        this.discountTech = false;
        this.discountFoodWasUsed = false;
        this.discountClothesWasUsed = false;
        this.discountTechWasUsed = false;
    }

    // NrOfTransactions
    public boolean hasDiscountAvailable() {
        return this.discountFood || this.discountClothes || this.discountTech;
    }

    public void applyDiscount(Commerciant commerciant, double amountSpent, Account account) {
        switch (commerciant.getType()) {
            case "Food" -> applyFoodDiscount(amountSpent, account);
            case "Clothes" -> applyClothesDiscount(amountSpent, account);
            case "Tech" -> applyTechDiscount(amountSpent, account);
        }
    }

    public void applyFoodDiscount(double amountSpent, Account account) {
        if (this.discountFood && !this.discountFoodWasUsed) {
            account.deposit(amountSpent * 0.02);
            setDiscountFoodAsUsed();
            this.discountFood = false;
        }
    }

    public void applyClothesDiscount(double amountSpent, Account account) {
        if (this.discountClothes && !this.discountClothesWasUsed) {
            account.deposit(amountSpent * 0.05);
            setDiscountClothesAsUsed();
            this.discountClothes = false;
        }
    }

    public void applyTechDiscount(double amountSpent, Account account) {
        if (this.discountTech && !this.discountTechWasUsed) {
            account.deposit(amountSpent * 0.1);
            setDiscountTechAsUsed();
            this.discountTech = false;
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

    private double calculateSpendingDiscount(double amountSpent, ServicePlan servicePlan, double totalAmountForSpendingThreshold) {
        double cashbackAmount = 0.0;

        if (totalAmountForSpendingThreshold >= 100 && totalAmountForSpendingThreshold < 300) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan, 0.001, 0.003, 0.005);
        } else if (totalAmountForSpendingThreshold >= 300 && totalAmountForSpendingThreshold < 500) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan, 0.002, 0.004, 0.0055);
        } else if (totalAmountForSpendingThreshold >= 500) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan, 0.0025, 0.005, 0.007);
        }
        return cashbackAmount;
    }

    private double getCashbackAmountForSpending(double amountSpent, ServicePlan servicePlan, double studentStandardRate, double silverRate, double goldRate) {
        if (servicePlan.isStudentPlan() || servicePlan.isStandardPlan()) {
            return amountSpent * studentStandardRate;
        } else if (servicePlan.isSilverPlan()) {
            return amountSpent * silverRate;
        } else if (servicePlan.isGoldPlan()) {
            return amountSpent * goldRate;
        }
        return 0.0;
    }

    public void applySpendingThresholdDiscount(User sender, double amountSpent, Account account) {
        ServicePlan servicePlan = sender.getServicePlan();
        // if the sender account is business, the owner's service plan is used for cashback
        if (account.isBusinessAccount()) {
            servicePlan = ((BusinessAccount) account).getOwner().getServicePlan();
        }

        double totalAmountForSpendingThreshold = account.getTotalAmountForSpendingThreshold();

        double cashbackAmount = calculateSpendingDiscount(amountSpent, servicePlan, totalAmountForSpendingThreshold);
        if (cashbackAmount > 0) {
            account.deposit(cashbackAmount);
        }
    }

}
