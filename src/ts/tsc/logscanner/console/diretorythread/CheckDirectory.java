package ts.tsc.logscanner.console.diretorythread;

import ts.tsc.logscanner.console.ConsoleInterface;
import ts.tsc.logscanner.inputline.inputparser.InputParser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class CheckDirectory implements Runnable{
    private final ConsoleInterface console;
    private final String directory;
    private final String[] extensions;

    public CheckDirectory(ConsoleInterface console, String directory, String[] extensions) {
        this.console = console;
        this.directory = directory;
        this.extensions = extensions;
    }

    private void search(Path path) {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            for(Path entry : dirStream) {
                if(Files.isReadable(entry)) {
                    BasicFileAttributes attrs =
                            Files.readAttributes(entry, BasicFileAttributes.class);
                    if(attrs.isRegularFile() && InputParser.isRightExtension(entry, extensions)) {
                        console.addListElement(entry);
                    } else if(attrs.isDirectory()) {
                        search(entry);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("> Ошибка в ходе просмотра директории, повторите запрос");
        }
    }
    @Override
    public void run() {
        console.setDirEndFalse();
        search(Paths.get(directory));
        console.setDirEndTrue();
    }
}
