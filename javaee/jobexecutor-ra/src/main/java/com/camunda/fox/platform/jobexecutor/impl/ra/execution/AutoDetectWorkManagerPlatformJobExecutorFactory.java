package com.camunda.fox.platform.jobexecutor.impl.ra.execution;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.util.ClassUtils;

import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.commonj.CommonjWorkManagerPlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi.PlatformJobExecutorFactory;

/**
 * Tries to auto-detect if {@link commonj.work.WorkManager} is present on the classpath.
 * If so, it returns the commonJ implementation of {@link PlatformJobExecutorFactory}, {@link CommonjWorkManagerPlatformJobExecutor}. 
 * Otherwise the JCA implementation -> {@link JcaWorkManagerPlatformJobExecutor} will be returned.
 * 
 * @author christian.lipphardt@camunda.com
 */
public class AutoDetectWorkManagerPlatformJobExecutorFactory implements PlatformJobExecutorFactory {

  private static Logger log = Logger.getLogger(AutoDetectWorkManagerPlatformJobExecutorFactory.class.getName());
  
  private static final String COMMONJ_WORKMANAGER_CLASS_NAME = "commonj.work.WorkManager";
  
  private static final String JCA_WORKMANAGER_CLASS_NAME = "javax.resource.spi.work.WorkManager";
  
  private static final boolean commonjWorkManagerPresent = 
          ClassUtils.isPresent(COMMONJ_WORKMANAGER_CLASS_NAME, AutoDetectWorkManagerPlatformJobExecutorFactory.class.getClassLoader());
  
  private static final boolean jcaWorkManagerPresent = 
          ClassUtils.isPresent(JCA_WORKMANAGER_CLASS_NAME, AutoDetectWorkManagerPlatformJobExecutorFactory.class.getClassLoader());
  
  @Override
  public PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector) {
    if (commonjWorkManagerPresent) {
      log.info("Auto-detected CommonJ WorkManager class. Using " + CommonjWorkManagerPlatformJobExecutor.class.getName());
      return new CommonjWorkManagerPlatformJobExecutor(platformJobExecutorConnector);
    } else if (jcaWorkManagerPresent) {
      log.info("Auto-detected JCA WorkManager class. Using " + JcaWorkManagerPlatformJobExecutor.class.getName());
      return new JcaWorkManagerPlatformJobExecutor(platformJobExecutorConnector.getWorkManager(), platformJobExecutorConnector);
    } else {
      // unable to detect any usable WorkManager
      throw new RuntimeException("Error while starting JobExecutor: could not detect commonJ WorkManager or any JCA WorkManager implementation on classpath.");
    }
  }

}
