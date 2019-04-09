package ts.tsc.logscanner.diretorythread;

import ts.tsc.logscanner.console.ConsoleInterface;
import ts.tsc.logscanner.inputline.inputparser.InputParser;
import ts.tsc.logscanner.observing.Observable;

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
    private final String directory;             //Начальная директория
    private final String[] extensions;          //Список расширений
    private final Observable[] observables;     /*Массив "наблюдателей", которых надо оповестить
                                                о добавлении элемента в список*/

    public CheckDirectory(ConsoleInterface console,
                          String directory,
                          String[] extensions,
                          Observable[] observables) {
        this.console = console;
        this.directory = directory;
        this.extensions = extensions;
        this.observables = observables;
    }

    /**
     * Поиск в директории.
     * @param path директория
     */
    private void search(Path path) {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            for(Path entry : dirStream) {

                //Проверка доступности файла
                if(Files.isReadable(entry)) {
                    BasicFileAttributes attrs =
                            Files.readAttributes(entry, BasicFileAttributes.class);

                    /* Если файл является обычным и его расширение есть в списке заданных,
                     * то осуществляется добавление пути к нему в список и оповещение наблюдателей
                     */
                    if(attrs.isRegularFile() && InputParser.isRightExtension(entry, extensions)) {
                        console.addListElement(entry);
                        updateObservers();
                    } else if(attrs.isDirectory()) {
                        //Если файл является папков, то осуществялется рекурсивный вызов для нее
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

        search(Paths.get(directory));

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
