package org.poo.serviceplans;

import org.poo.bank.Bank;

public final class SilverPlan extends ServicePlan {
    private static final double MIN_AMOUNT_FOR_COMMISSION_IN_RON = 500.0;
    private static final double COMMISSION_SILVER = 0.001;
    private static final double UPGRADE_FEE_TO_GOLD = 250.0;

    public SilverPlan(final Bank bank) {
        super("silver", COMMISSION_SILVER, bank);
        upgradeLevel = UPGRADE_LEVEL_SILVER;
    }

    /**
     * Apply commission according to silver plan
     * @param amountSpent amount spent by the user
     * @param currency currency of the amount spent
     * @return the amount spent with the commission applied for this plan
     */
    @Override
    public double applyCommission(final double amountSpent, final String currency) {
        // convert amountSpent to RON in order to compare to 500 RON
        double exchangeRate;
        try {
            exchangeRate = bank.getExchangeRate(currency, "RON");
        } catch (Exception e) {
            return -1.0;
        }
        double amountSpentInRON = amountSpent * exchangeRate;

        // for amounts spent in RON less than 500, the commission is not applied
        if (amountSpentInRON <= MIN_AMOUNT_FOR_COMMISSION_IN_RON) {
            return amountSpent;
        }
        return amountSpent * (1.0 + getCommission());
    }

    /**
     * Get the upgrade fee for the silver plan
     * @param upgradedPlanName the name of the plan to upgrade to
     * @return the upgrade fee in RON
     */
    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
        if (upgradedPlanName.equals("gold")) {
            return UPGRADE_FEE_TO_GOLD;
        }
        return 0.0;
    }

    @Override
    public boolean isStudentPlan() {
        return false;
    }

    @Override
    public boolean isStandardPlan() {
        return false;
    }

    @Override
    public boolean isSilverPlan() {
        return true;
    }

    @Override
    public boolean isGoldPlan() {
        return false;
    }
}
