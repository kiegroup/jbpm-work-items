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
package org.jbpm.workitem.repositoryspringboot.autoconfig;

import org.jbpm.process.workitem.archive.ArchiveWorkItemHandler;
import org.jbpm.process.workitem.camel.CXFCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.FTPCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.FTPSCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.FileCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.JMSCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.SQLCamelWorkitemHandler;
import org.jbpm.process.workitem.camel.XSLTCamelWorkitemHandler;
import org.jbpm.process.workitem.dropbox.DownloadFileWorkitemHandler;
import org.jbpm.process.workitem.dropbox.UploadFileWorkitemHandler;
import org.jbpm.process.workitem.ethereum.DeployContractWorkitemHandler;
import org.jbpm.process.workitem.ethereum.GetBalanceWorkitemHandler;
import org.jbpm.process.workitem.ethereum.QueryExistingContractWorkitemHandler;
import org.jbpm.process.workitem.ethereum.SendEtherWorkitemHandler;
import org.jbpm.process.workitem.ethereum.TransactExistingContractWorkitemHandler;
import org.jbpm.process.workitem.exec.ExecWorkItemHandler;
import org.jbpm.process.workitem.executesql.ExecuteSqlWorkItemHandler;
import org.jbpm.process.workitem.ftp.FTPUploadWorkItemHandler;
import org.jbpm.process.workitem.github.CreateGistWorkitemHandler;
import org.jbpm.process.workitem.github.FetchIssuesWorkitemHandler;
import org.jbpm.process.workitem.github.ForkRepositoryWorkitemHandler;
import org.jbpm.process.workitem.github.ListRepositoriesWorkitemHandler;
import org.jbpm.process.workitem.github.MergePullRequestWorkitemHandler;
import org.jbpm.process.workitem.google.calendar.AddCalendarWorkitemHandler;
import org.jbpm.process.workitem.google.calendar.AddEventWorkitemHandler;
import org.jbpm.process.workitem.google.calendar.GetCalendarsWorkitemHandler;
import org.jbpm.process.workitem.google.calendar.GetEventsWorkitemHandler;
import org.jbpm.process.workitem.google.sheets.ReadSheetValuesWorkitemHandler;
import org.jbpm.process.workitem.google.tasks.AddTaskWorkitemHandler;
import org.jbpm.process.workitem.google.tasks.GetTasksWorkitemHandler;
import org.jbpm.process.workitem.google.translate.DetectLanguageWorkitemHandler;
import org.jbpm.process.workitem.google.translate.TranslateWorkitemHandler;
import org.jbpm.process.workitem.ibm.watson.ClassifyImageWorkitemHandler;
import org.jbpm.process.workitem.ibm.watson.DetectFacesWorkitemHandler;
import org.jbpm.process.workitem.ifttt.IFTTTWorkitemHandler;
import org.jbpm.process.workitem.jabber.JabberWorkItemHandler;
import org.jbpm.process.workitem.java.JavaInvocationWorkItemHandler;
import org.jbpm.process.workitem.jira.AddCommentOnIssueWorkitemHandler;
import org.jbpm.process.workitem.jira.CreateIssueWorkitemHandler;
import org.jbpm.process.workitem.jira.JqlSearchWorkitemHandler;
import org.jbpm.process.workitem.jira.ResolveIssueWorkitemHandler;
import org.jbpm.process.workitem.kafka.KafkaWorkItemHandler;
import org.jbpm.process.workitem.mavenembedder.MavenEmbedderWorkItemHandler;
import org.jbpm.process.workitem.owm.CurrentWeatherWorkitemHandler;
import org.jbpm.process.workitem.owm.DailyForecastWorkitemHandler;
import org.jbpm.process.workitem.parser.ParserWorkItemHandler;
import org.jbpm.process.workitem.pastebin.CreatePastebinWorkitemHandler;
import org.jbpm.process.workitem.pastebin.GetExistingPastebinWorkitemHandler;
import org.jbpm.process.workitem.pdf.GeneratePDFWorkitemHandler;
import org.jbpm.process.workitem.repositoryspringboot.autoconfig.WorkitemsAutoConfiguration;
import org.jbpm.process.workitem.repositoryspringboot.autoconfig.WorkitemsAutoConfigurationProperties;
import org.jbpm.process.workitem.riot.LastSummonerMatchWorkitemHandler;
import org.jbpm.process.workitem.riot.MatchesInfoWorkitemHandler;
import org.jbpm.process.workitem.riot.SummonerInfoWorkitemHandler;
import org.jbpm.process.workitem.rss.RSSWorkItemHandler;
import org.jbpm.process.workitem.slack.AddReminderWorkitemHandler;
import org.jbpm.process.workitem.slack.PostMessageToChannelWorkitemHandler;
import org.jbpm.process.workitem.transform.TransformWorkItemHandler;
import org.jbpm.process.workitem.twitter.SendDirectMessageWorkitemHandler;
import org.jbpm.process.workitem.twitter.UpdateStatusWorkitemHandler;
import org.jbpm.process.workitem.vimeo.DeleteVideoWorkitemHandler;
import org.jbpm.process.workitem.vimeo.GetVideoInfoWorkitemHandler;
import org.jbpm.process.workitem.vimeo.UpdateVideoMetadataWorkitemHandler;
import org.jbpm.process.workitem.vimeo.UploadVideoWorkitemHandler;
import org.jbpm.workitem.google.drive.MediaDownloadWorkitemHandler;
import org.jbpm.workitem.google.drive.MediaUploadWorkitemHandler;
import org.jbpm.workitem.google.mail.SendMailWorkitemHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkitemsAutoConfiguration.class, loader = AnnotationConfigContextLoader.class)
@TestPropertySource(locations = "classpath:application-empty.properties")
public class RepositorySpringbootEmptyConfigTest {
    @Autowired(required = false)
    WorkitemsAutoConfigurationProperties workitemProperties;

    @Autowired(required = false)
    ArchiveWorkItemHandler archiveWorkItemHandler;

    @Autowired(required = false)
    private FileCamelWorkitemHandler fileCamelWorkitemHandler;

    @Autowired(required = false)
    private CXFCamelWorkitemHandler cxfCamelWorkitemHandler;

    @Autowired(required = false)
    private FTPCamelWorkitemHandler ftpCamelWorkitemHandler;

    @Autowired(required = false)
    private FTPSCamelWorkitemHandler ftpsCamelWorkitemHandler;

    @Autowired(required = false)
    private JMSCamelWorkitemHandler jmsCamelWorkitemHandler;

    @Autowired(required = false)
    private SQLCamelWorkitemHandler sqlCamelWorkitemHandler;

    @Autowired(required = false)
    private XSLTCamelWorkitemHandler xsltCamelWorkitemHandler;

    @Autowired(required = false)
    private DownloadFileWorkitemHandler downloadFileWorkitemHandler;

    @Autowired(required = false)
    private UploadFileWorkitemHandler uploadFileWorkitemHandler;

    @Autowired(required = false)
    private DeployContractWorkitemHandler deployContractWorkitemHandler;

    @Autowired(required = false)
    private GetBalanceWorkitemHandler getBalanceWorkitemHandler;

    @Autowired(required = false)
    private QueryExistingContractWorkitemHandler queryExistingContractWorkitemHandler;

    @Autowired(required = false)
    private SendEtherWorkitemHandler sendEtherWorkitemHandler;

    @Autowired(required = false)
    private TransactExistingContractWorkitemHandler transactExistingContractWorkitemHandler;

    @Autowired(required = false)
    private ExecWorkItemHandler execWorkItemHandler;

    @Autowired(required = false)
    private ExecuteSqlWorkItemHandler executeSqlWorkItemHandler;

    @Autowired(required = false)
    private FTPUploadWorkItemHandler ftpUploadWorkItemHandler;

    @Autowired(required = false)
    private CreateGistWorkitemHandler createGistWorkitemHandler;

    @Autowired(required = false)
    private FetchIssuesWorkitemHandler fetchIssuesWorkitemHandler;

    @Autowired(required = false)
    private ForkRepositoryWorkitemHandler forkRepositoryWorkitemHandler;

    @Autowired(required = false)
    private ListRepositoriesWorkitemHandler listRepositoriesWorkitemHandler;

    @Autowired(required = false)
    private MergePullRequestWorkitemHandler mergePullRequestWorkitemHandler;

    @Autowired(required = false)
    private MediaDownloadWorkitemHandler mediaDownloadWorkitemHandler;

    @Autowired(required = false)
    private MediaUploadWorkitemHandler mediaUploadWorkitemHandler;

    @Autowired(required = false)
    private AddCalendarWorkitemHandler addCalendarWorkitemHandler;

    @Autowired(required = false)
    private AddEventWorkitemHandler addEventWorkitemHandler;

    @Autowired(required = false)
    private GetCalendarsWorkitemHandler getCalendarsWorkitemHandler;

    @Autowired(required = false)
    private GetEventsWorkitemHandler getEventsWorkitemHandler;

    @Autowired(required = false)
    private SendMailWorkitemHandler sendMailWorkitemHandler;

    @Autowired(required = false)
    private ReadSheetValuesWorkitemHandler readSheetValuesWorkitemHandler;

    @Autowired(required = false)
    private AddTaskWorkitemHandler addTaskWorkitemHandler;

    @Autowired(required = false)
    private GetTasksWorkitemHandler getTasksWorkitemHandler;

    @Autowired(required = false)
    private DetectLanguageWorkitemHandler detectLanguageWorkitemHandler;

    @Autowired(required = false)
    private TranslateWorkitemHandler translateWorkitemHandler;

    @Autowired(required = false)
    private ClassifyImageWorkitemHandler classifyImageWorkitemHandler;

    @Autowired(required = false)
    private DetectFacesWorkitemHandler detectFacesWorkitemHandler;

    @Autowired(required = false)
    private IFTTTWorkitemHandler iftttWorkitemHandler;

    @Autowired(required = false)
    private JabberWorkItemHandler jabberWorkItemHandler;

    @Autowired(required = false)
    private JavaInvocationWorkItemHandler javaInvocationWorkItemHandler;

    @Autowired(required = false)
    private AddCommentOnIssueWorkitemHandler addCommentOnIssueWorkitemHandler;

    @Autowired(required = false)
    private CreateIssueWorkitemHandler createIssueWorkitemHandler;

    @Autowired(required = false)
    private JqlSearchWorkitemHandler jqlSearchWorkitemHandler;

    @Autowired(required = false)
    private ResolveIssueWorkitemHandler resolveIssueWorkitemHandler;

    @Autowired(required = false)
    private KafkaWorkItemHandler kafkaWorkItemHandler;

    @Autowired(required = false)
    private MavenEmbedderWorkItemHandler mavenEmbedderWorkItemHandler;

    @Autowired(required = false)
    private CurrentWeatherWorkitemHandler currentWeatherWorkitemHandler;

    @Autowired(required = false)
    private DailyForecastWorkitemHandler dailyForecastWorkitemHandler;

    @Autowired(required = false)
    private ParserWorkItemHandler parserWorkItemHandler;

    @Autowired(required = false)
    private CreatePastebinWorkitemHandler createPastebinWorkitemHandler;

    @Autowired(required = false)
    private GetExistingPastebinWorkitemHandler getExistingPastebinWorkitemHandler;

    @Autowired(required = false)
    private GeneratePDFWorkitemHandler generatePDFWorkitemHandler;

    @Autowired(required = false)
    private LastSummonerMatchWorkitemHandler lastSummonerMatchWorkitemHandler;

    @Autowired(required = false)
    private MatchesInfoWorkitemHandler matchesInfoWorkitemHandler;

    @Autowired(required = false)
    private SummonerInfoWorkitemHandler summonerInfoWorkitemHandler;

    @Autowired(required = false)
    private AddReminderWorkitemHandler addReminderWorkitemHandler;

    @Autowired(required = false)
    private PostMessageToChannelWorkitemHandler postMessageToChannelWorkitemHandler;

    @Autowired(required = false)
    private RSSWorkItemHandler rssWorkItemHandler;

    @Autowired(required = false)
    private TransformWorkItemHandler transformWorkItemHandler;

    @Autowired(required = false)
    private SendDirectMessageWorkitemHandler sendDirectMessageWorkitemHandler;

    @Autowired(required = false)
    private UpdateStatusWorkitemHandler updateStatusWorkitemHandler;

    @Autowired(required = false)
    private DeleteVideoWorkitemHandler deleteVideoWorkitemHandler;

    @Autowired(required = false)
    private GetVideoInfoWorkitemHandler getVideoInfoWorkitemHandler;

    @Autowired(required = false)
    private UpdateVideoMetadataWorkitemHandler updateVideoMetadataWorkitemHandler;

    @Autowired(required = false)
    private UploadVideoWorkitemHandler uploadVideoWorkitemHandler;

    @Test
    public void testWoritemProperties() {
        assertNotNull(workitemProperties);
    }

    @Test
    public void testArchiveWorkitemHandlers() {
        assertNotNull(archiveWorkItemHandler);
    }

    @Test
    public void testCamelWorkitemHandlers() {
        assertNotNull(fileCamelWorkitemHandler);
        assertNotNull(cxfCamelWorkitemHandler);
        assertNotNull(ftpCamelWorkitemHandler);
        assertNotNull(ftpsCamelWorkitemHandler);
        assertNotNull(jmsCamelWorkitemHandler);
        assertNotNull(sqlCamelWorkitemHandler);
        assertNotNull(xsltCamelWorkitemHandler);
    }

    @Test
    public void testDropboxWorkitemHandlers() {
        assertNull(downloadFileWorkitemHandler);
        assertNull(uploadFileWorkitemHandler);
    }

    @Test
    public void testEthereumWorkitemHandlers() {
        assertNull(deployContractWorkitemHandler);
        assertNull(getBalanceWorkitemHandler);
        assertNull(queryExistingContractWorkitemHandler);
        assertNull(sendEtherWorkitemHandler);
        assertNull(transactExistingContractWorkitemHandler);
    }

    @Test
    public void testExecWorkitemHandlers() {
        assertNotNull(execWorkItemHandler);
    }

    @Test
    public void testExecuteSQLWorkitemHandlers() {
        assertNull(executeSqlWorkItemHandler);
    }

    @Test
    public void testFTPWorkitemHandlers() {
        assertNotNull(ftpUploadWorkItemHandler);
    }

    @Test
    public void testGithubWorkitemHandlers() {
        assertNull(createGistWorkitemHandler);
        assertNull(fetchIssuesWorkitemHandler);
        assertNull(forkRepositoryWorkitemHandler);
        assertNull(listRepositoriesWorkitemHandler);
        assertNull(mergePullRequestWorkitemHandler);
    }

    @Test
    public void testGoogleCalendarWorkitemHandlers() {
        assertNull(addCalendarWorkitemHandler);
        assertNull(addEventWorkitemHandler);
        assertNull(getCalendarsWorkitemHandler);
        assertNull(getEventsWorkitemHandler);
    }

    @Test
    public void testGoogleDriveWorkitemHandlers() {
        assertNull(mediaDownloadWorkitemHandler);
        assertNull(mediaUploadWorkitemHandler);
    }

    @Test
    public void testGoogleMailWorkitemHandlers() {
        assertNull(sendMailWorkitemHandler);
    }

    @Test
    public void testGoogleSheetsWorkitemHandlers() {
        assertNull(readSheetValuesWorkitemHandler);
    }

    @Test
    public void testGoogleTasksWorkitemHandlers() {
        assertNull(addTaskWorkitemHandler);
        assertNull(getTasksWorkitemHandler);
    }

    @Test
    public void testGoogleTranslateWorkitemHandlers() {
        assertNull(detectLanguageWorkitemHandler);
        assertNull(translateWorkitemHandler);
    }

    @Test
    public void testIbmWatsonWorkitemHandlers() {
        assertNull(classifyImageWorkitemHandler);
        assertNull(detectFacesWorkitemHandler);
    }

    @Test
    public void testIFTTTWorkitemHandlers() {
        assertNull(iftttWorkitemHandler);
    }

    @Test
    public void testJabberWorkitemHandlers() {
        assertNotNull(jabberWorkItemHandler);
    }

    @Test
    public void testJavaWorkitemHandlers() {
        assertNotNull(javaInvocationWorkItemHandler);
    }

    @Test
    public void testJiraWorkitemHandlers() {
        assertNull(addCommentOnIssueWorkitemHandler);
        assertNull(createIssueWorkitemHandler);
        assertNull(jqlSearchWorkitemHandler);
        assertNull(resolveIssueWorkitemHandler);
    }

    @Test
    public void testKafkaWorkitemHandlers() {
        assertNotNull(kafkaWorkItemHandler);
    }

    @Test
    public void testMavenEmbedderWorkitemHandlers() {
        assertNotNull(mavenEmbedderWorkItemHandler);
    }

    @Test
    public void testOpenWeatherMapWorkitemHandlers() {
        assertNull(currentWeatherWorkitemHandler);
        assertNull(dailyForecastWorkitemHandler);
    }

    @Test
    public void testParserWorkitemHandlers() {
        assertNotNull(parserWorkItemHandler);
    }

    @Test
    public void testPastebinWorkitemHandlers() {
        assertNull(createPastebinWorkitemHandler);
        assertNull(getExistingPastebinWorkitemHandler);
    }

    @Test
    public void testPdfWorkitemHandlers() {
        assertNotNull(generatePDFWorkitemHandler);
    }

    @Test
    public void testRiotWorkitemHandlers() {
        assertNull(lastSummonerMatchWorkitemHandler);
        assertNull(matchesInfoWorkitemHandler);
        assertNull(summonerInfoWorkitemHandler);
    }

    @Test
    public void testRssWorkitemHandlers() {
        assertNotNull(rssWorkItemHandler);
    }

    @Test
    public void testSlackWorkitemHandlers() {
        assertNull(addReminderWorkitemHandler);
        assertNull(postMessageToChannelWorkitemHandler);
    }

    @Test
    public void testTransformWorkitemHandlers() {
        assertNotNull(transformWorkItemHandler);
    }

    @Test
    public void testTwitterWorkitemHandlers() {
        assertNull(sendDirectMessageWorkitemHandler);
        assertNull(updateStatusWorkitemHandler);
    }

    @Test
    public void testVimeoWorkitemHandlers() {
        assertNull(deleteVideoWorkitemHandler);
        assertNull(getVideoInfoWorkitemHandler);
        assertNull(updateVideoMetadataWorkitemHandler);
        assertNull(uploadVideoWorkitemHandler);
    }
}
