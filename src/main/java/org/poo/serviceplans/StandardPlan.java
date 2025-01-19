package org.poo.serviceplans;

import org.poo.bank.Bank;

public final class StandardPlan extends ServicePlan {
    private static final double COMMISSION_STANDARD = 0.002;
    private static final double UPGRADE_FEE_TO_SILVER = 100.0;
    private static final double UPGRADE_FEE_TO_GOLD = 350.0;

    public StandardPlan(final Bank bank) {
        super("standard", COMMISSION_STANDARD, bank);
        upgradeLevel = UPGRADE_LEVEL_STUDENT_STANDARD;
    }

    /**
     * Apply commission according to standard plan
     * @param amountSpent amount spent by the user
     * @param currency currency of the amount spent
     * @return the amount spent with the commission applied for this plan
     */
    @Override
    public double applyCommission(final double amountSpent, final String currency) {
        return amountSpent * (1.0 + getCommission());
    }

    /**
     * Get the upgrade fee for the standard plan
     * @param upgradedPlanName the name of the plan to upgrade to
     * @return the upgrade fee in RON
     */
    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
        if (upgradedPlanName.equals("silver")) {
            return UPGRADE_FEE_TO_SILVER;
        } else if (upgradedPlanName.equals("gold")) {
            return UPGRADE_FEE_TO_GOLD;
        } else {
            return 0.0;
        }
    }

    @Override
    public boolean isStudentPlan() {
        return false;
    }

    @Override
    public boolean isStandardPlan() {
        return true;
    }

    @Override
    public boolean isSilverPlan() {
        return false;
    }

    @Override
    public boolean isGoldPlan() {
        return false;
    }
}
