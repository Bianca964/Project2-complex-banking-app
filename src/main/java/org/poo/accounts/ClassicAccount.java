package org.poo.accounts;

public final class ClassicAccount extends Account {
    public ClassicAccount(final String currency, final String type, final int timestamp) {
        super(currency, type, timestamp);
    }

    /**
     * @return true as the classic account supports reports
     */
    @Override
    public boolean supportsReport() {
        return true;
    }

    @Override
    public boolean isBusinessAccount() {
        return false;
    }

    @Override
    public boolean isSavingAccount() {
        return false;
    }

    @Override
    public boolean isClassicAccount() {
        return true;
    }
}
