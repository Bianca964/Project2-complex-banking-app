package org.poo.serviceplans;

import org.poo.bank.Bank;

public class StudentPlan extends ServicePlan {
    public StudentPlan(Bank bank) {
        super("student", 0.0, bank);
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

    @Override
    public boolean isStudentPlan() {
        return true;
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
        return false;
    }
}
