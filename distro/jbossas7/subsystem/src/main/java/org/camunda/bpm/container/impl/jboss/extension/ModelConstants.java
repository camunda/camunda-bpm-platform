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
package org.camunda.bpm.container.impl.jboss.extension;


/**
 * Constants used in the model
 * 
 * @author Daniel Meyer
 * @author christian.lipphardt@camunda.com
 */
public interface ModelConstants {
  
  // elements
  @Deprecated
  public final static String ACQUISITION_STRATEGY = "acquisition-strategy";
  
  public final static String DATASOURCE = "datasource";
  public final static String HISTORY_LEVEL = "history-level";
  public final static String JOB_ACQUISITION = "job-acquisition";
  public final static String JOB_ACQUISITIONS = "job-acquisitions";
  public final static String JOB_EXECUTOR = "job-executor";
  public final static String PROCESS_ENGINE = "process-engine";
  public final static String PROCESS_ENGINES = "process-engines";
  public final static String PROPERTY = "property";
  public final static String PROPERTIES = "properties";
  public final static String CONFIGURATION = "configuration";

  public final static String PLUGINS = "plugins";
  public final static String PLUGIN = "plugin";
  public final static String PLUGIN_CLASS = "class";
  
  // attributes
  public final static String DEFAULT = "default";
  public final static String NAME = "name";
  public final static String THREAD_POOL_NAME = "thread-pool-name";
  /** The name of our subsystem within the model. */
  public static final String SUBSYSTEM_NAME = "camunda-bpm-platform";
  
}
