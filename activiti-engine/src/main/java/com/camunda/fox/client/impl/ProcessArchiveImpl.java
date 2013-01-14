/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.spi.ProcessApplication;
import org.camunda.bpm.application.spi.ProcessApplicationReference;

import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.client.impl.util.BpmnResourceLoader;
import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>Implementation of the {@link ProcessArchive} SPI.</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveImpl implements ProcessArchive {

  protected final ProcessArchiveXml processArchiveXml;
  protected final Map<String, Object> properties;
  protected final ProcessApplicationReference processApplicationReference;
  private ProcessApplication processApplication;
    
  public ProcessArchiveImpl(ProcessArchiveXml processArchiveXml, URL metaFileUrl, ProcessApplicationReference processApplicationReference, ProcessApplication processApplication) {
    this.processArchiveXml = processArchiveXml;
    this.processApplicationReference = processApplicationReference;
    this.processApplication = processApplication;
    properties = new HashMap<String, Object>();
    properties.put(PROP_IS_DELETE_UPON_UNDEPLOY, processArchiveXml.configuration.undeployment.delete);
    properties.put(PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, true);
    properties.put(PROP_META_FILE_URL, metaFileUrl);
    properties.put(PROP_RESOURCE_ROOT_PATH, processArchiveXml.configuration.resourceRootPath);
    properties.put(PROP_PROCESS_APPLICATION_REFERENCE, processApplicationReference);
  }
  
  public String getName() {
    return processArchiveXml.name;
  }
  
  public Map<String, byte[]> getProcessResources() {
    BpmnResourceLoader bpmnResourceLoader = getBpmnResourceLoader();
    return bpmnResourceLoader.readBpmnResources(processArchiveXml);
  }
  
  protected BpmnResourceLoader getBpmnResourceLoader() {
    return new BpmnResourceLoader();
  }
   
  public ClassLoader getClassLoader() {
    return processApplication.getProcessApplicationClassloader();
  }

  public <T> T executeWithinContext(final ProcessArchiveCallback<T> callback) throws Exception {
    // wrap deprecated ProcessArchiveCallback as Callable
    return processApplicationReference.getProcessApplication().execute(new Callable<T>() {
      public T call() throws Exception {
        return callback.execute(); 
      }
    });
 }

  public String getProcessEngineName() {
    return processArchiveXml.configuration.processEngineName;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
  
}
