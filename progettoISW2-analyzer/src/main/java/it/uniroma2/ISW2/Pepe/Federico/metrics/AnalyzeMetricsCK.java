package it.uniroma2.ISW2.Pepe.Federico.metrics;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import java.io.File;

public class AnalyzeMetricsCK {

    private final CK ckEngine;

    public AnalyzeMetricsCK() {
        // Configurazione motore CK (fields, maxAtOnce, partialMethods)
        this.ckEngine = new CK(false, 0, false);
    }

    public void computeMetricsCK(File javaFile, MetricsCollector metricsCollector) {
        String absolutePath = javaFile.getAbsolutePath();

        ckEngine.calculate(absolutePath, result -> {
            if ("class".equals(result.getType())) {
                updateCollector(result, metricsCollector);
                logMetrics(result, absolutePath);
            }
        });
    }

    private void updateCollector(CKClassResult result, MetricsCollector collector) {
        // Settaggio metriche
        collector.setWmc(result.getWmc());
        collector.setCbo(result.getCbo());
        collector.setDit(result.getDit());
        collector.setRfc(result.getRfc());
        collector.setLcom(result.getLcom());
    }

    private void logMetrics(CKClassResult result, String path) {
        System.out.println("-------------------------------------------------------");
        System.out.println(" FILE  : " + path);
        System.out.println(" CLASS : " + result.getClassName());
        System.out.println("-------------------------------------------------------");
        System.out.printf("  %-10s: %d | %-10s: %d%n", "WMC", result.getWmc(), "CBO", result.getCbo());
        System.out.printf("  %-10s: %d | %-10s: %d%n", "DIT", result.getDit(), "RFC", result.getRfc());
        System.out.printf("  %-10s: %d | %-10s: %d%n", "LCOM", result.getLcom(), "TYPE", 0); // placeholder per NOC
        System.out.println("-------------------------------------------------------");
    }
}