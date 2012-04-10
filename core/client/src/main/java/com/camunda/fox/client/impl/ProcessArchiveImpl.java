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

import java.util.Map;

import com.camunda.fox.client.impl.executor.ProcessArchiveContextExecutor;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.client.impl.util.BpmnResourceLoader;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>Implementation of the {@link ProcessArchive} SPI.</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveImpl implements ProcessArchive {

  protected final ClassLoader classLoader;
  protected final ProcessArchiveContextExecutor executor;
  private final ProcessArchiveXml processArchiveXml;
    
  public ProcessArchiveImpl(ProcessArchiveXml processArchiveXml, ProcessArchiveContextExecutor executor) {
    this.processArchiveXml = processArchiveXml;
    this.executor = executor;
    this.classLoader = getClass().getClassLoader();    
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
 
  public boolean isDeleteUponUndeploy() {
    return processArchiveXml.configuration.undeployment.delete;
  }
  
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {
    return executor.executeWithinContext(callback);
 }

  @Override
  public String getProcessEngineName() {
    return processArchiveXml.configuration.processEngineName;
  }

  @Override
  public boolean scanForProcessDefinitions() {
    return true;
  }
  
}
