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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.util.CollectionUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionContext {

  protected Map<String, List<List<String>>> rejectedJobBatchesByEngine;
  protected Map<String, AcquiredJobs> acquiredJobsByEngine;
  protected Map<String, List<List<String>>> additionalJobBatchesByEngine;
  protected Exception acquisitionException;
  protected long acquisitionTime;
  protected boolean isJobAdded;

  public JobAcquisitionContext() {
    this.rejectedJobBatchesByEngine = new HashMap<String, List<List<String>>>();
    this.additionalJobBatchesByEngine = new HashMap<String, List<List<String>>>();
    this.acquiredJobsByEngine = new HashMap<String, AcquiredJobs>();
  }

  public void submitRejectedBatch(String engineName, List<String> jobIds) {
    CollectionUtil.addToMapOfLists(rejectedJobBatchesByEngine, engineName, jobIds);
  }

  public void submitAcquiredJobs(String engineName, AcquiredJobs acquiredJobs) {
    acquiredJobsByEngine.put(engineName, acquiredJobs);
  }

  public void submitAdditionalJobBatch(String engineName, List<String> jobIds) {
    CollectionUtil.addToMapOfLists(additionalJobBatchesByEngine, engineName, jobIds);
  }

  public void reset() {
    additionalJobBatchesByEngine.clear();

    // jobs that were rejected in the previous acquisition cycle
    // are to be resubmitted for execution in the current cycle
    additionalJobBatchesByEngine.putAll(rejectedJobBatchesByEngine);

    rejectedJobBatchesByEngine.clear();
    acquiredJobsByEngine.clear();
    acquisitionException = null;
    acquisitionTime = 0;
    isJobAdded = false;
  }

  /**
   * @return true, if for all engines there were less jobs acquired than requested
   */
  public boolean areAllEnginesIdle() {
    for (AcquiredJobs acquiredJobs : acquiredJobsByEngine.values()) {
      int jobsAcquired = acquiredJobs.getJobIdBatches().size() + acquiredJobs.getNumberOfJobsFailedToLock();

      if (jobsAcquired >= acquiredJobs.getNumberOfJobsAttemptedToAcquire()) {
        return false;
      }
    }

    return true;
  }

  /**
   * true if at least one job could not be locked, regardless of engine
   */
  public boolean hasJobAcquisitionLockFailureOccurred() {
    for (AcquiredJobs acquiredJobs : acquiredJobsByEngine.values()) {
      if (acquiredJobs.getNumberOfJobsFailedToLock() > 0) {
        return true;
      }
    }

    return false;
  }

  // getters and setters

  public void setAcquisitionTime(long acquisitionTime) {
    this.acquisitionTime = acquisitionTime;
  }

  public long getAcquisitionTime() {
    return acquisitionTime;
  }

  /**
   * Jobs that were acquired in the current acquisition cycle.
   * @return
   */
  public Map<String, AcquiredJobs> getAcquiredJobsByEngine() {
    return acquiredJobsByEngine;
  }

  /**
   * Jobs that were rejected from execution in the acquisition cycle
   * due to lacking execution resources.
   * With an execution thread pool, these jobs could not be submitted due to
   * saturation of the underlying job queue.
   */
  public Map<String, List<List<String>>> getRejectedJobsByEngine() {
    return rejectedJobBatchesByEngine;
  }

  /**
   * Jobs that have been acquired in previous cycles and are supposed to
   * be re-submitted for execution
   */
  public Map<String, List<List<String>>> getAdditionalJobsByEngine() {
    return additionalJobBatchesByEngine;
  }

  public void setAcquisitionException(Exception e) {
    this.acquisitionException = e;
  }

  public Exception getAcquisitionException() {
    return acquisitionException;
  }

  public void setJobAdded(boolean isJobAdded) {
    this.isJobAdded = isJobAdded;
  }

  public boolean isJobAdded() {
    return isJobAdded;
  }
}
