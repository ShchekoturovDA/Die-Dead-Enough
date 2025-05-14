package org.shchek.exps;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Dumper {
    public static List<Summary> process(String path){
        List<Path> procFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path)))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    process(entry, procFiles);
                } else if(entry.toString().endsWith(".class")) {
                    procFiles.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Summary> summaries = new ArrayList<>();
        for(Path path1 : procFiles){
            try (FileImageInputStream file = new FileImageInputStream(new File(String.valueOf(path1)))) {

                final ClassDumper cd = new ClassDumper(file, String.valueOf(path1));
                List<Summary> sums = cd.dump();
                summaries.addAll(sums);
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }
        return summaries;
    }

    public static List<Path> process(Path path, List<Path> procFiles){
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    process(entry, procFiles);
                } else if(entry.endsWith(".class")) {
                    procFiles.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return procFiles;
    }

}
