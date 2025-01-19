package org.poo.accounts;

import org.poo.serviceplans.ServicePlan;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

public class DiscountManager {
    private static final double FOOD_CASHBACK_RATE = 0.02;
    private static final double CLOTHES_CASHBACK_RATE = 0.05;
    private static final double TECH_CASHBACK_RATE = 0.1;

    private static final int INF_LIMIT_LEVEL1_SP_TH = 100;
    private static final int INF_LIMIT_LEVEL2_SP_TH = 300;
    private static final int INF_LIMIT_LEVEL3_SP_TH = 500;

    private static final double STUD_STND_RATE_LVL1 = 0.001;
    private static final double SILVER_RATE_LVL1 = 0.003;
    private static final double GOLD_RATE_LVL1 = 0.005;
    private static final double STUD_STND_RATE_LVL2 = 0.002;
    private static final double SILVER_RATE_LVL2 = 0.004;
    private static final double GOLD_RATE_LVL2 = 0.0055;
    private static final double STUD_STND_RATE_LVL3 = 0.0025;
    private static final double SILVER_RATE_LVL3 = 0.005;
    private static final double GOLD_RATE_LVL3 = 0.007;

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

    /**
     * @return true if there is a discount available
     */
    public boolean hasDiscountAvailable() {
        return this.discountFood || this.discountClothes || this.discountTech;
    }

    /**
     * Applies discount according to the commerciant type
     * @param commerciant commerciant which offers the discount
     * @param amountSpent amount spent
     * @param account account of the user which receives the discount
     */
    public void applyDiscount(final Commerciant commerciant, final double amountSpent,
                              final Account account) {
        switch (commerciant.getType()) {
            case "Food" -> applyFoodDiscount(amountSpent, account);
            case "Clothes" -> applyClothesDiscount(amountSpent, account);
            case "Tech" -> applyTechDiscount(amountSpent, account);
            default -> {
                return;
            }
        }
    }

    /**
     * Applies discount for food for an account
     * @param amountSpent amount spent
     * @param account account of the user which receives the discount
     */
    public void applyFoodDiscount(final double amountSpent, final Account account) {
        if (this.discountFood && !this.discountFoodWasUsed) {
            account.deposit(amountSpent * FOOD_CASHBACK_RATE);
            setDiscountFoodAsUsed();
            this.discountFood = false;
        }
    }

    /**
     * Applies discount for clothes for an account
     * @param amountSpent amount spent
     * @param account account of the user which receives the discount
     */
    public void applyClothesDiscount(final double amountSpent, final Account account) {
        if (this.discountClothes && !this.discountClothesWasUsed) {
            account.deposit(amountSpent * CLOTHES_CASHBACK_RATE);
            setDiscountClothesAsUsed();
            this.discountClothes = false;
        }
    }

    /**
     * Applies discount for tech for an account
     * @param amountSpent amount spent
     * @param account account of the user which receives the discount
     */
    public void applyTechDiscount(final double amountSpent, final Account account) {
        if (this.discountTech && !this.discountTechWasUsed) {
            account.deposit(amountSpent * TECH_CASHBACK_RATE);
            setDiscountTechAsUsed();
            this.discountTech = false;
        }
    }

    /**
     * Sets the discount for food as available if it wasn't used already
     */
    public void setDiscountFood() {
        if (this.discountFoodWasUsed) {
            return;
        }
        this.discountFood = true;
    }

    /**
     * Sets the discount for clothes as available if it wasn't used already
     */
    public void setDiscountClothes() {
        if (this.discountClothesWasUsed) {
            return;
        }
        this.discountClothes = true;
    }

    /**
     * Sets the discount for tech as available if it wasn't used already
     */
    public void setDiscountTech() {
        if (this.discountTechWasUsed) {
            return;
        }
        this.discountTech = true;
    }

    /**
     * Marks the discount for food as used
     */
    public void setDiscountFoodAsUsed() {
        this.discountFoodWasUsed = true;
    }

    /**
     * Marks the discount for clothes as used
     */
    public void setDiscountClothesAsUsed() {
        this.discountClothesWasUsed = true;
    }

    /**
     * Marks the discount for tech as used
     */
    public void setDiscountTechAsUsed() {
        this.discountTechWasUsed = true;
    }

    /**
     * @return true if the discount for food was used
     */
    public boolean isDiscountFoodUsed() {
        return this.discountFoodWasUsed;
    }

    /**
     * @return true if the discount for clothes was used
     */
    public boolean isDiscountClothesUsed() {
        return this.discountClothesWasUsed;
    }

    /**
     * @return true if the discount for tech was used
     */
    public boolean isDiscountTechUsed() {
        return this.discountTechWasUsed;
    }

    /**
     * Calculates the cashback amount for spending threshold according to the total
     * amount spent for the spending threshold commerciants
     * @param amountSpent amount spent
     * @param servicePlan service plan of the user
     * @param totalAmountForSpendingThreshold total amount spent for the spending
     *                                        threshold commerciants
     * @return the cashback amount
     */
    private double calculateSpendingDiscount(final double amountSpent,
                                             final ServicePlan servicePlan,
                                             final double totalAmountForSpendingThreshold) {
        double cashbackAmount = 0.0;

        if (totalAmountForSpendingThreshold >= INF_LIMIT_LEVEL1_SP_TH
                && totalAmountForSpendingThreshold < INF_LIMIT_LEVEL2_SP_TH) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan,
                    STUD_STND_RATE_LVL1, SILVER_RATE_LVL1, GOLD_RATE_LVL1);

        } else if (totalAmountForSpendingThreshold >= INF_LIMIT_LEVEL2_SP_TH
                && totalAmountForSpendingThreshold < INF_LIMIT_LEVEL3_SP_TH) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan,
                    STUD_STND_RATE_LVL2, SILVER_RATE_LVL2, GOLD_RATE_LVL2);

        } else if (totalAmountForSpendingThreshold >= INF_LIMIT_LEVEL3_SP_TH) {
            cashbackAmount = getCashbackAmountForSpending(amountSpent, servicePlan,
                    STUD_STND_RATE_LVL3, SILVER_RATE_LVL3, GOLD_RATE_LVL3);
        }
        return cashbackAmount;
    }

    /**
     * Calculates the cashback amount for spending threshold according to the service plan
     * @param amountSpent amount spent
     * @param servicePlan service plan of the user
     * @param studentStandardRate cashback rate for student and standard plans
     * @param silverRate cashback rate for silver plan
     * @param goldRate cashback rate for gold plan
     * @return the cashback amount
     */
    private double getCashbackAmountForSpending(final double amountSpent,
                                                final ServicePlan servicePlan,
                                                final double studentStandardRate,
                                                final double silverRate, final double goldRate) {
        if (servicePlan.isStudentPlan() || servicePlan.isStandardPlan()) {
            return amountSpent * studentStandardRate;
        } else if (servicePlan.isSilverPlan()) {
            return amountSpent * silverRate;
        } else if (servicePlan.isGoldPlan()) {
            return amountSpent * goldRate;
        }
        return 0.0;
    }

    /**
     * Applies a discount for spending threshold for an account
     * @param sender user who receives the cashback
     * @param amountSpent amount spent
     * @param account account of the user which receives the cashback
     */
    public void applySpendingThresholdDiscount(final User sender, final double amountSpent,
                                               final Account account) {
        ServicePlan servicePlan = sender.getServicePlan();
        // if the sender account is business, the owner's service plan is used for cashback
        if (account.isBusinessAccount()) {
            servicePlan = ((BusinessAccount) account).getOwner().getServicePlan();
        }

        double totalAmountForSpendingThreshold = account.getTotalAmountForSpendingThreshold();

        double cashbackAmount = calculateSpendingDiscount(amountSpent, servicePlan,
                                                          totalAmountForSpendingThreshold);
        if (cashbackAmount > 0) {
            account.deposit(cashbackAmount);
        }
    }
}
