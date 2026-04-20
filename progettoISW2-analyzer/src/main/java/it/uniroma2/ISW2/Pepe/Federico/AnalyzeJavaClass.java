package it.uniroma2.ISW2.Pepe.Federico;

import com.github.mauricioaniche.ck.CK;
import java.io.File;
public class AnalyzeJavaClass {

    public void computeMetricsCK(File javaFile) {
        CK ck = new CK(false, 0, false);

        String absolutePath = javaFile.getAbsolutePath();

        ck.calculate(absolutePath, result -> {

            // Estrazione metriche C&K
            int wmc = result.getWmc();
            int cbo = result.getCbo();
            int dit = result.getDit();
            int rfc = result.getRfc();
            int lcom = result.getLcom();

            System.out.println("-------------------------------------------------------");
            System.out.println(" FILE: " + absolutePath);
            System.out.println(" CLASS: " + result.getClassName());
            System.out.println("-------------------------------------------------------");
            System.out.printf("  %-10s: %d | %-10s: %d%n", "WMC", wmc, "CBO", cbo);
            System.out.printf("  %-10s: %d | %-10s: %d%n", "DIT", dit, "RFC", rfc);
            System.out.printf("  %-10s: %d%n ", "LCOM", lcom);
            System.out.println("-------------------------------------------------------");
        });
    }
}