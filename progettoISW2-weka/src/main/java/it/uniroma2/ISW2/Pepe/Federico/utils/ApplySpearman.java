package it.uniroma2.ISW2.Pepe.Federico.utils;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import java.util.ArrayList;
import java.util.List;

public class ApplySpearman {

    public Instances applySpearmanCorrelation(Instances dataset, double sogliaCorrelazione){

        // Configura l'indice della classe target (Buggy) sul dataset pulito
        if (dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }

        int numAttributes = dataset.numAttributes();
        int classIdx = dataset.classIndex();

        // =========================================================================
        // 1. STAMPA DELLA MATRICE DI CORRELAZIONE (Per documentazione)
        // =========================================================================
        System.out.println("\n=== MATRICE DI CORRELAZIONE DI SPEARMAN ===");

        // Stampa l'intestazione delle colonne
        System.out.printf("%-10s", "");
        for (int j = 0; j < numAttributes; j++) {
            if (j == classIdx) continue;
            String attName = dataset.attribute(j).name();
            String shortName = attName.length() > 8 ? attName.substring(0, 8) : attName;
            System.out.printf("%-10s", shortName);
        }
        System.out.println();

        // Calcolo e stampa riga per riga
        for (int i = 0; i < numAttributes; i++) {
            if (i == classIdx) continue;

            // Stampa il nome della feature di riga
            String attName = dataset.attribute(i).name();
            String shortName = attName.length() > 8 ? attName.substring(0, 8) : attName;
            System.out.printf("%-10s", shortName);

            for (int j = 0; j < numAttributes; j++) {
                if (j == classIdx) continue;

                // Se siamo sulla diagonale principale, la correlazione è sempre 1.0
                if (i == j) {
                    System.out.printf("%-10s", "1.000");
                }
                // Sfruttiamo la simmetria: se siamo nel triangolo inferiore, calcoliamo invertendo gli indici
                else {
                    double correlation = calculateSpearman(dataset, i, j);
                    System.out.printf("%-10.3f", correlation);
                }
            }
            System.out.println();
        }
        System.out.println("===========================================\n");


        // =========================================================================
        // 2. LOGICA DI FILTRAGGIO (Ottimizzata sulla triangolare superiore)
        // =========================================================================
        List<Integer> indicesToRemove = new ArrayList<>();

        for (int i = 0; i < numAttributes; i++) {
            if (i == classIdx || indicesToRemove.contains(i)) continue;

            for (int j = i + 1; j < numAttributes; j++) {
                if (j == classIdx || indicesToRemove.contains(j)) continue;

                double correlation = calculateSpearman(dataset, i, j);

                if (Math.abs(correlation) > sogliaCorrelazione) {
                    System.out.printf("[Filtro] Feature fortemente correlate: %s e %s (Corr: %.3f). Rimuovo: %s%n",
                            dataset.attribute(i).name(), dataset.attribute(j).name(), correlation, dataset.attribute(j).name());
                    indicesToRemove.add(j);
                }
            }
        }

        //      Trovate le feature ridondante, dobbiamo rimuoverle col filtro Remove
        if (!indicesToRemove.isEmpty()){
            try {
                Remove remove = new Remove();

                //      Costruiamo la stringa degli indici (Weka usa indici che partono da 1)
                StringBuilder sb = new StringBuilder();
                //      Conversione da Lista Java a Lista index interpretabili da Weka
                for (int idx : indicesToRemove) {
                    sb.append(idx + 1).append(",");
                }
                sb.setLength(sb.length() - 1);

                remove.setAttributeIndices(sb.toString());
                remove.setInputFormat(dataset);
                Instances reducedDataset = Filter.useFilter(dataset, remove);

                reducedDataset.setRelationName(dataset.relationName() + "_Spearman");

                return reducedDataset;

            } catch (Exception e) {
                throw new RuntimeException("Errore durante il filtraggio delle feature con Spearman", e);
            }
        }
        return dataset;
    }

    private double calculateSpearman(Instances dataset, int attIndex1, int attIndex2) {

        int numInstances = dataset.numInstances();
        double [] vals1 = new double[numInstances];
        double [] vals2 = new double[numInstances];

        for (int i = 0; i < numInstances; i++){
            vals1[i] = dataset.instance(i).value(attIndex1);
            vals2[i] = dataset.instance(i).value(attIndex2);
        }

        double [] ranks1 = getRanks(vals1);
        double [] ranks2 = getRanks(vals2);

        return calculatePearson(ranks1, ranks2);

    }

    private double[] getRanks(double[] values) {

        int n           = values.length;
        double [] ranks = new double[n];
        int [] indices  = new int[n];

        for (int i = 0; i < n - 1; i++) indices[i] = i;

        //      Ordinamento degli indici basato sui valori delle metriche
        for (int i = 0; i < n - 1; i++){
            for (int j = i + 1; j < n; j++){
                if (values[indices[i]] > values[indices[j]]){
                    int tmp = indices[i];
                    indices[i] = indices[j];
                    indices[j] = tmp;
                }
            }
        }

        //      Assegnazione dei ranghi
        int i = 0;
        while(i < n){
            int j = i + 1;
            while(j < n && values[indices[i]] == values[indices[j]]) j++;
            double rank = (i + j + 1) / 2.0;
            for (int k = i; k < j; k++){
                ranks[indices[k]] = rank;
            }
            i = j;
        }
        return ranks;
    }

    private double calculatePearson(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++){
            sumX += x[i];
            sumY += y[i];
            sumXY += (x[i] * y[i]);
            sumX2 += (x[i] * x[i]);
            sumY2 += (y[i] * y[i]);
        }

        double numeratore = (n * sumXY) - (sumX * sumY);
        double denominatore = Math.sqrt(((n * sumX2) - (sumX * sumX)) * ((n * sumY2) - (sumY * sumY)));

        if (denominatore == 0) return 0;

        return numeratore / denominatore;
    }
}
