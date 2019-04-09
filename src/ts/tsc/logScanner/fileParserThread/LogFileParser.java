package ts.tsc.logScanner.fileParserThread;

import ts.tsc.logScanner.console.Console;
import ts.tsc.logScanner.console.ConsoleInterface;
import ts.tsc.logScanner.inputLine.InputLine;
import ts.tsc.logScanner.observing.Observable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Поиск в файле указанной подстроки
 */
public class LogFileParser implements Runnable, Observable {

    private final ConsoleInterface console;     //Интерфейс для доступа к списку путей к файлам
    private final InputLine inputLine;          // Структура для хранения входной строки
    private final int threadNumber;             //Номер потока

    /**
     * @param inputLine структура, в которой хранится входная строка
     * @param threadNumber номер потока
     */
    public LogFileParser(ConsoleInterface console, InputLine inputLine, int threadNumber) {
        this.console = console;
        this.inputLine = inputLine;
        this.threadNumber = threadNumber;
    }

    /**
     * Пока список путей к файлам не будет пуст и начальная директоря не будет
     * полностью обойдена, попытка получить элемент из списка.
     * Если объект получен, парсинг файла с указанным путем,
     * иначе ожидание оповещения о том, что в список добавлен элемент
     */
    @Override
    public synchronized void run() {
        Path path = null;
        while (!(path == null && console.isSearchFinished())) {
            path = console.popListElement();
            if(path == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                parseFile(path);
            }
        }
    }

    /**
     * Поиск подстроки в файле
     * @param path путь к файлу
     */
    private void parseFile(Path path) {
        List<String> lines = new LinkedList<>();

        //Начнаем считывать файл в буфер по строкам
        try (BufferedReader bufferedReader = Files.newBufferedReader(path, Charset.forName("ISO-8859-1"))) {
            String line;

            //Считываем, пока не будет найден конец файла
            while ((line = bufferedReader.readLine()) != null) {

                //Проверяем считанную строку на содержание подстроки
                if(line.toLowerCase().contains(inputLine.getErrorMessage().toLowerCase())) {

                    String directory = inputLine.getInputDir();
                    String pathString = path.toString();

                    //Преобразование путей в необходимый формат
                    int index = pathString.lastIndexOf("\\");
                    String subDirectory = pathString.substring(directory.length(), index+1);
                    String fileName = pathString.substring(index+1);
                    if(subDirectory.equals("\\")) {
                        subDirectory = ".\\";
                    } else {
                        subDirectory = "." + subDirectory.substring(0, subDirectory.length()-1);
                    }

                    //Запись строки в список
                    lines.add("[" + threadNumber + "] "
                            + subDirectory + " - "
                            + fileName + ": "
                            + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка в ходе чтения файла " + path.toString());
            //e.printStackTrace();
            return;
        }
        //Вызов метода для записи в списка в файл
        writeToFile(lines, Paths.get(inputLine.getOutputPath()));
    }

    /**
     * Синхронизированный метод для записи списка строк в файл
     * @param lines список строк
     * @param path путь к файлу
     */
    private synchronized void writeToFile(List<String> lines, Path path) {
        //Если список не пуст начинаем запись
        if(lines.size() > 0) {
            /*
            * Запись в файл через буферный вывод.
            * Если он существует, режим добавления в конец,
            * иначе создание нового и запись в него
            */
            try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("ISO-8859-1"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)){

                // Запись списка строковых значений в файл
                for(String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }

                /*
                 * Установка флага, хранящего состояние поиска, в состояние true,
                 * если он еще не в этом состоянии
                 */
                if(!Console.getFound()) {
                    Console.setFoundTrue();
                }

            }catch(IOException ex){
                System.out.println("Ошибка в ходе записи в файл");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void update() {
        notifyAll();
    }
}
