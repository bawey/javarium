package foo.bar.javarium.threading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class BankTest {
    private Bank bank = new Bank();
    private static Logger logger = Logger.getLogger(BankTest.class.getName());

    private long accountsLimit = 1000;

    @Test
    public void testParallelCreationIssues() {
        Long distinctIds = Stream.iterate(1, n -> n + 1).parallel().limit(accountsLimit)
                .map(n -> bank.openAccount().getId()).distinct().count();
        logger.info(String.format("Got %d unique account IDs", distinctIds));
        assertNotEquals(accountsLimit, distinctIds);
    }

    @Test
    public void testSequentialCreationWorks() {
        long distinctIds = Stream.iterate(1, n -> n + 1).limit(accountsLimit).map(n -> bank.openAccount().getId())
                .distinct().count();
        logger.info(String.format("Got %d unique account IDs", distinctIds));
        assertEquals(accountsLimit, distinctIds);
    }

    @Test
    public void testParallelRegistrationWithLock() {
        Long distinctIds = Stream.iterate(1, n -> n + 1).parallel().limit(accountsLimit)
                .map(n -> bank.openAccountWithLock().getId()).distinct().count();
        logger.info(String.format("Got %d unique account IDs using parallel registration with a lock", distinctIds));
        assertEquals(accountsLimit, distinctIds);
    }

}
