package org.jbpm.contrib.restservice;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Constant { //TODO clean-up

    public static final String TIMEOUT_NODE_INSTANCE_ID_VARIABLE = "nodeInstanceId"; //TODO update value

    public final static String CANCEL_SIGNAL_TYPE = "cancel-all";

    public final static String OPERATION_FAILED_SIGNAL_TYPE = "operationFailed";

    public static final String REMOTE_CANCEL_FAILED = "remote-cancel-failed";

    public static final String TIMEOUT_PROCESS_NAME = "timeout-handler-process";

    public static final String CANCEL_TIMEOUT_VARIABLE = "cancelTimeout";

    public static final String MAIN_PROCESS_INSTANCE_ID_VARIABLE = "mainProcessInstanceId";

    public static final String KIE_HOST_SYSTEM_PROPERTY = "KIE_HOSTNAME";

    public static final String CANCEL_URL_JSON_POINTER_VARIABLE = "cancelUrlJsonPointer";
}
