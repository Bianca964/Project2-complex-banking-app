package org.poo.bank;

public class GoldPlan extends ServicePlan {
    public GoldPlan(Bank bank) {
        super("Gold", 0.0, bank);
        upgradeLevel = 3;
    }

    @Override
    public double applyComission(final double amountSpent, final String currency) {
        return amountSpent * (1.0 + getComission());
    }

    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
        return 0.0;
    }
}
