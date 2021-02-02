package org.jbpm.process.longrest.demoservices.dto;

public class BuildRequest {

    private Scm scm;

    private Request callback;

    private String buildScript;

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public Request getCallback() {
        return callback;
    }

    public void setCallback(Request callback) {
        this.callback = callback;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }
}
