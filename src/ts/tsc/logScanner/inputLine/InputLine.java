package ts.tsc.logScanner.inputLine;

/**
 * Класс для хранения введенных данных
 */
public class InputLine {
    private final int numberOfThreads;    //Количество потоков
    private final String errorMessage;    //Подстрока с текстом для поиска
    private final String inputDir;        //Начальная директория
    private final String outputPath;      //Выходной файл
    private final String[] extensions;    //Массив необходимых расширений файлов

    public InputLine(String numberOfThreads, String errorMessage, String inputDir, String outputPath, String[] extensions) {
        this.numberOfThreads = Integer.parseInt(numberOfThreads);
        this.errorMessage = errorMessage;
        this.inputDir = inputDir;
        this.outputPath = outputPath;
        this.extensions = extensions;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getInputDir() {
        return inputDir;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
}
