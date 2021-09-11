package foo.bar.javarium.threading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.stream.Stream;

public class MultiWrite {
    public static void main(String[] args) {
        Stream.iterate(0, i -> i + 1).parallel().limit(6).forEach(i -> MultiWrite.writeStuff());
    }

    public static void writeStuff() {

        File destination = Path.of(System.getProperty("user.home"), "/Desktop/multiWrite.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination, true))) {
            for (int i = 0; i < 100; ++i) {
                writer.write(MessageFormat.format("Write number {0} from thread {1}\n", i, Thread.currentThread().getName()));
                Thread.sleep((long) (Math.random() * 100));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
