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
package org.camunda.bpm.container;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;


/**
 * @author Daniel Meyer
 *
 */
public interface ExecutorService {
  
  /**
   * <p>Passes a {@link Runnable} to the runtime container for execution. Some runtime
   * containers (like a Java EE container offer container provided
   * infrastructure for executing background work (such as a JCA WorkManager).
   * This method allows the process engine to take advantage of container
   * infrastructure for doing background work.</p>
   * 
   * @param runnable
   *          the {@link Runnable} to be executed.
   * @param isLongRunning
   *          indicates whether the runnable is a daemon.
   * @return true if the runnable could be successfully scheduled for execution.
   *         'false' otherwise.
   */
  public boolean schedule(Runnable runnable, boolean isLongRunning);
  
  /**
   * <p>Returns a runnable to be used for executing Jobs. 
   * 
   * @param jobIds
   * @param processEngine
   * @return
   */
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine);
  
}
