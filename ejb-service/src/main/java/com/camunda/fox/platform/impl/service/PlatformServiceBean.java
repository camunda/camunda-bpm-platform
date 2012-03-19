package com.camunda.fox.platform.impl.service;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.impl.service.SimplePlatformService;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Implementation of the platform services as singleton EJB</p>
 *   
 * @author Daniel Meyer
 */
@Startup
@Singleton(name="PlatformService")
@Local({ProcessArchiveService.class, ProcessEngineService.class})
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PlatformServiceBean extends PlatformService implements ProcessEngineService, ProcessArchiveService {

  final private static Logger log = Logger.getLogger(SimplePlatformService.class.getName());

  @Asynchronous
  public Future<ProcessEngineStartOperation> startProcessEngine(final ProcessEngineConfiguration processEngineConfiguration) {
    
    processEngineRegistry.startInstallingNewProcessEngine(processEngineConfiguration);
    
    try {
      ProcessEngineController processEngineController = new ProcessEngineController(processEngineConfiguration);
      processEngineController.setProcessEngineRegistry(processEngineRegistry);
      processEngineController.start();      
      return new AsyncResult<ProcessEngineStartOperation>(new ProcessEngineStartOperationImpl(processEngineController.getProcessEngine()));
      
    } catch (Exception e) {
      log.log(Level.SEVERE,"Caught exception while tying to start process engine", e);
      return new AsyncResult<ProcessEngineStartOperation>(new ProcessEngineStartOperationImpl(e));
      
    }
        
  }

  @Override
  public void stopProcessEngine(String name) {
    ProcessEngineController processEngineController = processEngineRegistry.getProcessEngineController(name);
    if(processEngineController == null) {
      throw new FoxPlatformException("Cannot stop process engine with name '"+name+"': no such process engine found.");
    }
    processEngineController.stop();
  }

}
