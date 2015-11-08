/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.services;

import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;

/**
 * @author Daniel Meyer
 *
 */
public class JmxManagedJobExecutor implements PlatformService<JobExecutor>, JmxManagedJobExecutorMBean {

  protected final JobExecutor jobExecutor;

  public JmxManagedJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public void start(PlatformServiceContainer mBeanServiceContainer) {
    // no-op:
    // job executor is lazy-started when first process engine is registered and jobExecutorActivate = true
    // See: #CAM-4817
  }

  public void stop(PlatformServiceContainer mBeanServiceContainer) {
    shutdown();
  }

  public void start() {
    jobExecutor.start();
  }

  public void shutdown() {
    jobExecutor.shutdown();
  }

  public int getWaitTimeInMillis() {
    return jobExecutor.getWaitTimeInMillis();
  }

  public void setWaitTimeInMillis(int waitTimeInMillis) {
    jobExecutor.setWaitTimeInMillis(waitTimeInMillis);
  }

  public int getLockTimeInMillis() {
    return jobExecutor.getLockTimeInMillis();
  }

  public void setLockTimeInMillis(int lockTimeInMillis) {
    jobExecutor.setLockTimeInMillis(lockTimeInMillis);
  }

  public String getLockOwner() {
    return jobExecutor.getLockOwner();
  }

  public void setLockOwner(String lockOwner) {
    jobExecutor.setLockOwner(lockOwner);
  }

  public int getMaxJobsPerAcquisition() {
    return jobExecutor.getMaxJobsPerAcquisition();
  }

  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    jobExecutor.setMaxJobsPerAcquisition(maxJobsPerAcquisition);
  }

  public String getName() {
    return jobExecutor.getName();
  }

  public JobExecutor getValue() {
    return jobExecutor;
  }

  public boolean isActive() {
    return jobExecutor.isActive();
  }
}
