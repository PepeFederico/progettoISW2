package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.ChurmMetrics;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyzerVersion {

    private String lastReleaseCommit = null;
    private final AnalyzerChurmMetrics analyzerChurmMetrics;
    private final AnalyzerStaticMetrics analyzerStaticMetrics;
    private final SmellAnalyzerPMD smellAnalyzer;

    public AnalyzerVersion(GitHandler gitHandler) {
        this.analyzerChurmMetrics   = new AnalyzerChurmMetrics(gitHandler);
        this.analyzerStaticMetrics  = new AnalyzerStaticMetrics();
        this.smellAnalyzer          = new SmellAnalyzerPMD();
    }

    public Map<String, MetricsCollector> analyzeVersion(String absolutePath, ProjectVersion release, String commitId, String root) {
        System.out.println("    >> Analisi della Release: " + release.versionName() + " [Commit: " + commitId + "]");

        // 1. Calcolo della Churm (Delta tra commit attuale e quello della release precedente)
        // Se lastReleaseCommit è null, la mappa sarà vuota (comportamento corretto per R1)
        Map<String, ChurmMetrics> deltaChurmMap = analyzerChurmMetrics.computeChurn(commitId, lastReleaseCommit);

        if (lastReleaseCommit == null) {
            System.out.println("    >> Release 1 individuata: Metriche Churm settate a zero");
        } else {
            System.out.println("    >> Churm calcolata rispetto al commit: " + lastReleaseCommit);
        }

        // 2. Esecuzione della PMD Analysis (Smells)
        Map<String, Integer> smellsMap = smellAnalyzer.computeSmells(absolutePath, root);

        // 3. Esecuzione dell'analisi delle metriche Statiche
        Map<String, MetricsCollector> releaseMetricsMap = new LinkedHashMap<>();
        analyzerStaticMetrics.computeMetricsBatch(absolutePath, commitId, release, root, releaseMetricsMap);

        // 4. MERGE: Arricchimento del "Blocco Singolo" con Smell e Churm
        releaseMetricsMap.forEach((path, mc) -> {
            mc.setnSmells(smellsMap.getOrDefault(path, 0));

            // Inseriamo le metriche di Churm (se il file non è cambiato, i valori restano quelli di default a 0)
            ChurmMetrics cm = deltaChurmMap.getOrDefault(path, null);
            if (cm != null) {
                mc.setnRevisions(cm.getnRevisions());
                mc.setLocAdded(cm.getLocAdded());
                mc.setLocDeleted(cm.getLocDeleted());
                mc.setMaxChurm(cm.getMaxChurm());
                mc.setAvgChurm(cm.getAvgChurm());
            }
        });

        // 5. AGGIORNAMENTO STATO: Fondamentale per la prossima iterazione (Release successiva)
        this.lastReleaseCommit = commitId;

        System.out.println("    >> Analisi della release " + release.versionName() + " completata!");
        return releaseMetricsMap;
    }
}
