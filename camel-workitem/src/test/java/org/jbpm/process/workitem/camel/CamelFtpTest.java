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
package org.jbpm.process.workitem.camel;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemManager;

public class CamelFtpTest extends CamelFtpBaseTest {

    @Test
    public void testFtp() throws IOException {
        FTPCamelWorkitemHandler handler = new FTPCamelWorkitemHandler();

        final String testData = "test-data";

        final WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("username",
                              USER);
        workItem.setParameter("password",
                              PASSWD);
        workItem.setParameter("hostname",
                              HOST);
        workItem.setParameter("port",
                              PORT.toString());
        workItem.setParameter("directoryname",
                              testFile.getParentFile().getName());
        workItem.setParameter("CamelFileName",
                              testFile.getName());
        workItem.setParameter("payload",
                              testData);

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem,
                                manager);

        Assert.assertTrue("Expected file does not exist.",
                          testFile.exists());

        String resultText = FileUtils.readFileToString(testFile);
        Assert.assertEquals(resultText,
                            testData);
    }

    @Override
    protected FtpServer configureFtpServer(CamelFtpBaseTest.FtpServerBuilder builder) throws FtpException {
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setServerAddress(HOST);
        listenerFactory.setPort(PORT);

        return builder.addUser(USER,
                               PASSWD,
                               ftpRoot,
                               true).registerDefaultListener(listenerFactory.createListener()).build();
    }
}
