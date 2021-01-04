/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.BeansConfigurationHelper;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.runtime.DeserializationTypeValidator;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;


/** Configuration information from which a process engine can be build.
 *
 * <p>Most common is to create a process engine based on the default configuration file:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .buildProcessEngine();
 * </pre>
 * </p>
 *
 * <p>To create a process engine programatic, without a configuration file,
 * the first option is {@link #createStandaloneProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to
 * a remote h2 database (jdbc:h2:tcp://localhost/activiti) in standalone
 * mode.  Standalone mode means that Activiti will manage the transactions
 * on the JDBC connections that it creates.  One transaction per
 * service method.
 * For a description of how to write the configuration files, see the
 * userguide.
 * </p>
 *
 * <p>The second option is great for testing: {@link #createStandaloneInMemProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneInMemProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to
 * an memory h2 database (jdbc:h2:tcp://localhost/activiti) in standalone
 * mode.  The DB schema strategy default is in this case <code>create-drop</code>.
 * Standalone mode means that Activiti will manage the transactions
 * on the JDBC connections that it creates.  One transaction per
 * service method.
 * </p>
 *
 * <p>On all forms of creating a process engine, you can first customize the configuration
 * before calling the {@link #buildProcessEngine()} method by calling any of the
 * setters like this:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .setMailServerHost("gmail.com")
 *   .setJdbcUsername("mickey")
 *   .setJdbcPassword("mouse")
 *   .buildProcessEngine();
 * </pre>
 * </p>
 *
 * @see ProcessEngines
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfiguration {

  /**
   * Checks the version of the DB schema against the library when
   * the process engine is being created and throws an exception
   * if the versions don't match.
   */
  public static final String DB_SCHEMA_UPDATE_FALSE = "false";

  /**
   * Creates the schema when the process engine is being created and
   * drops the schema when the process engine is being closed.
   */
  public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";

  /**
   * Upon building of the process engine, a check is performed and
   * an update of the schema is performed if it is necessary.
   */
  public static final String DB_SCHEMA_UPDATE_TRUE = "true";

  /**
   * Value for {@link #setHistory(String)} to ensure that no history is being recorded.
   */
  public static final String HISTORY_NONE = "none";
  /**
   * Value for {@link #setHistory(String)} to ensure that only historic process instances and
   * historic activity instances are being recorded.
   * This means no details for those entities.
   */
  public static final String HISTORY_ACTIVITY = "activity";
  /**
   * Value for {@link #setHistory(String)} to ensure that only historic process instances,
   * historic activity instances and last process variable values are being recorded.
   * <p><strong>NOTE:</strong> This history level has been deprecated. Use level {@link #HISTORY_ACTIVITY} instead.</p>
   */
  @Deprecated
  public static final String HISTORY_VARIABLE = "variable";
  /**
   * Value for {@link #setHistory(String)} to ensure that only historic process instances,
   * historic activity instances and submitted form property values are being recorded.
   */
  public static final String HISTORY_AUDIT = "audit";
  /**
   * Value for {@link #setHistory(String)} to ensure that all historic information is
   * being recorded, including the variable updates.
   */
  public static final String HISTORY_FULL = "full";

  /**
   * Value for {@link #setHistory(String)}. Choosing auto causes the configuration to choose the level
   * already present on the database. If none can be found, "audit" is taken.
   */
  public static final String HISTORY_AUTO = "auto";

  /**
   * The default history level that is used when no history level is configured
   */
  public static final String HISTORY_DEFAULT = HISTORY_AUDIT;

  /**
   * History cleanup is performed based on end time.
   */
  public static final String HISTORY_CLEANUP_STRATEGY_END_TIME_BASED = "endTimeBased";

  /**
   * History cleanup is performed based on removal time.
   */
  public static final String HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED = "removalTimeBased";

  /**
   * Removal time for historic entities is set on execution start.
   */
  public static final String HISTORY_REMOVAL_TIME_STRATEGY_START = "start";

  /**
   * Removal time for historic entities is set if execution has been ended.
   */
  public static final String HISTORY_REMOVAL_TIME_STRATEGY_END = "end";

  /**
   * Removal time for historic entities is not set.
   */
  public static final String HISTORY_REMOVAL_TIME_STRATEGY_NONE = "none";

  /**
   * Always enables check for {@link Authorization#AUTH_TYPE_REVOKE revoke} authorizations.
   * This mode is equal to the &lt; 7.5 behavior.
   *<p />
   * *NOTE:* Checking revoke authorizations is very expensive for resources with a high potential
   * cardinality like tasks or process instances and can render authorized access to the process engine
   * effectively unusable on most databases. You are therefore strongly discouraged from using this mode.
   *
   */
  public static final String AUTHORIZATION_CHECK_REVOKE_ALWAYS = "always";

  /**
   * Never checks for {@link Authorization#AUTH_TYPE_REVOKE revoke} authorizations. This mode
   * has best performance effectively disables the use of {@link Authorization#AUTH_TYPE_REVOKE}.
   * *Note*: It is strongly recommended to use this mode.
   */
  public static final String AUTHORIZATION_CHECK_REVOKE_NEVER = "never";

  /**
   * This mode only checks for {@link Authorization#AUTH_TYPE_REVOKE revoke} authorizations if at least
   * one revoke authorization currently exits for the current user or one of the groups the user is a member
   * of. To achieve this it is checked once per command whether potentially applicable revoke authorizations
   * exist. Based on the outcome, the authorization check then uses revoke or not.
   *<p />
   * *NOTE:* Checking revoke authorizations is very expensive for resources with a high potential
   * cardinality like tasks or process instances and can render authorized access to the process engine
   * effectively unusable on most databases.
   */
  public static final String AUTHORIZATION_CHECK_REVOKE_AUTO = "auto";

  protected String processEngineName = ProcessEngines.NAME_DEFAULT;
  protected int idBlockSize = 100;
  protected String history = HISTORY_DEFAULT;
  protected boolean jobExecutorActivate;
  protected boolean jobExecutorDeploymentAware = false;
  protected boolean jobExecutorPreferTimerJobs = false;
  protected boolean jobExecutorAcquireByDueDate = false;
  protected boolean jobExecutorAcquireByPriority = false;

  protected boolean ensureJobDueDateNotNull = false;
  protected boolean producePrioritizedJobs = true;
  protected boolean producePrioritizedExternalTasks = true;

  /**
   * The flag will be used inside the method "JobManager#send()". It will be used to decide whether to notify the
   * job executor that a new job has been created. It will be used for performance improvement, so that the new job could
   * be executed in some situations immediately.
   */
  protected boolean hintJobExecutor = true;

  protected String mailServerHost = "localhost";
  protected String mailServerUsername; // by default no name and password are provided, which
  protected String mailServerPassword; // means no authentication for mail server
  protected int mailServerPort = 25;
  protected boolean useTLS = false;
  protected String mailServerDefaultFrom = "camunda@localhost";

  protected String databaseType;
  protected String databaseVendor;
  protected String databaseVersion;
  protected String databaseSchemaUpdate = DB_SCHEMA_UPDATE_FALSE;
  protected String jdbcDriver = "org.h2.Driver";
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/activiti";
  protected String jdbcUsername = "sa";
  protected String jdbcPassword = "";
  protected String dataSourceJndiName = null;
  protected int jdbcMaxActiveConnections;
  protected int jdbcMaxIdleConnections;
  protected int jdbcMaxCheckoutTime;
  protected int jdbcMaxWaitTime;
  protected boolean jdbcPingEnabled = false;
  protected String jdbcPingQuery = null;
  protected int jdbcPingConnectionNotUsedFor;
  protected DataSource dataSource;
  protected SchemaOperationsCommand schemaOperationsCommand = new SchemaOperationsProcessEngineBuild();
  protected ProcessEngineBootstrapCommand bootstrapCommand = new BootstrapEngineCommand();
  protected HistoryLevelSetupCommand historyLevelCommand = new HistoryLevelSetupCommand();
  protected boolean transactionsExternallyManaged = false;
  /** the number of seconds the jdbc driver will wait for a response from the database */
  protected Integer jdbcStatementTimeout;
  protected boolean jdbcBatchProcessing = true;

  protected String jpaPersistenceUnitName;
  protected Object jpaEntityManagerFactory;
  protected boolean jpaHandleTransaction;
  protected boolean jpaCloseEntityManager;
  protected int defaultNumberOfRetries = JobEntity.DEFAULT_RETRIES;

  protected ClassLoader classLoader;

  protected boolean createIncidentOnFailedJobEnabled = true;

  /**
   * configuration of password policy
   */
  protected boolean enablePasswordPolicy;
  protected PasswordPolicy passwordPolicy;

  /**
   * switch for controlling whether the process engine performs authorization checks.
   * The default value is false.
   */
  protected boolean authorizationEnabled = false;

  /**
   * Provides the default task permission for the user related to a task
   * User can be related to a task in the following ways
   * - Candidate user
   * - Part of candidate group
   * - Assignee
   * - Owner
   * The default value is UPDATE.
   */
  protected String defaultUserPermissionNameForTask = "UPDATE";

  /**
   * <p>The following flag <code>authorizationEnabledForCustomCode</code> will
   * only be taken into account iff <code>authorizationEnabled</code> is set
   * <code>true</code>.</p>
   *
   * <p>If the value of the flag <code>authorizationEnabledForCustomCode</code>
   * is set <code>true</code> then an authorization check will be performed by
   * executing commands inside custom code (e.g. inside {@link JavaDelegate}).</p>
   *
   * <p>The default value is <code>false</code>.</p>
   *
   */
  protected boolean authorizationEnabledForCustomCode = false;

  /**
   * If the value of this flag is set <code>true</code> then the process engine
   * performs tenant checks to ensure that an authenticated user can only access
   * data that belongs to one of his tenants.
   */
  protected boolean tenantCheckEnabled = true;

  protected ValueTypeResolver valueTypeResolver;

  protected String authorizationCheckRevokes = AUTHORIZATION_CHECK_REVOKE_AUTO;

  /**
   * A parameter used for defining acceptable values for the User, Group
   * and Tenant IDs. The pattern can be defined by using the standard
   * Java Regular Expression syntax should be used.
   *
   * <p>By default only alphanumeric values (or 'camunda-admin') will be accepted.</p>
   */
  protected String generalResourceWhitelistPattern =  "[a-zA-Z0-9]+|camunda-admin";

  /**
   * A parameter used for defining acceptable values for the User IDs.
   * The pattern can be defined by using the standard Java Regular
   * Expression syntax should be used.
   *
   * <p>If not defined, the general pattern is used. Only alphanumeric
   * values (or 'camunda-admin') will be accepted.</p>
   */
  protected String userResourceWhitelistPattern;

  /**
   * A parameter used for defining acceptable values for the Group IDs.
   * The pattern can be defined by using the standard Java Regular
   * Expression syntax should be used.
   *
   * <p>If not defined, the general pattern is used. Only alphanumeric
   * values (or 'camunda-admin') will be accepted.</p>
   */
  protected String groupResourceWhitelistPattern;

  /**
   * A parameter used for defining acceptable values for the Tenant IDs.
   * The pattern can be defined by using the standard Java Regular
   * Expression syntax should be used.
   *
   * <p>If not defined, the general pattern is used. Only alphanumeric
   * values (or 'camunda-admin') will be accepted.</p>
   */
  protected String tenantResourceWhitelistPattern;

  /**
   * If the value of this flag is set <code>true</code> then the process engine
   * throws {@link ProcessEngineException} when no catching boundary event was
   * defined for an error event.
   *
   * <p>The default value is <code>false</code>.</p>
   */
  protected boolean enableExceptionsAfterUnhandledBpmnError = false;

  /**
   * If the value of this flag is set to <code>false</code>, {@link OptimisticLockingException}s
   * are not skipped for UPDATE or DELETE operations applied to historic entities.
   *
   * <p>The default value is <code>true</code>.</p>
   */
  protected boolean skipHistoryOptimisticLockingExceptions = true;

  /**
   * If the value of this flag is set to <code>true</code>,
   * READ_INSTANCE_VARIABLE,
   * READ_HISTORY_VARIABLE, or
   * READ_TASK_VARIABLE on Process Definition resource, and
   * READ_VARIABLE on Task resource
   * READ_VARIABLE on Historic Task Instance resource
   * will be required to fetch variables when the authorizations are enabled.
   */
  protected boolean enforceSpecificVariablePermission = false;

  /**
   * Specifies which permissions will not be taken into account in the
   * authorizations checks if authorization is enabled.
   */
  protected List<String> disabledPermissions = Collections.emptyList();

  /**
   * If the value of this flag is set to <code>false</code> exceptions that occur
   * during command execution will not be logged before re-thrown. This can prevent
   * multiple logs of the same exception (e.g. exceptions that occur during job execution)
   * but can also hide valuable debugging/rootcausing information.
   */
  protected boolean enableCmdExceptionLogging = true;

  /**
   * If the value of this flag is set to <code>true</code> exceptions that occur
   * during the execution of a job that still has retries left will not be logged.
   * If the job does not have any retries left, the exception will still be logged
   * on logging level WARN.
   */
  protected boolean enableReducedJobExceptionLogging = false;

  /** Specifies which classes are allowed for deserialization */
  protected String deserializationAllowedClasses;

  /** Specifies which packages are allowed for deserialization */
  protected String deserializationAllowedPackages;

  /** Validates types before deserialization */
  protected DeserializationTypeValidator deserializationTypeValidator;

  /** Indicates whether type validation should be done before deserialization */
  protected boolean deserializationTypeValidationEnabled = false;

  /** An unique installation identifier */
  protected String installationId;

  protected TelemetryRegistry telemetryRegistry;

  /**
   * On failing activities we can skip output mapping. This might be helpful if output mapping uses variables that might not
   * be available on failure (e.g. with external tasks or RPA tasks).
   */
  protected boolean skipOutputMappingOnCanceledActivities = false;

  /** use one of the static createXxxx methods instead */
  protected ProcessEngineConfiguration() {
  }

  public abstract ProcessEngine buildProcessEngine();

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResourceDefault() {
    ProcessEngineConfiguration processEngineConfiguration = null;
    try {
      processEngineConfiguration = createProcessEngineConfigurationFromResource("camunda.cfg.xml", "processEngineConfiguration");
    } catch (RuntimeException ex) {
      processEngineConfiguration = createProcessEngineConfigurationFromResource("activiti.cfg.xml", "processEngineConfiguration");
    }
    return processEngineConfiguration;
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource) {
    return createProcessEngineConfigurationFromResource(resource, "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromResource(resource, beanName);
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream) {
    return createProcessEngineConfigurationFromInputStream(inputStream, "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromInputStream(inputStream, beanName);
  }

  public static ProcessEngineConfiguration createStandaloneProcessEngineConfiguration() {
    return new StandaloneProcessEngineConfiguration();
  }

  public static ProcessEngineConfiguration createStandaloneInMemProcessEngineConfiguration() {
    return new StandaloneInMemProcessEngineConfiguration();
  }

// TODO add later when we have test coverage for this
//  public static ProcessEngineConfiguration createJtaProcessEngineConfiguration() {
//    return new JtaProcessEngineConfiguration();
//  }


  // getters and setters //////////////////////////////////////////////////////

  public String getProcessEngineName() {
    return processEngineName;
  }

  public ProcessEngineConfiguration setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  public int getIdBlockSize() {
    return idBlockSize;
  }

  public ProcessEngineConfiguration setIdBlockSize(int idBlockSize) {
    this.idBlockSize = idBlockSize;
    return this;
  }

  public String getHistory() {
    return history;
  }

  public ProcessEngineConfiguration setHistory(String history) {
    this.history = history;
    return this;
  }

  public String getMailServerHost() {
    return mailServerHost;
  }

  public ProcessEngineConfiguration setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
    return this;
  }

  public String getMailServerUsername() {
    return mailServerUsername;
  }

  public ProcessEngineConfiguration setMailServerUsername(String mailServerUsername) {
    this.mailServerUsername = mailServerUsername;
    return this;
  }

  public String getMailServerPassword() {
    return mailServerPassword;
  }

  public ProcessEngineConfiguration setMailServerPassword(String mailServerPassword) {
    this.mailServerPassword = mailServerPassword;
    return this;
  }

  public int getMailServerPort() {
    return mailServerPort;
  }

  public ProcessEngineConfiguration setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
    return this;
  }

  public boolean getMailServerUseTLS() {
    return useTLS;
  }

  public ProcessEngineConfiguration setMailServerUseTLS(boolean useTLS) {
    this.useTLS = useTLS;
    return this;
  }

  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  public ProcessEngineConfiguration setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
    return this;
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public ProcessEngineConfiguration setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    return this;
  }

  public String getDatabaseVendor() {
    return databaseVendor;
  }

  public ProcessEngineConfiguration setDatabaseVendor(String databaseVendor) {
    this.databaseVendor = databaseVendor;
    return this;
  }

  public String getDatabaseVersion() {
    return databaseVersion;
  }

  public ProcessEngineConfiguration setDatabaseVersion(String databaseVersion) {
    this.databaseVersion = databaseVersion;
    return this;
  }

  public String getDatabaseSchemaUpdate() {
    return databaseSchemaUpdate;
  }

  public ProcessEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    this.databaseSchemaUpdate = databaseSchemaUpdate;
    return this;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public ProcessEngineConfiguration setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public SchemaOperationsCommand getSchemaOperationsCommand() {
    return schemaOperationsCommand;
  }

  public void setSchemaOperationsCommand(SchemaOperationsCommand schemaOperationsCommand) {
    this.schemaOperationsCommand = schemaOperationsCommand;
  }

  public ProcessEngineBootstrapCommand getProcessEngineBootstrapCommand() {
    return bootstrapCommand;
  }

  public void setProcessEngineBootstrapCommand(ProcessEngineBootstrapCommand bootstrapCommand) {
    this.bootstrapCommand = bootstrapCommand;
  }

  public HistoryLevelSetupCommand getHistoryLevelCommand() {
    return historyLevelCommand;
  }

  public void setHistoryLevelCommand(HistoryLevelSetupCommand historyLevelCommand) {
    this.historyLevelCommand = historyLevelCommand;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public ProcessEngineConfiguration setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public ProcessEngineConfiguration setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public ProcessEngineConfiguration setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public ProcessEngineConfiguration setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  public boolean isTransactionsExternallyManaged() {
    return transactionsExternallyManaged;
  }

  public ProcessEngineConfiguration setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    this.transactionsExternallyManaged = transactionsExternallyManaged;
    return this;
  }

  public int getJdbcMaxActiveConnections() {
    return jdbcMaxActiveConnections;
  }

  public ProcessEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
    return this;
  }

  public int getJdbcMaxIdleConnections() {
    return jdbcMaxIdleConnections;
  }

  public ProcessEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
    return this;
  }

  public int getJdbcMaxCheckoutTime() {
    return jdbcMaxCheckoutTime;
  }

  public ProcessEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
    return this;
  }

  public int getJdbcMaxWaitTime() {
    return jdbcMaxWaitTime;
  }

  public ProcessEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    this.jdbcMaxWaitTime = jdbcMaxWaitTime;
    return this;
  }

  public boolean isJdbcPingEnabled() {
    return jdbcPingEnabled;
  }

  public ProcessEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  public String getJdbcPingQuery() {
    return jdbcPingQuery;
  }

  public ProcessEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  public int getJdbcPingConnectionNotUsedFor() {
    return jdbcPingConnectionNotUsedFor;
  }

  public ProcessEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingNotUsedFor;
    return this;
  }

  /** Gets the number of seconds the jdbc driver will wait for a response from the database. */
  public Integer getJdbcStatementTimeout() {
    return jdbcStatementTimeout;
  }

  /** Sets the number of seconds the jdbc driver will wait for a response from the database. */
  public ProcessEngineConfiguration setJdbcStatementTimeout(Integer jdbcStatementTimeout) {
    this.jdbcStatementTimeout = jdbcStatementTimeout;
    return this;
  }

  public boolean isJdbcBatchProcessing() {
    return jdbcBatchProcessing;
  }

  public ProcessEngineConfiguration setJdbcBatchProcessing(boolean jdbcBatchProcessing) {
    this.jdbcBatchProcessing = jdbcBatchProcessing;
    return this;
  }

  public boolean isJobExecutorActivate() {
    return jobExecutorActivate;
  }

  public ProcessEngineConfiguration setJobExecutorActivate(boolean jobExecutorActivate) {
    this.jobExecutorActivate = jobExecutorActivate;
    return this;
  }

  public boolean isJobExecutorDeploymentAware() {
    return jobExecutorDeploymentAware;
  }

  public ProcessEngineConfiguration setJobExecutorDeploymentAware(boolean jobExecutorDeploymentAware) {
    this.jobExecutorDeploymentAware = jobExecutorDeploymentAware;
    return this;
  }

  public boolean isJobExecutorAcquireByDueDate() {
    return jobExecutorAcquireByDueDate;
  }

  public ProcessEngineConfiguration setJobExecutorAcquireByDueDate(boolean jobExecutorAcquireByDueDate) {
    this.jobExecutorAcquireByDueDate = jobExecutorAcquireByDueDate;
    return this;
  }

  public boolean isJobExecutorPreferTimerJobs() {
    return jobExecutorPreferTimerJobs;
  }

  public ProcessEngineConfiguration setJobExecutorPreferTimerJobs(boolean jobExecutorPreferTimerJobs) {
    this.jobExecutorPreferTimerJobs = jobExecutorPreferTimerJobs;
    return this;
  }

  public boolean isHintJobExecutor() {
    return hintJobExecutor;
  }

  public ProcessEngineConfiguration setHintJobExecutor(boolean hintJobExecutor) {
    this.hintJobExecutor = hintJobExecutor;
    return this;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public ProcessEngineConfiguration setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public Object getJpaEntityManagerFactory() {
    return jpaEntityManagerFactory;
  }

  public ProcessEngineConfiguration setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  public boolean isJpaHandleTransaction() {
    return jpaHandleTransaction;
  }

  public ProcessEngineConfiguration setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }

  public boolean isJpaCloseEntityManager() {
    return jpaCloseEntityManager;
  }

  public ProcessEngineConfiguration setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }

  public String getJpaPersistenceUnitName() {
    return jpaPersistenceUnitName;
  }

  public void setJpaPersistenceUnitName(String jpaPersistenceUnitName) {
    this.jpaPersistenceUnitName = jpaPersistenceUnitName;
  }

  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  public void setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
  }

  public boolean isCreateIncidentOnFailedJobEnabled() {
    return createIncidentOnFailedJobEnabled;
  }

  public ProcessEngineConfiguration setCreateIncidentOnFailedJobEnabled(boolean createIncidentOnFailedJobEnabled) {
    this.createIncidentOnFailedJobEnabled = createIncidentOnFailedJobEnabled;
    return this;
  }

  public boolean isAuthorizationEnabled() {
    return authorizationEnabled;
  }

  public ProcessEngineConfiguration setAuthorizationEnabled(boolean isAuthorizationChecksEnabled) {
    this.authorizationEnabled = isAuthorizationChecksEnabled;
    return this;
  }

  public String getDefaultUserPermissionNameForTask() {
    return defaultUserPermissionNameForTask;
  }

  public ProcessEngineConfiguration setDefaultUserPermissionNameForTask(String defaultUserPermissionNameForTask) {
    this.defaultUserPermissionNameForTask = defaultUserPermissionNameForTask;
    return this;
  }

  public boolean isAuthorizationEnabledForCustomCode() {
    return authorizationEnabledForCustomCode;
  }

  public ProcessEngineConfiguration setAuthorizationEnabledForCustomCode(boolean authorizationEnabledForCustomCode) {
    this.authorizationEnabledForCustomCode = authorizationEnabledForCustomCode;
    return this;
  }

  public boolean isTenantCheckEnabled() {
    return tenantCheckEnabled;
  }

  public ProcessEngineConfiguration setTenantCheckEnabled(boolean isTenantCheckEnabled) {
    this.tenantCheckEnabled = isTenantCheckEnabled;
    return this;
  }

  public String getGeneralResourceWhitelistPattern() {
    return generalResourceWhitelistPattern;
  }

  public void setGeneralResourceWhitelistPattern(String generalResourceWhitelistPattern) {
    this.generalResourceWhitelistPattern = generalResourceWhitelistPattern;
  }

  public String getUserResourceWhitelistPattern() {
    return userResourceWhitelistPattern;
  }

  public void setUserResourceWhitelistPattern(String userResourceWhitelistPattern) {
    this.userResourceWhitelistPattern = userResourceWhitelistPattern;
  }

  public String getGroupResourceWhitelistPattern() {
    return groupResourceWhitelistPattern;
  }

  public void setGroupResourceWhitelistPattern(String groupResourceWhitelistPattern) {
    this.groupResourceWhitelistPattern = groupResourceWhitelistPattern;
  }

  public String getTenantResourceWhitelistPattern() {
    return tenantResourceWhitelistPattern;
  }

  public void setTenantResourceWhitelistPattern(String tenantResourceWhitelistPattern) {
    this.tenantResourceWhitelistPattern = tenantResourceWhitelistPattern;
  }

  public int getDefaultNumberOfRetries() {
    return defaultNumberOfRetries;
  }

  public void setDefaultNumberOfRetries(int defaultNumberOfRetries) {
    this.defaultNumberOfRetries = defaultNumberOfRetries;
  }

  public ValueTypeResolver getValueTypeResolver() {
    return valueTypeResolver;
  }

  public ProcessEngineConfiguration setValueTypeResolver(ValueTypeResolver valueTypeResolver) {
    this.valueTypeResolver = valueTypeResolver;
    return this;
  }

  public boolean isEnsureJobDueDateNotNull() {
    return ensureJobDueDateNotNull;
  }

  public void setEnsureJobDueDateNotNull(boolean ensureJobDueDateNotNull) {
    this.ensureJobDueDateNotNull = ensureJobDueDateNotNull;
  }

  public boolean isProducePrioritizedJobs() {
    return producePrioritizedJobs;
  }

  public void setProducePrioritizedJobs(boolean producePrioritizedJobs) {
    this.producePrioritizedJobs = producePrioritizedJobs;
  }

  public boolean isJobExecutorAcquireByPriority() {
    return jobExecutorAcquireByPriority;
  }

  public void setJobExecutorAcquireByPriority(boolean jobExecutorAcquireByPriority) {
    this.jobExecutorAcquireByPriority = jobExecutorAcquireByPriority;
  }

  public boolean isProducePrioritizedExternalTasks() {
    return producePrioritizedExternalTasks;
  }

  public void setProducePrioritizedExternalTasks(boolean producePrioritizedExternalTasks) {
    this.producePrioritizedExternalTasks = producePrioritizedExternalTasks;
  }

  public void setAuthorizationCheckRevokes(String authorizationCheckRevokes) {
    this.authorizationCheckRevokes = authorizationCheckRevokes;
  }

  public String getAuthorizationCheckRevokes() {
    return authorizationCheckRevokes;
  }

  public boolean isEnableExceptionsAfterUnhandledBpmnError() {
    return enableExceptionsAfterUnhandledBpmnError;
  }

  public void setEnableExceptionsAfterUnhandledBpmnError(boolean enableExceptionsAfterUnhandledBpmnError) {
    this.enableExceptionsAfterUnhandledBpmnError = enableExceptionsAfterUnhandledBpmnError;
  }

  public boolean isSkipHistoryOptimisticLockingExceptions() {
    return skipHistoryOptimisticLockingExceptions;
  }

  public ProcessEngineConfiguration setSkipHistoryOptimisticLockingExceptions(boolean skipHistoryOptimisticLockingExceptions) {
    this.skipHistoryOptimisticLockingExceptions = skipHistoryOptimisticLockingExceptions;
    return this;
  }

  public boolean isEnforceSpecificVariablePermission() {
    return enforceSpecificVariablePermission;
  }

  public void setEnforceSpecificVariablePermission(boolean ensureSpecificVariablePermission) {
    this.enforceSpecificVariablePermission = ensureSpecificVariablePermission;
  }

  public List<String> getDisabledPermissions() {
    return disabledPermissions;
  }

  public void setDisabledPermissions(List<String> disabledPermissions) {
    this.disabledPermissions = disabledPermissions;
  }

  public boolean isEnablePasswordPolicy() {
    return enablePasswordPolicy;
  }

  public ProcessEngineConfiguration setEnablePasswordPolicy(boolean enablePasswordPolicy) {
    this.enablePasswordPolicy = enablePasswordPolicy;
    return this;
  }

  public PasswordPolicy getPasswordPolicy() {
    return passwordPolicy;
  }

  public ProcessEngineConfiguration setPasswordPolicy(PasswordPolicy passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
    return this;
  }

  public boolean isEnableCmdExceptionLogging() {
    return enableCmdExceptionLogging;
  }

  public ProcessEngineConfiguration setEnableCmdExceptionLogging(boolean enableCmdExceptionLogging) {
    this.enableCmdExceptionLogging = enableCmdExceptionLogging;
    return this;
  }

  public boolean isEnableReducedJobExceptionLogging() {
    return enableReducedJobExceptionLogging;
  }

  public ProcessEngineConfiguration setEnableReducedJobExceptionLogging(boolean enableReducedJobExceptionLogging) {
    this.enableReducedJobExceptionLogging = enableReducedJobExceptionLogging;
    return this;
  }

  public String getDeserializationAllowedClasses() {
    return deserializationAllowedClasses;
  }

  public ProcessEngineConfiguration setDeserializationAllowedClasses(String deserializationAllowedClasses) {
    this.deserializationAllowedClasses = deserializationAllowedClasses;
    return this;
  }

  public String getDeserializationAllowedPackages() {
    return deserializationAllowedPackages;
  }

  public ProcessEngineConfiguration setDeserializationAllowedPackages(String deserializationAllowedPackages) {
    this.deserializationAllowedPackages = deserializationAllowedPackages;
    return this;
  }

  public DeserializationTypeValidator getDeserializationTypeValidator() {
    return deserializationTypeValidator;
  }

  public ProcessEngineConfiguration setDeserializationTypeValidator(DeserializationTypeValidator deserializationTypeValidator) {
    this.deserializationTypeValidator = deserializationTypeValidator;
    return this;
  }

  public boolean isDeserializationTypeValidationEnabled() {
    return deserializationTypeValidationEnabled;
  }

  public ProcessEngineConfiguration setDeserializationTypeValidationEnabled(boolean deserializationTypeValidationEnabled) {
    this.deserializationTypeValidationEnabled = deserializationTypeValidationEnabled;
    return this;
  }

  public String getInstallationId() {
    return installationId;
  }

  public ProcessEngineConfiguration setInstallationId(String installationId) {
    this.installationId = installationId;
    return this;
  }

  public TelemetryRegistry getTelemetryRegistry() {
    return telemetryRegistry;
  }

  public ProcessEngineConfiguration setTelemetryRegistry(TelemetryRegistry telemetryRegistry) {
    this.telemetryRegistry = telemetryRegistry;
    return this;
  }

  public boolean isSkipOutputMappingOnCanceledActivities() {
    return skipOutputMappingOnCanceledActivities;
  }

  public void setSkipOutputMappingOnCanceledActivities(boolean skipOutputMappingOnCanceledActivities) {
    this.skipOutputMappingOnCanceledActivities = skipOutputMappingOnCanceledActivities;
  }
}
