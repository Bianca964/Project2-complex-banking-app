package org.poo.bank;

public class StandardPlan extends ServicePlan {
    public StandardPlan(Bank bank) {
        super("Standard", 0.2, bank);
        upgradeLevel = 1;
    }

    @Override
    public double applyComission(final double amountSpent, final String currency) {
        return amountSpent * (1.0 + getComission());
    }

    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
        if (upgradedPlanName.equals("silver")) {
            return 100.0;
        } else if (upgradedPlanName.equals("gold")) {
            return 350.0;
        } else {
            return 0.0;
        }
    }
}
