package org.jbpm.contrib.mockserver;

import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServiceFlowTestProcess {

    enum Mode {
        PASS, FAIL;
    }

    private final RuleFlowProcess process;

    public ServiceFlowTestProcess(Mode mode) {
        String successCondition;
        String processName;

        switch (mode) {
            case PASS:
                successCondition = "resultA.person.name == 'Matej'";
                processName = "ServiceFlowTest";
                break;
            case FAIL:
                successCondition = "resultA.person.name == 'SomeOneElse'";
                processName = "ServiceFlowFailingServiceTest";
                break;
            default:
                throw new RuntimeException("Invalid mode.");
        }

        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.jbpm." + processName);
        factory
                // Header
                .name(processName)
                .version("1.0")
                .packageName("org.jbpm")
                .variable("resultA", new StringDataType())
                .variable("resultB", new StringDataType())
                // Nodes
                .startNode(1).name("Start").done()

                .workItemNode(2)
                .workName("RestServiceWorkItemHandler")
                .name("serviceA")
                .workParameter("requestUrl", "http://localhost:8080/demo-service/service/A?callbackDelay=1")
                .workParameter("requestMethod", "POST")
                .workParameter("requestBody", "{\"callbackUrl\":\"${handler.callback.url}\",\"name\":\"Matej\"}")
                .workParameter("successCondition", successCondition)
                .outMapping("content", "resultA")
                .done()

                .workItemNode(3)
                .workName("RestServiceWorkItemHandler")
                .name("serviceB")
                .workParameter("requestUrl", "http://localhost:8080/demo-service/service/B?callbackDelay=1")
                .workParameter("requestMethod", "POST")
                .workParameter("requestBody", "{\"callbackUrl\":\"${handler.callback.url}\",\"nameFromA\":\"#{resultA.person.name}\",\"surname\":\"Lazar\"}")
                .outMapping("content", "resultB")
                .done()

                .endNode(4).name("End").done()
                // Connections
                .connection(1, 2)
                .connection(2, 3)
                .connection(3, 4);
        process = factory.validate().getProcess();
    }

    public byte[] getBytes() {
        return XmlBPMNProcessDumper.INSTANCE.dump(process).getBytes();
    }
}
