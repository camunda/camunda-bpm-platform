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
package org.camunda.bpm.container.impl.threading.ra.outbound;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;


/**
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaExecutorServiceConnectionImpl implements JcaExecutorServiceConnection {

  protected JcaExecutorServiceManagedConnection mc;
  protected JcaExecutorServiceManagedConnectionFactory mcf;
  
  public JcaExecutorServiceConnectionImpl() {
  }
  
  public JcaExecutorServiceConnectionImpl(JcaExecutorServiceManagedConnection mc, JcaExecutorServiceManagedConnectionFactory mcf) {
    this.mc = mc;
    this.mcf = mcf;
  }

  public void closeConnection() {
    mc.closeHandle(this);
  }

  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    return mc.schedule(runnable, isLongRunning);
  }

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return mc.getExecuteJobsRunnable(jobIds, processEngine);
  }
 


}
