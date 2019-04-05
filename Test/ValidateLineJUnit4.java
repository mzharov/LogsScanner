import org.junit.Assert;
import org.junit.Test;
import ts.tsc.logscanner.console.UserInterface;

public class ValidateLineJUnit4 extends Assert {

    @Test
    public void validateLineTest() {
        assertTrue(UserInterface.validateLine("10; Error; C:\\logs; c:\\temp\\out.txt; txt log out"));
        assertFalse(UserInterface.validateLine("; Error; C:\\logs; c:\\temp\\out.txt; txt log out"));
    }
}
