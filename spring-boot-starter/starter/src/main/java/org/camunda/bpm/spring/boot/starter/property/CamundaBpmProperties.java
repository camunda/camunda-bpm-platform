package org.camunda.bpm.spring.boot.starter.property;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.spring.boot.starter.configuration.id.IdGeneratorConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

@ConfigurationProperties(CamundaBpmProperties.PREFIX)
public class CamundaBpmProperties {

  public static final String PREFIX = "camunda.bpm";

  public static final String[] DEFAULT_BPMN_RESOURCE_SUFFIXES = new String[]{"bpmn20.xml", "bpmn" };
  public static final String[] DEFAULT_CMMN_RESOURCE_SUFFIXES = new String[]{"cmmn11.xml", "cmmn10.xml", "cmmn" };
  public static final String[] DEFAULT_DMN_RESOURCE_SUFFIXES = new String[]{"dmn11.xml", "dmn" };

  static String[] initDeploymentResourcePattern() {
    final Set<String> suffixes = new HashSet<String>();
    suffixes.addAll(Arrays.asList(DEFAULT_DMN_RESOURCE_SUFFIXES));
    suffixes.addAll(Arrays.asList(DEFAULT_BPMN_RESOURCE_SUFFIXES));
    suffixes.addAll(Arrays.asList(DEFAULT_CMMN_RESOURCE_SUFFIXES));

    final Set<String> patterns = new HashSet<String>();
    for (String suffix : suffixes) {
      patterns.add(String.format("%s**/*.%s", CLASSPATH_ALL_URL_PREFIX, suffix));
    }

    return patterns.toArray(new String[patterns.size()]);
  }

  static StringJoiner joinOn(final Class<?> clazz) {
    return new StringJoiner(", ", clazz.getSimpleName() + "[", "]");
  }

  /**
   * name of the process engine
   */
  private String processEngineName = ProcessEngines.NAME_DEFAULT;

  private String idGenerator = IdGeneratorConfiguration.STRONG;

  /**
   * the history level to use
   */
  private String historyLevel = ProcessEngineConfiguration.HISTORY_FULL;

  /**
   * the default history level to use when 'historyLevel' is 'auto'
   */
  private String historyLevelDefault = ProcessEngineConfiguration.HISTORY_FULL;

  /**
   * enables auto deployment of processes
   */
  private boolean autoDeploymentEnabled = true;

  /**
   * resource pattern for locating process sources
   */
  private String[] deploymentResourcePattern = initDeploymentResourcePattern();

  /**
   * default serialization format to use
   */
  private String defaultSerializationFormat = Defaults.INSTANCE.getDefaultSerializationFormat();

  private URL licenseFile;

  /**
   * decativate camunda auto configuration
   */
  private boolean enabled = true;

  /**
   * metrics configuration
   */
  @NestedConfigurationProperty
  private MetricsProperty metrics = new MetricsProperty();

  /**
   * database configuration
   */
  @NestedConfigurationProperty
  private DatabaseProperty database = new DatabaseProperty();

  /**
   * JPA configuration
   */
  @NestedConfigurationProperty
  private JpaProperty jpa = new JpaProperty();

  /**
   * job execution configuration
   */
  @NestedConfigurationProperty
  private JobExecutionProperty jobExecution = new JobExecutionProperty();

  /**
   * webapp configuration
   */
  @NestedConfigurationProperty
  private WebappProperty webapp = new WebappProperty();

  /**
   * process application/processes.xml configuration
   */
  @NestedConfigurationProperty
  private ApplicationProperty application = new ApplicationProperty();

  @NestedConfigurationProperty
  private AuthorizationProperty authorization = new AuthorizationProperty();

  @NestedConfigurationProperty
  private GenericProperties genericProperties = new GenericProperties();

  @NestedConfigurationProperty
  private AdminUserProperty adminUser = new AdminUserProperty();

  @NestedConfigurationProperty
  private FilterProperty filter = new FilterProperty();

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public String getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(String historyLevel) {
    this.historyLevel = historyLevel;
  }

  public String getHistoryLevelDefault() {
    return historyLevelDefault;
  }

  public void setHistoryLevelDefault(String historyLevelDefault) {
    this.historyLevelDefault = historyLevelDefault;
  }

  public boolean isAutoDeploymentEnabled() {
    return autoDeploymentEnabled;
  }

  public void setAutoDeploymentEnabled(boolean autoDeploymentEnabled) {
    this.autoDeploymentEnabled = autoDeploymentEnabled;
  }

  public String[] getDeploymentResourcePattern() {
    return deploymentResourcePattern;
  }

  public void setDeploymentResourcePattern(String[] deploymentResourcePattern) {
    this.deploymentResourcePattern = deploymentResourcePattern;
  }

  public String getDefaultSerializationFormat() {
    return defaultSerializationFormat;
  }

  public void setDefaultSerializationFormat(String defaultSerializationFormat) {
    this.defaultSerializationFormat = defaultSerializationFormat;
  }

  public URL getLicenseFile() {
    return licenseFile;
  }

  public void setLicenseFile(URL licenseFile) {
    this.licenseFile = licenseFile;
  }

  public MetricsProperty getMetrics() {
    return metrics;
  }

  public void setMetrics(MetricsProperty metrics) {
    this.metrics = metrics;
  }

  public DatabaseProperty getDatabase() {
    return database;
  }

  public void setDatabase(DatabaseProperty database) {
    this.database = database;
  }

  public JpaProperty getJpa() {
    return jpa;
  }

  public void setJpa(JpaProperty jpa) {
    this.jpa = jpa;
  }

  public JobExecutionProperty getJobExecution() {
    return jobExecution;
  }

  public void setJobExecution(JobExecutionProperty jobExecution) {
    this.jobExecution = jobExecution;
  }

  public WebappProperty getWebapp() {
    return webapp;
  }

  public void setWebapp(WebappProperty webapp) {
    this.webapp = webapp;
  }

  public ApplicationProperty getApplication() {
    return application;
  }

  public void setApplication(ApplicationProperty application) {
    this.application = application;
  }

  public AuthorizationProperty getAuthorization() {
    return authorization;
  }

  public void setAuthorization(AuthorizationProperty authorization) {
    this.authorization = authorization;
  }

  public GenericProperties getGenericProperties() {
    return genericProperties;
  }

  public void setGenericProperties(GenericProperties genericProperties) {
    this.genericProperties = genericProperties;
  }

  public AdminUserProperty getAdminUser() {
    return adminUser;
  }

  public void setAdminUser(AdminUserProperty adminUser) {
    this.adminUser = adminUser;
  }

  public FilterProperty getFilter() {
    return filter;
  }

  public void setFilter(FilterProperty filter) {
    this.filter = filter;
  }

  public String getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(String idGenerator) {
    this.idGenerator = idGenerator;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("processEngineName=" + processEngineName)
      .add("historyLevel=" + historyLevel)
      .add("historyLevelDefault=" + historyLevelDefault)
      .add("autoDeploymentEnabled=" + autoDeploymentEnabled)
      .add("deploymentResourcePattern=" + Arrays.toString(deploymentResourcePattern))
      .add("defaultSerializationFormat=" + defaultSerializationFormat)
      .add("licenseFile=" + licenseFile)
      .add("metrics=" + metrics)
      .add("database=" + database)
      .add("jpa=" + jpa)
      .add("jobExecution=" + jobExecution)
      .add("webapp=" + webapp)
      .add("application=" + application)
      .add("authorization=" + authorization)
      .add("genericProperties=" + genericProperties)
      .add("adminUser=" + adminUser)
      .add("filter=" + filter)
      .add("idGenerator=" + idGenerator)
      .toString();
  }

}
