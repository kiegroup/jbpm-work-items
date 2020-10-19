package org.jbpm.contrib.demoservices.dto;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompleteRequest {

    private String buildConfigurationId;
    private Scm scm;
    private String completionStatus;

    public String getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public void setBuildConfigurationId(String buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }
}
