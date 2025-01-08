package org.poo.serviceplans;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;

@Getter
@Setter
public abstract class ServicePlan {
    private final String name;
    private final double comission;
    protected int upgradeLevel;
    protected Bank bank;

    public ServicePlan(final String name, final double comission, final Bank bank) {
        this.name = name;
        this.comission = comission;
        this.bank = bank;
    }

    public abstract double applyComission(final double amountSpent, final String currency);
    public abstract double getUpgradeFee(final String upgradedPlanName);
}
