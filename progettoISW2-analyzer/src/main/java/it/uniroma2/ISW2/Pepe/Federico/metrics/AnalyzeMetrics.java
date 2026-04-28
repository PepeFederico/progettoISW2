package it.uniroma2.ISW2.Pepe.Federico.metrics;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKMethodResult;
import it.uniroma2.ISW2.Pepe.Federico.ProjectVersion;
import it.uniroma2.ISW2.Pepe.Federico.SmellAnalyzerPMD;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.lang.reflect.Modifier;

public class AnalyzeMetrics {

    private final CK ckEngine;
    private final SmellAnalyzerPMD smellAnalyzer;

    public AnalyzeMetrics() {
        this.ckEngine       = new CK(false, 0, false);
        this.smellAnalyzer  = new SmellAnalyzerPMD();
    }

    public void computeMetricsBatch(String projectPath, String commitId, ProjectVersion release, String root, Map<String, MetricsCollector> resultMap){
        // Pre-calcoliamo il Path della root per evitare di farlo nel loop
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();

        Map<String, Integer> smellsMap = smellAnalyzer.computeSmells(projectPath, root);

        ckEngine.calculate(projectPath, result -> {
            // 1. Otteniamo il percorso relativo "pulito" (es: storm-core/src/...)
            String relativePath = getRelativePath(result.getFile(), rootPath);

            // 2. Filtriamo usando il percorso relativo
            if (result.getType().equals("class") &&
                    !relativePath.contains("/test/") &&
                    !relativePath.contains("/target/") &&
                    !result.getClassName().contains("Test")) {

                MetricsCollector mc = new MetricsCollector(
                        release.index(),
                        release.versionName(),
                        commitId);

                // Settiamo il path nel formato corretto modulo/src/...
                mc.setClassPath(relativePath);

                updateCollector(result, mc);

                // Recupero e settaggio degli smells
                int nSmells = smellsMap.getOrDefault(relativePath, 0);
                mc.setnSmells(nSmells);

                // USIAMO IL PATH RELATIVO COME CHIAVE
                resultMap.put(relativePath, mc);
            }
        });
    }

    private void updateCollector(CKClassResult result, MetricsCollector collector) {
        // Cache del numero di metodi per efficienza
        int nom = result.getMethods().size();

        // --- Metriche di Accoppiamento ---
        collector.setCbo(result.getCbo());
        collector.setRfc(result.getRfc());

        // --- Metriche di Ereditarietà ---
        collector.setDit(result.getDit());
        collector.setMfa(calculateMfa(result)); // Corretto qui!

        // --- Metriche di Coesione e Complessità ---
        collector.setWmc(result.getWmc());
        collector.setLcom(result.getLcom());

        // --- Metriche di Size ---
        collector.setLoc(result.getLoc());
        collector.setnOfMethod(nom);
        collector.setnOfField(result.getNumberOfFields());

        // --- Metriche Bansiy & Davis ---
        collector.setNpm(getPublicMethodsCount(result));
        collector.setDam(getDataAccessMetric(result));

        // --- AMC ---
        if (nom > 0) {
            collector.setAmc((float) result.getWmc() / nom);
        } else {
            collector.setAmc((float) 0.0);
        }
    }

    private long getPublicMethodsCount(CKClassResult result) {
        // --- NPM (Numero di metodi pubblici) ---
        long publicMethodsCount = 0;
        for (CKMethodResult m : result.getMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                publicMethodsCount++;
            }
        }
        return publicMethodsCount;
    }

    private float getDataAccessMetric(CKClassResult result) {
        int totalFields = result.getNumberOfFields();
        if (totalFields > 0) {
            int privateFields = result.getNumberOfPrivateFields();
            int protectedFields = result.getNumberOfProtectedFields();

            // DAM = (Private + Protected) / Total
            return (float) (privateFields + protectedFields) / totalFields;
        } else {
            return (float) 1.0;  // Se non ci sono campi, l'incapsulamento è considerato massimo
        }
    }

    private String getRelativePath(String absoluteFilePath, Path rootPath) {
        Path filePath = Paths.get(absoluteFilePath).toAbsolutePath().normalize();
        return rootPath.relativize(filePath).toString().replace("\\", "/");
    }

    private float calculateMfa(CKClassResult result) {
        // Se la classe è alla radice, l'astrazione funzionale ereditata è 0
        if (result.getDit() <= 1) {
            return 0;
        }
        /* * NOTA: Senza l'accesso alla tabella dei simboli completa del padre,
         * l'MFA reale è difficile. Molti ricercatori usano 1 - (1/DIT)
         * come stima dell'impatto dell'astrazione ereditata.
         */
        return (float) 1 - ((float) 1 / result.getDit());
    }
}