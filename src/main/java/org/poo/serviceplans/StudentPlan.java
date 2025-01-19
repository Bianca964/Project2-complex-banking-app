package org.poo.serviceplans;

import org.poo.bank.Bank;

public final class StudentPlan extends ServicePlan {
    private static final double COMMISSION_STUDENT = 0.0;
    private static final double UPGRADE_FEE_TO_SILVER = 100.0;
    private static final double UPGRADE_FEE_TO_GOLD = 350.0;

    public StudentPlan(final Bank bank) {
        super("student", COMMISSION_STUDENT, bank);
        upgradeLevel = UPGRADE_LEVEL_STUDENT_STANDARD;
    }

    /**
     * Apply commission according to student plan
     * @param amountSpent amount spent by the user
     * @param currency currency of the amount spent
     * @return the amount spent with the commission applied for this plan
     */
    @Override
    public double applyCommission(final double amountSpent, final String currency) {
        return amountSpent * (1.0 + getCommission());
    }

    /**
     * Get the upgrade fee for the student plan
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
