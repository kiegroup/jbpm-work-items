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

package org.jbpm.process.workitem.jabber;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "JabberDefinitions.wid", name = "Jabber",
        displayName = "Jabber",
        defaultHandler = "mvel: new org.jbpm.process.workitem.jabber.JabberWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "Jabber.png",
        parameters = {
                @WidParameter(name = "User"),
                @WidParameter(name = "Password"),
                @WidParameter(name = "Server"),
                @WidParameter(name = "Port"),
                @WidParameter(name = "Service"),
                @WidParameter(name = "Text"),
                @WidParameter(name = "To", required = true)
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "jabber,im,xmpp,message,send",
                action = @WidAction(title = "Send a message using Jabber"),
                authinfo = @WidAuth(required = true, params = {"user", "password"},
                        paramsdescription = {"Jabber user", "Jabber password"},
                        referencesite = "https://www.jabber.org/")
        ))
public class JabberWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(JabberWorkItemHandler.class);

    private String user;
    private String password;
    private String server;
    private int port;
    private String service;
    private String text;
    private ConnectionConfiguration conf;
    private XMPPConnection connection;

    private List<String> toUsers = new ArrayList<String>();

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {

        try {

            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            this.user = (String) workItem.getParameter("User");
            this.password = (String) workItem.getParameter("Password");
            this.server = (String) workItem.getParameter("Server");
            String portString = (String) workItem.getParameter("Port");
            if (portString != null && !portString.equals("")) {
                this.port = Integer.valueOf((String) workItem.getParameter("Port"));
            }
            this.service = (String) workItem.getParameter("Service");
            this.text = (String) workItem.getParameter("Text");

            String to = (String) workItem.getParameter("To");

            for (String s : to.split(";")) {
                if (s != null && !"".equals(s)) {
                    this.toUsers.add(s);
                }
            }

            if (conf == null) {
                conf = new ConnectionConfiguration(server,
                                                   port,
                                                   service);
            }

            if (server != null && !server.equals("") && port != 0) {
                if (connection == null) {
                    connection = new XMPPConnection(conf);
                }
            } else {
                if (connection == null) {
                    connection = new XMPPConnection(service);
                }
            }

            connection.connect();
            logger.info("Connected to {}",
                        connection.getHost());

            connection.login(user,
                             password);
            logger.info("Logged in as {}",
                        connection.getUser());
            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);

            for (String toUser : toUsers) {

                ChatManager chatmanager = connection.getChatManager();
                Chat chat = chatmanager.createChat(toUser,
                                                   null);

                // google bounces back the default message types, you must use chat
                Message msg = new Message(toUser,
                                          Message.Type.chat);
                msg.setBody(text);
                chat.sendMessage(msg);
                logger.info("Message Sent {}",
                            msg);
            }

            connection.disconnect();

            manager.completeWorkItem(workItem.getId(),
                                     null);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
    }

    // for testing
    public void setConf(ConnectionConfiguration conf) {
        this.conf = conf;
    }

    // for testing
    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }
}
