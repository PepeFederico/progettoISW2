package it.uniroma2.ISW2.Pepe.Federico;

import java.time.LocalDateTime;

public class VersionField {
    private int index;
    private String versionName;
    private String versionId;
    private LocalDateTime date;

    public VersionField(int index, String versionId, String versionName, LocalDateTime date) {
        this.index = index;
        this.versionId = versionId;
        this.versionName = versionName;
        this.date = date;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionId() {
        return versionId;
    }
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
