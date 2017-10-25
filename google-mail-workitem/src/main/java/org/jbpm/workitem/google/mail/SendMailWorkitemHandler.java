/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.workitem.google.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.jbpm.document.Document;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "GoogleSendMailDefinitions.wid", name = "GoogleSendMail",
        displayName = "GoogleSendMail",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.mail.SendMailWorkitemHandler()",
        parameters = {
                @WidParameter(name = "To"),
                @WidParameter(name = "From"),
                @WidParameter(name = "Subject"),
                @WidParameter(name = "BodyText"),
                @WidParameter(name = "Attachment")
        },
        results = {
                @WidResult(name = "Message")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.google.api-client", artifact = "google-api-client", version = "1.23.0"),
                @WidMavenDepends(group = "com.google.oauth-client", artifact = "google-oauth-client-jetty", version = "1.23.0"),
                @WidMavenDepends(group = "com.google.apis", artifact = "google-api-services-gmail", version = "v1-rev72-1.23.0")
        })
public class SendMailWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private String appName;
    private String clientSecret;
    private GoogleMailAuth auth = new GoogleMailAuth();

    public SendMailWorkitemHandler(String appName,
                                   String clentSecret) {
        this.appName = appName;
        this.clientSecret = clentSecret;
    }

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {
        String paramTo = (String) workItem.getParameter("To");
        String paramFrom = (String) workItem.getParameter("From");
        String paramSubject = (String) workItem.getParameter("Subject");
        String paramBodyText = (String) workItem.getParameter("BodyText");
        Document paramAttachment = (Document) workItem.getParameter("Attachment");

        try {
            Gmail gmailService = auth.getGmailService(appName,
                                                      clientSecret);
            Message outEmailMessage = sendMessage(gmailService,
                                                  paramTo,
                                                  paramFrom,
                                                  paramSubject,
                                                  paramBodyText,
                                                  paramAttachment);
        } catch (Exception e) {
            handleException(e);
        }

        workItemManager.completeWorkItem(workItem.getId(),
                                         null);
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    public Message sendMessage(Gmail service,
                               String to,
                               String from,
                               String subject,
                               String bodyText,
                               Document attachment)
            throws MessagingException, IOException {
        MimeMessage mimeMessage = createEmailWithAttachment(to,
                                                            from,
                                                            subject,
                                                            bodyText,
                                                            attachment);
        Message message = service.users().messages().send(from,
                                                          createMessageWithEmail(mimeMessage)).execute();

        return message;
    }

    public static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public MimeMessage createEmailWithAttachment(String to,
                                                 String from,
                                                 String subject,
                                                 String bodyText,
                                                 Document attachment)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props,
                                                     null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                           new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText,
                                "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        if (attachment != null) {
            mimeBodyPart = new MimeBodyPart();
            DataSource source = new InputStreamDataSource(new ByteArrayInputStream(attachment.getContent()));

            mimeBodyPart.setDataHandler(new DataHandler(source));
            mimeBodyPart.setFileName(attachment.getName());
            multipart.addBodyPart(mimeBodyPart);
        }
        email.setContent(multipart);

        return email;
    }

    // for testing
    public void setAuth(GoogleMailAuth auth) {
        this.auth = auth;
    }

    public class InputStreamDataSource implements DataSource {

        private InputStream inputStream;

        public InputStreamDataSource(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getContentType() {
            return "*/*";
        }

        @Override
        public String getName() {
            return "InputStreamDataSource";
        }
    }
}
