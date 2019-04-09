package ts.tsc.logscanner.inputline.inputparser;

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
    public static boolean checkArraySize(final int length) {
        return length == 5;
    }

    /**
     * Преобразование параметра количества потоков
     * @param number строка, хранящая количество потоков
     * @return true - если значение типа int; иначе - false
     */
    public static boolean parseNumberOfThreads(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException error) {
            return false;
        }
    }
    public static boolean isAccessible(String path) {
        return Files.isWritable(Paths.get(path));
    }
    public static boolean isRegular(String path) {
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
    public static boolean checkNumberOfThreads(String number) {
        return Integer.parseInt(number) > 0;
    }

    /**
     * Проверка существования директории
     * @param directory строка, хранящая директорию
     * @return true - если заданная директория существует; иначе - false
     */
    public static boolean checkDirectory(String directory) {
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
    public static boolean checkExtension(String extension) {
        return extension.matches("(([0-9]*)[a-z]+([0-9]*))+");
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
    public static boolean isEmpty(String line) {
        return line.equals(" ") || line.equals("");
    }

    /**
     * Проверка текста ошибки на длину
     * @param line текст ошибки
     * @return true - если длина больше 1; иначе - false
     */
    public static boolean checkMessageLength(String line) {
        return line.length() > 1;
    }
}
