package com.camunda.fox.platform.impl.ext.config.engine.spi;

import com.camunda.fox.platform.api.ProcessEngineService;

/**
 * Class responsible for
 * <ul>
 * <li>loading and parsing all META-INF/process-engines.xml files</li>
 * <li>starting and stopping the process engines defined there</li>
 * </ul>
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessEnginesXmlSupport {

  /** 
   * <p>Process engines are started asynchronouly. This method blocks until either 
   * <ul>
   * <li>all process engines have started sucessfully</li>
   * <li>if an engine fails to start, all other process engines are stopped</li> 
   * </ul>
   * 
   * @param processEnginesXml
   * @param processEngineService
   */
  public abstract void startProcessEngines(ProcessEngineService processEngineService);

  public abstract void stopProcessEngines(ProcessEngineService processEngineService);

}