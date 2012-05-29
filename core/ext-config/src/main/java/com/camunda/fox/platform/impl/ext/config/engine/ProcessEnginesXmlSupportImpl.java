package com.camunda.fox.platform.impl.ext.config.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlParser;
import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlSupport;
import com.camunda.fox.platform.impl.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEnginesXmlSupportImpl implements ProcessEnginesXmlSupport {
  
  private static final String META_INF_PROCESS_ENGINES_XML = "META-INF/process-engines.xml";

  private Logger log = Logger.getLogger(ProcessEnginesXmlSupportImpl.class.getName());
  
  /** process engines by filename */
  private Map<String, List<ProcessEngine>> processEngines = new HashMap<String, List<ProcessEngine>>();
  
  public void startProcessEngines(ProcessEngineService processEngineService) {    
    List<ProcessEnginesXml> parsedProcessEnginesXml = parseConfigurationFiles();
    for (ProcessEnginesXml processEnginesXml : parsedProcessEnginesXml) {
      try {
        startProcessEngines(processEnginesXml, processEngineService);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while staring process engines defined in file '"+processEnginesXml.resourceName+"'.");
        stopProcessEngines(processEngineService);
        throw new FoxPlatformException("Exception while starting process engines defined in file '"+processEnginesXml.resourceName+"'", e);
      }
    }
  }

  protected void startProcessEngines(ProcessEnginesXml processEnginesXml, ProcessEngineService processEngineService) {
    List<Future<ProcessEngineStartOperation>> startingEngines = new ArrayList<Future<ProcessEngineService.ProcessEngineStartOperation>>();
    List<ProcessEngine> startedEngines = new ArrayList<ProcessEngine>();
    
    log.info("Found '"+processEnginesXml.processEngines.size()+"' process engine definitions in file "+processEnginesXml.resourceName);
    
    // first start all engines asynchronously
    for (ProcessEngineConfiguration processEngineConfiguration : processEnginesXml.processEngines) {
      Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
      startingEngines.add(startProcessEngine);
    }

    boolean allEnginesStarted = true;
        
    // wait for all process engines to start sucessfully
    for (Future<ProcessEngineStartOperation> startingEngine : startingEngines) {
      try {
        ProcessEngineStartOperation engineStart = startingEngine.get();
        if(!engineStart.wasSuccessful()) {
          allEnginesStarted = false;
          break;
        } else  {
          ProcessEngine processenEngine = engineStart.getProcessenEngine();
          startedEngines.add(processenEngine);
        }
      } catch (InterruptedException e) {
        throw new FoxPlatformException("InterruptedException while waiting for process engine to start", e);
      } catch (ExecutionException e) {
        throw new FoxPlatformException("ExecutionException while waiting for process engine to start", e);
      }      
    }
    
    // at least one engine failed  to start: stop engines that did start sucessfully:
    if(!allEnginesStarted) {
      for (Future<ProcessEngineStartOperation> startingEngine : startingEngines) {
        try {
          ProcessEngineStartOperation engineStart = startingEngine.get();
          if(engineStart.wasSuccessful()) {
            try {
              processEngineService.stopProcessEngine(engineStart.getProcessenEngine());
            }catch (Exception e) {
              log.log(Level.SEVERE, "exception while stopping process engine", e);
            }
          }  else {
            log.log(Level.SEVERE, "exception while starting process engine", engineStart.getException());
          }
        } catch (InterruptedException e) {
          throw new FoxPlatformException("InterruptedException while waiting for process engine to start", e);
        } catch (ExecutionException e) {
          throw new FoxPlatformException("ExecutionException while waiting for process engine to start", e);
        }
      }
      // then trow exception:
      throw new FoxPlatformException("Could not start all configured process engines, see logs for details.");
    } else { 
      log.info(startingEngines.size() +" process engines started sucessfully.");
      this.processEngines.put(processEnginesXml.resourceName, startedEngines);
    }
  }
  
  public void stopProcessEngines(ProcessEngineService processEngineService) {
    List<String> enginesFailedToStop = new ArrayList<String>();
    for (Entry<String, List<ProcessEngine>> enginesByFilename : processEngines.entrySet()) {
      String filename = enginesByFilename.getKey();
      List<ProcessEngine> processEngines = enginesByFilename.getValue();
      log.info("Stopping process engines defined in '"+filename+"'");
      for (ProcessEngine processEngine : processEngines) {
        try {
          processEngineService.stopProcessEngine(processEngine);
        }catch (Exception e) {
          log.log(Level.SEVERE, "Exception while stopping process engine with name '"+processEngine.getName()+"' defined in file '"+filename);
          enginesFailedToStop.add(processEngine.getName());
        }
      }
    }
    if(enginesFailedToStop.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      errorMessage.append("The following process engines failed to stop and might still be running:");
      errorMessage.append("\n");
      for (String engineName : enginesFailedToStop) {
        errorMessage.append("   ");
        errorMessage.append(engineName);
      }
      errorMessage.append("Consider restarting the fox platform.");
      log.severe(errorMessage.toString());      
    }
  }

  protected List<ProcessEnginesXml> parseConfigurationFiles() {
    ProcessEnginesXmlParser parser = getParser();    
    String processEnginesXmlLocation = getProcessEnginesXmlLocation();
    return parser.parseProcessEnginesXml(processEnginesXmlLocation);
  }
  
  protected ProcessEnginesXmlParser getParser() {
    return ServiceLoaderUtil.loadService(ProcessEnginesXmlParser.class, ProcessEnginesXmlParserImpl.class);
  }
  
  protected String getProcessEnginesXmlLocation() {
    return META_INF_PROCESS_ENGINES_XML;
  }

}
