package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.ChurmMetrics;
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

public class AnalyzerChurmMetrics {
    private final GitHandler gitHandler;

    public AnalyzerChurmMetrics(GitHandler gitHandler) {
        this.gitHandler = gitHandler;
    }

    public Map<String, ChurmMetrics> computeChurn(String currentCommit, String previousCommit) {
        Map<String, ChurmMetrics> resultMap = new HashMap<>();
        if (previousCommit == null) return resultMap;

        try {
            // Usiamo il nuovo metodo delegato
            Iterable<RevCommit> commits = gitHandler.getCommitsInRange(previousCommit, currentCommit);

            for (RevCommit commit : commits) {
                processCommit(commit, gitHandler.getRepository(), resultMap);
            }

            // Calcolo medie finali...
            resultMap.values().forEach(metrics -> {
                if (metrics.getnRevisions() > 0) {
                    metrics.setAvgChurm((float) (metrics.getLocAdded() + metrics.getLocDeleted()) / metrics.getnRevisions());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private void processCommit(RevCommit commit, Repository repo, Map<String, ChurmMetrics> resultMap) throws IOException {
        // Logica per estrarre le differenze del singolo commit rispetto al padre
        try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            df.setRepository(repo);
            RevCommit parent = (commit.getParentCount() > 0) ? commit.getParent(0) : null;
            List<DiffEntry> entries = df.scan(parent != null ? parent.getTree() : null, commit.getTree());

            for (DiffEntry entry : entries) {
                String path = entry.getNewPath();
                if (!path.endsWith(".java")) continue;

                ChurmMetrics metrics = resultMap.computeIfAbsent(path, k -> new ChurmMetrics());

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
                if (currentTotal > metrics.getMaxChurm()) {
                    metrics.setMaxChurm(currentTotal);
                }
            }
        }
    }
}
