package ts.tsc.logScanner.console;

import java.nio.file.Path;

/**
 * Обобщенный интерфейс для консоли
 */
public interface ConsoleInterface {

    /**
     * Вызов основного метода
     */
    void main();

    /**
     * Получение элемента из начала списка
     *
     * @return Путь к файлу
     */
    Path popListElement();

    /**
     * Добавление элемента
     *
     * @param path Путь к файлу
     */
    void addListElement(Path path);

    /**
     * Проверка завершения поиска файло в директории
     *
     * @return true - поиск закончен, иначе - false
     */
    boolean isSearchFinished();

    /**
     * Установка флага окончания поиска в состояние - true
     */
    void setDirEndTrue();

    /**
     * Установка флага окончания поиска в состояние - false
     */
    void setDirEndFalse();
}
