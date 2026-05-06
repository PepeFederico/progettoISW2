package it.uniroma2.ISW2.Pepe.Federico.metrics;

public class MetricsCollector {
    // --- Metadati (Indispensabili per il dataset) ---
    private int numRelease;         // Il VersionID numerico
    private String versionName;     // Il nome della versione (es. "1.1.0")
    private String commitId;        // Il CommitID specifico

    private String classPath;       // Identificativo della classe (Modulo/Src/...)

    // --- Metriche CK (Standard) ---
    private int wmc;
    private int cbo;
    private int dit;                // Profondità ereditarietà (Intero)
    private int rfc;
    private int lcom;
    private int loc;
    private int nOfMethod;
    private int nOfField;

    // --- Metriche Bansiy & Davis (QMOOD) ---
    private long npm;               // Metodi pubblici
    private float dam;              // Incapsulamento (Rapporto 0-1)
    private float mfa;              // Astrazione (Rapporto 0-1)
    private float amc;              // Complessità media

    // --- Metriche relative alla Churm ---
    private int nRevisions;
    private int locAdded;
    private int locDeleted;
    private float maxChurn;
    private float avgChurn;
    private int totalChurn;
    private int nAuthor;
    private float m1;       //Churned LOC / Total LOC
    private float m2;       //Deleted LOC / Total LOC

    private int nSmells;
    private boolean buggy;

    public MetricsCollector(int numRelease, String versionName, String commitId) {
        this.numRelease = numRelease;
        this.versionName = (versionName != null) ? versionName.intern() : null;
        this.commitId = (commitId != null) ? commitId.intern() : null;

        // Settiamo a zero le metriche relative alla Churm
        this.nRevisions     = 0;
        this.locAdded       = 0;
        this.locDeleted     = 0;
        this.maxChurn       = 0;
        this.avgChurn       = 0;
        this.totalChurn     = 0;
        this.nAuthor        = 0;
        this.m1             = 0;
        this.m2             = 0;

        //Settiamo la Buggyness a False
        this.buggy          = false;
    }

    public MetricsCollector(){}

    public int getNumRelease() {
        return numRelease;
    }
    public String getVersionName() {
        return versionName;
    }
    public String getCommitId() {
        return commitId;
    }
    public void setNumRelease(int numRelease) {
        this.numRelease = numRelease;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getClassPath() {
        return classPath;
    }
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public int getWmc() {
        return wmc;
    }
    public void setWmc(int wmc) {
        this.wmc = wmc;
    }

    public int getCbo() {
        return cbo;
    }
    public void setCbo(int cbo) {
        this.cbo = cbo;
    }

    public int getDit() {
        return dit;
    }
    public void setDit(int dit) {
        this.dit = dit;
    }

    public int getRfc() {
        return rfc;
    }
    public void setRfc(int rfc) {
        this.rfc = rfc;
    }

    public int getLcom() {
        return lcom;
    }
    public void setLcom(int lcom) {
        this.lcom = lcom;
    }

    public int getLoc() {
        return loc;
    }
    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getnOfMethod() {
        return nOfMethod;
    }
    public void setnOfMethod(int nOfMethod) {
        this.nOfMethod = nOfMethod;
    }

    public int getnOfField() {
        return nOfField;
    }
    public void setnOfField(int nOfField) {
        this.nOfField = nOfField;
    }

    public long getNpm() {
        return npm;
    }
    public void setNpm(long npm) {
        this.npm = npm;
    }

    public float getDam() {
        return dam;
    }
    public void setDam(float dam) {
        this.dam = dam;
    }

    public float getMfa() {
        return mfa;
    }
    public void setMfa(float mfa) {
        this.mfa = mfa;
    }

    public float getAmc() {
        return amc;
    }
    public void setAmc(float amc) {
        this.amc = amc;
    }

    public int getnSmells() {
        return nSmells;
    }
    public void setnSmells(int nSmells) {
        this.nSmells = nSmells;
    }

    public int getnRevisions() {
        return nRevisions;
    }
    public void setnRevisions(int nRevisions) {
        this.nRevisions = nRevisions;
    }

    public int getLocAdded() {
        return locAdded;
    }
    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocDeleted() {
        return locDeleted;
    }
    public void setLocDeleted(int lodDeleted) {
        this.locDeleted = lodDeleted;
    }

    public float getMaxChurn() {
        return maxChurn;
    }
    public void setMaxChurn(float maxChurn) {
        this.maxChurn = maxChurn;
    }

    public float getAvgChurn() {
        return avgChurn;
    }
    public void setAvgChurn(float avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getTotalChurn() {
        return totalChurn;
    }
    public void setTotalChurn(int totalChurn) {
        this.totalChurn = totalChurn;
    }

    public int getnAuthor() {
        return nAuthor;
    }
    public void setnAuthor(int nAuthor) {
        this.nAuthor = nAuthor;
    }

    public float getM1() {
        return m1;
    }
    public void setM1(float m1) {
        this.m1 = m1;
    }

    public float getM2() {
        return m2;
    }
    public void setM2(float m2) {
        this.m2 = m2;
    }

    public boolean isBuggy() {
        return buggy;
    }
    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }
}
