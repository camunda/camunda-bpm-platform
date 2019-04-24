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

import java.util.Map;


/**
 * <p>Java API to the JobAcquisition deployment metadata</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface JobAcquisitionXml {

  public final static String LOCK_TIME_IN_MILLIS = "lockTimeInMillis";
  public final static String WAIT_TIME_IN_MILLIS = "waitTimeInMillis";
  public final static String MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";

  /**
   * @return the name of the JobExecutor.
   */
  public String getName();
  
  /**
   * @return the fully qualified classname of the JobExecutor to be used.
   */
  public String getJobExecutorClassName();

  /**
   * @return a set of properties to configure the Job Executor. The
   *         properties are mapped to bean properties of the JobExecutor
   *         class used.
   * 
   * @see #LOCK_TIME_IN_MILLIS
   * @see #WAIT_TIME_IN_MILLIS
   * @see #MAX_JOBS_PER_ACQUISITION
   * 
   */
  public Map<String, String> getProperties();

}
