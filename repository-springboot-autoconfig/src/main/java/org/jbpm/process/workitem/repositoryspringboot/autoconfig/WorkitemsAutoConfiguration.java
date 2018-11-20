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
package org.jbpm.process.workitem.repositoryspringboot.autoconfig;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WorkitemsAutoConfigurationProperties.class)
public class WorkitemsAutoConfiguration {

    private WorkitemsAutoConfigurationProperties workitemProperties;

    public WorkitemsAutoConfiguration(WorkitemsAutoConfigurationProperties workitemProperties) {
        this.workitemProperties = workitemProperties;
    }

    // Archive workitems
    @Bean
    public static ArchiveWorkItemHandler archiveWorkItemHandler() {
        return new ArchiveWorkItemHandler();
    }

    // Camel workitems
    @Bean
    public CXFCamelWorkitemHandler cxfCamelWorkitemHandler() {
        if (workitemProperties.getCamelHeaders() != null) {
            return new CXFCamelWorkitemHandler(workitemProperties.getCamelHeaders());
        } else {
            return new CXFCamelWorkitemHandler();
        }
    }

    @Bean
    public FileCamelWorkitemHandler fileCamelWorkitemHandler() {
        if (workitemProperties.getCamelHeaders() != null) {
            return new FileCamelWorkitemHandler(workitemProperties.getCamelHeaders());
        } else {
            return new FileCamelWorkitemHandler();
        }
    }

    @Bean
    public FTPCamelWorkitemHandler ftpCamelWorkitemHandler() {
        return new FTPCamelWorkitemHandler();
    }

    @Bean
    public FTPSCamelWorkitemHandler ftpsCamelWorkitemHandler() {
        return new FTPSCamelWorkitemHandler();
    }

    @Bean
    public JMSCamelWorkitemHandler jmsCamelWorkitemHandler() {
        if (workitemProperties.getCamelHeaders() != null) {
            return new JMSCamelWorkitemHandler(workitemProperties.getCamelHeaders());
        } else {
            return new JMSCamelWorkitemHandler();
        }
    }

    @Bean
    public SQLCamelWorkitemHandler sqlCamelWorkitemHandler() {
        if (workitemProperties.getCamelHeaders() != null) {
            return new SQLCamelWorkitemHandler(workitemProperties.getCamelHeaders());
        } else {
            return new SQLCamelWorkitemHandler();
        }
    }

    @Bean
    public XSLTCamelWorkitemHandler xsltCamelWorkitemHandler() {
        if (workitemProperties.getCamelHeaders() != null) {
            return new XSLTCamelWorkitemHandler(workitemProperties.getCamelHeaders());
        } else {
            return new XSLTCamelWorkitemHandler();
        }
    }

    // Dropbox workitems
    @Bean
    @ConditionalOnProperty({"workitems.dropboxClientIdentifier", "workitems.dropboxAccessToken"})
    public DownloadFileWorkitemHandler downloadFileWorkitemHandler() {
        return new DownloadFileWorkitemHandler(workitemProperties.getDropboxClientIdentifier(),
                                               workitemProperties.getDropboxAccessToken());
    }

    @Bean
    @ConditionalOnProperty({"workitems.dropboxClientIdentifier", "workitems.dropboxAccessToken"})
    public UploadFileWorkitemHandler uploadFileWorkitemHandler() {
        return new UploadFileWorkitemHandler(workitemProperties.getDropboxClientIdentifier(),
                                             workitemProperties.getDropboxAccessToken());
    }

    // Ethereum workitems
    @Bean
    @ConditionalOnProperty({"workitems.ethereumWalletPassword", "workitems.ethereumWalletPath"})
    public DeployContractWorkitemHandler deployContractWorkitemHandler() {
        return new DeployContractWorkitemHandler(workitemProperties.getEthereumWalletPassword(),
                                                 workitemProperties.getEthereumWalletPath());
    }

    @Bean
    @ConditionalOnProperty({"workitems.ethereumWalletPassword", "workitems.ethereumWalletPath"})
    public GetBalanceWorkitemHandler getBalanceWorkitemHandler() {
        return new GetBalanceWorkitemHandler(workitemProperties.getEthereumWalletPassword(),
                                             workitemProperties.getEthereumWalletPath());
    }

    @Bean
    @ConditionalOnProperty({"workitems.ethereumWalletPassword", "workitems.ethereumWalletPath"})
    public QueryExistingContractWorkitemHandler queryExistingContractWorkitemHandler() {
        return new QueryExistingContractWorkitemHandler(workitemProperties.getEthereumWalletPassword(),
                                                        workitemProperties.getEthereumWalletPath());
    }

    @Bean
    @ConditionalOnProperty({"workitems.ethereumWalletPassword", "workitems.ethereumWalletPath"})
    public SendEtherWorkitemHandler sendEtherWorkitemHandler() {
        return new SendEtherWorkitemHandler(workitemProperties.getEthereumWalletPassword(),
                                            workitemProperties.getEthereumWalletPath());
    }

    @Bean
    @ConditionalOnProperty({"workitems.ethereumWalletPassword", "workitems.ethereumWalletPath"})
    public TransactExistingContractWorkitemHandler transactExistingContractWorkitemHandler() {
        return new TransactExistingContractWorkitemHandler(workitemProperties.getEthereumWalletPassword(),
                                                           workitemProperties.getEthereumWalletPath());
    }

    // Exec workitems
    @Bean
    public ExecWorkItemHandler execWorkItemHandlers() {
        return new ExecWorkItemHandler();
    }

    // ExecSQL workitems
    @Bean
    @ConditionalOnProperty("workitems.execSqlDataSourceName")
    public ExecuteSqlWorkItemHandler executeSqlWorkItemHandler() {
        return new ExecuteSqlWorkItemHandler(workitemProperties.getExecSqlDataSourceName());
    }

    // FTP workitems
    @Bean
    public FTPUploadWorkItemHandler ftpUploadWorkItemHandler() {
        return new FTPUploadWorkItemHandler();
    }

    // Github workitems
    @Bean
    @ConditionalOnProperty({"workitems.githubUserName", "workitems.githubPassword"})
    public CreateGistWorkitemHandler createGistWorkitemHandler() {
        return new CreateGistWorkitemHandler(workitemProperties.getGithubUserName(),
                                             workitemProperties.getGithubPassword());
    }

    @Bean
    @ConditionalOnProperty({"workitems.githubUserName", "workitems.githubPassword"})
    public FetchIssuesWorkitemHandler fetchIssuesWorkitemHandler() {
        return new FetchIssuesWorkitemHandler(workitemProperties.getGithubUserName(),
                                              workitemProperties.getGithubPassword());
    }

    @Bean
    @ConditionalOnProperty({"workitems.githubUserName", "workitems.githubPassword"})
    public ForkRepositoryWorkitemHandler forkRepositoryWorkitemHandler() {
        return new ForkRepositoryWorkitemHandler(workitemProperties.getGithubUserName(),
                                                 workitemProperties.getGithubPassword());
    }

    @Bean
    @ConditionalOnProperty({"workitems.githubUserName", "workitems.githubPassword"})
    public ListRepositoriesWorkitemHandler listRepositoriesWorkitemHandler() {
        return new ListRepositoriesWorkitemHandler(workitemProperties.getGithubUserName(),
                                                   workitemProperties.getGithubPassword());
    }

    @Bean
    @ConditionalOnProperty({"workitems.githubUserName", "workitems.githubPassword"})
    public MergePullRequestWorkitemHandler mergePullRequestWorkitemHandler() {
        return new MergePullRequestWorkitemHandler(workitemProperties.getGithubUserName(),
                                                   workitemProperties.getGithubPassword());
    }

    // Google Calendar workitems
    @Bean
    @ConditionalOnProperty({"workitems.googleCalendarAppName", "workitems.googleCalendarClientSecret"})
    public AddCalendarWorkitemHandler addCalendarWorkitemHandler() {
        return new AddCalendarWorkitemHandler(workitemProperties.getGoogleCalendarAppName(),
                                              workitemProperties.getGoogleCalendarClientSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.googleCalendarAppName", "workitems.googleCalendarClientSecret"})
    public AddEventWorkitemHandler addEventWorkitemHandler() {
        return new AddEventWorkitemHandler(workitemProperties.getGoogleCalendarAppName(),
                                           workitemProperties.getGoogleCalendarClientSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.googleCalendarAppName", "workitems.googleCalendarClientSecret"})
    public GetCalendarsWorkitemHandler getCalendarsWorkitemHandler() {
        return new GetCalendarsWorkitemHandler(workitemProperties.getGoogleCalendarAppName(),
                                               workitemProperties.getGoogleCalendarClientSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.googleCalendarAppName", "workitems.googleCalendarClientSecret"})
    public GetEventsWorkitemHandler getEventsWorkitemHandler() {
        return new GetEventsWorkitemHandler(workitemProperties.getGoogleCalendarAppName(),
                                            workitemProperties.getGoogleCalendarClientSecret());
    }

    // Google Drive workitems
    @Bean
    @ConditionalOnProperty({"workitems.googleDriveAppName", "workitems.googleDriveClientSecret"})
    public MediaDownloadWorkitemHandler mediaDownloadWorkitemHandler() {
        return new MediaDownloadWorkitemHandler(workitemProperties.getGoogleDriveAppName(),
                                                workitemProperties.getGoogleDriveClientSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.googleDriveAppName", "workitems.googleDriveClientSecret"})
    public MediaUploadWorkitemHandler mediaUploadWorkitemHandler() {
        return new MediaUploadWorkitemHandler(workitemProperties.getGoogleDriveAppName(),
                                              workitemProperties.getGoogleDriveClientSecret());
    }

    // Google Mail workitems
    @Bean
    @ConditionalOnProperty({"workitems.googleMailAppName", "workitems.googleMailClientSecret"})
    public SendMailWorkitemHandler sendMailWorkitemHandler() {
        return new SendMailWorkitemHandler(workitemProperties.getGoogleMailAppName(),
                                           workitemProperties.getGoogleMailClientSecret());
    }

    // Google Sheets workitems
    @Bean
    @ConditionalOnProperty({"workitems.googleSheetsAppName", "workitems.googleSheetsClientSecret"})
    public ReadSheetValuesWorkitemHandler readSheetValuesWorkitemHandler() {
        return new ReadSheetValuesWorkitemHandler(workitemProperties.getGoogleSheetsAppName(),
                                                  workitemProperties.getGoogleSheetsClientSecret());
    }

    // Google Tasks workitems
    @Bean
    @ConditionalOnProperty({"workitems.googleTasksAppName", "workitems.googleTasksClientSecret"})
    public GetTasksWorkitemHandler getTasksWorkitemHandler() {
        return new GetTasksWorkitemHandler(workitemProperties.getGoogleTasksAppName(),
                                           workitemProperties.getGoogleTasksClientSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.googleTasksAppName", "workitems.googleTasksClientSecret"})
    public AddTaskWorkitemHandler addTaskWorkitemHandler() {
        return new AddTaskWorkitemHandler(workitemProperties.getGoogleTasksAppName(),
                                          workitemProperties.getGoogleTasksClientSecret());
    }

    // Google Translate workitems
    @Bean
    @ConditionalOnProperty("workitems.googleTranslateApiKey")
    public DetectLanguageWorkitemHandler detectLanguageWorkitemHandler() {
        return new DetectLanguageWorkitemHandler(workitemProperties.getGoogleTranslateApiKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.googleTranslateApiKey")
    public TranslateWorkitemHandler translateWorkitemHandler() {
        return new TranslateWorkitemHandler(workitemProperties.getGoogleTranslateApiKey());
    }

    // IBM Watson workitems
    @Bean
    @ConditionalOnProperty("workitems.ibmWatsonApiKey")
    public ClassifyImageWorkitemHandler classifyImageWorkitemHandler() {
        return new ClassifyImageWorkitemHandler(workitemProperties.getIbmWatsonApiKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.ibmWatsonApiKey")
    public DetectFacesWorkitemHandler detectFacesWorkitemHandler() {
        return new DetectFacesWorkitemHandler(workitemProperties.getIbmWatsonApiKey());
    }

    // IFTTT Workitems
    @Bean
    @ConditionalOnProperty("workitems.iftttKey")
    public IFTTTWorkitemHandler iftttWorkitemHandler() {
        return new IFTTTWorkitemHandler(workitemProperties.getIftttKey());
    }

    // Jabber Workitems
    @Bean
    public JabberWorkItemHandler jabberWorkItemHandler() {
        return new JabberWorkItemHandler();
    }

    // Java Workitems
    @Bean
    public JavaInvocationWorkItemHandler javaInvocationWorkItemHandler() {
        return new JavaInvocationWorkItemHandler();
    }

    // Jira Workitems
    @Bean
    @ConditionalOnProperty({"workitems.jiraUserName", "workitems.jiraPassword", "workitems.jiraRepoURI"})
    public AddCommentOnIssueWorkitemHandler addCommentOnIssueWorkitemHandler() {
        return new AddCommentOnIssueWorkitemHandler(workitemProperties.getJiraUserName(),
                                                    workitemProperties.getJiraPassword(),
                                                    workitemProperties.getJiraRepoURI());
    }

    @Bean
    @ConditionalOnProperty({"workitems.jiraUserName", "workitems.jiraPassword", "workitems.jiraRepoURI"})
    public CreateIssueWorkitemHandler createIssueWorkitemHandler() {
        return new CreateIssueWorkitemHandler(workitemProperties.getJiraUserName(),
                                              workitemProperties.getJiraPassword(),
                                              workitemProperties.getJiraRepoURI());
    }

    @Bean
    @ConditionalOnProperty({"workitems.jiraUserName", "workitems.jiraPassword", "workitems.jiraRepoURI"})
    public JqlSearchWorkitemHandler jqlSearchWorkitemHandler() {
        return new JqlSearchWorkitemHandler(workitemProperties.getJiraUserName(),
                                            workitemProperties.getJiraPassword(),
                                            workitemProperties.getJiraRepoURI());
    }

    @Bean
    @ConditionalOnProperty({"workitems.jiraUserName", "workitems.jiraPassword", "workitems.jiraRepoURI"})
    public ResolveIssueWorkitemHandler resolveIssueWorkitemHandler() {
        return new ResolveIssueWorkitemHandler(workitemProperties.getJiraUserName(),
                                               workitemProperties.getJiraPassword(),
                                               workitemProperties.getJiraRepoURI());
    }

    // Kafka Workitems
    @Bean
    public KafkaWorkItemHandler kafkaWorkItemHandler() {
        return new KafkaWorkItemHandler();
    }

    // Maven Embedder Workitems
    @Bean
    public MavenEmbedderWorkItemHandler mavenEmbedderWorkItemHandler() {
        return new MavenEmbedderWorkItemHandler();
    }

    // OpenWeatherMap Workitems
    @Bean
    @ConditionalOnProperty("workitems.openWeatherMapApiKey")
    public CurrentWeatherWorkitemHandler currentWeatherWorkitemHandler() {
        return new CurrentWeatherWorkitemHandler(workitemProperties.getOpenWeatherMapApiKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.openWeatherMapApiKey")
    public DailyForecastWorkitemHandler dailyForecastWorkitemHandler() {
        return new DailyForecastWorkitemHandler(workitemProperties.getOpenWeatherMapApiKey());
    }

    // Parser Workitems
    @Bean
    public ParserWorkItemHandler parserWorkItemHandler() {
        return new ParserWorkItemHandler();
    }

    // Pastebin Workitems
    @Bean
    @ConditionalOnProperty("workitems.pastebinDevelKey")
    public CreatePastebinWorkitemHandler createPastebinWorkitemHandler() {
        return new CreatePastebinWorkitemHandler(workitemProperties.getPastebinDevelKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.pastebinDevelKey")
    public GetExistingPastebinWorkitemHandler getExistingPastebinWorkitemHandler() {
        return new GetExistingPastebinWorkitemHandler(workitemProperties.getPastebinDevelKey());
    }

    // PDF Workitems
    @Bean
    public GeneratePDFWorkitemHandler generatePDFWorkitemHandler() {
        return new GeneratePDFWorkitemHandler();
    }

    // Riot Workitems
    @Bean
    @ConditionalOnProperty("workitems.riotApiKey")
    public LastSummonerMatchWorkitemHandler lastSummonerMatchWorkitemHandler() {
        return new LastSummonerMatchWorkitemHandler(workitemProperties.getRiotApiKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.riotApiKey")
    public MatchesInfoWorkitemHandler matchesInfoWorkitemHandler() {
        return new MatchesInfoWorkitemHandler(workitemProperties.getRiotApiKey());
    }

    @Bean
    @ConditionalOnProperty("workitems.riotApiKey")
    public SummonerInfoWorkitemHandler summonerInfoWorkitemHandler() {
        return new SummonerInfoWorkitemHandler(workitemProperties.getRiotApiKey());
    }

    // RSS Workitems
    @Bean
    public RSSWorkItemHandler rssWorkItemHandler() {
        return new RSSWorkItemHandler();
    }

    // Slack Workitems
    @Bean
    @ConditionalOnProperty("workitems.slackAccessToken")
    public AddReminderWorkitemHandler addReminderWorkitemHandler() {
        return new AddReminderWorkitemHandler(workitemProperties.getSlackAccessToken());
    }

    @Bean
    @ConditionalOnProperty("workitems.slackAccessToken")
    public PostMessageToChannelWorkitemHandler postMessageToChannelWorkitemHandler() {
        return new PostMessageToChannelWorkitemHandler(workitemProperties.getSlackAccessToken());
    }

    // Transform Workitems
    @Bean
    public TransformWorkItemHandler transformWorkItemHandler() {
        return new TransformWorkItemHandler();
    }

    // Twitter Workitems
    @Bean
    @ConditionalOnProperty({"workitems.twitterConsumerKey", "workitems.twitterConsumerSecret", "workitems.twitterAccessKey", "workitems.twitterAccessSecret"})
    public SendDirectMessageWorkitemHandler sendDirectMessageWorkitemHandler() {
        return new SendDirectMessageWorkitemHandler(workitemProperties.getTwitterConsumerKey(),
                                                    workitemProperties.getTwitterConsumerSecret(),
                                                    workitemProperties.getTwitterAccessKey(),
                                                    workitemProperties.getTwitterAccessSecret());
    }

    @Bean
    @ConditionalOnProperty({"workitems.twitterConsumerKey", "workitems.twitterConsumerSecret", "workitems.twitterAccessKey", "workitems.twitterAccessSecret"})
    public UpdateStatusWorkitemHandler updateStatusWorkitemHandler() {
        return new UpdateStatusWorkitemHandler(workitemProperties.getTwitterConsumerKey(),
                                               workitemProperties.getTwitterConsumerSecret(),
                                               workitemProperties.getTwitterAccessKey(),
                                               workitemProperties.getTwitterAccessSecret());
    }

    // Vimeo Workitems
    @Bean
    @ConditionalOnProperty("workitems.vimeoAccessToken")
    public DeleteVideoWorkitemHandler deleteVideoWorkitemHandler() {
        return new DeleteVideoWorkitemHandler(workitemProperties.getVimeoAccessToken());
    }

    @Bean
    @ConditionalOnProperty("workitems.vimeoAccessToken")
    public GetVideoInfoWorkitemHandler getVideoInfoWorkitemHandler() {
        return new GetVideoInfoWorkitemHandler(workitemProperties.getVimeoAccessToken());
    }

    @Bean
    @ConditionalOnProperty("workitems.vimeoAccessToken")
    public UpdateVideoMetadataWorkitemHandler updateVideoMetadataWorkitemHandler() {
        return new UpdateVideoMetadataWorkitemHandler(workitemProperties.getVimeoAccessToken());
    }

    @Bean
    @ConditionalOnProperty("workitems.vimeoAccessToken")
    public UploadVideoWorkitemHandler uploadVideoWorkitemHandler() {
        return new UploadVideoWorkitemHandler(workitemProperties.getVimeoAccessToken());
    }
}
