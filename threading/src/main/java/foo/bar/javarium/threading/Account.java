package foo.bar.javarium.threading;

import java.math.BigDecimal;

public class Account {
    private final Long id;

    public Account(Long id) {
        this.id = id;
        this.amount = BigDecimal.ZERO;
    }

    private BigDecimal amount;

    public Long getId() {
        return id;

    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal credit(BigDecimal amount) {
        this.amount = this.amount.add(amount);
        return this.amount;
    }

    public BigDecimal charge(BigDecimal amount) {
        this.amount = this.amount.subtract(amount);
        return this.amount;
    }

}
