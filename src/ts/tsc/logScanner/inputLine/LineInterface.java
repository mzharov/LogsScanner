package ts.tsc.logScanner.inputLine;

/**
 * Интерйес входной строки
 */
public interface LineInterface {
    String getErrorMessage();
    String getInputDir();
    String getOutputPath();
    String[] getExtensions();
}
