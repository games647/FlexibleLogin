package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

public class SaveTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();
    private final Account account;

    public SaveTask(Account account) {
        this.account = account;
    }

    @Override
    public void run() {
        plugin.getDatabase().save(account);
    }
}
