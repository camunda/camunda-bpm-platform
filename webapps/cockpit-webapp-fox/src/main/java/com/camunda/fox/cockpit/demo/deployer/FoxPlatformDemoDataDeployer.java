package com.camunda.fox.cockpit.demo.deployer;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import com.camunda.fox.cockpit.demo.DemoDataDeployer;
import com.camunda.fox.platform.api.ProcessArchiveService;

@ApplicationScoped
@Alternative
public class FoxPlatformDemoDataDeployer implements DemoDataDeployer {
  
  // lookup the process archive service using the portable global jndi name
  @EJB(lookup=
     "java:global/" +
     "camunda-fox-platform/" +
     "process-engine/" +
     "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService")
  protected ProcessArchiveService processEngineService;
 
  // lookup the process archive context executor
  @EJB
  protected ProcessArchiveContextExecutor processArchiveContextExecutorBean;

  private DemoDataProcessArchiveImpl demoDataProcessArchiveImpl;
  
  public String deployDemoData(String processEngineName) {
    demoDataProcessArchiveImpl = new DemoDataProcessArchiveImpl(processArchiveContextExecutorBean);
    demoDataProcessArchiveImpl.setProcessEngineName(processEngineName);
    return processEngineService.installProcessArchive(demoDataProcessArchiveImpl).getProcessEngineDeploymentId();
  }
  
  public void undeployDemoData() {
    processEngineService.unInstallProcessArchive(demoDataProcessArchiveImpl);
  }
  
}
