package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.AnalyzeMetricsCK;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerVersion {

    public void analyzeVersion(String absolutePath, ProjectVersion release, String commitId, String root){
        System.out.println("    >> Analisi della Release: " + release.versionName() + " --> Riferita al seguente Commit: " + commitId);

        MetricsCollector metricsCollector = new MetricsCollector();
        AnalyzeMetricsCK analyzeMetricsCK = new AnalyzeMetricsCK();
        File rootFolder = new File(absolutePath);
        List<File> javaFiles = new ArrayList<>();

        findJavaFile(rootFolder, javaFiles);

        System.out.println("        >> Trovati " + javaFiles.size() + " file .java");

        // Inserimento campi nella MetricsCollector
        metricsCollector.setNumRelease(release.index());
        metricsCollector.setVersionName(release.versionName());
        metricsCollector.setCommitId(commitId);

        for (File f : javaFiles){
            prepareFileForAnalysis(f, metricsCollector, root);
            analyzeMetricsCK.computeMetricsCK(f, metricsCollector);
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

    private void prepareFileForAnalysis(File javaFile, MetricsCollector collector, String root) {
        // 1    Rendiamo la root un percorso assoluto e normalizzato
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();

        // 2    Rendiamo il percorso del file assoluto e normalizzato
        Path filePath = javaFile.toPath().toAbsolutePath().normalize();

        // 3    Calcoliamo il percorso relativo (taglio la parte della root)
        String relativePath = rootPath.relativize(filePath).toString();

        // 4    Uniformiamo i separatori per Git
        relativePath = relativePath.replace("\\", "/");

        // 5    Salviamo nel collector
        collector.setClassPath(relativePath);
    }

}
