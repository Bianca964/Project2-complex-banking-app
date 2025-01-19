package org.poo.serviceplans;

import org.poo.bank.Bank;

public final class GoldPlan extends ServicePlan {
    private static final double COMMISSION_GOLD = 0.0;

    public GoldPlan(final Bank bank) {
        super("gold", COMMISSION_GOLD, bank);
        upgradeLevel = UPGRADE_LEVEL_GOLD;
    }

    /**
     * Apply commission according to gold plan
     * @param amountSpent amount spent by the user
     * @param currency currency of the amount spent
     * @return the amount spent with the commission applied for this plan
     */
    @Override
    public double applyCommission(final double amountSpent, final String currency) {
        return amountSpent * (1.0 + getCommission());
    }

    /**
     * Get the upgrade fee for the gold plan
     * @param upgradedPlanName the name of the plan to upgrade to
     * @return the upgrade fee in RON (gold is the highest plan, so there is no upgrade fee)
     */
    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
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
        return false;
    }

    @Override
    public boolean isGoldPlan() {
        return true;
    }
}
