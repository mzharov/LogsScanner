import org.junit.Assert;
import org.junit.Test;
import ts.tsc.logScanner.inputLine.inputParser.InputParser;

public class InputParserJunit4 extends Assert {

    /**
     * Проверка входной строки на правильность
     */
    @Test
    public void validateLineTest() {
        //15; password; c:\logs; c:\temp\out.txt; txt log out err
        assertNotNull(InputParser
                .validateLine("15; password; c:\\logs; c:\\temp\\out.txt; txt log out err"));
        assertNotNull(InputParser
                .validateLine("15; password; c:; c:\\temp\\out.txt; txt log out err"));
        assertNotNull(InputParser
                .validateLine("15; password; c:; c:\\temp\\out.txt; txt log out e13"));

        System.out.print(1 + " ");
        assertNull(InputParser
                .validateLine("-1; password; c:\\logs; c:\\temp\\out.txt; txt log out err"));
        System.out.print(2 + " ");
        assertNull(InputParser
                .validateLine("15; password; c:\\logs; c:\\temp\\out.txt;"));
        System.out.print(3 + " ");
        assertNull(InputParser
                .validateLine("15; password; c:\\logs; c:; txt log out err"));
        System.out.print(4 + " ");
        assertNull(InputParser
                .validateLine("15; password; c:\\logs; c:; /"));
        System.out.print(5 + " ");
        assertNull(InputParser
                .validateLine("15; password; c:; c:\\temp\\out*.txt; txt log out e13"));
        System.out.print(6 + " ");
        assertNull(InputParser
                .validateLine("15; password; c:; c:\\temp\\out/.txt; txt log out e13"));

    }
}
