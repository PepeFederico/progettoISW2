package it.uniroma2.ISW2.Pepe.Federico;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SmellAnalyzerPMD {

    public Map<String, Integer> computeSmells(String projectPath, String root) {
        Map<String, Integer> smellsMap = new HashMap<>();
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();

        PMDConfiguration config = new PMDConfiguration();
        config.addRuleSet("category/java/design.xml");
        config.addRuleSet("category/java/bestpractices.xml");
        config.setMinimumPriority(RulePriority.MEDIUM);
        config.addInputPath(Paths.get(projectPath));

        // Creiamo un contenitore per il report
        Report report;

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            // Facciamo girare l'analisi e salviamo il risultato
            report = pmd.performAnalysisAndCollectReport();

            // Ora che abbiamo il report, iteriamo sulle violazioni
            for (RuleViolation violation : report.getViolations()) {
                String absPath = violation.getLocation().getFileId().getUriString();
                absPath = absPath.replace("file:", "");

                try {
                    Path path = Paths.get(absPath).toAbsolutePath().normalize();
                    String relPath = rootPath.relativize(path).toString().replace("\\", "/");

                    smellsMap.merge(relPath, 1, Integer::sum);
                } catch (Exception e) {
                    // Ignora file fuori dalla root
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return smellsMap;
    }
}
