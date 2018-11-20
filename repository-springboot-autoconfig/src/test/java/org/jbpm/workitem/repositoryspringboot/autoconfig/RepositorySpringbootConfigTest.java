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
@TestPropertySource(locations = "classpath:application-test.properties")
public class RepositorySpringbootConfigTest {

    @Autowired
    WorkitemsAutoConfigurationProperties workitemProperties;

    @Autowired
    ArchiveWorkItemHandler archiveWorkItemHandler;

    @Autowired
    private FileCamelWorkitemHandler fileCamelWorkitemHandler;

    @Autowired
    private CXFCamelWorkitemHandler cxfCamelWorkitemHandler;

    @Autowired
    private FTPCamelWorkitemHandler ftpCamelWorkitemHandler;

    @Autowired
    private FTPSCamelWorkitemHandler ftpsCamelWorkitemHandler;

    @Autowired
    private JMSCamelWorkitemHandler jmsCamelWorkitemHandler;

    @Autowired
    private SQLCamelWorkitemHandler sqlCamelWorkitemHandler;

    @Autowired
    private XSLTCamelWorkitemHandler xsltCamelWorkitemHandler;

    @Autowired
    private DownloadFileWorkitemHandler downloadFileWorkitemHandler;

    @Autowired
    private UploadFileWorkitemHandler uploadFileWorkitemHandler;

    @Autowired
    private DeployContractWorkitemHandler deployContractWorkitemHandler;

    @Autowired
    private GetBalanceWorkitemHandler getBalanceWorkitemHandler;

    @Autowired
    private QueryExistingContractWorkitemHandler queryExistingContractWorkitemHandler;

    @Autowired
    private SendEtherWorkitemHandler sendEtherWorkitemHandler;

    @Autowired
    private TransactExistingContractWorkitemHandler transactExistingContractWorkitemHandler;

    @Autowired
    private ExecWorkItemHandler execWorkItemHandler;

    @Autowired
    private ExecuteSqlWorkItemHandler executeSqlWorkItemHandler;

    @Autowired
    private FTPUploadWorkItemHandler ftpUploadWorkItemHandler;

    @Autowired
    private CreateGistWorkitemHandler createGistWorkitemHandler;

    @Autowired
    private FetchIssuesWorkitemHandler fetchIssuesWorkitemHandler;

    @Autowired
    private ForkRepositoryWorkitemHandler forkRepositoryWorkitemHandler;

    @Autowired
    private ListRepositoriesWorkitemHandler listRepositoriesWorkitemHandler;

    @Autowired
    private MergePullRequestWorkitemHandler mergePullRequestWorkitemHandler;

    @Autowired
    private MediaDownloadWorkitemHandler mediaDownloadWorkitemHandler;

    @Autowired
    private MediaUploadWorkitemHandler mediaUploadWorkitemHandler;

    @Autowired
    private AddCalendarWorkitemHandler addCalendarWorkitemHandler;

    @Autowired
    private AddEventWorkitemHandler addEventWorkitemHandler;

    @Autowired
    private GetCalendarsWorkitemHandler getCalendarsWorkitemHandler;

    @Autowired
    private GetEventsWorkitemHandler getEventsWorkitemHandler;

    @Autowired
    private SendMailWorkitemHandler sendMailWorkitemHandler;

    @Autowired
    private ReadSheetValuesWorkitemHandler readSheetValuesWorkitemHandler;

    @Autowired
    private AddTaskWorkitemHandler addTaskWorkitemHandler;

    @Autowired
    private GetTasksWorkitemHandler getTasksWorkitemHandler;

    @Autowired
    private DetectLanguageWorkitemHandler detectLanguageWorkitemHandler;

    @Autowired
    private TranslateWorkitemHandler translateWorkitemHandler;

    @Autowired
    private ClassifyImageWorkitemHandler classifyImageWorkitemHandler;

    @Autowired
    private DetectFacesWorkitemHandler detectFacesWorkitemHandler;

    @Autowired
    private IFTTTWorkitemHandler iftttWorkitemHandler;

    @Autowired
    private JabberWorkItemHandler jabberWorkItemHandler;

    @Autowired
    private JavaInvocationWorkItemHandler javaInvocationWorkItemHandler;

    @Autowired
    private AddCommentOnIssueWorkitemHandler addCommentOnIssueWorkitemHandler;

    @Autowired
    private CreateIssueWorkitemHandler createIssueWorkitemHandler;

    @Autowired
    private JqlSearchWorkitemHandler jqlSearchWorkitemHandler;

    @Autowired
    private ResolveIssueWorkitemHandler resolveIssueWorkitemHandler;

    @Autowired
    private KafkaWorkItemHandler kafkaWorkItemHandler;

    @Autowired
    private MavenEmbedderWorkItemHandler mavenEmbedderWorkItemHandler;

    @Autowired
    private CurrentWeatherWorkitemHandler currentWeatherWorkitemHandler;

    @Autowired
    private DailyForecastWorkitemHandler dailyForecastWorkitemHandler;

    @Autowired
    private ParserWorkItemHandler parserWorkItemHandler;

    @Autowired
    private CreatePastebinWorkitemHandler createPastebinWorkitemHandler;

    @Autowired
    private GetExistingPastebinWorkitemHandler getExistingPastebinWorkitemHandler;

    @Autowired
    private GeneratePDFWorkitemHandler generatePDFWorkitemHandler;

    @Autowired
    private LastSummonerMatchWorkitemHandler lastSummonerMatchWorkitemHandler;

    @Autowired
    private MatchesInfoWorkitemHandler matchesInfoWorkitemHandler;

    @Autowired
    private SummonerInfoWorkitemHandler summonerInfoWorkitemHandler;

    @Autowired
    private AddReminderWorkitemHandler addReminderWorkitemHandler;

    @Autowired
    private PostMessageToChannelWorkitemHandler postMessageToChannelWorkitemHandler;

    @Autowired
    private RSSWorkItemHandler rssWorkItemHandler;

    @Autowired
    private TransformWorkItemHandler transformWorkItemHandler;

    @Autowired
    private SendDirectMessageWorkitemHandler sendDirectMessageWorkitemHandler;

    @Autowired
    private UpdateStatusWorkitemHandler updateStatusWorkitemHandler;

    @Autowired
    private DeleteVideoWorkitemHandler deleteVideoWorkitemHandler;

    @Autowired
    private GetVideoInfoWorkitemHandler getVideoInfoWorkitemHandler;

    @Autowired
    private UpdateVideoMetadataWorkitemHandler updateVideoMetadataWorkitemHandler;

    @Autowired
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
        assertNotNull(downloadFileWorkitemHandler);
        assertNotNull(uploadFileWorkitemHandler);
    }

    @Test
    public void testEthereumWorkitemHandlers() {
        assertNotNull(deployContractWorkitemHandler);
        assertNotNull(getBalanceWorkitemHandler);
        assertNotNull(queryExistingContractWorkitemHandler);
        assertNotNull(sendEtherWorkitemHandler);
        assertNotNull(transactExistingContractWorkitemHandler);
    }

    @Test
    public void testExecWorkitemHandlers() {
        assertNotNull(execWorkItemHandler);
    }

    @Test
    public void testExecuteSQLWorkitemHandlers() {
        assertNotNull(executeSqlWorkItemHandler);
    }

    @Test
    public void testFTPWorkitemHandlers() {
        assertNotNull(ftpUploadWorkItemHandler);
    }

    @Test
    public void testGithubWorkitemHandlers() {
        assertNotNull(createGistWorkitemHandler);
        assertNotNull(fetchIssuesWorkitemHandler);
        assertNotNull(forkRepositoryWorkitemHandler);
        assertNotNull(listRepositoriesWorkitemHandler);
        assertNotNull(mergePullRequestWorkitemHandler);
    }

    @Test
    public void testGoogleCalendarWorkitemHandlers() {
        assertNotNull(addCalendarWorkitemHandler);
        assertNotNull(addEventWorkitemHandler);
        assertNotNull(getCalendarsWorkitemHandler);
        assertNotNull(getEventsWorkitemHandler);
    }

    @Test
    public void testGoogleDriveWorkitemHandlers() {
        assertNotNull(mediaDownloadWorkitemHandler);
        assertNotNull(mediaUploadWorkitemHandler);
    }

    @Test
    public void testGoogleMailWorkitemHandlers() {
        assertNotNull(sendMailWorkitemHandler);
    }

    @Test
    public void testGoogleSheetsWorkitemHandlers() {
        assertNotNull(readSheetValuesWorkitemHandler);
    }

    @Test
    public void testGoogleTasksWorkitemHandlers() {
        assertNotNull(addTaskWorkitemHandler);
        assertNotNull(getTasksWorkitemHandler);
    }

    @Test
    public void testGoogleTranslateWorkitemHandlers() {
        assertNotNull(detectLanguageWorkitemHandler);
        assertNotNull(translateWorkitemHandler);
    }

    @Test
    public void testIbmWatsonWorkitemHandlers() {
        assertNotNull(classifyImageWorkitemHandler);
        assertNotNull(detectFacesWorkitemHandler);
    }

    @Test
    public void testIFTTTWorkitemHandlers() {
        assertNotNull(iftttWorkitemHandler);
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
        assertNotNull(addCommentOnIssueWorkitemHandler);
        assertNotNull(createIssueWorkitemHandler);
        assertNotNull(jqlSearchWorkitemHandler);
        assertNotNull(resolveIssueWorkitemHandler);
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
        assertNotNull(currentWeatherWorkitemHandler);
        assertNotNull(dailyForecastWorkitemHandler);
    }

    @Test
    public void testParserWorkitemHandlers() {
        assertNotNull(parserWorkItemHandler);
    }

    @Test
    public void testPastebinWorkitemHandlers() {
        assertNotNull(createPastebinWorkitemHandler);
        assertNotNull(getExistingPastebinWorkitemHandler);
    }

    @Test
    public void testPdfWorkitemHandlers() {
        assertNotNull(generatePDFWorkitemHandler);
    }

    @Test
    public void testRiotWorkitemHandlers() {
        assertNotNull(lastSummonerMatchWorkitemHandler);
        assertNotNull(matchesInfoWorkitemHandler);
        assertNotNull(summonerInfoWorkitemHandler);
    }

    @Test
    public void testRssWorkitemHandlers() {
        assertNotNull(rssWorkItemHandler);
    }

    @Test
    public void testSlackWorkitemHandlers() {
        assertNotNull(addReminderWorkitemHandler);
        assertNotNull(postMessageToChannelWorkitemHandler);
    }

    @Test
    public void testTransformWorkitemHandlers() {
        assertNotNull(transformWorkItemHandler);
    }

    @Test
    public void testTwitterWorkitemHandlers() {
        assertNotNull(sendDirectMessageWorkitemHandler);
        assertNotNull(updateStatusWorkitemHandler);
    }

    @Test
    public void testVimeoWorkitemHandlers() {
        assertNotNull(deleteVideoWorkitemHandler);
        assertNotNull(getVideoInfoWorkitemHandler);
        assertNotNull(updateVideoMetadataWorkitemHandler);
        assertNotNull(uploadVideoWorkitemHandler);
    }
}
