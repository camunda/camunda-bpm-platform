package org.camunda.bpm.spring.boot.starter.util;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.User;
import org.camunda.commons.logging.BaseLogger;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.Set;

public class SpringBootProcessEngineLogger extends BaseLogger {
  static final String PROJECT_CODE = "STARTER";
  static final String PROJECT_ID = "SB";
  static final String PACKAGE = "org.camunda.bpm.spring.boot";

  public static final SpringBootProcessEngineLogger LOG = createLogger(SpringBootProcessEngineLogger.class, PROJECT_CODE, PACKAGE, PROJECT_ID);

  public void creatingInitialAdminUser(User adminUser) {
    logInfo("010", "creating initital Admin User: {}", adminUser);
  }

  public void skipAdminUserCreation(User existingUser) {
    logInfo("011", "skip creating initital Admin User, user does exist: {}", existingUser);
  }

  public void createInitialFilter(Filter filter) {
    logInfo("015", "create initial filter: id={} name={}", filter.getId(), filter.getName());
  }

  public void skipAutoDeployment() {
    logInfo("020", "ProcessApplication enabled: autoDeployment via springConfiguration#deploymentResourcePattern is disabled");
  }

  public void autoDeployResources(Set<Resource> resources) {
    logInfo("021", "Auto-Deploying resources: {}", resources);
  }

  public void enterLicenseKey(URL licenseKeyFile) {
    logInfo("030", "Setting up license key: {}", licenseKeyFile);
  }

  public void configureJobExecutorPool(Integer corePoolSize, Integer maxPoolSize) {
    logInfo("040", "Setting up jobExecutor with corePoolSize={}, maxPoolSize:{}", corePoolSize, maxPoolSize);
  }

  public SpringBootStarterException exceptionDuringBinding(String message) {
    return new SpringBootStarterException(exceptionMessage(
        "050", message));
  }

}
