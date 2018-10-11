package org.jbpm.contrib.restservice;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Constant {

    public static final String TIMEOUT_NODE_INSTANCE_ID_VARIABLE = "nodeInstanceId"; //TODO update value

    /**
     * When true the task is canceled internally without trying to cancel remote operation.
     */
    public static final String FORCE_CANCEL_VARIABLE = "forceCancel";

    public final static String CANCEL_SIGNAL_TYPE = "cancel-all";

    public static final String REMOTE_CANCEL_FAILED = "remote-cancel-failed";

    public static final String TIMEOUT_PROCESS_NAME = "timeout-handler-process";

    public static final String CANCEL_TIMEOUT_VARIABLE = "cancelTimeout";

    public static final String MAIN_PROCESS_INSTANCE_ID_VARIABLE = "mainProcessInstanceId";
}
