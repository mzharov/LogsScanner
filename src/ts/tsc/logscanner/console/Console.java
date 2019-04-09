package ts.tsc.logscanner.console;

import ts.tsc.logscanner.console.diretorythread.CheckDirectory;
import ts.tsc.logscanner.inputline.InputLine;
import ts.tsc.logscanner.inputline.inputparser.InputParser;
import ts.tsc.logscanner.thread.LogFileParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

public class Console implements ConsoleInterface{

    private static InputLine inputLine;         //Структура для хранения строки
    private static final
    LinkedList<Path> filesList = new LinkedList<>();  //Список для хранения путей к файлам
    private static boolean found  = false;      //Флаг, показывающий, были ли найдены подстроки в указанных файлах
    private static boolean dirEnd;

    /**
     * Проверка входной строки на корректность
     * @param inputString входная строка
     * @return  true - если все элементы соотвествуют необходимым параметрам;
     *          false -  1) если хотя бы один элемент не соответствует формату,
     *                   2) если указанные директории не существуют,
     *                   3) если не были указаны расширения или они имеют неправильынй формат
     */
    private static boolean validateLine(String inputString) {

        //Разделение строки и удаление лишних пробельных символов
        String[] line
                = inputString.replace("\\s+", " ").split(";");
        for (int iterator = 0; iterator < line.length; iterator++) {
            line[iterator] = line[iterator].trim();
        }

        if(!InputParser.checkArraySize(line.length)) {
            System.out.println("> Неверный формат входных параметров");
            return false;
        }
        if(!InputParser.parseNumberOfThreads(line[0])) {
            System.out.println("> Указан неверный формат для количества потоков");
            return false;
        }
        if(!InputParser.checkNumberOfThreads(line[0])) {
            System.out.println("> Количество потоков должно быть больше нуля");
            return false;
        }
        if(InputParser.isEmpty(line[1])) {
            System.out.println("> Не указан текст ошибки (введен пробел или пустое значение)");
            return false;
        }
        if(!InputParser.checkMessageLength(line[1])) {
            System.out.println("> Текст для поиска должен содержать более одного символа");
            return false;
        }
        if(InputParser.isEmpty(line[2])) {
            System.out.println("> Не указана директория (введен пробел или пустое значение)");
            return false;
        }
        if(InputParser.checkDirectory(line[2])) {
            System.out.println("> Директории, указанной в качестве начального " +
                    "каталога для поиска не существует: " + line[2]);
            return false;
        }
        if(InputParser.isEmpty(line[3])) {
            System.out.println("> Не указан путь к выходному файлу (введен пробел или пустое значение)");
            return false;
        }

        int delimiter = line[3].lastIndexOf("\\");
        String tmpOutPath = line[3].substring(0, delimiter);
        String tmpFile = line[3].substring(delimiter+1);
        if(InputParser.checkDirectory(tmpOutPath)) {
            System.out.println("> Директории, указанной для выходного файла не существует: "
                    + tmpOutPath);
            return false;
        }
        if(!InputParser.isAccessible(tmpOutPath)) {
            System.out.println("> Директория, указанная для выходного файла недоступна для записи: "
                    + tmpOutPath);
            return false;
        }
        if(!InputParser.isRegular(line[3])) {
            System.out.println("> " + tmpFile + " не является файлом");
            return false;
        }

        if(InputParser.isEmpty(line[4])) {
            System.out.println("> Не указаны расширения (введен пробел или пустое значение)");
            return false;
        }
        String[] extensions = line[4].toLowerCase().split("\\s+");
        boolean[] validExtensions = new boolean[extensions.length];
        int count = 0;
        for(int iterator = 0; iterator < validExtensions.length; iterator++) {
            boolean isMatches = InputParser.checkExtension(extensions[iterator]);
            if(isMatches) {
                validExtensions[iterator] = true;
                count++;
            } else {
                System.out.println("> Строка " + extensions[iterator]
                        + " не соответствует формату расширений");
            }
        }

        if(count < 1) {
            System.out.println("> В списке расширений нет подходящих под формат расширений");
            return false;
        }

        inputLine = new InputLine(line[0], line[1], line[2], line[3], extensions);
        return true;
    }

    /**
     * Изменение из потока состояния флага записи на true
     */
    public static void setFoundTrue() {
        found = true;
    }
    private static void setFoundFalse() {
        found = false;
    }
    public static boolean getFound() {return found;}


    /**
     * Поиск подстроки в списке файлов из директории
     */
    private void search() {

        Thread dirThread = new Thread(new CheckDirectory(this,
                inputLine.getInputDir(),
                inputLine.getExtensions()));
        dirThread.start();

        //Установка времени начала поиска
        double startTime = System.nanoTime();

        //Создание необходимого количества потоков с заданными параметрами
        Thread[] parseThreads = new Thread[inputLine.getNumberOfThreads()];
        for(int iterator = 0; iterator < parseThreads.length; iterator++) {
            parseThreads[iterator] =
                    new Thread(new LogFileParser(this, inputLine, iterator+1));
            parseThreads[iterator].start();
        }

        //Ожидание выполнения потоков в главном потока
        for (Thread parseThread : parseThreads) {
            try {
                parseThread.join();
            } catch (InterruptedException e) {
                System.out.println("> Ошибка в ходе работы потоков");
            }
        }

        //Вычисление времени выполнения поиска
        double timeSpent = System.nanoTime() - startTime;

        //Запись в файл списка строк, если они были найдены и файл существует
        File file = new File(inputLine.getOutputPath());
        if(file.exists() && found) {
            try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(inputLine.getOutputPath()),
                    Charset.forName("UTF-8"), StandardOpenOption.APPEND)){
                writer.newLine();
                String time = "Поиск длился всего: " + timeSpent/1000000000 + " секунд";
                writer.write(time);
                writer.newLine();
                writer.newLine();
                System.out.println("> Данные записаны в файл " + file);
            } catch(IOException ex){
                System.out.println("> Ошибка в ходе записи в файл");
                ex.printStackTrace();
            }
        }
        if(!found) {
            System.out.println("> В логах не было найдено указанной подстроки: "
                    + inputLine.getErrorMessage());
        }
        if(!file.exists() && found) {
            System.out.println("> Выходной файл с результатами поиска не найден, возможно он был удален");
        }

        //Установление флага в состояние false
        setFoundFalse();
    }

    /**
     * Считывание входных параметров и запуск потоков обработки
     */
    public void main() {
        System.out.println("> Введите через точку с запятой (;) данные для поиска в логах в указанном формате:\n" +
                "количество потоков; текст для поиска (более одного символа); начальный каталог; путь до выходного файла; " +
                "список расширений, в которых будет осуществляться поиск (одно или более)\n" +
                "Пример: 15; password; c:\\logs; c:\\temp\\out.txt; txt log out err\n" +
                "для выхода введите команду exit");

        //Считывание ввода данных с консоли
        try(BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in))) {

            //Бесконечный цикл для реализации консольного UI
            while (true) {

                //Считывание из буфера в строку
                String input = inputBuffer.readLine();

                //Выход, если введено ключевое слово exit
                if (input.toLowerCase().equals("exit")) {
                    break;
                }

                //Проверка введенных данных
                if(validateLine(input)) {
                    search();
                    System.out.println("> Введите новый запрос для поиска или введите слово exit  для выхода");
                }
            }

        } catch (IOException e) {
            System.out.println("> Ошибка в ходе чтения с консоли");
        }
        System.out.println("> Выполнение программы закончено");
    }

    @Override
    public synchronized Path popListElement() {
        if(filesList.size() > 0) {
            return filesList.pop();
        } else {
            return null;
        }
    }

    @Override
    public synchronized void addListElement(Path list) {
        filesList.add(list);
    }

    @Override
    public synchronized boolean isSearchFinished() {
        return dirEnd;
    }

    @Override
    public synchronized void setDirEndTrue() {
        dirEnd = true;
    }
    @Override
    public synchronized void setDirEndFalse() {
        dirEnd = false;
    }
}
