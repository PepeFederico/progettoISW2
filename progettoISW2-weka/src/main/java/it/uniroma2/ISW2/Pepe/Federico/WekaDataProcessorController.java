package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import it.uniroma2.ISW2.Pepe.Federico.utils.ApplyInformationGain;
import it.uniroma2.ISW2.Pepe.Federico.utils.ApplySmooth;
import it.uniroma2.ISW2.Pepe.Federico.utils.ApplySpearman;
import it.uniroma2.ISW2.Pepe.Federico.utils.ConvertToIstances;
import weka.core.*;
import weka.core.converters.ArffSaver;

public class WekaDataProcessorController {

    public void processDataset(String fileName, Path path, List<MetricsCollector> dataset, String useCase) {
        ConvertToIstances converter = new ConvertToIstances();

        //      Conversione da .csv a tipo Instances
        Instances instances = converter.conversions(fileName, dataset);

        switch (useCase) {
            case "dataset_base":
                // 1    Salvataggio dataset originale
                //      Crea il file: dataset_base.arff
                Path fullPath = path.resolve(fileName + ".arff");
                saveAsArff(instances, fullPath);

                // 2    Operazione di Bilanciamento
                applyBalanceData(fileName, path, instances);
                break;

            case "dataset_ig":
                //      Impostiamo la scelta delle Feature da mantenere --> variabile featureToKeep
                Instances igDataset = applyInfoGain(instances);
                Path infoGainPath = path.resolve(fileName + ".arff");
                saveAsArff(igDataset, infoGainPath);

                //  Applichiamo il bilanciamento
                applyBalanceData(fileName, path, igDataset);
                break;

            case "dataset_ig_spearman":
                //      Applichiamo prima l'information gain
                Instances ig_dataset = applyInfoGain(instances);

                //      Applichiamo lo correlazione di Spearman
                Instances ig_spearman_dataset = applySpearmanCorrelation(ig_dataset);
                Path infoGainSpearmaPath = path.resolve(fileName + ".arff");
                saveAsArff(ig_spearman_dataset, infoGainSpearmaPath);

                //      Applichiamo il bilanciamento
                applyBalanceData(fileName, path, ig_spearman_dataset);
                break;

            default:
                //TODO : Implementare la classe ApplyLasso. In questo caso applichiamo IG + Spearman + Lasso1

        }

    }

    private void applyBalanceData(String fileName, Path path, Instances instances) {
        ApplySmooth balancer = new ApplySmooth();

        try {
            //      Applichiamo il bilanciamento al 50%
            Instances balacedInstances = balancer.applySmote50(instances);
            Path balancedPath = path.resolve(fileName + "_balanced.arff");
            saveAsArff(balacedInstances, balancedPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Instances applyInfoGain(Instances instances){
        ApplyInformationGain informationGain = new ApplyInformationGain();

        try {
            return informationGain.applyInfoGain(instances, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Instances applySpearmanCorrelation(Instances instances){
        ApplySpearman spearman = new ApplySpearman();

        return spearman.applySpearmanCorrelation(instances, 0.85);
    }

    private void saveAsArff(Instances data, Path fullPath) {
        try {
            // 1. Otteniamo la directory che deve contenere il file
            Path parentDir = fullPath.getParent();

            // 2. Creiamo le directory se non esistono (es: feature_selections/dataset_base)
            if (parentDir != null && Files.notExists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 3. Verifichiamo che fullPath non sia una directory già esistente
            if (Files.exists(fullPath) && Files.isDirectory(fullPath)) {
                throw new IOException("Il percorso specificato è una cartella, non un file: " + fullPath);
            }

            // 4. Configurazione e scrittura Weka
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(fullPath.toFile());
            saver.writeBatch();

            System.out.println("File ARFF creato correttamente in: " + fullPath.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio ARFF in: " + fullPath, e);
        }
    }
}
