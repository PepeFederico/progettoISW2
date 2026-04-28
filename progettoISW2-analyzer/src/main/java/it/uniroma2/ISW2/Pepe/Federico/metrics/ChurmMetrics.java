package it.uniroma2.ISW2.Pepe.Federico.metrics;

public class ChurmMetrics {

    private int nRevisions;
    private int locAdded;
    private int locDeleted;
    private float maxChurm;
    private float avgChurm;

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
    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
    }

    public float getMaxChurm() {
        return maxChurm;
    }
    public void setMaxChurm(float maxChurm) {
        this.maxChurm = maxChurm;
    }

    public float getAvgChurm() {
        return avgChurm;
    }
    public void setAvgChurm(float avgChurm) {
        this.avgChurm = avgChurm;
    }
}
