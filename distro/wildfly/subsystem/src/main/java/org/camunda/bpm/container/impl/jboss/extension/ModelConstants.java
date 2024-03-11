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
  String ACQUISITION_STRATEGY = "acquisition-strategy";
  String DATASOURCE = "datasource";
  String HISTORY_LEVEL = "history-level";
  String JOB_ACQUISITION = "job-acquisition";
  String JOB_ACQUISITIONS = "job-acquisitions";
  String JOB_EXECUTOR = "job-executor";
  String PROCESS_ENGINE = "process-engine";
  String PROCESS_ENGINES = "process-engines";
  String PROPERTY = "property";
  String PROPERTIES = "properties";
  String CONFIGURATION = "configuration";

  String PLUGINS = "plugins";
  String PLUGIN = "plugin";
  String PLUGIN_CLASS = "class";

  // attributes
  String DEFAULT = "default";
  String NAME = "name";
  String THREAD_POOL_NAME = "thread-pool-name";
  String MAX_THREADS = "max-threads";
  String CORE_THREADS = "core-threads";
  String QUEUE_LENGTH = "queue-length";
  String ALLOW_CORE_TIMEOUT = "allow-core-timeout";
  String KEEPALIVE_TIME = "keepalive-time";

  /** The name of our subsystem within the model. */
  String SUBSYSTEM_NAME = "camunda-bpm-platform";
}
