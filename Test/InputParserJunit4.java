import org.junit.Assert;
import org.junit.Test;
import ts.tsc.logScanner.inputLine.inputParser.InputParser;

public class InputParserJunit4 extends Assert {

    @Test
    public void validateLineTest() {
        //15; password; c:\logs; c:\temp\out.txt; txt log out err
        assertNotNull(InputParser
                .validateLine("15; password; c:\\logs; c:\\temp\\out.txt; txt log out err"));
        assertNull(InputParser
                .validateLine("-1; password; c:\\logs; c:\\temp\\out.txt; txt log out err"));
        assertNull(InputParser
                .validateLine("15; password; c:\\logs; c:\\temp\\out.txt;"));
        assertNotNull(InputParser
                .validateLine("15; password; c:; c:\\temp\\out.txt; txt log out err"));
        assertNull(InputParser
                .validateLine("15; password; c:\\logs; c:; txt log out err"));
    }
}
