package it.uniroma2.ISW2.Pepe.Federico.metrics;

import it.uniroma2.ISW2.Pepe.Federico.utils.CSVExporter;
import java.util.List;
import java.util.Locale;

public class DatasetExporter {

    private static final String HEADER = "NumRelease,VersionName,CommitId,ClassPath,WMC,CBO,DIT,RFC,LCOM,LOC,nOfMethod,nOfField,NPM,DAM,MOA,MFA,CAM,AMC,nRevisions,locAdded,locDeleted,maxChurm,avgChurm,nSmells,Buggy";

    public void generateDataset(String projectName, List<MetricsCollector> dataset) {
        String fileName = String.format("%s_Metrics_Dataset.csv", projectName.toUpperCase());

        CSVExporter.writeToCSV(fileName, HEADER, dataset, mc ->
                String.format(Locale.US,
                        "%d,%s,%s,%s," +                 // 4: numRelease, versionName, commitId, classPath
                                "%d,%d,%d,%d,%d," +             // 5: wmc, cbo, dit, rfc, lcom
                                "%d,%d,%d,%d," +                // 4: loc, nOfMethod, nOfField, npm
                                "%.2f,%d,%.2f,%.2f,%.2f," +     // 5: dam, moa, mfa, cam, amc
                                "%d,%d,%d,%.2f,%.2f," +         // 5: nRevisions, locAdded, locDeleted, maxChurm, avgChurm
                                "%d,%b",                        // 2: nSmells, buggy
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
                        mc.getMoa(),
                        mc.getMfa(),
                        mc.getCam(),
                        mc.getAmc(),
                        mc.getnRevisions(),
                        mc.getLocAdded(),
                        mc.getLocDeleted(),
                        mc.getMaxChurm(),
                        mc.getAvgChurm(),
                        mc.getnSmells(),
                        false // Labeling di default
                )
        );
    }
}
