/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.workitem.repository;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.IO;
import org.jbpm.process.workitem.WorkDefinitionImpl;
import org.jbpm.process.workitem.WorkItemRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HostedRepositoryIntegrationTest {

    private static Server server;
    private static ServerConnector connector;

    private static int PORT = 0;
    private static String DEFAULT_HOST = "localhost";

    // update this when new workitems are added
    private static int TOTAL_WORKITEM_HANDLERS = Integer.parseInt(System.getProperty("total.wi.count", "94"));

    @BeforeClass
    public static void setUp() throws Exception {
        // uncomment this to debug locally in IDE
        //setConfigSystemPropertiesForDebugging();

        server = new Server();
        connector = new ServerConnector(server);
        connector.setPort(PORT);
        server.addConnector(connector);

        HandlerList handlers = new HandlerList();
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder defaultServ = new ServletHolder("default",
                                                      DefaultServlet.class);
        defaultServ.setInitParameter("resourceBase",
                                     System.getProperty("builddir") + "/" +
                                             System.getProperty("artifactId") + "-" +
                                             System.getProperty("version") + "/");
        defaultServ.setInitParameter("dirAllowed",
                                     "true");
        context.addServlet(defaultServ,
                           "/");
        handlers.addHandler(context);
        server.setHandler(handlers);

        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            server.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRepositoryIndexPageExists() throws Exception {
        URI uri = toServerURI(connector);
        assertNotNull(getResponse(uri));
    }

    @Test
    public void testRepositoryValidity() throws Exception {
        URI uri = toServerURI(connector);
        Map<String, WorkDefinitionImpl> repoResults = WorkItemRepository.getWorkDefinitions(uri.toString());
        assertNotNull(repoResults);
        assertEquals(TOTAL_WORKITEM_HANDLERS, repoResults.size());
    }

    private URI toServerURI(ServerConnector connector) throws URISyntaxException {
        String host = connector.getHost();
        if (host == null) {
            host = DEFAULT_HOST;
        }
        int port = connector.getLocalPort();
        return new URI(String.format("http://%s:%d",
                                     host,
                                     port));
    }

    private String getResponse(URI uri) throws IOException {
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        assertThat("Valid Response Code",
                   http.getResponseCode(),
                   anyOf(is(200),
                         is(404)));

        try (InputStream in = http.getInputStream()) {
            return IO.toString(in,
                               StandardCharsets.UTF_8);
        }
    }

    private static void setConfigSystemPropertiesForDebugging() throws Exception {
        System.setProperty("groupId",
                           "org.jbpm.contrib");
        System.setProperty("builddir",
                           "/Users/tsurdilovic/devel/jbpm-work-items/repository/target");
        System.setProperty("artifactId",
                           "repository");
        System.setProperty("version",
                           getProjectVersion());
    }

    private static String getProjectVersion() throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));
        return model.getParent().getVersion();
    }
}
