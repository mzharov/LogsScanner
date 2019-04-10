package ts.tsc.logScanner.inputLine.inputParser;

import ts.tsc.logScanner.inputLine.InputLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Проверка входной строки на соответствие необходимым параметрам
 */
public class InputParser {
    /**
     * Проверка количества входных параметров
     * @param length длина массива входных параметров
     * @return true - если параметров 5 (расширения пока хранятся в одной строке); иначе - false
     */
    private static boolean checkArraySize(final int length) {
        return length == 5;
    }

    /**
     * Преобразование параметра количества потоков
     * @param number строка, хранящая количество потоков
     * @return true - если значение типа int; иначе - false
     */
    private static boolean parseNumberOfThreads(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    /**
     * Проверка доступности директории для записи
     * @param path Путь к директории
     * @return true - доступна; иначе - false
     */
    private static boolean isDirWritable(String path) {
        return Files.isWritable(Paths.get(path));
    }

    /**
     * Проверка возможности создавать в директории файлы
     * (например для проверки корневых дисков)
     * @param path Путь к директории
     * @return true - возможно; иначе - false
     */
    private static boolean isFileCreatable(String path) {
        if(Files.exists(Paths.get(path))) {
            return true;
        }
        try {
            Files.createFile(Paths.get(path));
            Files.delete(Paths.get(path));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Определение типа файла
     * @param path Путь к файлу
     * @return true - обычный файл, иначе - false
     */
    private static boolean isRegular(String path) {
        if(Files.exists(Paths.get(path))) {
            return Files.isRegularFile(Paths.get(path));
        } else {
            return true;
        }
    }

    /**
     * Проверка количества введенных потоков
     * @param number строка, хранящая количество введенных потоков
     * @return true - если потоков больше 0; иначе false
     */
    private static boolean checkNumberOfThreads(String number) {
        return Integer.parseInt(number) > 0;
    }

    /**
     * Проверка существования директории
     * @param directory строка, хранящая директорию
     * @return true - если заданная директория существует; иначе - false
     */
    private static boolean checkDirectory(String directory) {
        try {
            Path path = Paths.get(directory);
            return !Files.exists(path);
        } catch (InvalidPathException exception) {
            return true;
        }
    }

    /**
     * Проверка на соответствие расширения
     * @param extension строка, хранящая расширение
     * @return true - если соответсвует формату, иначе - false
     */
    private static boolean checkFileName(String extension) {
        return extension.matches("[^/:*?\">|]+");
    }

    /**
     * Проверка на соответствие пути указанным расширениям
     * @param path путь к файлу
     * @param extensions массив заданных расширений
     * @return true - расширение файла соответствует заданным параметрам; иначе - false
     */
    public static boolean isRightExtension(Path path, String[] extensions) {
        String sPath = path.toString().toLowerCase();
        for (String extension : extensions) {
            if(sPath.endsWith("." + extension)) return true;
        }
        return false;
    }

    /**
     * Проверка строки на пустоту
     * @param line строка
     * @return true - если пустая или содержит один пробельный символ
     */
    private static boolean isEmpty(String line) {
        return line.equals(" ") || line.equals("");
    }

    /**
     * Проверка текста ошибки на длину
     * @param line текст ошибки
     * @return true - если длина больше 1; иначе - false
     */
    private static boolean checkMessageLength(String line) {
        return line.length() > 1;
    }

    /**
     * Проверка входной строки на корректность
     * @param inputString входная строка
     * @return  инициализированный объект InputLine если все элементы соответствуют необходимым параметрам;
     *          null - 1) если хотя бы один элемент не соответствует формату,
     *                 2) если указанные директории не существуют,
     *                 3) если не были указаны расширения или они имеют неправильынй формат
     */
    public static InputLine validateLine(String inputString) {

        //Разделение строки и удаление лишних пробельных символов
        String[] line
                = inputString.replace("\\s+", " ").split(";");
        for (int iterator = 0; iterator < line.length; iterator++) {
            line[iterator] = line[iterator].trim();
        }

        if(!InputParser.checkArraySize(line.length)) {
            System.out.println("> Неверный формат входных параметров");
            return null;
        }
        if(!InputParser.parseNumberOfThreads(line[0])) {
            System.out.println("> Указан неверный формат для количества потоков");
            return null;
        }
        if(!InputParser.checkNumberOfThreads(line[0])) {
            System.out.println("> Количество потоков должно быть больше нуля");
            return null;
        }
        if(InputParser.isEmpty(line[1])) {
            System.out.println("> Не указан текст ошибки (введен пробел или пустое значение)");
            return null;
        }
        if(!InputParser.checkMessageLength(line[1])) {
            System.out.println("> Текст для поиска должен содержать более одного символа");
            return null;
        }
        if(InputParser.isEmpty(line[2])) {
            System.out.println("> Не указана директория (введен пробел или пустое значение)");
            return null;
        }
        if(InputParser.checkDirectory(line[2])) {
            System.out.println("> Директории, указанной в качестве начального " +
                    "каталога для поиска не существует: " + line[2]);
            return null;
        }
        if(InputParser.isEmpty(line[3])) {
            System.out.println("> Не указан путь к выходному файлу (введен пробел или пустое значение)");
            return null;
        }

        int delimiter = line[3].lastIndexOf("\\");
        if(delimiter == -1) {
            System.out.println("> Не указан выходной файл");
            return null;
        }
        String tmpOutPath = line[3].substring(0, delimiter);
        String tmpFile = line[3].substring(delimiter+1);
        if(InputParser.checkDirectory(tmpOutPath)) {
            System.out.println("> Директории, указанной для выходного файла не существует: "
                    + tmpOutPath);
            return null;
        }

        if(!InputParser.isDirWritable(tmpOutPath)) {
            System.out.println("> Директория, указанная для выходного файла недоступна для записи: "
                    + tmpOutPath);
            return null;
        }

        if(!InputParser.checkFileName(tmpFile)) {
            System.out.println("> Указанное для выходного файла имя содержит недопустимые символы: "
                    + tmpFile);
            return null;
        }

        if(!InputParser.isRegular(line[3])) {
            System.out.println("> Не указан выходной файл");
            return null;
        }


        if(!InputParser.isFileCreatable(line[3])) {
            System.out.println("> Нельзя создать выходной файл в указанной директории: "
                    + tmpOutPath);
            return null;
        }


        if(InputParser.isEmpty(line[4])) {
            System.out.println("> Не указаны расширения (введен пробел или пустое значение)");
            return null;
        }
        String[] extensions = line[4].toLowerCase().split("\\s+");
        boolean[] validExtensions = new boolean[extensions.length];
        int count = 0;
        for(int iterator = 0; iterator < validExtensions.length; iterator++) {
            if(InputParser.checkFileName(extensions[iterator])) {
                validExtensions[iterator] = true;
                count++;
            } else {
                System.out.println("> Строка " + extensions[iterator]
                        + " не соответствует формату расширений");
            }
        }

        if(count < 1) {
            System.out.println("> В списке расширений нет подходящих под формат расширений");
            return null;
        }

        return new InputLine(line[0], line[1], line[2], line[3], extensions);
    }
}
