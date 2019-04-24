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
package org.camunda.bpm.container.impl.metadata;

/**
 * <p>Collection of constant string values used by the parsers.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DeploymentMetadataConstants {

  public static final String NAME = "name";
  public static final String TENANT_ID = "tenantId";
  public static final String DEFAULT = "default";
  public static final String PROPERTIES = "properties";
  public static final String PROPERTY = "property";

  public static final String PROCESS_APPLICATION = "process-application";

  public static final String JOB_EXECUTOR = "job-executor";
  public static final String JOB_ACQUISITION = "job-acquisition";
  public static final String JOB_EXECUTOR_CLASS_NAME = "job-executor-class";

  public static final String PROCESS_ENGINE = "process-engine";
  public static final String CONFIGURATION = "configuration";
  public static final String DATASOURCE = "datasource";
  public static final String PLUGINS = "plugins";
  public static final String PLUGIN = "plugin";
  public static final String PLUGIN_CLASS = "class";

  public static final String PROCESS_ARCHIVE = "process-archive";
  // deprecated since 7.2.0 (use resource instead)
  public static final String PROCESS = "process";
  public static final String RESOURCE = "resource";

}
