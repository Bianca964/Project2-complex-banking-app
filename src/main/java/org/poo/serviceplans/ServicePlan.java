package org.poo.serviceplans;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;

@Getter
@Setter
public abstract class ServicePlan {
    protected static final int UPGRADE_LEVEL_STUDENT_STANDARD = 1;
    protected static final int UPGRADE_LEVEL_SILVER = 2;
    protected static final int UPGRADE_LEVEL_GOLD = 3;

    private final String name;
    private final double commission;
    protected int upgradeLevel;
    protected Bank bank;

    public ServicePlan(final String name, final double commission, final Bank bank) {
        this.name = name;
        this.commission = commission;
        this.bank = bank;
    }

    /**
     * Apply commission according to the plan
     * @param amountSpent amount spent by the user
     * @param currency currency of the amount spent
     * @return the amount spent with the commission applied according to this plan
     */
    public abstract double applyCommission(double amountSpent, String currency);

    /**
     * Get the upgrade fee for the plan
     * @param upgradedPlanName the name of the plan to upgrade to
     * @return the upgrade fee in RON
     */
    public abstract double getUpgradeFee(String upgradedPlanName);

    /**
     * @return true if the plan is a student plan, false otherwise
     */
    public abstract boolean isStudentPlan();

    /**
     * @return true if the plan is a standard plan, false otherwise
     */
    public abstract boolean isStandardPlan();

    /**
     * @return true if the plan is a silver plan, false otherwise
     */
    public abstract boolean isSilverPlan();

    /**
     * @return true if the plan is a gold plan, false otherwise
     */
    public abstract boolean isGoldPlan();
}
