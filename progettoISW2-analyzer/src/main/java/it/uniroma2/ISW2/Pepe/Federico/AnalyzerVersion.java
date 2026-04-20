package it.uniroma2.ISW2.Pepe.Federico;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerVersion {

    public void analyzeVersion(String absolutePath, ProjectVersion release){
        System.out.println("    >> Analisi della Release: " + release.versionName());

        AnalyzeJavaClass analyzeJavaClass = new AnalyzeJavaClass();
        File rootFolder = new File(absolutePath);
        List<File> javaFiles = new ArrayList<>();

        findJavaFile(rootFolder, javaFiles);

        System.out.println("        >> Trovati " + javaFiles.size() + " file .java");

        for (File f : javaFiles){
            analyzeJavaClass.computeMetricsCK(f);
        }

        System.out.println("    >> Analisi della release " + release.versionName() + " completata!");
    }

    private void findJavaFile(File file, List<File> resultList) {
        if (file == null || !file.exists()) return;

        if (file.isDirectory()) {
            String folderName = file.getName().toLowerCase();

            if (folderName.equals(".git") ||
                    folderName.equals("test") ||
                    folderName.equals("tests") ||
                    folderName.equals("target")) {
                return;
            }

            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    findJavaFile(child, resultList);
                }
            }
        } else if (file.isFile()) {
            String fileName = file.getName();

            if (fileName.endsWith(".java")) {
                if (!fileName.contains("Test") && !fileName.contains("Tests")) resultList.add(file);
            }

        }
    }

}
