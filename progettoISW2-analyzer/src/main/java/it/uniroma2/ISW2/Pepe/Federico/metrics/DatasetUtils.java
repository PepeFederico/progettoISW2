package it.uniroma2.ISW2.Pepe.Federico.metrics;

import it.uniroma2.ISW2.Pepe.Federico.ProjectVersion;
import it.uniroma2.ISW2.Pepe.Federico.VersionField;
import it.uniroma2.ISW2.Pepe.Federico.utils.CSVExporter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatasetUtils {

    private static final String HEADER =    "NumRelease," +
                                            "VersionName," +
                                            "CommitId," +
                                            "ClassPath," +
                                            "WMC," +
                                            "CBO," +
                                            "DIT," +
                                            "RFC," +
                                            "LCOM," +
                                            "LOC," +
                                            "nOfMethod," +
                                            "nOfField," +
                                            "NPM," +
                                            "DAM," +
                                            "MFA," +
                                            "AMC," +
                                            "nRevisions," +
                                            "locAdded," +
                                            "locDeleted," +
                                            "nAuthors," +
                                            "TotalChurn," +
                                            "maxChurn," +
                                            "avgChurn," +
                                            "M1," +
                                            "M2," +
                                            "nSmells," +
                                            "Buggy";

    private static final String FINAL_HEADER =  "NumRelease," +
                                                "VersionName," +
                                                "ClassPath," +
                                                "WMC," +
                                                "CBO," +
                                                "DIT," +
                                                "RFC," +
                                                "LCOM," +
                                                "LOC," +
                                                "nOfMethod," +
                                                "nOfField," +
                                                "NPM," +
                                                "DAM," +
                                                "MFA," +
                                                "AMC," +
                                                "nRevisions," +
                                                "locAdded," +
                                                "locDeleted," +
                                                "nAuthors," +
                                                "TotalChurn," +
                                                "maxChurn," +
                                                "avgChurn," +
                                                "M1," +
                                                "M2," +
                                                "nSmells," +
                                                "Buggy";

    private static final String HEADER_VERSIONS =   "Index," +
                                                    "VersionID," +
                                                    "VersionName," +
                                                    "Date";

    public void generateVersionsDataset(String projectName, List<ProjectVersion> projectVersions){
        String fileName = String.format("%s_Version_Table.csv", projectName.toUpperCase());

        CSVExporter.writeToCSV(fileName, HEADER_VERSIONS, projectVersions, projectVersion -> String.format(
                "%d,%s,%s,%s",
                projectVersion.index(),
                projectVersion.versionID(),
                projectVersion.versionName(),
                projectVersion.date()
        ));
    }

    public void generateDataset(String projectName, List<MetricsCollector> dataset) {
        String fileName = String.format("%s_Metrics_Dataset.csv", projectName.toUpperCase());

        CSVExporter.writeToCSV(fileName, HEADER, dataset, mc ->
                String.format(Locale.US,
                        "%d,%s,%s,%s," +                         // 4: numRelease, versionName, commitId, classPath
                                "%d,%d,%d,%d,%d," +                     // 5: wmc, cbo, dit, rfc, lcom
                                "%d,%d,%d,%d," +                        // 4: loc, nOfMethod, nOfField, npm
                                "%.2f,%.2f,%.2f," +                     // 3: dam, mfa, amc
                                "%d,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f," +           // 9: nRevisions, locAdded, locDeleted, nAuthors, TotalChurn, maxChurn, avgChurn, M1, M2
                                "%d,%b",                                // 2: nSmells, buggy
                        mc.getNumRelease(),
                        mc.getVersionName(),
                        mc.getCommitId(),
                        mc.getClassPath(),
                        mc.getWmc(),
                        mc.getCbo(),
                        mc.getDit(),
                        mc.getRfc(),
                        mc.getLcom(),
                        mc.getLoc(),
                        mc.getnOfMethod(),
                        mc.getnOfField(),
                        mc.getNpm(),
                        mc.getDam(),
                        mc.getMfa(),
                        mc.getAmc(),
                        mc.getnRevisions(),
                        mc.getLocAdded(),
                        mc.getLocDeleted(),
                        mc.getnAuthor(),
                        mc.getTotalChurn(),
                        mc.getMaxChurn(),
                        mc.getAvgChurn(),
                        mc.getM1(),
                        mc.getM2(),
                        mc.getnSmells(),
                        mc.isBuggy()        //Settata a False in MetricsCollector
                )
        );
    }

    public void generateFinalDataset(String projectName, List<MetricsCollector> dataset) {
        String fileName = String.format("%s_Final_Metrics_Dataset.csv", projectName.toUpperCase());

        CSVExporter.writeToCSV(fileName, FINAL_HEADER, dataset, mc ->
                String.format(Locale.US,
                        "%d,%s,%s," +                                // 3: numRelease, versionName, classPath
                                "%d,%d,%d,%d,%d," +                         // 5: wmc, cbo, dit, rfc, lcom
                                "%d,%d,%d,%d," +                            // 4: loc, nOfMethod, nOfField, npm
                                "%.2f,%.2f,%.2f," +                         // 3: dam, mfa, amc
                                "%d,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f," +     // 9: nRevisions, locAdded, locDeleted, nAuthors, TotalChurn, maxChurn, avgChurn, M1, M2
                                "%d,%b",                                    // 2: nSmells, buggy
                        mc.getNumRelease(),
                        mc.getVersionName(),
                        mc.getClassPath(),
                        mc.getWmc(),
                        mc.getCbo(),
                        mc.getDit(),
                        mc.getRfc(),
                        mc.getLcom(),
                        mc.getLoc(),
                        mc.getnOfMethod(),
                        mc.getnOfField(),
                        mc.getNpm(),
                        mc.getDam(),
                        mc.getMfa(),
                        mc.getAmc(),
                        mc.getnRevisions(),
                        mc.getLocAdded(),
                        mc.getLocDeleted(),
                        mc.getnAuthor(),
                        mc.getTotalChurn(),
                        mc.getMaxChurn(),
                        mc.getAvgChurn(),
                        mc.getM1(),
                        mc.getM2(),
                        mc.getnSmells(),
                        mc.isBuggy()
                )
        );
    }

    public List<MetricsCollector> loadDatasetFromFile(String path){
        List<MetricsCollector> dataset = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // Salta l'intestazione (HEADER)

            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");

                // Mappatura seguendo l'ordine esatto del tuo DatasetExporter
                MetricsCollector mc = new MetricsCollector();

                // 1-4: Identificativi
                mc.setNumRelease(Integer.parseInt(v[0]));
                mc.setVersionName(v[1]);
                mc.setCommitId(v[2]);
                mc.setClassPath(v[3]);

                // 5-9: Metriche classiche
                mc.setWmc(Integer.parseInt(v[4]));
                mc.setCbo(Integer.parseInt(v[5]));
                mc.setDit(Integer.parseInt(v[6]));
                mc.setRfc(Integer.parseInt(v[7]));
                mc.setLcom(Integer.parseInt(v[8]));

                // 10-13: Dimensioni
                mc.setLoc(Integer.parseInt(v[9]));
                mc.setnOfMethod(Integer.parseInt(v[10]));
                mc.setnOfField(Integer.parseInt(v[11]));
                mc.setNpm(Integer.parseInt(v[12]));

                // 14-16: Metriche decimali (DAM, MFA, AMC)
                mc.setDam(Float.parseFloat(v[13]));
                mc.setMfa(Float.parseFloat(v[14]));
                mc.setAmc(Float.parseFloat(v[15]));

                // 17-25: Revisioni e Churn
                mc.setnRevisions(Integer.parseInt(v[16]));
                mc.setLocAdded(Integer.parseInt(v[17]));
                mc.setLocDeleted(Integer.parseInt(v[18]));
                mc.setnAuthor(Integer.parseInt(v[19]));
                mc.setTotalChurn(Integer.parseInt(v[20]));
                mc.setMaxChurn(Float.parseFloat(v[21]));
                mc.setAvgChurn(Float.parseFloat(v[22]));
                mc.setM1(Float.parseFloat(v[23]));
                mc.setM2(Float.parseFloat(v[24]));

                // 26-27: Smells e Buggy
                mc.setnSmells(Integer.parseInt(v[25]));
                mc.setBuggy(Boolean.parseBoolean(v[26]));

                dataset.add(mc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dataset;
    }

    public List<VersionField> loadDatasetVersionsFromFile(String path){
        List<VersionField> allVersions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // Salta l'intestazione (HEADER)

            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                 allVersions.add(new VersionField(
                        Integer.parseInt(v[0]), //Index
                        v[1].trim(),            //VersionID
                        v[2].trim(),            //VersionName
                        LocalDateTime.parse(v[3])
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return allVersions;
    }
}

