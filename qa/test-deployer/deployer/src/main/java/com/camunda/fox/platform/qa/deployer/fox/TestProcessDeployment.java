package com.camunda.fox.platform.qa.deployer.fox;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.camunda.fox.platform.api.ProcessArchiveService;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class TestProcessDeployment {
  
  private static Logger logger = Logger.getLogger(TestProcessDeployment.class.getName());
  
  private ProcessArchiveService processArchiveService;
  
  private boolean installed = false;
  
  private final ProcessArchiveImpl processArchive;
  
  public TestProcessDeployment(ProcessArchiveImpl processArchive, ProcessArchiveService processArchiveService) {
    this.processArchive = processArchive;
    this.processArchiveService = processArchiveService;
  }
  
  public void deploy() {
    try {
      processArchiveService.installProcessArchive(processArchive);
      installed = true;
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Failed to install process archive", e);
      throw e;
    }
  }

  public void undeploy() {
    if (installed) {
      try {
        processArchiveService.unInstallProcessArchive(processArchive);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Failed to uninstall process archive", e);
        throw e;
      }
    } else {
      logger.log(Level.WARNING, "Skipping uninstall process archive as it was not installed");
    }
  }
}
