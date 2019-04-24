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
package org.camunda.bpm.container.impl.jmx.services;

/**
 * <p>MBean interface exposing management properties of the jobExecutor through JMX.
 * This MBean also allows to {@link #start()} and {@link #shutdown()} the Job Executor.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface JmxManagedJobExecutorMBean {

  public String getName();

  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition);

  public int getMaxJobsPerAcquisition();

  public void setLockOwner(String lockOwner);

  public String getLockOwner();

  public void setLockTimeInMillis(int lockTimeInMillis);

  public int getLockTimeInMillis();

  public void setWaitTimeInMillis(int waitTimeInMillis);

  public int getWaitTimeInMillis();

  public void shutdown();

  public void start();

  public boolean isActive();

}
