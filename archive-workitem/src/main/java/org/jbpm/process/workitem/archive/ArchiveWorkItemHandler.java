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

package org.jbpm.process.workitem.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "ArchiveDefinitions.wid", name = "Archive",
        displayName = "Archive",
        defaultHandler = "mvel: new org.jbpm.process.workitem.archive.ArchiveWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        parameters = {
                @WidParameter(name = "Archive", required = true),
                @WidParameter(name = "Files", runtimeType = "java.util.List")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "archive,file,files,zip",
                action = @WidAction(title = "Archive a list of files.")
        )
)
public class ArchiveWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager manager) {
        String archive = (String) workItem.getParameter("Archive");
        List<File> files = (List<File>) workItem.getParameter("Files");

        try {
            RequiredParameterValidator.validate(this.getClass(),
                                                workItem);

            OutputStream outputStream = new FileOutputStream(new File(archive));
            ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar",
                                                                                          outputStream);

            if (files != null) {
                for (File file : files) {
                    final TarArchiveEntry entry = new TarArchiveEntry("testdata/test1.xml");
                    entry.setModTime(0);
                    entry.setSize(file.length());
                    entry.setUserId(0);
                    entry.setGroupId(0);
                    entry.setMode(0100000);
                    os.putArchiveEntry(entry);
                    IOUtils.copy(new FileInputStream(file),
                                 os);
                }
            }
            os.closeArchiveEntry();
            os.close();
            manager.completeWorkItem(workItem.getId(),
                                     null);
        } catch (Exception e) {
            handleException(e);
            manager.abortWorkItem(workItem.getId());
        }
    }

    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, this work item cannot be aborted
    }
}
