package org.poo.users;

import org.poo.bank.Bank;
import org.poo.fileio.UserInput;

public class Owner extends User {

    public Owner(final UserInput userInfo, final Bank bank) {
        super(userInfo, bank);
    }

    public Owner(final User user) {
        super(user.getUserInfo(), user.getServicePlan().getBank());
    }


}
