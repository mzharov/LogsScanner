package ts.tsc.logScanner.directory;

import ts.tsc.logScanner.console.ConsoleInterface;
import ts.tsc.logScanner.inputLine.LineInterface;
import ts.tsc.logScanner.inputLine.inputParser.InputParser;
import ts.tsc.logScanner.observing.Observable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Класс для обхода начальной директории
 */
public class CheckDirectory implements Runnable {
    private final ConsoleInterface console;     //Интерфейс класс Console для доступа к списку путей
    private final LineInterface line;
    private final Observable[] observables;     /*Массив "наблюдателей", которых надо оповестить
                                                о добавлении элемента в список*/

    public CheckDirectory(ConsoleInterface console,
                          LineInterface line,
                          Observable[] observables) {
        this.console = console;
        this.line = line;
        this.observables = observables;
    }

    /**
     * Поиск в директории файлов, имеющих заданные расширения.
     * @param path директория
     */
    private void search(Path path) {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            for(Path entry : dirStream) {

                //Проверка доступности файла
                if(Files.isReadable(entry)) {
                    BasicFileAttributes attrs =
                            Files.readAttributes(entry, BasicFileAttributes.class);

                    /*
                     * Если файл является обычным и его расширение есть в списке заданных,
                     * то осуществляется добавление пути к нему в список и оповещение наблюдателей
                     */
                    if(attrs.isRegularFile()
                            && InputParser.isRightExtension(entry, line.getExtensions())
                            && !(entry.toString()).equals(line.getOutputPath())) {
                        console.addListElement(entry);
                        updateObservers();
                    } else if(attrs.isDirectory()) {
                        //Если файл является папкой, то осуществялется рекурсивный вызов для нее
                        search(entry);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("> Ошибка в ходе просмотра директории");
        }
    }

    @Override
    public void run() {
        //Установка флага окончания обхода директории в состояние false
        console.setDirEndFalse();

        search(Paths.get(line.getInputDir()));

        //Установка флага окончания обхода директории в состояние false
        console.setDirEndTrue();
        //Оповещение наблюдателей об окончании обхода директории
        updateObservers();
    }

    /**
     * Оповещение наблюдателей об изменении состояния
     */
    private void updateObservers() {
        for(Observable observable : observables) {
            observable.update();
        }
    }
}
