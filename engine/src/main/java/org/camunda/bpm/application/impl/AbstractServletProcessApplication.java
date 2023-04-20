/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationReference;

public class AbstractServletProcessApplication extends AbstractProcessApplication {

  protected String servletContextName;
  protected String servletContextPath;

  protected ProcessApplicationReferenceImpl reference;

  protected ClassLoader processApplicationClassloader;


  @Override
  protected String autodetectProcessApplicationName() {
    String name = (servletContextName != null && !servletContextName.isEmpty()) ? servletContextName : servletContextPath;
    if(name.startsWith("/")) {
      name = name.substring(1);
    }
    return name;
  }

  @Override
  public ProcessApplicationReference getReference() {
     if(reference == null) {
       reference = new ProcessApplicationReferenceImpl(this);
     }
     return reference;
  }

  @Override
  public ClassLoader getProcessApplicationClassloader() {
    return processApplicationClassloader;
  }

  @Override
  public Map<String, String> getProperties() {
    Map<String, String> properties = new HashMap<String, String>();

    // set the servlet context path as property
    properties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, servletContextPath);

    return properties;
  }

}