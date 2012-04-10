package com.camunda.fox.platform.impl.service;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>EJB representing a process engine configuration</p>
 * 
 * <p>This bean self-installs using the {@link ProcessEngineService}</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineConfigurationBean implements ProcessEngineConfiguration {

  // state ////////////////////////////////////

  private boolean isActivateJobExcutor;
  private boolean isAutoSchemaUpdate;
  private String historyLevel;
  private boolean isDefault;
  private String processEngineName;
  private String datasourceJndiName;

  // //////////////////////////////////////////

  @EJB
  private ProcessEngineService processEngineService;

  // lifecycle //////////////////////////////

  @PostConstruct
  protected void install() {
    try {
      // deployment should fail if the process engine fails to start.
      ProcessEngineStartOperation processEngineStartOperation = processEngineService.startProcessEngine(this).get();
      if(!processEngineStartOperation.wasSuccessful()) {
        throw new FoxPlatformException("Exception while starting process engine: ", processEngineStartOperation.getException());  
      }
    } catch (Exception e) {     
      throw new FoxPlatformException("Exception while starting process engine: ", e);
    }
  }

  @PreDestroy
  protected void uninstall() {
    processEngineService.stopProcessEngine(processEngineName);
  }

  // ProcessEngineConfiguration implementation

  public boolean isActivateJobExcutor() {
    return isActivateJobExcutor;
  }

  public boolean isAutoSchemaUpdate() {
    return isAutoSchemaUpdate;
  }

  public String getHistoryLevel() {
    return historyLevel;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public String getDatasourceJndiName() {
    return datasourceJndiName;
  }
  
  // getters / setters ///////////////////////////

  public void setActivateJobExcutor(boolean isActivateJobExcutor) {
    this.isActivateJobExcutor = isActivateJobExcutor;
  }

  public void setAutoSchemaUpdate(boolean isAutoSchemaUpdate) {
    this.isAutoSchemaUpdate = isAutoSchemaUpdate;
  }

  public void setHistoryLevel(String historyLevel) {
    this.historyLevel = historyLevel;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public void setDatasourceJndiName(String datasourceJndiName) {
    this.datasourceJndiName = datasourceJndiName;
  }

}
