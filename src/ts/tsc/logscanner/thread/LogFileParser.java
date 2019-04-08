package ts.tsc.logscanner.thread;

import ts.tsc.logscanner.console.UserInterface;
import ts.tsc.logscanner.inputline.InputLine;

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
public class LogFileParser implements Runnable{

    private final LinkedList<Path> filesList;   // Список, в котором хранятся пути к файлам
    private final InputLine inputLine;          // Структура для хранения входной строки
    private final int threadNumber;             //Номер потока

    /**
     * @param filesList список, хранящий пути к файлам
     * @param inputLine структура, в которой хранится входная строка
     * @param threadNumber номер потока
     */
    public LogFileParser(LinkedList<Path> filesList, InputLine inputLine, int threadNumber) {
        this.filesList = filesList;
        this.inputLine = inputLine;
        this.threadNumber = threadNumber;
    }

    /**
     * Попытка достать из начала связанного списка элемент.
     * Если их там не осталось, завершаем выполнение потока,
     * иначе достаем элемент и вызываем функцию поиска подстроки в файле
     */
    @Override
    public void run() {
        boolean hasNext = true;
        Path path;
        synchronized (filesList) {
            path = filesList.peek();
            if(path != null) {
                filesList.pop();
            } else hasNext = false;
        }
        if(hasNext)  {
            parseFile(path);
            run();
        }
    }

    /**
     * Поиск подстроки в файле
     * @param path путь к файлу
     */
    private void parseFile(Path path) {
        List<String> lines = new LinkedList<>();

        //Начнаем считывать файл в буфер по строкам
        try (BufferedReader bufferedReader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            String line;

            //Счтываем, пока не будет найден конец файла
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
            * Запись в файл черех буферный вывод.
            * Если он существует, режим добавления в конец,
            * иначе создание нового и запись в него
            */
            try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)){
                for(String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }catch(IOException ex){
                System.out.println("Ошибка в ходе записи в файл");
            }

            /*
             * Установка флага, хранящего состояние поиска, в состояние true,
             * если он еще не в этом состоянии
             */
            if(!UserInterface.getFound()) {
                UserInterface.setFoundTrue();
            }
        }
    }
}
