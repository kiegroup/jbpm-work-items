package org.jbpm.contrib.demoservices.dto;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildRequest {

    private Scm scm;

    private Callback callback;

    private String buildScript;

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }
}
