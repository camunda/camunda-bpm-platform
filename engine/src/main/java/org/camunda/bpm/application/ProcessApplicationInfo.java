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
package org.camunda.bpm.application;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.ProcessApplicationService;

/**
 * <p>Object holding information about a deployed Process Application</p>
 * 
 * @author Daniel Meyer
 * 
 * @see ProcessApplicationService#getProcessApplicationInfo(String)
 *
 */
public interface ProcessApplicationInfo {
  
  /** constant for the servlet context path property */
  public final static String PROP_SERVLET_CONTEXT_PATH = "servletContextPath";  

  /**
   * @return the name of the process application
   */
  public String getName();

  /**
   * @return a list of {@link ProcessApplicationDeploymentInfo} objects that
   *         provide information about the deployments made by the process
   *         application to the process engine(s).
   */
  public List<ProcessApplicationDeploymentInfo> getDeploymentInfo();
  
  /**
   * <p>Provides access to a list of process application-provided properties.</p>
   * 
   * <p>This class provides a set of constants for commonly-used properties</p>
   * 
   * @see ProcessApplicationInfo#PROP_SERVLET_CONTEXT_PATH
   */
  public Map<String, String> getProperties();
  

}
