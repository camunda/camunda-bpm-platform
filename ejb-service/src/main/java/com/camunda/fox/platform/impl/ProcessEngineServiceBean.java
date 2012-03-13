package com.camunda.fox.platform.impl;


import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.configuration.JtaCmpeProcessEngineConfiguration;

/**
 * <p>Implementation of the {@link ProcessArchiveService} and {@link ProcessEngineService} 
 * interfaces as EJB 3.1 singleton bean.</p>
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
@Startup
@Singleton(name="ProcessEngineService")
@Local({ProcessArchiveService.class, ProcessEngineService.class})
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProcessEngineServiceBean extends AbstractProcessEngineService {

  /** common locations for the JTA transaction manger in JNDI*/
  public static String[] TRANSACTION_MANAGER_LOCATIONS = {
    "java:/TransactionManager", // jboss 6/7
    "java:appserver/TransactionManager", // glassfish 3.1
    "java:jboss/TransactionManager",  // jboss 7
  };
    
  // configuration ///////////////////////////////////
    
  /**
   * @param datasourceJndiName the datasourceJndiName to set
   */
  @Resource
  @Override
  public void setDatasourceJndiName(String datasourceJndiName) {
    super.setDatasourceJndiName(datasourceJndiName);
  }

  /**
   * @param isAutoUpdateSchema the isAutoUpdateSchema to set
   */
  @Resource
  @Override
  public void setAutoUpdateSchema(boolean autoUpdateSchema) {
    super.setAutoUpdateSchema(autoUpdateSchema);
  }
  
  /**
   * @param activateJobExecutor the activateJobExecutor to set
   */
  @Resource
  @Override
  public void setActivateJobExecutor(boolean activateJobExecutor) {
    super.setActivateJobExecutor(activateJobExecutor);
  }
  
  /**
   * @param history the history to set
   */
  @Resource
  @Override
  public void setHistory(String history) {
    super.setHistory(history);
  }

  /**
   * @param jobExecutor_maxJobsPerAcquisition the jobExecutor_maxJobsPerAcquisition to set
   */
  @Resource
  @Override
  public void setJobExecutor_maxJobsPerAcquisition(int jobExecutor_maxJobsPerAcquisition) {
    super.setJobExecutor_maxJobsPerAcquisition(jobExecutor_maxJobsPerAcquisition);
  }

  /**
   * @return the jobExecutor_waitTimeInMillis
   */
  public int getJobExecutor_waitTimeInMillis() {
    return jobExecutor_waitTimeInMillis;
  }

  /**
   * @param jobExecutor_waitTimeInMillis the jobExecutor_waitTimeInMillis to set
   */
  @Resource
  @Override
  public void setJobExecutor_waitTimeInMillis(int jobExecutor_waitTimeInMillis) {
    super.setJobExecutor_waitTimeInMillis(jobExecutor_waitTimeInMillis);
  }

  /**
   * @param jobExecutor_lockTimeInMillis the jobExecutor_lockTimeInMillis to set
   */
  @Resource
  @Override
  public void setJobExecutor_lockTimeInMillis(int jobExecutor_lockTimeInMillis) {
    super.setJobExecutor_lockTimeInMillis(jobExecutor_lockTimeInMillis);
  }
  
  // lifecycle //////////////////////////////////////

  @PostConstruct
  @Override
  public void start() {
    super.start();
  }

  @PreDestroy
  @Override
  public void stop() {
    super.stop();
  }

  @Override
  protected void initProcessEngineConfiguration() {
    super.initProcessEngineConfiguration();
    setTransactionManagerLookup();    
  }
  
  /* NOTE: we need to do this at runtime since websphere does not expose a 
   * javax.transaction.TransactionManager. **/
  protected void setTransactionManagerLookup() {
    if (processEngineConfiguration instanceof JtaCmpeProcessEngineConfiguration) {
      String lookup = null;
      for (String txManagerName : TRANSACTION_MANAGER_LOCATIONS) {
        try {
          InitialContext.doLookup(txManagerName);
          lookup = txManagerName;
          break;
        } catch (NamingException e) {
          // ignore
        }
      }
      if (lookup == null) {
        throw new FoxPlatformException("Could not lookup a transaction manager using one of the provided names: "
                + Arrays.toString(TRANSACTION_MANAGER_LOCATIONS));
      } else {
        ((JtaCmpeProcessEngineConfiguration) processEngineConfiguration).setTransactionManagerLookup(lookup);
      }
    }
  }
  
}
