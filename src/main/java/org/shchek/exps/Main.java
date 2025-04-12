package org.shchek.exps;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String path = "C:\\SCAS\\build\\agent-1.0-SNAPSHOT\\BOOT-INF\\" +
                "classes\\org\\github\\babkiniaa\\scas\\parsers\\PmdParser.class";
        try (FileImageInputStream file = new FileImageInputStream(new File(path))) {
            final ClassDumper cd = new ClassDumper(file, path);
            cd.dump();
        } catch (IOException e) {
            System.out.println("Closed");
        }


    }
}
