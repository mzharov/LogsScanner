package ts.tsc.logscanner.console;

import ts.tsc.logscanner.inputparser.InputParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

class UserInterface {

    private final static String EXIT = "exit";
    private static String[] inputLineArray;

    /**
     * Проверка входной строки на корректность
     * @param line входная строка
     * @return  true - если все элементы соотвествуют необходимым параметрам;
     *          false -  1) если хотя бы один элемент не соответствует формату,
     *                   2) если указанные директории не существуют,
     *                   3) если не были указаны расширения или они имеют неправильынй формат
     */
    private static boolean validateLine(String[] line) {
        if(!InputParser.checkArraySize(line.length)) {
            System.out.println("> Неверный формат входных параметров");
            return false;
        }
        if(!InputParser.parseNumberOfThreads(line[0])) {
            System.out.println("> Указан неверный формат количества потоков");
            return false;
        }
        if(!InputParser.checkNumberOfThreads(line[0])) {
            System.out.println("> Количество потоков должно быть больше нуля");
            return false;
        }
        if(InputParser.directoryNotExists(line[2])) {
            System.out.println("> Директории, указанной в качестве начального каталога для поиска не существует");
            return false;
        }
        int delimiter = line[3].lastIndexOf("\\");
        if(InputParser.directoryNotExists(line[3].substring(0, delimiter))) {
            System.out.println("> Директории, указанной для выходного файла не существует");
            return false;
        }

        String[] extensions = line[4].toLowerCase().split("\\s+");
        boolean[] validExtensions = new boolean[extensions.length];
        int count = 0;
        for(int iterator = 0; iterator < validExtensions.length; iterator++) {
            boolean isMatches = InputParser.isExtensionMatches(extensions[iterator]);
            if(isMatches) {
                validExtensions[iterator] = true;
                count++;
            } else {
                System.out.println("Строка " + extensions[iterator] + " не соответствует формату расширений");
            }
        }

        if(count < 1) {
            System.out.println("В списке расширений нет подходящих под формат расширений");
            return false;
        }
        inputLineArray = new String[line.length + count-1];
        System.arraycopy(line, 0, inputLineArray, 0, line.length - 1);

        for(int mainIterator = line.length-1, iterator = 0; iterator < extensions.length; iterator++) {
            if(validExtensions[iterator]) {
                inputLineArray[mainIterator] = extensions[iterator];
                mainIterator++;
            }
        }
        System.out.println(Arrays.toString(inputLineArray));
        return true;
    }

    /**
     * Считывание входных параметров и запус потоков обработки
     * @param args стандартные входные параметры
     */
    public static void main(String[] args) {
        System.out.println("> Введите через точку с запятой данные для поиска в логах в указанном формате: \n" +
                "количество потоков; код ошибки; начальный каталог; путь до выходного файла; " +
                "список расширений, в которых будет осуществляться поиск (одно или более) \n" +
                "для выхода введите команду exit");
        boolean isReadLine = true;
        try(BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in))) {
            while (isReadLine) {
                String inputLine = inputBuffer.readLine();
                if (inputLine.equals(EXIT)) {
                    isReadLine = false;
                }
                String[] inputLineArrayTemp = inputLine.replace("\\s+", " ").split(";");

                for (int iterator = 0; iterator < inputLineArrayTemp.length; iterator++) {
                    inputLineArrayTemp[iterator] = inputLineArrayTemp[iterator].trim();
                }
                if(validateLine(inputLineArrayTemp)) break;
            }

        } catch (IOException e) {
            System.out.println("Ошибка в ходе чтения с консоли.");
        }
    }
}
