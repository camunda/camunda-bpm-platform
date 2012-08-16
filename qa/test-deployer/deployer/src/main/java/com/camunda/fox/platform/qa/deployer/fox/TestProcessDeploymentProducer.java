package com.camunda.fox.platform.qa.deployer.fox;

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.qa.deployer.event.AfterFoxTest;
import com.camunda.fox.platform.qa.deployer.event.BeforeFoxTest;
import com.camunda.fox.platform.qa.deployer.metadata.MetadataProvider;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import java.util.Set;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

import com.camunda.fox.platform.qa.deployer.configuration.FoxDeploymentConfiguration;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class TestProcessDeploymentProducer {
  
  @Inject
  @TestScoped
  private InstanceProducer<TestProcessDeployment> testProcessDeployment;
  
  @Inject
  private Instance<ApplicationArchiveContext> applicationArchiveContext;
  
  @Inject
  private Instance<ProcessArchiveService> processArchiveService;
  
  @Inject
  private Instance<MetadataProvider> metadataProvider;
  
  @Inject
  private Instance<FoxDeploymentConfiguration> foxConfiguration;
  
  public void beforeTest(@Observes BeforeFoxTest event) {
    Set<String> processes = metadataProvider.get().getProcessDeploymentResources();
    
    ApplicationArchiveContext context = applicationArchiveContext.get();
    ProcessArchiveService service = processArchiveService.get();
    
    FoxDeploymentConfiguration config = foxConfiguration.get();
    
    TestProcessDeployment deployment = new TestProcessDeployment(
      new ProcessArchiveImpl(context, processes, config.getProcessEngineName()),
      service);
    
    testProcessDeployment.set(deployment);
  }
  
  public void afterTest(@Observes AfterFoxTest event) {
    
  }
}
