package org.shchek.exps;

import org.objectweb.asm.util.Textifier;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        /*String path = "C:\\SCAS\\build\\agent-1.0-SNAPSHOT\\BOOT-INF\\" +
                "classes\\org\\github\\babkiniaa\\scas\\parsers\\PmdParser.class";*/
        String path = "C:\\Users\\Дмитрий\\Downloads\\progs\\Task5K.class";
//        String[] path = new String[1];
//        path[0] = "C:\\Users\\Дмитрий\\Downloads\\progs\\Task5K.class";
        try (FileImageInputStream file = new FileImageInputStream(new File(path))) {
            final ClassDumper cd = new ClassDumper(file, path);
            cd.dump();
        } catch (IOException e) {
            System.out.println("Closed");
        }
//        Textifier textifier = new Textifier();
//
//        Textifier.main(path);

    }
}
