package org.camunda.bpm.container.impl.threading.ra.util;

import java.util.logging.Logger;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.JcaExecutorServiceConnector;
import org.camunda.bpm.container.impl.threading.ra.JcaWorkManagerExecutorService;
import org.camunda.bpm.container.impl.threading.ra.commonj.CommonJWorkManagerExecutorService;


/**
 * Tries to auto-detect if {@link commonj.work.WorkManager} is present on the classpath.
 * If so, it returns the commonJ implementation {@link CommonJWorkManagerExecutorService}. 
 * Otherwise the JCA implementation -> {@link JcaWorkManagerExecutorService} will be returned.
 * 
 * @author christian.lipphardt@camunda.com
 */
public class AutodetectWorkManagerExecutorService {

  private static Logger log = Logger.getLogger(AutodetectWorkManagerExecutorService.class.getName());
  
  private static final String COMMONJ_WORKMANAGER_CLASS_NAME = "commonj.work.WorkManager";
  
  private static final String JCA_WORKMANAGER_CLASS_NAME = "javax.resource.spi.work.WorkManager";
  
  private static final boolean commonjWorkManagerPresent = 
          DetectClassUtils.isPresent(COMMONJ_WORKMANAGER_CLASS_NAME, AutodetectWorkManagerExecutorService.class.getClassLoader());
  
  private static final boolean jcaWorkManagerPresent = 
          DetectClassUtils.isPresent(JCA_WORKMANAGER_CLASS_NAME, AutodetectWorkManagerExecutorService.class.getClassLoader());
  
  public static ExecutorService getExecutorService(JcaExecutorServiceConnector ra) {
    if (commonjWorkManagerPresent) {
      log.info("Auto-detected CommonJ WorkManager class. Using " + CommonJWorkManagerExecutorService.class.getName());
      return new CommonJWorkManagerExecutorService(ra);
      
    } else if (jcaWorkManagerPresent) {
      log.info("Auto-detected JCA WorkManager class. Using " + JcaWorkManagerExecutorService.class.getName());
      return new JcaWorkManagerExecutorService(ra);
      
    } else {
      // unable to detect any usable WorkManager
      throw new RuntimeException("Error while starting JobExecutor: could not detect commonJ WorkManager or any JCA WorkManager implementation on classpath.");
    }
  }

}
