package org.poo.serviceplans;

import org.poo.bank.Bank;

public class StandardPlan extends ServicePlan {
    public StandardPlan(Bank bank) {
        super("standard", 0.002, bank);
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
