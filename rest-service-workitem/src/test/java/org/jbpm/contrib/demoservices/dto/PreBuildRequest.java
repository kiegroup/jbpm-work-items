package org.jbpm.contrib.demoservices.dto;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
public class PreBuildRequest {
    
    //protected static ObjectMapper objectMapper = new ObjectMapper();

    private Scm scm;

    private Request callback;

    private Request heartBeat;

    private Boolean syncEnabled;

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

    public void setHeartBeat(Request heartBeat) {
        this.heartBeat = heartBeat;
    }

    public Request getHeartBeat() {
        return heartBeat;
    }

    public Boolean getSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(Boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
}
