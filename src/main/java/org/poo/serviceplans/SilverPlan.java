package org.poo.serviceplans;

import org.poo.bank.Bank;

public class SilverPlan extends ServicePlan {
    public SilverPlan(Bank bank) {
        super("silver", 0.001, bank);
        upgradeLevel = 2;
    }

    @Override
    public double applyComission(final double amountSpent, final String currency) {
        // convert amountSpent to RON in order to compare to 500 RON
        double exchangeRate;
        try {
            exchangeRate = bank.getExchangeRate(currency, "RON");
        } catch (Exception e) {
            return -1.0;
        }
        double amountSpentInRON = amountSpent * exchangeRate;

        // for amounts spent in RON less than 500, the comission is not applied
        if (amountSpentInRON <= 500.0) {
            return amountSpent;
        }
        return amountSpent * (1.0 + getComission());
    }

    @Override
    public double getUpgradeFee(final String upgradedPlanName) {
        if (upgradedPlanName.equals("gold")) {
            return 250.0;
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
