package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.ChurnMetrics;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalyzerChurnMetrics {
    private final GitHandler gitHandler;

    public AnalyzerChurnMetrics(GitHandler gitHandler) {
        this.gitHandler = gitHandler;
    }

    public Map<String, ChurnMetrics> computeChurn(String currentCommit, String previousCommit) {
        Map<String, ChurnMetrics> resultMap = new HashMap<>();
        if (previousCommit == null) return resultMap;

        Map<String, Set<String>> nAuthors = new HashMap<>();

        try {
            // Usiamo il nuovo metodo delegato
            Iterable<RevCommit> commits = gitHandler.getCommitsInRange(previousCommit, currentCommit);

            for (RevCommit commit : commits) {
                String authorName = commit.getAuthorIdent().getName();
                processCommit(commit, gitHandler.getRepository(), resultMap, nAuthors, authorName);

            }

            // --- Calcolo medie finali ---
            resultMap.forEach((path, metrics) -> {
                    metrics.setTotalChurn(metrics.getLocAdded() + metrics.getLocDeleted());

                    if (nAuthors.containsKey(path)) metrics.setnAuthor(nAuthors.get(path).size());

                    if (metrics.getnRevisions() > 0) metrics.setAvgChurn((float) metrics.getTotalChurn() / metrics.getnRevisions());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public void computeRelativeChurnMetrics(Map<String, ChurnMetrics> churnMap, Map<String, MetricsCollector> staticMetrics) {
        churnMap.forEach((path, metrics) -> {
            // 1. Recupera l'oggetto MetricsCollector (che contiene le LOC totali della release)
            MetricsCollector mc = staticMetrics.get(path);

            // 2. Verifica che il file esista nelle metriche statiche e abbia LOC > 0
            if (mc != null && mc.getLoc() > 0) {
                int totalLoc = mc.getLoc();

                // Calcolo M1: Churned LOC / Total LOC
                float m1 = (float) metrics.getLocAdded() / totalLoc;
                metrics.setM1(m1);

                // Calcolo M2: Deleted LOC / Total LOC
                float m2 = (float) metrics.getLocDeleted() / totalLoc;
                metrics.setM2(m2);
            }
        });
    }

    private void processCommit(RevCommit commit, Repository repo, Map<String, ChurnMetrics> resultMap, Map<String, Set<String>> authorsMap, String authorName) throws IOException {
        // Logica per estrarre le differenze del singolo commit rispetto al padre
        try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            df.setRepository(repo);
            RevCommit parent = (commit.getParentCount() > 0) ? commit.getParent(0) : null;
            List<DiffEntry> entries = df.scan(parent != null ? parent.getTree() : null, commit.getTree());

            for (DiffEntry entry : entries) {
                String path = entry.getNewPath();
                if (!path.endsWith(".java")) continue;

                ChurnMetrics metrics = resultMap.computeIfAbsent(path, k -> new ChurnMetrics());
                authorsMap.computeIfAbsent(path, k -> new java.util.HashSet<>()).add(authorName);

                // Calcoliamo locAdded e locDeleted per questo commit
                int added = 0;
                int deleted = 0;
                for (Edit edit : df.toFileHeader(entry).toEditList()) {
                    added += edit.getEndB() - edit.getBeginB();
                    deleted += edit.getEndA() - edit.getBeginA();
                }

                // Aggiorniamo i totali
                metrics.setLocAdded(metrics.getLocAdded() + added);
                metrics.setLocDeleted(metrics.getLocDeleted() + deleted);
                metrics.setnRevisions(metrics.getnRevisions() + 1);

                // Aggiorniamo maxChurm (il picco massimo di righe cambiate in un commit)
                int currentTotal = added + deleted;
                if (currentTotal > metrics.getMaxChurn()) {
                    metrics.setMaxChurn(currentTotal);
                }
            }
        }
    }
}
