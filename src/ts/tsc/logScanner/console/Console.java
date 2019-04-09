package ts.tsc.logScanner.console;

import ts.tsc.logScanner.diretoryThread.CheckDirectory;
import ts.tsc.logScanner.inputLine.InputLine;
import ts.tsc.logScanner.inputLine.inputParser.InputParser;
import ts.tsc.logScanner.fileParserThread.LogFileParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

public class Console implements ConsoleInterface{

    private static InputLine inputLine;                 //Структура для хранения строки
    private static final
    LinkedList<Path> filesList = new LinkedList<>();    //Список для хранения путей к файлам
    private static boolean found  = false;              //Флаг, показывающий,
                                                        // были ли найдены подстроки в указанных файлах
    private static boolean dirEnd;

    /**
     * Проверка выходной строки на корректность
     * @param inputString строка
     * @return true - корректа, иначе - false
     * результат зависит от метода
     * {@link ts.tsc.logScanner.inputLine.inputParser.InputParser#validateLine(String)} ;}
     */
    private static boolean validateLine(String inputString) {
        inputLine = InputParser.validateLine(inputString);
        return inputLine != null;
    }


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

        LogFileParser[] fileParsers = new LogFileParser[inputLine.getNumberOfThreads()];
        for(int iterator = 0; iterator < fileParsers.length; iterator++) {
            fileParsers[iterator] =
                    new LogFileParser(this, inputLine, iterator+1);
        }

        Thread dirThread = new Thread(
                new CheckDirectory(this,
                inputLine.getInputDir(),
                inputLine.getExtensions(),
                fileParsers));
        dirThread.start();

        //Установка времени начала поиска
        double startTime = System.nanoTime();

        //Создание необходимого количества потоков с заданными параметрами
        Thread[] parseThreads = new Thread[inputLine.getNumberOfThreads()];
        for(int iterator = 0; iterator < parseThreads.length; iterator++) {
            parseThreads[iterator] = new Thread(fileParsers[iterator]);
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
                    //Поиск в директории если введена верная строка
                    search();
                    System.out.println("> Введите новый запрос для поиска " +
                            "или введите слово exit  для выхода");
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
