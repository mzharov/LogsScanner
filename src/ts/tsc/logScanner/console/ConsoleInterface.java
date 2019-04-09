package ts.tsc.logScanner.console;

import java.nio.file.Path;

public interface ConsoleInterface {
    void main();
    Path popListElement();
    void addListElement(Path path);
    boolean isSearchFinished();
    void setDirEndTrue();
    void setDirEndFalse();
}
