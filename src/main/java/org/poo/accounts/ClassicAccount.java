package org.poo.accounts;

public class ClassicAccount extends Account {
    public ClassicAccount(final String currency, final String type, final int timestamp) {
        super(currency, type, timestamp);
    }

    /**
     * @return false as the classic account does not have interest
     */
    @Override
   public boolean hasInterest() {
        return false;
   }

    /**
     * @return true as the classic account supports reports
     */
    @Override
    public boolean supportsReport() {
        return true;
    }
}
