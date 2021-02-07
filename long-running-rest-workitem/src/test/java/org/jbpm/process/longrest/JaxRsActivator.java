package org.jbpm.process.longrest;

import org.jbpm.process.longrest.demoservices.Service;
import org.jbpm.process.longrest.mockserver.WorkItems;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class JaxRsActivator extends Application {

    private final Set<Class<?>> classes;

    public JaxRsActivator() {
        classes = new HashSet<>();
        classes.add(Service.class);
        classes.add(WorkItems.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
