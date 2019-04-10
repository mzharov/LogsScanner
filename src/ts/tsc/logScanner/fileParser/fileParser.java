package ts.tsc.logScanner.fileParser;

import ts.tsc.logScanner.console.Console;
import ts.tsc.logScanner.console.ConsoleInterface;
import ts.tsc.logScanner.inputLine.LineInterface;
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
public class fileParser implements Runnable, Observable {

    private final ConsoleInterface console;     //Интерфейс для доступа к списку
    private final LineInterface inputLine;
    private final int threadNumber;             //Номер потока

    /**
     * @param inputLine структура, в которой хранится входная строка
     * @param threadNumber номер потока
     */
    public fileParser(ConsoleInterface console, LineInterface inputLine, int threadNumber) {
        this.console = console;
        this.inputLine = inputLine;
        this.threadNumber = threadNumber;
    }

    /**
     * Пока список путей к файлам не будет пуст и начальная директория не будет
     * полностью просканирована (проверка по флагу), попытка получить элемент из списка.
     * Если объект получен, парсинг файла с указанным путем,
     * иначе если список пуст - ожидание оповещения о том, что в список добавлен элемент
     */
    @Override
    public synchronized void run() {
        while (!console.isSearchFinished()) {
            Path path = console.popListElement();
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

            try {
                //Считываем, пока не будет найден конец файла
                while ((line = bufferedReader.readLine()) != null) {
                    //Проверяем считанную строку на содержание подстроки
                    if(line.toLowerCase().contains(inputLine.getErrorMessage().toLowerCase())) {
                        //Запись строки в список
                        lines.add("[" + threadNumber + "] "
                                + subDirectory + " - "
                                + fileName + ": "
                                + line);
                    }
                }
            } catch (OutOfMemoryError e) {
                System.out.println("В файле " + path +
                        " не удалось произвести поиск подстроки в строке, " +
                        "так как она имеет слишком большой размер");
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
        int i = -1;
        //Если список не пуст начинаем запись
        if(lines.size() > 0) {
            /*
            * Запись в файл через буферный вывод.
            * Если он существует, режим добавления в конец,
            * иначе создание нового и запись в него
            */
            try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)){

                // Запись списка строковых значений в файл
                for(String line : lines) {
                    i++;
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
                System.out.println(lines.get(i));
                System.out.println("Ошибка в ходе записи в файл");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void update() {
        notify();
    }
}
