package org.poo.serviceplans;

import org.poo.bank.Bank;

public class GoldPlan extends ServicePlan {
    public GoldPlan(Bank bank) {
        super("gold", 0.0, bank);
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
