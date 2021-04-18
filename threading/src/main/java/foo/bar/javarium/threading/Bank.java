package foo.bar.javarium.threading;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Bank {
    private static final Logger logger = Logger.getLogger(Bank.class.getName());
    private long nextId = 1;
    private List<Account> accounts = new ArrayList<>();
    private ReentrantLock registrationLock = new ReentrantLock();

    public void transferMoney(Account source, Account destination, BigDecimal amount) {
        destination.credit(source.charge(amount));
    }

    public Account openAccount() {
        Account account = new Account(nextId++);
        accounts.add(account);
        return account;
    }

    public Account openAccountWithLock() {
        registrationLock.lock();
        try {
            return this.openAccount();
        } finally {
            registrationLock.unlock();
        }
    }

}
