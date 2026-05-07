package it.uniroma2.ISW2.Pepe.Federico.proportion;

import it.uniroma2.ISW2.Pepe.Federico.JiraTicket;
import it.uniroma2.ISW2.Pepe.Federico.VersionField;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProportionCalculator {
    private List<VersionField> allVersions;
    private List<JiraTicket> allTicket;
    private List<Double> pValues;

    public ProportionCalculator(List<VersionField> allVersions, List<JiraTicket> allTicket) {
        this.allVersions = allVersions;
        this.allTicket = allTicket;
        this.pValues = new ArrayList<>();
    }

    public ProportionCalculator(){}

    public void runCalculation() {
        for (JiraTicket ticket : allTicket) {
            // Ignoriamo i ticket senza risoluzione o senza Affected Versions per il calcolo di P
            if (ticket.resolutionDate() == null || ticket.affectedVersions().isEmpty()) continue;

            // 1. IV (Injected Version) -> Indice della versione più vecchia in Affected Versions
            int iv = getIndexByName(ticket.affectedVersions().getFirst());

            // 2. OV (Opening Version) -> Indice basato sulla data di creazione
            int ov = getIndexByDate(ticket.created());

            // 3. FV (Fix Version) -> Indice basato sulla data di risoluzione
            int fv = getIndexByDate(ticket.resolutionDate());

            // Applichiamo la formula solo se il ticket è "logico"
            if (fv >= ov && ov >= iv && fv > iv) {
                double denominator = (fv - ov == 0) ? 1.0 : (double) (fv - ov);
                double p = (fv - iv) / denominator;

                // P deve essere >= 1 secondo la definizione classica (IV <= OV <= FV)
                if (p >= 1.0) {
                    pValues.add(p);
                }
            }
        }
        filterOutliers();
    }

    public void setAllVersions(List<VersionField> allVersions) {
        this.allVersions = allVersions;
    }

    private void filterOutliers(){

        if (pValues.size() < 4) return;

        Collections.sort(pValues);

        double q1 = pValues.get((int) (pValues.size() * 0.25));
        double q3 = pValues.get((int) (pValues.size() * 0.75));
        double iqr = q3 - q1;

        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        this.pValues = pValues.stream()
                .filter(p -> p >= lowerBound && p <= upperBound)
                .collect(Collectors.toList());
    }

    public double getConfidenceIntervalWidth(){
        double sigma = getStandardDeviation();

        return 1.960 * (sigma / Math.sqrt(this.pValues.size()));
    }
    // --- Metodi Statistici ---

    public double getMean() {
        return pValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double getStandardDeviation() {
        double mean = getMean();
        double variance = pValues.stream()
                .mapToDouble(p -> Math.pow(p - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance);
    }

    // --- Metodi di Mapping (Il "Righello") ---

    public int getIndexByName(String name) {
        if (name == null) return -1;

        String target = hardClean(name);

        return allVersions.stream()
                .filter(v -> {
                    String current = hardClean(v.getVersionName());
                    return current.equalsIgnoreCase(target);
                })
                .map(VersionField::getIndex)
                .findFirst()
                .orElse(-1);
    }

    public int getIndexByDate(LocalDateTime date) {
        for (VersionField v : allVersions) {
            // Se la data del ticket è prima o uguale alla release, il ticket "appartiene" a quella release
            if (!date.isAfter(v.getDate())) {
                return v.getIndex();
            }
        }
        return allVersions.size(); // Se è oltre l'ultima release catalogata
    }

    private String hardClean(String input) {
        if (input == null) return null;
        // Rimuove tutto ciò che non è un carattere alfanumerico, punto, trattino o underscore
        // [^a-zA-Z0-9\.\-_] significa "tutto ciò che NON è in questo set"
        return input.replaceAll("[^a-zA-Z0-9.\\-_]", "").trim();
    }
}

