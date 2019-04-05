package ts.tsc.logscanner.inputparser;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputParser {
    public static boolean checkArraySize(final int length) {
        return length == 5;
    }
    public static boolean parseNumberOfThreads(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException error) {
            return false;
        }
    }
    public static boolean checkNumberOfThreads(String number) {
        return Integer.parseInt(number) > 0;
    }
    public static boolean directoryNotExists(String directory) {
        try {
            Path path = Paths.get(directory);
            return !Files.exists(path);
        } catch (InvalidPathException exception) {
            return true;
        }
    }
    public static boolean isExtensionMatches(String extension) {
        return extension.matches("(([0-9]*)[a-z]+([0-9]*))+");
    }

}
