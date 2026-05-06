package it.uniroma2.ISW2.Pepe.Federico.metrics;

public class ChurnMetrics {

    private int nRevisions;
    private int locAdded;
    private int locDeleted;
    private int totalChurn;
    private int nAuthor;
    private float maxChurn;
    private float avgChurn;
    private float m1;       //Churned LOC / Total LOC
    private float m2;       //Deleted LOC / Total LOC

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

    public void setTotalChurn(int totalChurn) {
        this.totalChurn = totalChurn;
    }
    public int getTotalChurn() {
        return totalChurn;
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
}
