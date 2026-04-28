package it.uniroma2.ISW2.Pepe.Federico.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

public class CSVExporter {

    private CSVExporter(){}

    public static <T> void writeToCSV(String fileName, String header, List<T> items, Function<T, String> mapper){
        Path pathDir = Paths.get("output");

        try{
            if(Files.notExists(pathDir)) Files.createDirectories(pathDir);

            File fullPath = pathDir.resolve(fileName).toFile();

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))){
                writer.write(header);
                writer.newLine();

                for(T item : items){
                    writer.write(mapper.apply(item));
                    writer.newLine();
                }

                System.out.println("CSV file created: " + fullPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
