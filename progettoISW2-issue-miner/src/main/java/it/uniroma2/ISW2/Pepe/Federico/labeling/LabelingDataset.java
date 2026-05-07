package it.uniroma2.ISW2.Pepe.Federico.labeling;

import it.uniroma2.ISW2.Pepe.Federico.GitHandler;
import it.uniroma2.ISW2.Pepe.Federico.JiraTicket;
import it.uniroma2.ISW2.Pepe.Federico.VersionField;
import it.uniroma2.ISW2.Pepe.Federico.metrics.DatasetUtils;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import it.uniroma2.ISW2.Pepe.Federico.proportion.ProportionCalculator;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelingDataset {
    public LabelingDataset() {}

    public void labelTickets(Double p, List<MetricsCollector> dataset, List<JiraTicket> allTicket, List<VersionField> allVersions, GitHandler gitHandler) throws GitAPIException, IOException {
        Map<String, List<String>> ticketFileMap = gitHandler.buildTicketToFileMap();
        ProportionCalculator proportionCalculator = new ProportionCalculator();
        proportionCalculator.setAllVersions(allVersions);

        // --- Creazione Indice di Ricerca ---  --> Mappa chiave: "versionIndex:filePath" -> Valore: MetricsCollector
        Map<String, MetricsCollector> datasetLookup = new HashMap<>();
        for (MetricsCollector entry : dataset) {
            String key = entry.getNumRelease() + ":" + entry.getClassPath().trim().replace("\\", "/");
            datasetLookup.put(key, entry);
        }

        for (JiraTicket ticket : allTicket) {
            List<String> touchedFile = ticketFileMap.get(ticket.key());
            if (touchedFile == null || touchedFile.isEmpty()) continue;

            int ov = proportionCalculator.getIndexByDate(ticket.created());
            int fv = proportionCalculator.getIndexByDate(ticket.resolutionDate());

            if (fv == -1 || ov == -1) continue;

            int iv;
            if (!ticket.affectedVersions().isEmpty()){
                iv = proportionCalculator.getIndexByName(ticket.affectedVersions().getFirst());
            } else {
                double estimatedIv = fv - ((fv - ov) * p);
                iv = (int) Math.max(1, Math.round(estimatedIv));
            }

            if (iv > ov) iv = ov;

            // --- Lookup della classe coinvolta  ---
            for (int v = iv; v < fv; v++) {
                for (String filePath : touchedFile) {
                    String cleanPath = filePath.trim().replace("\\", "/");
                    String lookupKey = v + ":" + cleanPath;

                    MetricsCollector entry = datasetLookup.get(lookupKey);
                    if (entry != null) {
                        entry.setBuggy(true);
                    }
                }
            }
        }
        dataset.removeIf(entry -> entry.getClassPath() == null || entry.getClassPath().isBlank());

        DatasetUtils datasetUtils = new DatasetUtils();
        datasetUtils.generateFinalDataset("STORM", dataset);
    }
}
