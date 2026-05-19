package it.uniroma2.ISW2.Pepe.Federico.utils;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.Remove;

public class ApplySmooth {

    public Instances applySmote50(Instances dataset) throws Exception {
        Instances cleanedData;

        if (dataset.attribute("VersionName") != null || dataset.attribute("ClassPath") != null){
            //      Rimozione delle colonne VersionName e ClassPath --> Weka lavora con numeri, quindi genera valori numerici
            Remove remove = new Remove();
            remove.setAttributeIndices("2,3");
            remove.setInputFormat(dataset);
            cleanedData = Filter.useFilter(dataset, remove);
        } else {
            //      Se sono già rimosse allora il dataset è già pulito
            cleanedData = dataset;
        }

        if (cleanedData.classIndex() == -1) {
            cleanedData.setClassIndex(cleanedData.numAttributes() - 1);
        }

        int numInstances    = cleanedData.numInstances();
        int classIdx        = cleanedData.classIndex();
        int buggyCount      = 0;
        int noBuggyCount    = 0;

        for (int i = 0; i < numInstances; i++){
            if (cleanedData.instance(i).value(classIdx) == 1.0) buggyCount++;
            else noBuggyCount++;
        }

        if (buggyCount == 0 || noBuggyCount == 0 || buggyCount >= noBuggyCount) return cleanedData; //      I dati sono già bilanciati

        double percentage = ((double) (noBuggyCount - buggyCount) / buggyCount) * 100;

        SMOTE smote = new SMOTE();
        smote.setInputFormat(cleanedData);
        smote.setPercentage(percentage);

        Instances balancedData = Filter.useFilter(cleanedData, smote);
        balancedData.setRelationName(cleanedData.relationName() + "_balanced");

        return balancedData;
    }
}
