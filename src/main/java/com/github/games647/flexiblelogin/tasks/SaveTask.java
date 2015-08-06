package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

public class SaveTask implements Runnable {

    private final FlexibleLogin login;
    private final Account account;

    public SaveTask(FlexibleLogin login, Account account) {
        this.login = login;
        this.account = account;
    }

    @Override
    public void run() {
        login.getDatabase().save(account);
    }
}
