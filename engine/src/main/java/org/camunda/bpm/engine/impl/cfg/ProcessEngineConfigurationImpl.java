/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.cfg;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.scriptengine.DmnScriptEngineFactory;
import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.DefaultArtifactFactory;
import org.camunda.bpm.engine.impl.FilterServiceImpl;
import org.camunda.bpm.engine.impl.FormServiceImpl;
import org.camunda.bpm.engine.impl.HistoryServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.calendar.BusinessCalendarManager;
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.DueDateBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.DurationBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.MapBusinessCalendarManager;
import org.camunda.bpm.engine.impl.cfg.auth.DefaultAuthorizationProvider;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.cfg.standalone.StandaloneTransactionContextFactory;
import org.camunda.bpm.engine.impl.cmmn.CaseServiceImpl;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionManager;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartManager;
import org.camunda.bpm.engine.impl.cmmn.handler.DefaultCmmnElementHandlerRegistry;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformFactory;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformListener;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformer;
import org.camunda.bpm.engine.impl.cmmn.transformer.DefaultCmmnTransformFactory;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManagerFactory;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityCacheKeyMapping;
import org.camunda.bpm.engine.impl.db.sql.DbSqlPersistenceProviderFactory;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.delegate.DefaultDelegateInterceptor;
import org.camunda.bpm.engine.impl.digest.PasswordEncryptor;
import org.camunda.bpm.engine.impl.digest.ShaHashDigest;
import org.camunda.bpm.engine.impl.dmn.configuration.ProcessEngineDmnEngineConfiguration;
import org.camunda.bpm.engine.impl.dmn.deployer.DmnDeployer;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.el.CommandContextFunctionMapper;
import org.camunda.bpm.engine.impl.el.DateTimeFunctionMapper;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.event.CompensationEventHandler;
import org.camunda.bpm.engine.impl.event.EventHandler;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.event.SignalEventHandler;
import org.camunda.bpm.engine.impl.form.engine.FormEngine;
import org.camunda.bpm.engine.impl.form.engine.HtmlFormEngine;
import org.camunda.bpm.engine.impl.form.engine.JuelFormEngine;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.impl.form.type.BooleanFormType;
import org.camunda.bpm.engine.impl.form.type.DateFormType;
import org.camunda.bpm.engine.impl.form.type.FormTypes;
import org.camunda.bpm.engine.impl.form.type.LongFormType;
import org.camunda.bpm.engine.impl.form.type.StringFormType;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator;
import org.camunda.bpm.engine.impl.form.validator.FormValidators;
import org.camunda.bpm.engine.impl.form.validator.MaxLengthValidator;
import org.camunda.bpm.engine.impl.form.validator.MaxValidator;
import org.camunda.bpm.engine.impl.form.validator.MinLengthValidator;
import org.camunda.bpm.engine.impl.form.validator.MinValidator;
import org.camunda.bpm.engine.impl.form.validator.ReadOnlyValidator;
import org.camunda.bpm.engine.impl.form.validator.RequiredValidator;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.parser.HistoryParseListener;
import org.camunda.bpm.engine.impl.history.producer.CacheAwareCmmnHistoryEventProducer;
import org.camunda.bpm.engine.impl.history.producer.CacheAwareHistoryEventProducer;
import org.camunda.bpm.engine.impl.history.producer.CmmnHistoryEventProducer;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.history.transformer.CmmnHistoryTransformListener;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;
import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutorImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.DelegateInterceptor;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.CallerRunsRejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobPriorityProvider;
import org.camunda.bpm.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.RejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateJobDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendJobDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.metrics.MetricsReporterIdProvider;
import org.camunda.bpm.engine.impl.metrics.SimpleIpBasedProvider;
import org.camunda.bpm.engine.impl.metrics.parser.MetricsBpmnParseListener;
import org.camunda.bpm.engine.impl.metrics.parser.MetricsCmmnTransformListener;
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter;
import org.camunda.bpm.engine.impl.persistence.GenericManagerFactory;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentManager;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.CommentManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentManager;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.FilterManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseActivityInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricStatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkManager;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MeterLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.persistence.entity.StatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.TableDataManager;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceManager;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.DefaultCorrelationHandler;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.BeansResolverFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptBindingsFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.scripting.engine.VariableScopeResolverFactory;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.variable.ValueTypeResolverImpl;
import org.camunda.bpm.engine.impl.variable.serializer.BooleanValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ByteArrayValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.DateValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.DefaultVariableSerializers;
import org.camunda.bpm.engine.impl.variable.serializer.DoubleValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.FileValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.IntegerValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.LongValueSerlializer;
import org.camunda.bpm.engine.impl.variable.serializer.NullValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ShortValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.StringValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSession;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.EntityManagerSessionFactory;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.JPAVariableSerializer;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;


/**
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfigurationImpl extends ProcessEngineConfiguration {

  private static Logger log = Logger.getLogger(ProcessEngineConfigurationImpl.class.getName());

  public static final String DB_SCHEMA_UPDATE_CREATE = "create";
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";

  public static final int HISTORYLEVEL_NONE = HistoryLevel.HISTORY_LEVEL_NONE.getId();
  public static final int HISTORYLEVEL_ACTIVITY = HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId();
  public static final int HISTORYLEVEL_AUDIT = HistoryLevel.HISTORY_LEVEL_AUDIT.getId();
  public static final int HISTORYLEVEL_FULL = HistoryLevel.HISTORY_LEVEL_FULL.getId();

  public static final String DEFAULT_WS_SYNC_FACTORY = "org.camunda.bpm.engine.impl.webservice.CxfWebServiceClientFactory";

  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/camunda/bpm/engine/impl/mapping/mappings.xml";

  // SERVICES /////////////////////////////////////////////////////////////////

  protected RepositoryService repositoryService = new RepositoryServiceImpl();
  protected RuntimeService runtimeService = new RuntimeServiceImpl();
  protected HistoryService historyService = new HistoryServiceImpl();
  protected IdentityService identityService = new IdentityServiceImpl();
  protected TaskService taskService = new TaskServiceImpl();
  protected FormService formService = new FormServiceImpl();
  protected ManagementService managementService = new ManagementServiceImpl();
  protected AuthorizationService authorizationService = new AuthorizationServiceImpl();
  protected CaseService caseService = new CaseServiceImpl();
  protected FilterService filterService = new FilterServiceImpl();

  // COMMAND EXECUTORS ////////////////////////////////////////////////////////

  // Command executor and interceptor stack
  /** the configurable list which will be {@link #initInterceptorChain(java.util.List) processed} to build the {@link #commandExecutorTxRequired} */
  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequired;
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequired;

  protected List<CommandInterceptor> commandInterceptorsTxRequired;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequired;

  /** the configurable list which will be {@link #initInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequiresNew} */
  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew;
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew;

  protected List<CommandInterceptor> commandInterceptorsTxRequiresNew;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequiresNew;

  /** Separate command executor to be used for db schema operations. Must always use NON-JTA transactions */
  protected CommandExecutor commandExecutorSchemaOperations;

  // SESSION FACTORIES ////////////////////////////////////////////////////////

  protected List<SessionFactory> customSessionFactories;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;

  // DEPLOYERS ////////////////////////////////////////////////////////////////

  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;
  protected List<Deployer> deployers;
  protected DeploymentCache deploymentCache;

  // JOB EXECUTOR /////////////////////////////////////////////////////////////

  protected List<JobHandler> customJobHandlers;
  protected Map<String, JobHandler> jobHandlers;
  protected JobExecutor jobExecutor;

  protected JobPriorityProvider jobPriorityProvider;

  // MYBATIS SQL SESSION FACTORY //////////////////////////////////////////////

  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;


  // ID GENERATOR /////////////////////////////////////////////////////////////
  protected IdGenerator idGenerator;
  protected DataSource idGeneratorDataSource;
  protected String idGeneratorDataSourceJndiName;

  // INCIDENT HANDLER /////////////////////////////////////////////////////////

  protected Map<String, IncidentHandler> incidentHandlers;
  protected List<IncidentHandler> customIncidentHandlers;


  // OTHER ////////////////////////////////////////////////////////////////////
  protected List<FormEngine> customFormEngines;
  protected Map<String, FormEngine> formEngines;

  protected List<AbstractFormFieldType> customFormTypes;
  protected FormTypes formTypes;
  protected FormValidators formValidators;
  protected Map<String, Class<? extends FormFieldValidator>> customFormFieldValidators;

  protected List<TypedValueSerializer> customPreVariableSerializers;
  protected List<TypedValueSerializer> customPostVariableSerializers;
  protected VariableSerializers variableSerializers;
  protected String defaultSerializationFormat = Variables.SerializationDataFormats.JAVA.getName();
  protected String defaultCharsetName = null;
  protected Charset defaultCharset = null;

  protected ExpressionManager expressionManager;
  protected List<String> customScriptingEngineClasses;
  protected ScriptingEngines scriptingEngines;
  protected List<ResolverFactory> resolverFactories;
  protected ScriptingEnvironment scriptingEnvironment;
  protected List<ScriptEnvResolver> scriptEnvResolvers;
  protected ScriptFactory scriptFactory;
  protected boolean autoStoreScriptVariables = false;
  protected boolean enableScriptCompilation = true;
  protected boolean cmmnEnabled = true;
  protected boolean dmnEnabled = true;

  protected boolean enableGracefulDegradationOnContextSwitchFailure = true;

  protected BusinessCalendarManager businessCalendarManager;

  protected String wsSyncFactoryClassName = DEFAULT_WS_SYNC_FACTORY;

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;
  protected BpmnParseFactory bpmnParseFactory;

  // cmmn
  protected CmmnTransformFactory cmmnTransformFactory;
  protected DefaultCmmnElementHandlerRegistry cmmnElementHandlerRegistry;

  // dmn
  protected DmnEngineConfiguration dmnEngineConfiguration;
  protected DmnEngine dmnEngine;

  protected HistoryLevel historyLevel;

  /** a list of supported history levels */
  protected List<HistoryLevel> historyLevels;

  /** a list of supported custom history levels */
  protected List<HistoryLevel> customHistoryLevels;

  protected List<BpmnParseListener> preParseListeners;
  protected List<BpmnParseListener> postParseListeners;

  protected List<CmmnTransformListener> customPreCmmnTransformListeners;
  protected List<CmmnTransformListener> customPostCmmnTransformListeners;

  protected Map<Object, Object> beans;

  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;

  protected DelegateInterceptor delegateInterceptor;

  protected CommandInterceptor actualCommandExecutor;

  protected RejectedJobsHandler customRejectedJobsHandler;

  protected Map<String, EventHandler> eventHandlers;
  protected List<EventHandler> customEventHandlers;

  protected FailedJobCommandFactory failedJobCommandFactory;

  protected String databaseTablePrefix = "";

  /**
   * In some situations you want to set the schema to use for table checks / generation if the database metadata
   * doesn't return that correctly, see https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema = null;

  protected boolean isCreateDiagramOnDeploy = false;

  protected ProcessApplicationManager processApplicationManager;

  protected CorrelationHandler correlationHandler;

  /** session factory to be used for obtaining identity provider sessions */
  protected SessionFactory identityProviderSessionFactory;

  protected PasswordEncryptor passwordEncryptor;

  protected Set<String> registeredDeployments;

  protected ResourceAuthorizationProvider resourceAuthorizationProvider;

  protected List<ProcessEnginePlugin> processEnginePlugins = new ArrayList<ProcessEnginePlugin>();

  protected HistoryEventProducer historyEventProducer;

  protected CmmnHistoryEventProducer cmmnHistoryEventProducer;

  protected HistoryEventHandler historyEventHandler;

  protected boolean isExecutionTreePrefetchEnabled = true;

  /** If true the process engine will attempt to acquire an exclusive lock before
   * creating a deployment.
   */
  protected boolean isDeploymentLockUsed = true;

  /** Allows setting whether the process engine should try reusing the first level entity cache.
   * Default setting is false, enabling it improves performance of asynchronous continuations.
   */
  protected boolean isDbEntityCacheReuseEnabled = false;

  protected boolean isInvokeCustomVariableListeners = true;

  /**
   * The process engine created by this configuration.
   */
  protected ProcessEngineImpl processEngine;

  /** used to create instances for listeners, JavaDelegates, etc */
  protected ArtifactFactory artifactFactory;

  protected DbEntityCacheKeyMapping dbEntityCacheKeyMapping = DbEntityCacheKeyMapping.defaultEntityCacheKeyMapping();

  /** the metrics registry */
  protected MetricsRegistry metricsRegistry;

  protected DbMetricsReporter dbMetricsReporter;

  protected boolean isMetricsEnabled = true;
  protected boolean isDbMetricsReporterActivate = true;

  protected MetricsReporterIdProvider metricsReporterIdProvider;

  protected boolean isBpmnStacktraceVerbose = false;

  // buildProcessEngine ///////////////////////////////////////////////////////

  @Override
  public ProcessEngine buildProcessEngine() {
    init();
    processEngine = new ProcessEngineImpl(this);
    invokePostProcessEngineBuild(processEngine);
    return processEngine;
  }

  // init /////////////////////////////////////////////////////////////////////

  protected void init() {
    invokePreInit();
    initDefaultCharset();
    initHistoryLevel();
    initHistoryEventProducer();
    initCmmnHistoryEventProducer();
    initHistoryEventHandler();
    initExpressionManager();
    initBeans();
    initArtifactFactory();
    initFormEngines();
    initFormTypes();
    initFormFieldValidators();
    initScripting();
    initDmnEngine();
    initBusinessCalendarManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initServices();
    initIdGenerator();
    initDeployers();
    initJobProvider();
    initJobExecutor();
    initDataSource();
    initTransactionFactory();
    initSqlSessionFactory();
    initIdentityProviderSessionFactory();
    initSessionFactories();
    initValueTypeResolver();
    initSerialization();
    initJpa();
    initDelegateInterceptor();
    initEventHandlers();
    initFailedJobCommandFactory();
    initProcessApplicationManager();
    initCorrelationHandler();
    initIncidentHandlers();
    initPasswordDigest();
    initDeploymentRegistration();
    initResourceAuthorizationProvider();
    initMetrics();

    invokePostInit();
  }

  protected void invokePreInit() {
    for (ProcessEnginePlugin plugin : processEnginePlugins) {

      log.log(Level.INFO, "PLUGIN {0} activated on process engine {1}",
          new String[]{plugin.getClass().getSimpleName(),
          getProcessEngineName()});

      plugin.preInit(this);
    }
  }

  protected void invokePostInit() {
    for (ProcessEnginePlugin plugin : processEnginePlugins) {
      plugin.postInit(this);
    }
  }

  protected void invokePostProcessEngineBuild(ProcessEngine engine) {
    for (ProcessEnginePlugin plugin : processEnginePlugins) {
      plugin.postProcessEngineBuild(engine);
    }
  }


  // failedJobCommandFactory ////////////////////////////////////////////////////////

  protected void initFailedJobCommandFactory() {
    if (failedJobCommandFactory == null) {
      failedJobCommandFactory = new DefaultFailedJobCommandFactory();
    }
  }

  // incident handlers /////////////////////////////////////////////////////////////

  protected void initIncidentHandlers() {
    if (incidentHandlers == null) {
      incidentHandlers = new HashMap<String, IncidentHandler>();

      FailedJobIncidentHandler failedJobIncidentHandler = new FailedJobIncidentHandler();
      incidentHandlers.put(failedJobIncidentHandler.getIncidentHandlerType(), failedJobIncidentHandler);
    }
    if(customIncidentHandlers != null) {
      for (IncidentHandler incidentHandler : customIncidentHandlers) {
        incidentHandlers.put(incidentHandler.getIncidentHandlerType(), incidentHandler);
      }
    }
  }

  // command executors ////////////////////////////////////////////////////////

  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired();
  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew();

  protected void initCommandExecutors() {
    initActualCommandExecutor();
    initCommandInterceptorsTxRequired();
    initCommandExecutorTxRequired();
    initCommandInterceptorsTxRequiresNew();
    initCommandExecutorTxRequiresNew();
    initCommandExecutorDbSchemaOperations();
  }

  protected void initActualCommandExecutor() {
    actualCommandExecutor = new CommandExecutorImpl();
  }

  protected void initCommandInterceptorsTxRequired() {
    if (commandInterceptorsTxRequired==null) {
      if (customPreCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequired);
      } else {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequired.addAll(getDefaultCommandInterceptorsTxRequired());
      if (customPostCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired.addAll(customPostCommandInterceptorsTxRequired);
      }
      commandInterceptorsTxRequired.add(actualCommandExecutor);
    }
  }

  protected void initCommandInterceptorsTxRequiresNew() {
    if (commandInterceptorsTxRequiresNew==null) {
      if (customPreCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequiresNew);
      } else {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequiresNew.addAll(getDefaultCommandInterceptorsTxRequiresNew());
      if (customPostCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew.addAll(customPostCommandInterceptorsTxRequiresNew);
      }
      commandInterceptorsTxRequiresNew.add(actualCommandExecutor);
    }
  }

  protected void initCommandExecutorTxRequired() {
    if (commandExecutorTxRequired==null) {
      commandExecutorTxRequired = initInterceptorChain(commandInterceptorsTxRequired);
    }
  }

  protected void initCommandExecutorTxRequiresNew() {
    if (commandExecutorTxRequiresNew==null) {
      commandExecutorTxRequiresNew = initInterceptorChain(commandInterceptorsTxRequiresNew);
    }
  }

  protected void initCommandExecutorDbSchemaOperations() {
    if (commandExecutorSchemaOperations==null) {
      // in default case, we use the same command executor for DB Schema Operations as for runtime operations.
      // configurations that Use JTA Transactions should override this method and provide a custom command executor
      // that uses NON-JTA Transactions.
      commandExecutorSchemaOperations = commandExecutorTxRequired;
    }
  }

  protected CommandInterceptor initInterceptorChain(List<CommandInterceptor> chain) {
    if (chain==null || chain.isEmpty()) {
      throw new ProcessEngineException("invalid command interceptor chain configuration: "+chain);
    }
    for (int i = 0; i < chain.size()-1; i++) {
      chain.get(i).setNext( chain.get(i+1) );
    }
    return chain.get(0);
  }

  // services /////////////////////////////////////////////////////////////////

  protected void initServices() {
    initService(repositoryService);
    initService(runtimeService);
    initService(historyService);
    initService(identityService);
    initService(taskService);
    initService(formService);
    initService(managementService);
    initService(authorizationService);
    initService(caseService);
    initService(filterService);
  }

  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl)service).setCommandExecutor(commandExecutorTxRequired);
    }
  }

  // DataSource ///////////////////////////////////////////////////////////////

  protected void initDataSource() {
    if (dataSource==null) {
      if (dataSourceJndiName!=null) {
        try {
          dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
        } catch (Exception e) {
          throw new ProcessEngineException("couldn't lookup datasource from "+dataSourceJndiName+": "+e.getMessage(), e);
        }

      } else if (jdbcUrl!=null) {
        if ( (jdbcDriver==null) || (jdbcUrl==null) || (jdbcUsername==null) ) {
          throw new ProcessEngineException("DataSource or JDBC properties have to be specified in a process engine configuration");
        }

        log.fine("initializing datasource to db: "+jdbcUrl);

        PooledDataSource pooledDataSource =
          new PooledDataSource(ReflectUtil.getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword );

        if (jdbcMaxActiveConnections > 0) {
          pooledDataSource.setPoolMaximumActiveConnections(jdbcMaxActiveConnections);
        }
        if (jdbcMaxIdleConnections > 0) {
          pooledDataSource.setPoolMaximumIdleConnections(jdbcMaxIdleConnections);
        }
        if (jdbcMaxCheckoutTime > 0) {
          pooledDataSource.setPoolMaximumCheckoutTime(jdbcMaxCheckoutTime);
        }
        if (jdbcMaxWaitTime > 0) {
          pooledDataSource.setPoolTimeToWait(jdbcMaxWaitTime);
        }
        if (jdbcPingEnabled == true) {
          pooledDataSource.setPoolPingEnabled(true);
          if (jdbcPingQuery != null) {
            pooledDataSource.setPoolPingQuery(jdbcPingQuery);
          }
          pooledDataSource.setPoolPingConnectionsNotUsedFor(jdbcPingConnectionNotUsedFor);
        }
        dataSource = pooledDataSource;
      }

      if (dataSource instanceof PooledDataSource) {
        // ACT-233: connection pool of Ibatis is not properely initialized if this is not called!
        ((PooledDataSource)dataSource).forceCloseAll();
      }
    }

    if (databaseType == null) {
      initDatabaseType();
    }
  }

  protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

  protected static Properties getDefaultDatabaseTypeMappings() {
    Properties databaseTypeMappings = new Properties();
    databaseTypeMappings.setProperty("H2","h2");
    databaseTypeMappings.setProperty("MySQL","mysql");
    databaseTypeMappings.setProperty("Oracle","oracle");
    databaseTypeMappings.setProperty("PostgreSQL","postgres");
    databaseTypeMappings.setProperty("Microsoft SQL Server","mssql");
    databaseTypeMappings.setProperty("DB2","db2");
    databaseTypeMappings.setProperty("DB2","db2");
    databaseTypeMappings.setProperty("DB2/NT","db2");
    databaseTypeMappings.setProperty("DB2/NT64","db2");
    databaseTypeMappings.setProperty("DB2 UDP","db2");
    databaseTypeMappings.setProperty("DB2/LINUX","db2");
    databaseTypeMappings.setProperty("DB2/LINUX390","db2");
    databaseTypeMappings.setProperty("DB2/LINUXX8664","db2");
    databaseTypeMappings.setProperty("DB2/LINUXZ64","db2");
    databaseTypeMappings.setProperty("DB2/400 SQL","db2");
    databaseTypeMappings.setProperty("DB2/6000","db2");
    databaseTypeMappings.setProperty("DB2 UDB iSeries","db2");
    databaseTypeMappings.setProperty("DB2/AIX64","db2");
    databaseTypeMappings.setProperty("DB2/HPUX","db2");
    databaseTypeMappings.setProperty("DB2/HP64","db2");
    databaseTypeMappings.setProperty("DB2/SUN","db2");
    databaseTypeMappings.setProperty("DB2/SUN64","db2");
    databaseTypeMappings.setProperty("DB2/PTX","db2");
    databaseTypeMappings.setProperty("DB2/2","db2");
    return databaseTypeMappings;
  }

  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      log.fine("database product name: '" + databaseProductName + "'");
      databaseType = databaseTypeMappings.getProperty(databaseProductName);
      ensureNotNull("couldn't deduct database type from database product name '" + databaseProductName + "'", "databaseType", databaseType);
      log.fine("using database type: " + databaseType);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection!=null) {
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  // myBatis SqlSessionFactory ////////////////////////////////////////////////

  protected void initTransactionFactory() {
    if (transactionFactory==null) {
      if (transactionsExternallyManaged) {
        transactionFactory = new ManagedTransactionFactory();
      } else {
        transactionFactory = new JdbcTransactionFactory();
      }
    }
  }

  protected void initSqlSessionFactory() {
    if (sqlSessionFactory==null) {
      InputStream inputStream = null;
      try {
        inputStream = getMyBatisXmlConfigurationSteam();

        // update the jdbc parameters to the configured ones...
        Environment environment = new Environment("default", transactionFactory, dataSource);
        Reader reader = new InputStreamReader(inputStream);
        Properties properties = new Properties();
        properties.put("prefix", databaseTablePrefix);
        if(databaseType != null) {
          properties.put("limitBefore" , DbSqlSessionFactory.databaseSpecificLimitBeforeStatements.get(databaseType));
          properties.put("limitAfter" , DbSqlSessionFactory.databaseSpecificLimitAfterStatements.get(databaseType));
          properties.put("innerLimitAfter" , DbSqlSessionFactory.databaseSpecificInnerLimitAfterStatements.get(databaseType));
          properties.put("limitBetween" , DbSqlSessionFactory.databaseSpecificLimitBetweenStatements.get(databaseType));
          properties.put("limitBetweenClob" , DbSqlSessionFactory.databaseSpecificLimitBetweenClobStatements.get(databaseType));
          properties.put("orderBy" , DbSqlSessionFactory.databaseSpecificOrderByStatements.get(databaseType));
          properties.put("limitBeforeNativeQuery" , DbSqlSessionFactory.databaseSpecificLimitBeforeNativeQueryStatements.get(databaseType));

          properties.put("bitand1" , DbSqlSessionFactory.databaseSpecificBitAnd1.get(databaseType));
          properties.put("bitand2" , DbSqlSessionFactory.databaseSpecificBitAnd2.get(databaseType));
          properties.put("bitand3" , DbSqlSessionFactory.databaseSpecificBitAnd3.get(databaseType));

          properties.put("trueConstant", DbSqlSessionFactory.databaseSpecificTrueConstant.get(databaseType));
          properties.put("falseConstant", DbSqlSessionFactory.databaseSpecificFalseConstant.get(databaseType));

          properties.put("dbSpecificDummyTable" , DbSqlSessionFactory.databaseSpecificDummyTable.get(databaseType));

          Map<String, String> constants = DbSqlSessionFactory.dbSpecificConstants.get(databaseType);
          for (Entry<String, String> entry : constants.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
          }

        }
        XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
        Configuration configuration = parser.getConfiguration();
        configuration.setEnvironment(environment);
        configuration = parser.parse();

        configuration.setDefaultStatementTimeout(jdbcStatementTimeout);

        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ProcessEngineException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
  }

  protected InputStream getMyBatisXmlConfigurationSteam() {
    return ReflectUtil.getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
  }

  // session factories ////////////////////////////////////////////////////////

  protected void initIdentityProviderSessionFactory() {
    if(identityProviderSessionFactory == null) {
      identityProviderSessionFactory = new GenericManagerFactory(DbIdentityServiceProvider.class);
    }
  }

  protected void initSessionFactories() {
    if (sessionFactories==null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      initPersistenceProviders();

      addSessionFactory(new DbEntityManagerFactory(idGenerator));

      addSessionFactory(new GenericManagerFactory(AttachmentManager.class));
      addSessionFactory(new GenericManagerFactory(CommentManager.class));
      addSessionFactory(new GenericManagerFactory(DeploymentManager.class));
      addSessionFactory(new GenericManagerFactory(ExecutionManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricActivityInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricCaseActivityInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricStatisticsManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricDetailManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricProcessInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricCaseInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(UserOperationLogManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricTaskInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricVariableInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricIncidentManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricJobLogManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityInfoManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityLinkManager.class));
      addSessionFactory(new GenericManagerFactory(JobManager.class));
      addSessionFactory(new GenericManagerFactory(JobDefinitionManager.class));
      addSessionFactory(new GenericManagerFactory(ProcessDefinitionManager.class));
      addSessionFactory(new GenericManagerFactory(PropertyManager.class));
      addSessionFactory(new GenericManagerFactory(ResourceManager.class));
      addSessionFactory(new GenericManagerFactory(ByteArrayManager.class));
      addSessionFactory(new GenericManagerFactory(TableDataManager.class));
      addSessionFactory(new GenericManagerFactory(TaskManager.class));
      addSessionFactory(new GenericManagerFactory(VariableInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(EventSubscriptionManager.class));
      addSessionFactory(new GenericManagerFactory(StatisticsManager.class));
      addSessionFactory(new GenericManagerFactory(IncidentManager.class));
      addSessionFactory(new GenericManagerFactory(AuthorizationManager.class));
      addSessionFactory(new GenericManagerFactory(FilterManager.class));
      addSessionFactory(new GenericManagerFactory(MeterLogManager.class));

      addSessionFactory(new GenericManagerFactory(CaseDefinitionManager.class));
      addSessionFactory(new GenericManagerFactory(CaseExecutionManager.class));
      addSessionFactory(new GenericManagerFactory(CaseSentryPartManager.class));

      addSessionFactory(new GenericManagerFactory(DecisionDefinitionManager.class));

      sessionFactories.put(ReadOnlyIdentityProvider.class, identityProviderSessionFactory);

      // check whether identityProviderSessionFactory implements WritableIdentityProvider
      Class<?> identityProviderType = identityProviderSessionFactory.getSessionType();
      if(WritableIdentityProvider.class.isAssignableFrom(identityProviderType)) {
        sessionFactories.put(WritableIdentityProvider.class, identityProviderSessionFactory);
      }

    }
    if (customSessionFactories!=null) {
      for (SessionFactory sessionFactory: customSessionFactories) {
        addSessionFactory(sessionFactory);
      }
    }
  }

  protected void initPersistenceProviders() {
    ensurePrefixAndSchemaFitToegether(databaseTablePrefix, databaseSchema);
    dbSqlSessionFactory = new DbSqlSessionFactory();
    dbSqlSessionFactory.setDatabaseType(databaseType);
    dbSqlSessionFactory.setIdGenerator(idGenerator);
    dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
    dbSqlSessionFactory.setDbIdentityUsed(isDbIdentityUsed);
    dbSqlSessionFactory.setDbHistoryUsed(isDbHistoryUsed);
    dbSqlSessionFactory.setCmmnEnabled(cmmnEnabled);
    dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
    dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
    addSessionFactory(dbSqlSessionFactory);
    addSessionFactory(new DbSqlPersistenceProviderFactory());
  }

  /**
   * When providing a schema and a prefix  the prefix has to be the schema ending with a dot.
   */
  protected void ensurePrefixAndSchemaFitToegether(String prefix, String schema) {
    if(schema == null) {
      return;
    } else if(prefix == null || (prefix != null && !prefix.startsWith(schema + "."))){
      throw new ProcessEngineException("When setting a schema the prefix has to be schema + '.'. Received schema: " + schema + " prefix: " + prefix);
    }
  }

  protected void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }

  // deployers ////////////////////////////////////////////////////////////////

  protected void initDeployers() {
    if (this.deployers==null) {
      this.deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        this.deployers.addAll(customPreDeployers);
      }
      this.deployers.addAll(getDefaultDeployers());
      if (customPostDeployers!=null) {
        this.deployers.addAll(customPostDeployers);
      }
    }
    if (deploymentCache==null) {
      List<Deployer> deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        deployers.addAll(customPreDeployers);
      }
      deployers.addAll(getDefaultDeployers());
      if (customPostDeployers!=null) {
        deployers.addAll(customPostDeployers);
      }

      deploymentCache = new DeploymentCache();
      deploymentCache.setDeployers(deployers);
    }
  }

  protected Collection< ? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    BpmnDeployer bpmnDeployer = getBpmnDeployer();
    defaultDeployers.add(bpmnDeployer);

    if (isCmmnEnabled()) {
      CmmnDeployer cmmnDeployer = getCmmnDeployer();
      defaultDeployers.add(cmmnDeployer);
    }

    if (isDmnEnabled()) {
      DmnDeployer dmnDeployer = getDmnDeployer();
      defaultDeployers.add(dmnDeployer);
    }

    return defaultDeployers;
  }

  protected BpmnDeployer getBpmnDeployer() {
    BpmnDeployer bpmnDeployer = new BpmnDeployer();
    bpmnDeployer.setExpressionManager(expressionManager);
    bpmnDeployer.setIdGenerator(idGenerator);

    if (bpmnParseFactory == null) {
      bpmnParseFactory = new DefaultBpmnParseFactory();
    }

    BpmnParser bpmnParser = new BpmnParser(expressionManager, bpmnParseFactory);

    if(preParseListeners != null) {
      bpmnParser.getParseListeners().addAll(preParseListeners);
    }
    bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
    if(postParseListeners != null) {
      bpmnParser.getParseListeners().addAll(postParseListeners);
    }

    bpmnDeployer.setBpmnParser(bpmnParser);

    return bpmnDeployer;
  }

  protected List<BpmnParseListener> getDefaultBPMNParseListeners() {
    List<BpmnParseListener> defaultListeners = new ArrayList<BpmnParseListener>();
    if (!HistoryLevel.HISTORY_LEVEL_NONE.equals(historyLevel)) {
      defaultListeners.add(new HistoryParseListener(historyLevel, historyEventProducer));
    }
    if(isMetricsEnabled) {
      defaultListeners.add(new MetricsBpmnParseListener());
    }
    return defaultListeners;
  }

  protected CmmnDeployer getCmmnDeployer() {
    CmmnDeployer cmmnDeployer = new CmmnDeployer();

    cmmnDeployer.setIdGenerator(idGenerator);

    if (cmmnTransformFactory == null) {
      cmmnTransformFactory = new DefaultCmmnTransformFactory();
    }

    if (cmmnElementHandlerRegistry == null) {
      cmmnElementHandlerRegistry = new DefaultCmmnElementHandlerRegistry();
    }

    CmmnTransformer cmmnTransformer = new CmmnTransformer(expressionManager, cmmnElementHandlerRegistry, cmmnTransformFactory);

    if (customPreCmmnTransformListeners != null) {
      cmmnTransformer.getTransformListeners().addAll(customPreCmmnTransformListeners);
    }
    cmmnTransformer.getTransformListeners().addAll(getDefaultCmmnTransformListeners());
    if (customPostCmmnTransformListeners != null) {
      cmmnTransformer.getTransformListeners().addAll(customPostCmmnTransformListeners);
    }

    cmmnDeployer.setTransformer(cmmnTransformer);

    return cmmnDeployer;
  }

  protected List<CmmnTransformListener> getDefaultCmmnTransformListeners() {
    List<CmmnTransformListener> defaultListener = new ArrayList<CmmnTransformListener>();
    if (!HistoryLevel.HISTORY_LEVEL_NONE.equals(historyLevel)) {
      defaultListener.add(new CmmnHistoryTransformListener(historyLevel, cmmnHistoryEventProducer));
    }
    if(isMetricsEnabled) {
      defaultListener.add(new MetricsCmmnTransformListener());
    }
    return defaultListener;
  }

  protected DmnDeployer getDmnDeployer() {
    DmnDeployer dmnDeployer = new DmnDeployer();
    dmnDeployer.setIdGenerator(idGenerator);
    dmnDeployer.setTransformer(dmnEngineConfiguration.getTransformer());
    return dmnDeployer;
  }

  // job executor /////////////////////////////////////////////////////////////

  protected void initJobExecutor() {
    if (jobExecutor==null) {
      jobExecutor = new DefaultJobExecutor();
    }

    jobHandlers = new HashMap<String, JobHandler>();
    TimerExecuteNestedActivityJobHandler timerExecuteNestedActivityJobHandler = new TimerExecuteNestedActivityJobHandler();
    jobHandlers.put(timerExecuteNestedActivityJobHandler.getType(), timerExecuteNestedActivityJobHandler);

    TimerCatchIntermediateEventJobHandler timerCatchIntermediateEvent = new TimerCatchIntermediateEventJobHandler();
    jobHandlers.put(timerCatchIntermediateEvent.getType(), timerCatchIntermediateEvent);

    TimerStartEventJobHandler timerStartEvent = new TimerStartEventJobHandler();
    jobHandlers.put(timerStartEvent.getType(), timerStartEvent);

    TimerStartEventSubprocessJobHandler timerStartEventSubprocess = new TimerStartEventSubprocessJobHandler();
    jobHandlers.put(timerStartEventSubprocess.getType(), timerStartEventSubprocess);

    AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
    jobHandlers.put(asyncContinuationJobHandler.getType(), asyncContinuationJobHandler);

    ProcessEventJobHandler processEventJobHandler = new ProcessEventJobHandler();
    jobHandlers.put(processEventJobHandler.getType(), processEventJobHandler);

    TimerSuspendProcessDefinitionHandler suspendProcessDefinitionHandler = new TimerSuspendProcessDefinitionHandler();
    jobHandlers.put(suspendProcessDefinitionHandler.getType(), suspendProcessDefinitionHandler);

    TimerActivateProcessDefinitionHandler activateProcessDefinitionHandler = new TimerActivateProcessDefinitionHandler();
    jobHandlers.put(activateProcessDefinitionHandler.getType(), activateProcessDefinitionHandler);

    TimerSuspendJobDefinitionHandler suspendJobDefinitionHandler = new TimerSuspendJobDefinitionHandler();
    jobHandlers.put(suspendJobDefinitionHandler.getType(), suspendJobDefinitionHandler);

    TimerActivateJobDefinitionHandler activateJobDefinitionHandler = new TimerActivateJobDefinitionHandler();
    jobHandlers.put(activateJobDefinitionHandler.getType(), activateJobDefinitionHandler);

    // if we have custom job handlers, register them
    if (getCustomJobHandlers()!=null) {
      for (JobHandler customJobHandler : getCustomJobHandlers()) {
        jobHandlers.put(customJobHandler.getType(), customJobHandler);
      }
    }

    jobExecutor.setAutoActivate(jobExecutorActivate);

    if(jobExecutor.getRejectedJobsHandler() == null) {
      if(customRejectedJobsHandler != null) {
        jobExecutor.setRejectedJobsHandler(customRejectedJobsHandler);
      } else {
        jobExecutor.setRejectedJobsHandler(new CallerRunsRejectedJobsHandler());
      }
    }

  }

  protected void initJobProvider() {
    if (producePrioritizedJobs && jobPriorityProvider == null) {
      jobPriorityProvider = new DefaultJobPriorityProvider();
    }
  }

  // history //////////////////////////////////////////////////////////////////

  public void initHistoryLevel() {
    if(historyLevel == null) {
      if(historyLevels == null) {
        historyLevels = new ArrayList<HistoryLevel>();
        historyLevels.add(HistoryLevel.HISTORY_LEVEL_NONE);
        historyLevels.add(HistoryLevel.HISTORY_LEVEL_ACTIVITY);
        historyLevels.add(HistoryLevel.HISTORY_LEVEL_AUDIT);
        historyLevels.add(HistoryLevel.HISTORY_LEVEL_FULL);
      }

      if(customHistoryLevels != null) {
        historyLevels.addAll(customHistoryLevels);
      }

      if(HISTORY_VARIABLE.equalsIgnoreCase(history)) {
        historyLevel = HistoryLevel.HISTORY_LEVEL_ACTIVITY;
        log.warning("Using deprecated history level 'variable'. " +
            "This history level is deprecated and replaced by 'activity'. " +
            "Consider using 'ACTIVITY' instead.");

      } else {
        for (HistoryLevel historyLevel : historyLevels) {
          if(historyLevel.getName().equalsIgnoreCase(history)) {
            this.historyLevel = historyLevel;
          }
        }
      }


      // do allow null for history level in case of "auto"
      if(historyLevel == null && !ProcessEngineConfiguration.HISTORY_AUTO.equalsIgnoreCase(history)) {
        throw new ProcessEngineException("invalid history level: "+history);
      }
    }
  }

  // id generator /////////////////////////////////////////////////////////////

  protected void initIdGenerator() {
    if (idGenerator==null) {
      CommandExecutor idGeneratorCommandExecutor = null;
      if (idGeneratorDataSource!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(idGeneratorDataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
      } else if (idGeneratorDataSourceJndiName!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSourceJndiName(idGeneratorDataSourceJndiName);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
      } else {
        idGeneratorCommandExecutor = commandExecutorTxRequiresNew;
      }

      DbIdGenerator dbIdGenerator = new DbIdGenerator();
      dbIdGenerator.setIdBlockSize(idBlockSize);
      dbIdGenerator.setCommandExecutor(idGeneratorCommandExecutor);
      idGenerator = dbIdGenerator;
    }
  }

  // OTHER ////////////////////////////////////////////////////////////////////

  protected void initCommandContextFactory() {
    if (commandContextFactory==null) {
      commandContextFactory = new CommandContextFactory();
      commandContextFactory.setProcessEngineConfiguration(this);
    }
  }

  protected void initTransactionContextFactory() {
    if (transactionContextFactory==null) {
      transactionContextFactory = new StandaloneTransactionContextFactory();
    }
  }

  protected void initValueTypeResolver() {
    if(valueTypeResolver == null) {
      valueTypeResolver = new ValueTypeResolverImpl();
    }
  }

  protected void initDefaultCharset() {
    if(defaultCharset == null) {
      if(defaultCharsetName == null) {
        defaultCharsetName = "UTF-8";
      }
      defaultCharset = Charset.forName(defaultCharsetName);
    }
  }

  protected void initMetrics() {
    if(isMetricsEnabled) {

      if (metricsReporterIdProvider == null) {
        metricsReporterIdProvider = new SimpleIpBasedProvider();
      }

      if(metricsRegistry == null) {
        metricsRegistry = new MetricsRegistry();
      }

      initDefaultMetrics(metricsRegistry);

      if(dbMetricsReporter == null) {
        dbMetricsReporter = new DbMetricsReporter(metricsRegistry, commandExecutorTxRequired);
      }
    }
  }

  protected void initDefaultMetrics(MetricsRegistry metricsRegistry) {
    metricsRegistry.createMeter(Metrics.ACTIVTY_INSTANCE_START);

    metricsRegistry.createMeter(Metrics.JOB_ACQUISITION_ATTEMPT);
    metricsRegistry.createMeter(Metrics.JOB_ACQUIRED_SUCCESS);
    metricsRegistry.createMeter(Metrics.JOB_ACQUIRED_FAILURE);
    metricsRegistry.createMeter(Metrics.JOB_SUCCESSFUL);
    metricsRegistry.createMeter(Metrics.JOB_FAILED);
    metricsRegistry.createMeter(Metrics.JOB_LOCKED_EXCLUSIVE);
  }

  protected void initSerialization() {
    if (variableSerializers==null) {
      variableSerializers = new DefaultVariableSerializers();

      if (customPreVariableSerializers!=null) {
        for (TypedValueSerializer<?> customVariableType: customPreVariableSerializers) {
          variableSerializers.addSerializer(customVariableType);
        }
      }

      // register built-in serializers
      variableSerializers.addSerializer(new NullValueSerializer());
      variableSerializers.addSerializer(new StringValueSerializer());
      variableSerializers.addSerializer(new BooleanValueSerializer());
      variableSerializers.addSerializer(new ShortValueSerializer());
      variableSerializers.addSerializer(new IntegerValueSerializer());
      variableSerializers.addSerializer(new LongValueSerlializer());
      variableSerializers.addSerializer(new DateValueSerializer());
      variableSerializers.addSerializer(new DoubleValueSerializer());
      variableSerializers.addSerializer(new ByteArrayValueSerializer());
      variableSerializers.addSerializer(new JavaObjectSerializer());
      variableSerializers.addSerializer(new FileValueSerializer());

      if (customPostVariableSerializers!=null) {
        for (TypedValueSerializer<?> customVariableType: customPostVariableSerializers) {
          variableSerializers.addSerializer(customVariableType);
        }
      }

    }
  }

  protected void initFormEngines() {
    if (formEngines==null) {
      formEngines = new HashMap<String, FormEngine>();
      // html form engine = default form engine
      FormEngine defaultFormEngine = new HtmlFormEngine();
      formEngines.put(null, defaultFormEngine); // default form engine is looked up with null
      formEngines.put(defaultFormEngine.getName(), defaultFormEngine);
      FormEngine juelFormEngine = new JuelFormEngine();
      formEngines.put(juelFormEngine.getName(), juelFormEngine);

    }
    if (customFormEngines!=null) {
      for (FormEngine formEngine: customFormEngines) {
        formEngines.put(formEngine.getName(), formEngine);
      }
    }
  }

  protected void initFormTypes() {
    if (formTypes==null) {
      formTypes = new FormTypes();
      formTypes.addFormType(new StringFormType());
      formTypes.addFormType(new LongFormType());
      formTypes.addFormType(new DateFormType("dd/MM/yyyy"));
      formTypes.addFormType(new BooleanFormType());
    }
    if (customFormTypes!=null) {
      for (AbstractFormFieldType customFormType: customFormTypes) {
        formTypes.addFormType(customFormType);
      }
    }
  }

  protected void initFormFieldValidators() {
    if(formValidators == null) {
      formValidators = new FormValidators();
      formValidators.addValidator("min", MinValidator.class);
      formValidators.addValidator("max", MaxValidator.class);
      formValidators.addValidator("minlength", MinLengthValidator.class);
      formValidators.addValidator("maxlength", MaxLengthValidator.class);
      formValidators.addValidator("required", RequiredValidator.class);
      formValidators.addValidator("readonly", ReadOnlyValidator.class);
    }
    if(customFormFieldValidators != null) {
      for (Entry<String, Class<? extends FormFieldValidator>> validator : customFormFieldValidators.entrySet()) {
        formValidators.addValidator(validator.getKey(), validator.getValue());
      }
    }

  }

  protected void initScripting() {
    if (resolverFactories==null) {
      resolverFactories = new ArrayList<ResolverFactory>();
      resolverFactories.add(new VariableScopeResolverFactory());
      resolverFactories.add(new BeansResolverFactory());
    }
    if (scriptingEngines==null) {
      scriptingEngines = new ScriptingEngines(new ScriptBindingsFactory(resolverFactories));
    }
    if(scriptFactory == null) {
      scriptFactory = new ScriptFactory();
    }
    if(scriptEnvResolvers == null) {
      scriptEnvResolvers = new ArrayList<ScriptEnvResolver>();
    }
    if(scriptingEnvironment == null) {
      scriptingEnvironment = new ScriptingEnvironment(scriptFactory, scriptEnvResolvers, scriptingEngines);
    }
  }

  protected void initDmnEngine() {
    if (dmnEngine == null) {
      if (dmnEngineConfiguration == null) {
        dmnEngineConfiguration = new ProcessEngineDmnEngineConfiguration(scriptingEngines);
      }
      dmnEngine = dmnEngineConfiguration.buildEngine();
    }
    else if (dmnEngineConfiguration == null) {
      dmnEngineConfiguration = dmnEngine.getConfiguration();
    }

    scriptingEngines.addScriptEngineFactory(new DmnScriptEngineFactory(dmnEngine));
  }

  protected void initExpressionManager() {
    if (expressionManager==null) {
      expressionManager = new ExpressionManager(beans);
    }

    // add function mapper for command context (eg currentUser(), currentUserGroups())
    expressionManager.addFunctionMapper(new CommandContextFunctionMapper());
    // add function mapper for date time (eg now(), dateTime())
    expressionManager.addFunctionMapper(new DateTimeFunctionMapper());
  }

  protected void initBusinessCalendarManager() {
    if (businessCalendarManager==null) {
      MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
      mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
      mapBusinessCalendarManager.addBusinessCalendar(DueDateBusinessCalendar.NAME, new DueDateBusinessCalendar());
      mapBusinessCalendarManager.addBusinessCalendar(CycleBusinessCalendar.NAME, new CycleBusinessCalendar());

      businessCalendarManager = mapBusinessCalendarManager;
    }
  }

  protected void initDelegateInterceptor() {
    if(delegateInterceptor == null) {
      delegateInterceptor = new DefaultDelegateInterceptor();
    }
  }

  protected void initEventHandlers() {
    if(eventHandlers == null) {
      eventHandlers = new HashMap<String, EventHandler>();

      SignalEventHandler signalEventHander = new SignalEventHandler();
      eventHandlers.put(signalEventHander.getEventHandlerType(), signalEventHander);

      CompensationEventHandler compensationEventHandler = new CompensationEventHandler();
      eventHandlers.put(compensationEventHandler.getEventHandlerType(), compensationEventHandler);

      MessageEventHandler messageEventHandler = new MessageEventHandler();
      eventHandlers.put(messageEventHandler.getEventHandlerType(), messageEventHandler);

    }
    if(customEventHandlers != null) {
      for (EventHandler eventHandler : customEventHandlers) {
        eventHandlers.put(eventHandler.getEventHandlerType(), eventHandler);
      }
    }
  }

  // JPA //////////////////////////////////////////////////////////////////////

  protected void initJpa() {
    if(jpaPersistenceUnitName!=null) {
      jpaEntityManagerFactory = JpaHelper.createEntityManagerFactory(jpaPersistenceUnitName);
    }
    if(jpaEntityManagerFactory!=null) {
      sessionFactories.put(EntityManagerSession.class, new EntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
      JPAVariableSerializer jpaType = (JPAVariableSerializer) variableSerializers.getSerializerByName(JPAVariableSerializer.NAME);
      // Add JPA-type
      if(jpaType == null) {
        // We try adding the variable right after byte serializer, if available
        int serializableIndex = variableSerializers.getSerializerIndexByName(ValueType.BYTES.getName());
        if(serializableIndex > -1) {
          variableSerializers.addSerializer(new JPAVariableSerializer(), serializableIndex);
        } else {
          variableSerializers.addSerializer(new JPAVariableSerializer());
        }
      }
    }
  }

  protected void initBeans() {
    if (beans == null) {
      beans = new HashMap<Object, Object>();
    }
  }

  protected void initArtifactFactory() {
    if (artifactFactory == null) {
      artifactFactory = new DefaultArtifactFactory();
    }
  }

  protected void initProcessApplicationManager() {
    if(processApplicationManager == null) {
      processApplicationManager = new ProcessApplicationManager();
    }
  }

  // correlation handler //////////////////////////////////////////////////////
  protected void initCorrelationHandler() {
    if (correlationHandler == null) {
      correlationHandler = new DefaultCorrelationHandler();
    }

  }

  // history handlers /////////////////////////////////////////////////////

  protected void initHistoryEventProducer() {
    if(historyEventProducer == null) {
      historyEventProducer = new CacheAwareHistoryEventProducer();
    }
  }

  protected void initCmmnHistoryEventProducer() {
    if(cmmnHistoryEventProducer == null) {
      cmmnHistoryEventProducer = new CacheAwareCmmnHistoryEventProducer();
    }
  }

  protected void initHistoryEventHandler() {
    if(historyEventHandler == null) {
      historyEventHandler = new DbHistoryEventHandler();
    }
  }

  // password digest //////////////////////////////////////////////////////////

  protected void initPasswordDigest() {
    if(passwordEncryptor == null) {
      passwordEncryptor = new ShaHashDigest();
    }
  }


  protected void initDeploymentRegistration() {
    if (registeredDeployments == null) {
      registeredDeployments = new CopyOnWriteArraySet<String>();
    }
  }

  // resource authorization provider //////////////////////////////////////////

  protected void initResourceAuthorizationProvider() {
    if(resourceAuthorizationProvider == null) {
      resourceAuthorizationProvider = new DefaultAuthorizationProvider();
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  @Override
  public String getProcessEngineName() {
    return processEngineName;
  }

  public HistoryLevel getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(HistoryLevel historyLevel) {
    this.historyLevel = historyLevel;
  }

  public HistoryLevel getDefaultHistoryLevel() {
    if (historyLevels != null) {
      for (HistoryLevel historyLevel : historyLevels) {
        if (HISTORY_DEFAULT != null && HISTORY_DEFAULT.equalsIgnoreCase(historyLevel.getName())) {
          return historyLevel;
        }
      }
    }

    return null;
  }

  @Override
  public ProcessEngineConfigurationImpl setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequired() {
    return customPreCommandInterceptorsTxRequired;
  }

  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequired(List<CommandInterceptor> customPreCommandInterceptorsTxRequired) {
    this.customPreCommandInterceptorsTxRequired = customPreCommandInterceptorsTxRequired;
    return this;
  }

  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequired() {
    return customPostCommandInterceptorsTxRequired;
  }

  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequired(List<CommandInterceptor> customPostCommandInterceptorsTxRequired) {
    this.customPostCommandInterceptorsTxRequired = customPostCommandInterceptorsTxRequired;
    return this;
  }

  public List<CommandInterceptor> getCommandInterceptorsTxRequired() {
    return commandInterceptorsTxRequired;
  }

  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequired(List<CommandInterceptor> commandInterceptorsTxRequired) {
    this.commandInterceptorsTxRequired = commandInterceptorsTxRequired;
    return this;
  }

  public CommandExecutor getCommandExecutorTxRequired() {
    return commandExecutorTxRequired;
  }

  public ProcessEngineConfigurationImpl setCommandExecutorTxRequired(CommandExecutor commandExecutorTxRequired) {
    this.commandExecutorTxRequired = commandExecutorTxRequired;
    return this;
  }

  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequiresNew() {
    return customPreCommandInterceptorsTxRequiresNew;
  }

  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew) {
    this.customPreCommandInterceptorsTxRequiresNew = customPreCommandInterceptorsTxRequiresNew;
    return this;
  }

  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequiresNew() {
    return customPostCommandInterceptorsTxRequiresNew;
  }

  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew) {
    this.customPostCommandInterceptorsTxRequiresNew = customPostCommandInterceptorsTxRequiresNew;
    return this;
  }

  public List<CommandInterceptor> getCommandInterceptorsTxRequiresNew() {
    return commandInterceptorsTxRequiresNew;
  }

  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequiresNew(List<CommandInterceptor> commandInterceptorsTxRequiresNew) {
    this.commandInterceptorsTxRequiresNew = commandInterceptorsTxRequiresNew;
    return this;
  }

  public CommandExecutor getCommandExecutorTxRequiresNew() {
    return commandExecutorTxRequiresNew;
  }

  public ProcessEngineConfigurationImpl setCommandExecutorTxRequiresNew(CommandExecutor commandExecutorTxRequiresNew) {
    this.commandExecutorTxRequiresNew = commandExecutorTxRequiresNew;
    return this;
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public ProcessEngineConfigurationImpl setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    return this;
  }

  public RuntimeService getRuntimeService() {
    return runtimeService;
  }

  public ProcessEngineConfigurationImpl setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
    return this;
  }

  public HistoryService getHistoryService() {
    return historyService;
  }

  public ProcessEngineConfigurationImpl setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
    return this;
  }

  public IdentityService getIdentityService() {
    return identityService;
  }

  public ProcessEngineConfigurationImpl setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
    return this;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public ProcessEngineConfigurationImpl setTaskService(TaskService taskService) {
    this.taskService = taskService;
    return this;
  }

  public FormService getFormService() {
    return formService;
  }

  public ProcessEngineConfigurationImpl setFormService(FormService formService) {
    this.formService = formService;
    return this;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public AuthorizationService getAuthorizationService() {
    return authorizationService;
  }

  public ProcessEngineConfigurationImpl setManagementService(ManagementService managementService) {
    this.managementService = managementService;
    return this;
  }

  public CaseService getCaseService() {
    return caseService;
  }

  public void setCaseService(CaseService caseService) {
    this.caseService = caseService;
  }

  public FilterService getFilterService() {
    return filterService;
  }

  public void setFilterService(FilterService filterService) {
    this.filterService = filterService;
  }

  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public ProcessEngineConfigurationImpl setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }

  public List<Deployer> getDeployers() {
    return deployers;
  }

  public ProcessEngineConfigurationImpl setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
    return this;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public ProcessEngineConfigurationImpl setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
    return this;
  }

  public JobPriorityProvider getJobPriorityProvider() {
    return jobPriorityProvider;
  }

  public void setJobPriorityProvider(JobPriorityProvider jobPriorityProvider) {
    this.jobPriorityProvider = jobPriorityProvider;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public ProcessEngineConfigurationImpl setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }

  public String getWsSyncFactoryClassName() {
    return wsSyncFactoryClassName;
  }

  public ProcessEngineConfigurationImpl setWsSyncFactoryClassName(String wsSyncFactoryClassName) {
    this.wsSyncFactoryClassName = wsSyncFactoryClassName;
    return this;
  }

  public Map<String, FormEngine> getFormEngines() {
    return formEngines;
  }

  public ProcessEngineConfigurationImpl setFormEngines(Map<String, FormEngine> formEngines) {
    this.formEngines = formEngines;
    return this;
  }

  public FormTypes getFormTypes() {
    return formTypes;
  }

  public ProcessEngineConfigurationImpl setFormTypes(FormTypes formTypes) {
    this.formTypes = formTypes;
    return this;
  }

  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }

  public ProcessEngineConfigurationImpl setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
    return this;
  }

  public VariableSerializers getVariableSerializers() {
    return variableSerializers;
  }

  public ProcessEngineConfigurationImpl setVariableTypes(VariableSerializers variableSerializers) {
    this.variableSerializers = variableSerializers;
    return this;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public ProcessEngineConfigurationImpl setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
    return this;
  }

  public BusinessCalendarManager getBusinessCalendarManager() {
    return businessCalendarManager;
  }

  public ProcessEngineConfigurationImpl setBusinessCalendarManager(BusinessCalendarManager businessCalendarManager) {
    this.businessCalendarManager = businessCalendarManager;
    return this;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public ProcessEngineConfigurationImpl setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
    return this;
  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }

  public ProcessEngineConfigurationImpl setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
    return this;
  }


  public List<Deployer> getCustomPreDeployers() {
    return customPreDeployers;
  }


  public ProcessEngineConfigurationImpl setCustomPreDeployers(List<Deployer> customPreDeployers) {
    this.customPreDeployers = customPreDeployers;
    return this;
  }


  public List<Deployer> getCustomPostDeployers() {
    return customPostDeployers;
  }


  public ProcessEngineConfigurationImpl setCustomPostDeployers(List<Deployer> customPostDeployers) {
    this.customPostDeployers = customPostDeployers;
    return this;
  }


  public Map<String, JobHandler> getJobHandlers() {
    return jobHandlers;
  }


  public ProcessEngineConfigurationImpl setJobHandlers(Map<String, JobHandler> jobHandlers) {
    this.jobHandlers = jobHandlers;
    return this;
  }


  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }


  public ProcessEngineConfigurationImpl setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    return this;
  }


  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  public ProcessEngineConfigurationImpl setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    return this;
  }

  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  public ProcessEngineConfigurationImpl setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
    return this;
  }

  public List<SessionFactory> getCustomSessionFactories() {
    return customSessionFactories;
  }

  public ProcessEngineConfigurationImpl setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
    this.customSessionFactories = customSessionFactories;
    return this;
  }

  public List<JobHandler> getCustomJobHandlers() {
    return customJobHandlers;
  }

  public ProcessEngineConfigurationImpl setCustomJobHandlers(List<JobHandler> customJobHandlers) {
    this.customJobHandlers = customJobHandlers;
    return this;
  }

  public List<FormEngine> getCustomFormEngines() {
    return customFormEngines;
  }

  public ProcessEngineConfigurationImpl setCustomFormEngines(List<FormEngine> customFormEngines) {
    this.customFormEngines = customFormEngines;
    return this;
  }

  public List<AbstractFormFieldType> getCustomFormTypes() {
    return customFormTypes;
  }


  public ProcessEngineConfigurationImpl setCustomFormTypes(List<AbstractFormFieldType> customFormTypes) {
    this.customFormTypes = customFormTypes;
    return this;
  }


  public List<String> getCustomScriptingEngineClasses() {
    return customScriptingEngineClasses;
  }


  public ProcessEngineConfigurationImpl setCustomScriptingEngineClasses(List<String> customScriptingEngineClasses) {
    this.customScriptingEngineClasses = customScriptingEngineClasses;
    return this;
  }

  public List<TypedValueSerializer> getCustomPreVariableSerializers() {
    return customPreVariableSerializers;
  }


  public ProcessEngineConfigurationImpl setCustomPreVariableSerializers(List<TypedValueSerializer> customPreVariableTypes) {
    this.customPreVariableSerializers = customPreVariableTypes;
    return this;
  }


  public List<TypedValueSerializer> getCustomPostVariableSerializers() {
    return customPostVariableSerializers;
  }


  public ProcessEngineConfigurationImpl setCustomPostVariableSerializers(List<TypedValueSerializer> customPostVariableTypes) {
    this.customPostVariableSerializers = customPostVariableTypes;
    return this;
  }

  public List<BpmnParseListener> getCustomPreBPMNParseListeners() {
    return preParseListeners;
  }

  public void setCustomPreBPMNParseListeners(List<BpmnParseListener> preParseListeners) {
    this.preParseListeners = preParseListeners;
  }

  public List<BpmnParseListener> getCustomPostBPMNParseListeners() {
    return postParseListeners;
  }

  public void setCustomPostBPMNParseListeners(List<BpmnParseListener> postParseListeners) {
    this.postParseListeners = postParseListeners;
  }

  /**
   * @deprecated use {@link #getCustomPreBPMNParseListeners} instead.
   */
  @Deprecated
  public List<BpmnParseListener> getPreParseListeners() {
    return preParseListeners;
  }

  /**
   * @deprecated use {@link #setCustomPreBPMNParseListeners} instead.
   */
  @Deprecated
  public void setPreParseListeners(List<BpmnParseListener> preParseListeners) {
    this.preParseListeners = preParseListeners;
  }

  /**
   * @deprecated use {@link #getCustomPostBPMNParseListeners} instead.
   */
  @Deprecated
  public List<BpmnParseListener> getPostParseListeners() {
    return postParseListeners;
  }

  /**
   * @deprecated use {@link #setCustomPostBPMNParseListeners} instead.
   */
  @Deprecated
  public void setPostParseListeners(List<BpmnParseListener> postParseListeners) {
    this.postParseListeners = postParseListeners;
  }

  public List<CmmnTransformListener> getCustomPreCmmnTransformListeners() {
    return customPreCmmnTransformListeners;
  }

  public void setCustomPreCmmnTransformListeners(List<CmmnTransformListener> customPreCmmnTransformListeners) {
    this.customPreCmmnTransformListeners = customPreCmmnTransformListeners;
  }

  public List<CmmnTransformListener> getCustomPostCmmnTransformListeners() {
    return customPostCmmnTransformListeners;
  }

  public void setCustomPostCmmnTransformListeners(List<CmmnTransformListener> customPostCmmnTransformListeners) {
    this.customPostCmmnTransformListeners = customPostCmmnTransformListeners;
  }

  public Map<Object, Object> getBeans() {
    return beans;
  }

  public void setBeans(Map<Object, Object> beans) {
    this.beans = beans;
  }

  @Override
  public ProcessEngineConfigurationImpl setClassLoader(ClassLoader classLoader) {
    super.setClassLoader(classLoader);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDatabaseType(String databaseType) {
    super.setDatabaseType(databaseType);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDataSource(DataSource dataSource) {
    super.setDataSource(dataSource);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    super.setDatabaseSchemaUpdate(databaseSchemaUpdate);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setHistory(String history) {
    super.setHistory(history);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setIdBlockSize(int idBlockSize) {
    super.setIdBlockSize(idBlockSize);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcDriver(String jdbcDriver) {
    super.setJdbcDriver(jdbcDriver);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcPassword(String jdbcPassword) {
    super.setJdbcPassword(jdbcPassword);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcUrl(String jdbcUrl) {
    super.setJdbcUrl(jdbcUrl);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcUsername(String jdbcUsername) {
    super.setJdbcUsername(jdbcUsername);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJobExecutorActivate(boolean jobExecutorActivate) {
    super.setJobExecutorActivate(jobExecutorActivate);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerDefaultFrom(String mailServerDefaultFrom) {
    super.setMailServerDefaultFrom(mailServerDefaultFrom);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerHost(String mailServerHost) {
    super.setMailServerHost(mailServerHost);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerPassword(String mailServerPassword) {
    super.setMailServerPassword(mailServerPassword);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerPort(int mailServerPort) {
    super.setMailServerPort(mailServerPort);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerUseTLS(boolean useTLS) {
    super.setMailServerUseTLS(useTLS);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerUsername(String mailServerUsername) {
    super.setMailServerUsername(mailServerUsername);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    super.setJdbcMaxActiveConnections(jdbcMaxActiveConnections);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    super.setJdbcMaxCheckoutTime(jdbcMaxCheckoutTime);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    super.setJdbcMaxIdleConnections(jdbcMaxIdleConnections);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    super.setJdbcMaxWaitTime(jdbcMaxWaitTime);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    super.setTransactionsExternallyManaged(transactionsExternallyManaged);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcPingConnectionNotUsedFor(int jdbcPingNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingNotUsedFor;
    return this;
  }

  public boolean isDbIdentityUsed() {
    return isDbIdentityUsed;
  }


  public void setDbIdentityUsed(boolean isDbIdentityUsed) {
    this.isDbIdentityUsed = isDbIdentityUsed;
  }

  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }

  public void setDbHistoryUsed(boolean isDbHistoryUsed) {
    this.isDbHistoryUsed = isDbHistoryUsed;
  }

  public List<ResolverFactory> getResolverFactories() {
    return resolverFactories;
  }

  public void setResolverFactories(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }

  public DeploymentCache getDeploymentCache() {
    return deploymentCache;
  }

  public void setDeploymentCache(DeploymentCache deploymentCache) {
    this.deploymentCache = deploymentCache;
  }

  public ProcessEngineConfigurationImpl setDelegateInterceptor(DelegateInterceptor delegateInterceptor) {
    this.delegateInterceptor = delegateInterceptor;
    return this;
  }

  public DelegateInterceptor getDelegateInterceptor() {
    return delegateInterceptor;
  }

  public RejectedJobsHandler getCustomRejectedJobsHandler() {
    return customRejectedJobsHandler;
  }

  public ProcessEngineConfigurationImpl setCustomRejectedJobsHandler(RejectedJobsHandler customRejectedJobsHandler) {
    this.customRejectedJobsHandler = customRejectedJobsHandler;
    return this;
  }

  public EventHandler getEventHandler(String eventType) {
    return eventHandlers.get(eventType);
  }

  public void setEventHandlers(Map<String, EventHandler> eventHandlers) {
    this.eventHandlers = eventHandlers;
  }

  public Map<String, EventHandler> getEventHandlers() {
    return eventHandlers;
  }

  public List<EventHandler> getCustomEventHandlers() {
    return customEventHandlers;
  }

  public void setCustomEventHandlers(List<EventHandler> customEventHandlers) {
    this.customEventHandlers = customEventHandlers;
  }

  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }

  public ProcessEngineConfigurationImpl setFailedJobCommandFactory(FailedJobCommandFactory failedJobCommandFactory) {
    this.failedJobCommandFactory = failedJobCommandFactory;
    return this;
  }

  /**
   * Allows configuring a database table prefix which is used for all runtime operations of the process engine.
   * For example, if you specify a prefix named 'PRE1.', activiti will query for executions in a table named
   * 'PRE1.ACT_RU_EXECUTION_'.
   *
   * <p />
   * <strong>NOTE: the prefix is not respected by automatic database schema management. If you use
   * {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_CREATE_DROP}
   * or {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_TRUE}, activiti will create the database tables
   * using the default names, regardless of the prefix configured here.</strong>
   *
   * @since 5.9
   */
  public ProcessEngineConfiguration setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
    return this;
  }

  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }

  public boolean isCreateDiagramOnDeploy() {
    return isCreateDiagramOnDeploy;
  }

  public ProcessEngineConfiguration setCreateDiagramOnDeploy(boolean createDiagramOnDeploy) {
    this.isCreateDiagramOnDeploy = createDiagramOnDeploy;
    return this;
  }

  public String getDatabaseSchema() {
    return databaseSchema;
  }

  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

  public DataSource getIdGeneratorDataSource() {
    return idGeneratorDataSource;
  }

  public void setIdGeneratorDataSource(DataSource idGeneratorDataSource) {
    this.idGeneratorDataSource = idGeneratorDataSource;
  }

  public String getIdGeneratorDataSourceJndiName() {
    return idGeneratorDataSourceJndiName;
  }

  public void setIdGeneratorDataSourceJndiName(String idGeneratorDataSourceJndiName) {
    this.idGeneratorDataSourceJndiName = idGeneratorDataSourceJndiName;
  }

  public ProcessApplicationManager getProcessApplicationManager() {
    return processApplicationManager;
  }

  public void setProcessApplicationManager(ProcessApplicationManager processApplicationManager) {
    this.processApplicationManager = processApplicationManager;
  }

  public CommandExecutor getCommandExecutorSchemaOperations() {
    return commandExecutorSchemaOperations;
  }

  public void setCommandExecutorSchemaOperations(CommandExecutor commandExecutorSchemaOperations) {
    this.commandExecutorSchemaOperations = commandExecutorSchemaOperations;
  }

  public CorrelationHandler getCorrelationHandler() {
    return correlationHandler;
  }

  public void setCorrelationHandler(CorrelationHandler correlationHandler) {
    this.correlationHandler = correlationHandler;
  }

  public ProcessEngineConfigurationImpl setHistoryEventHandler(HistoryEventHandler historyEventHandler) {
    this.historyEventHandler = historyEventHandler;
    return this;
  }

  public HistoryEventHandler getHistoryEventHandler() {
    return historyEventHandler;
  }

  public IncidentHandler getIncidentHandler(String incidentType) {
    return incidentHandlers.get(incidentType);
  }

  public Map<String, IncidentHandler> getIncidentHandlers() {
    return incidentHandlers;
  }

  public void setIncidentHandlers(Map<String, IncidentHandler> incidentHandlers) {
    this.incidentHandlers = incidentHandlers;
  }

  public List<IncidentHandler> getCustomIncidentHandlers() {
    return customIncidentHandlers;
  }

  public void setCustomIncidentHandlers(List<IncidentHandler> customIncidentHandlers) {
    this.customIncidentHandlers = customIncidentHandlers;
  }

  public SessionFactory getIdentityProviderSessionFactory() {
    return identityProviderSessionFactory;
  }

  public void setIdentityProviderSessionFactory(SessionFactory identityProviderSessionFactory) {
    this.identityProviderSessionFactory = identityProviderSessionFactory;
  }

  public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
    this.passwordEncryptor = passwordEncryptor;
  }

  public PasswordEncryptor getPasswordEncryptor() {
    return passwordEncryptor;
  }

  public Set<String> getRegisteredDeployments() {
    return registeredDeployments;
  }

  public void setRegisteredDeployments(Set<String> registeredDeployments) {
    this.registeredDeployments = registeredDeployments;
  }

  public ResourceAuthorizationProvider getResourceAuthorizationProvider() {
    return resourceAuthorizationProvider;
  }

  public void setResourceAuthorizationProvider(ResourceAuthorizationProvider resourceAuthorizationProvider) {
    this.resourceAuthorizationProvider = resourceAuthorizationProvider;
  }

  public List<ProcessEnginePlugin> getProcessEnginePlugins() {
    return processEnginePlugins;
  }

  public void setProcessEnginePlugins(List<ProcessEnginePlugin> processEnginePlugins) {
    this.processEnginePlugins = processEnginePlugins;
  }

  public ProcessEngineConfigurationImpl setHistoryEventProducer(HistoryEventProducer historyEventProducer) {
    this.historyEventProducer = historyEventProducer;
    return this;
  }

  public HistoryEventProducer getHistoryEventProducer() {
    return historyEventProducer;
  }

  public ProcessEngineConfigurationImpl setCmmnHistoryEventProducer(CmmnHistoryEventProducer cmmnHistoryEventProducer) {
    this.cmmnHistoryEventProducer = cmmnHistoryEventProducer;
    return this;
  }

  public CmmnHistoryEventProducer getCmmnHistoryEventProducer() {
    return cmmnHistoryEventProducer;
  }

  public Map<String, Class<? extends FormFieldValidator>> getCustomFormFieldValidators() {
    return customFormFieldValidators;
  }

  public void setCustomFormFieldValidators(Map<String, Class<? extends FormFieldValidator>> customFormFieldValidators) {
    this.customFormFieldValidators = customFormFieldValidators;
  }

  public void setFormValidators(FormValidators formValidators) {
    this.formValidators = formValidators;
  }

  public FormValidators getFormValidators() {
    return formValidators;
  }

  public boolean isExecutionTreePrefetchEnabled() {
    return isExecutionTreePrefetchEnabled;
  }

  public void setExecutionTreePrefetchEnabled(boolean isExecutionTreePrefetchingEnabled) {
    this.isExecutionTreePrefetchEnabled = isExecutionTreePrefetchingEnabled;
  }

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  /**
   * If set to true, the process engine will save all script variables (created from Java Script, Groovy ...)
   * as process variables.
   */
  public void setAutoStoreScriptVariables(boolean autoStoreScriptVariables) {
    this.autoStoreScriptVariables = autoStoreScriptVariables;
  }

  /**
   * @return true if the process engine should save all script variables (created from Java Script, Groovy ...)
   * as process variables.
   */
  public boolean isAutoStoreScriptVariables() {
    return autoStoreScriptVariables;
  }

  /**
   * If set to true, the process engine will attempt to pre-compile script sources at runtime
   * to optimize script task execution performance.
   */
  public void setEnableScriptCompilation(boolean enableScriptCompilation) {
    this.enableScriptCompilation = enableScriptCompilation;
  }

  /**
   * @return true if compilation of script sources ins enabled. False otherwise.
   */
  public boolean isEnableScriptCompilation() {
    return enableScriptCompilation;
  }

  public boolean isEnableGracefulDegradationOnContextSwitchFailure() {
    return enableGracefulDegradationOnContextSwitchFailure;
  }

  /**
   * <p>If set to true, the process engine will tolerate certain exceptions that may result
   * from the fact that it cannot switch to the context of a process application that has made
   * a deployment.</p>
   *
   * <p>Affects the following scenarios:</p>
   * <ul>
   *   <li><b>Determining job priorities</b>: uses a default priority in case an expression fails to evaluate</li>
   * </ul>
   */
  public void setEnableGracefulDegradationOnContextSwitchFailure(boolean enableGracefulDegradationOnContextSwitchFailure) {
    this.enableGracefulDegradationOnContextSwitchFailure = enableGracefulDegradationOnContextSwitchFailure;
  }

  /**
   * @return true if the process engine acquires an exclusive lock when creating a deployment.
   */
  public boolean isDeploymentLockUsed() {
    return isDeploymentLockUsed;
  }

  /**
   * If set to true, the process engine will acquire an exclusive lock when creating a deployment.
   * This ensures that {@link DeploymentBuilder#enableDuplicateFiltering()} works correctly in a clustered environment.
   */
  public void setDeploymentLockUsed(boolean isDeploymentLockUsed) {
    this.isDeploymentLockUsed = isDeploymentLockUsed;
  }

  public boolean isCmmnEnabled() {
    return cmmnEnabled;
  }

  public void setCmmnEnabled(boolean cmmnEnabled) {
    this.cmmnEnabled = cmmnEnabled;
  }

  public boolean isDmnEnabled() {
    return dmnEnabled;
  }

  public void setDmnEnabled(boolean dmnEnabled) {
    this.dmnEnabled = dmnEnabled;
  }

  public ScriptFactory getScriptFactory() {
    return scriptFactory;
  }

  public ScriptingEnvironment getScriptingEnvironment() {
    return scriptingEnvironment;
  }

  public void setScriptFactory(ScriptFactory scriptFactory) {
    this.scriptFactory = scriptFactory;
  }

  public void setScriptingEnvironment(ScriptingEnvironment scriptingEnvironment) {
    this.scriptingEnvironment = scriptingEnvironment;
  }

  public List<ScriptEnvResolver> getEnvScriptResolvers() {
    return scriptEnvResolvers;
  }

  public void setEnvScriptResolvers(List<ScriptEnvResolver> scriptEnvResolvers) {
    this.scriptEnvResolvers = scriptEnvResolvers;
  }

  public ProcessEngineConfiguration setArtifactFactory(ArtifactFactory artifactFactory) {
    this.artifactFactory = artifactFactory;
    return this;
  }

  public ArtifactFactory getArtifactFactory() {
    return artifactFactory;
  }

  public String getDefaultSerializationFormat() {
    return defaultSerializationFormat;
  }

  public ProcessEngineConfigurationImpl setDefaultSerializationFormat(String defaultSerializationFormat) {
    this.defaultSerializationFormat = defaultSerializationFormat;
    return this;
  }

  public ProcessEngineConfigurationImpl setDefaultCharsetName(String defaultCharsetName) {
    this.defaultCharsetName = defaultCharsetName;
    return this;
  }

  public ProcessEngineConfigurationImpl setDefaultCharset(Charset defautlCharset) {
    this.defaultCharset = defautlCharset;
    return this;
  }

  public Charset getDefaultCharset() {
    return defaultCharset;
  }

  public boolean isDbEntityCacheReuseEnabled() {
    return isDbEntityCacheReuseEnabled;
  }

  public ProcessEngineConfigurationImpl setDbEntityCacheReuseEnabled(boolean isDbEntityCacheReuseEnabled) {
    this.isDbEntityCacheReuseEnabled = isDbEntityCacheReuseEnabled;
    return this;
  }

  public DbEntityCacheKeyMapping getDbEntityCacheKeyMapping() {
    return dbEntityCacheKeyMapping;
  }

  public ProcessEngineConfigurationImpl setDbEntityCacheKeyMapping(DbEntityCacheKeyMapping dbEntityCacheKeyMapping) {
    this.dbEntityCacheKeyMapping = dbEntityCacheKeyMapping;
    return this;
  }

  public ProcessEngineConfigurationImpl setCustomHistoryLevels(List<HistoryLevel> customHistoryLevels) {
    this.customHistoryLevels = customHistoryLevels;
    return this;
  }

  public List<HistoryLevel> getHistoryLevels() {
    return historyLevels;
  }

  public List<HistoryLevel> getCustomHistoryLevels() {
    return customHistoryLevels;
  }

  public boolean isInvokeCustomVariableListeners() {
    return isInvokeCustomVariableListeners;
  }

  public ProcessEngineConfigurationImpl setInvokeCustomVariableListeners(boolean isInvokeCustomVariableListeners) {
    this.isInvokeCustomVariableListeners = isInvokeCustomVariableListeners;
    return this;
  }

  public void close() {
    if (dataSource instanceof PooledDataSource) {
      // ACT-233: connection pool of Ibatis is not properely initialized if this is not called!
      ((PooledDataSource)dataSource).forceCloseAll();
    }
  }

  public MetricsRegistry getMetricsRegistry() {
    return metricsRegistry;
  }

  public ProcessEngineConfigurationImpl setMetricsRegistry(MetricsRegistry metricsRegistry) {
    this.metricsRegistry = metricsRegistry;
    return this;
  }

  public ProcessEngineConfigurationImpl setMetricsEnabled(boolean isMetricsEnabled) {
    this.isMetricsEnabled = isMetricsEnabled;
    return this;
  }

  public boolean isMetricsEnabled() {
    return isMetricsEnabled;
  }

  public DbMetricsReporter getDbMetricsReporter() {
    return dbMetricsReporter;
  }

  public ProcessEngineConfigurationImpl setDbMetricsReporter(DbMetricsReporter dbMetricsReporter) {
    this.dbMetricsReporter = dbMetricsReporter;
    return this;
  }

  public boolean isDbMetricsReporterActivate() {
    return isDbMetricsReporterActivate;
  }

  public ProcessEngineConfigurationImpl setDbMetricsReporterActivate(boolean isDbMetricsReporterEnabled) {
    this.isDbMetricsReporterActivate = isDbMetricsReporterEnabled;
    return this;
  }

  public MetricsReporterIdProvider getMetricsReporterIdProvider() {
    return metricsReporterIdProvider;
  }

  public void setMetricsReporterIdProvider(MetricsReporterIdProvider metricsReporterIdProvider) {
    this.metricsReporterIdProvider = metricsReporterIdProvider;
  }

  public ProcessEngineConfigurationImpl setBpmnStacktraceVerbose(boolean isBpmnStacktraceVerbose) {
    this.isBpmnStacktraceVerbose = isBpmnStacktraceVerbose;
    return this;
  }

  public boolean isBpmnStacktraceVerbose() {
    return this.isBpmnStacktraceVerbose;
  }
}
