package org.jbpm.process.workitem.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "PropertiesDefinitions.wid", name = "Properties",
        displayName = "Properties",
        defaultHandler = "mvel: new org.jbpm.process.workitem.properties.PropertiesWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "Properties",
        icon = "defaultlogicon.gif",
        parameters = {
                @WidParameter(name = "fileName", runtimeType = "java.lang.String"),
        },
        results = {
                @WidResult(name = "properties", runtimeType = "java.util.Map")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "Properties", description = "Loads a properties file",
                keywords = "properties",
                action = @WidAction(title = "Loads a properties file"),
                authinfo = @WidAuth(required = true, params = {"fileName"},
                paramsdescription = {"File Name"})
        ))
public class PropertiesWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    String directory;
    private static final Logger logger = LoggerFactory.getLogger(PropertiesWorkItemHandler.class);

    /**
     * defaults properties directory to /tmp/
     */
    public PropertiesWorkItemHandler() {
        setLogThrownException(true);
        //set default properties directory
        this.directory = "/tmp/";
    }

    /**
     * 
     * @param directory - where properties files are stored
     */
    public PropertiesWorkItemHandler(String directory) {
        setLogThrownException(true);

        //add file separator to end of directory name
        String lastChar = directory.substring(directory.length() - 1);
        if (lastChar ==  File.separator) {
            this.directory = directory;
        } else {
            this.directory = directory + File.separator;
        }        
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        try {
            String fileName = (String) workItem.getParameter("fileName");
            String fullPath = directory + fileName;

            logger.debug("Loading properties file: {}", fullPath);

            InputStream input = new FileInputStream(fullPath);
            Properties prop = new Properties();
            prop.load(input);

            Map<String, Object> results = new HashMap<>();
            results.put("properties", (Map)prop);
            workItemManager.completeWorkItem(workItem.getId(), results);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        //do nothing
    }

}