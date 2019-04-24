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
package org.camunda.bpm.container.impl.metadata.spi;

import java.util.List;
import java.util.Map;

/**
 * <p>Deployment Metadata for the JobExecutor Service.</p>
 * 
 *  
 * @author Daniel Meyer
 *
 */
public interface JobExecutorXml {
  
  public static final String QUEUE_SIZE = "queueSize";
  
  public static final String CORE_POOL_SIZE = "corePoolSize";
  
  public static final String MAX_POOL_SIZE = "maxPoolSize";
  
  /**
   * The time in milliseconds that threads over {@link #CORE_POOL_SIZE} will be kept alive.
   */
  public static final String KEEP_ALIVE_TIME = "keepAliveTime";
  /**
   * @return a list of configured JobAcquisitions.
   */
  public List<JobAcquisitionXml> getJobAcquisitions();
  
  /**
   * @return a set of properties to configure the Job Executor.
   * 
   * @see #QUEUE_SIZE
   * @see #CORE_POOL_SIZE
   * @see #MAX_POOL_SIZE
   * @see #KEEP_ALIVE_TIME
   * 
   */
  public Map<String, String> getProperties();
}
