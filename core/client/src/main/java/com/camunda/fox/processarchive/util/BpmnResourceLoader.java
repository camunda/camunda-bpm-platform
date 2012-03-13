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
package com.camunda.fox.processarchive.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.processarchive.schema.ProcessesXml;

/**
 * 
 * @author Daniel Meyer
 */
public class BpmnResourceLoader {

  public Map<String, byte[]> readBpmnResources(ProcessesXml processesXml) {
    HashMap<String, byte[]> processesMap = new HashMap<String, byte[]>();
        
    for (ProcessesXml.Process process : processesXml.processes) {
      String resourceName = process.resourceName;
      InputStream processAsInputStream = getProcessAsInputStream(resourceName);
      byte[] bs = IoUtil.readInputStream(processAsInputStream, resourceName);
      IoUtil.closeSilently(processAsInputStream);
      processesMap.put(resourceName, bs);
    }
    
    return processesMap;
  }

  protected InputStream getProcessAsInputStream(String resourceName) {
    return getClass().getClassLoader().getResourceAsStream(resourceName);
  }

}
