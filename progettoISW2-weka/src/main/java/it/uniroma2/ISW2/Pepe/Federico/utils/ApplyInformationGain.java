package it.uniroma2.ISW2.Pepe.Federico.utils;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


public class ApplyInformationGain {
    /*
    *   In questa classe andremo ad applicare l'information Gain per la selezione delle feature, al fine di identificare le variabili più rilevanti per il nostro modello,
    *   calcolando la differenza di entropia prima e dopo aver appreso qualcosa di nuovo.
    *   Entropia è un modo per calcolare l'incertezza o l'impurità di un dataset. L'obiettivo è ridurre l'entropia rendendo il dataset più predicibile
    *   e migliorandone l'accuratezza. IG è la metrica che misura la variazione di entropia.
    * */

    public Instances applyInfoGain(Instances dataset, int featureToKeep) throws Exception {

        //      Rimozione delle colonne VersionName e ClassPath --> Weka lavora con numeri, quindi genera valori numerici
        Remove remove = new Remove();
        remove.setAttributeIndices("2,3");
        remove.setInputFormat(dataset);
        Instances cleanedData = Filter.useFilter(dataset, remove);

        // Configura l'indice della classe target (Buggy) sul dataset pulito
        if (cleanedData.classIndex() == -1) {
            cleanedData.setClassIndex(cleanedData.numAttributes() - 1);
        }

        try{
            InfoGainAttributeEval eval      = new InfoGainAttributeEval();
            Ranker ranker                   = new Ranker();
            AttributeSelection filter       = new AttributeSelection();

            ranker.setNumToSelect(featureToKeep);
            filter.setEvaluator(eval);
            filter.setSearch(ranker);

            filter.setInputFormat(cleanedData);
            Instances igDataset = Filter.useFilter(cleanedData, filter);

            igDataset.setRelationName(dataset.relationName() + "_IG_" + featureToKeep);
            return igDataset;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'applicazione dell'Information Gain", e);
        }
    }
}
