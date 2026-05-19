package it.uniroma2.ISW2.Pepe.Federico.utils;

import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import weka.core.*;
import java.util.ArrayList;
import java.util.List;

public class ConvertToIstances {

    public Instances conversions(String fileName, List<MetricsCollector> dataset){
        ArrayList<Attribute> attributes = new ArrayList<>();

        //      Definizione delle colonne
        attributes.add(new Attribute("NumRelease"));
        attributes.add(new Attribute("VersionName", (List<String>) null));
        attributes.add(new Attribute("ClassPath", (List<String>) null));
        attributes.add(new Attribute("WMC"));
        attributes.add(new Attribute("CBO"));
        attributes.add(new Attribute("DIT"));
        attributes.add(new Attribute("RFC"));
        attributes.add(new Attribute("LCOM"));
        attributes.add(new Attribute("LOC"));
        attributes.add(new Attribute("nOfMethod"));
        attributes.add(new Attribute("nOfField"));
        attributes.add(new Attribute("NPM"));
        attributes.add(new Attribute("DAM"));
        attributes.add(new Attribute("MFA"));
        attributes.add(new Attribute("AMC"));
        attributes.add(new Attribute("nRevisions"));
        attributes.add(new Attribute("locAdded"));
        attributes.add(new Attribute("locDeleted"));
        attributes.add(new Attribute("nAuthors"));
        attributes.add(new Attribute("TotalChurn"));
        attributes.add(new Attribute("maxChurn"));
        attributes.add(new Attribute("avgChurn"));
        attributes.add(new Attribute("M1"));
        attributes.add(new Attribute("M2"));
        attributes.add(new Attribute("nSmells"));

        // 2. La colonna TARGET (Buggy) deve essere NOMINAL per i classificatori
        List<String> buggyValues = new ArrayList<>();
        buggyValues.add("false");
        buggyValues.add("true");
        attributes.add(new Attribute("Buggy", buggyValues));

        // 3. Creiamo l'oggetto Instances
        Instances data = new Instances(fileName, attributes, dataset.size());
        data.setClassIndex(data.numAttributes() - 1); // Impostiamo isBuggy come target

        // 4. Popoliamo con i dati
        for (MetricsCollector mc : dataset) {
            double[] values = new double[data.numAttributes()];
            values[0] = mc.getNumRelease();
            values[1] = data.attribute(1).addStringValue(mc.getVersionName());
            values[2] = data.attribute(2).addStringValue(mc.getClassPath());
            values[3] = mc.getWmc();
            values[4] = mc.getCbo();
            values[5] = mc.getDit();
            values[6] = mc.getRfc();
            values[7] = mc.getLcom();
            values[8] = mc.getLoc();
            values[9] = mc.getnOfMethod();
            values[10] = mc.getnOfField();
            values[11] = mc.getNpm();
            values[12] = mc.getDam();
            values[13] = mc.getMfa();
            values[14] = mc.getAmc();
            values[15] = mc.getnRevisions();
            values[16] = mc.getLocAdded();
            values[17] = mc.getLocDeleted();
            values[18] = mc.getnAuthor();
            values[19] = mc.getTotalChurn();
            values[20] = mc.getMaxChurn();
            values[21] = mc.getAvgChurn();
            values[22] = mc.getM1();
            values[23] = mc.getM2();
            values[24] = mc.getnSmells();
            values[values.length - 1] = mc.isBuggy() ? 1.0 : 0.0; // 1.0 = true, 0.0 = false

            data.add(new DenseInstance(1.0, values));
        }
        return data;
    }

}
