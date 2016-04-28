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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class AcquiredJobs {

  protected int numberOfJobsAttemptedToAcquire;

  protected List<List<String>> acquiredJobBatches = new ArrayList<List<String>>();
  protected Set<String> acquiredJobs = new HashSet<String>();

  protected int numberOfJobsFailedToLock = 0;

  public AcquiredJobs(int numberOfJobsAttemptedToAcquire) {
    this.numberOfJobsAttemptedToAcquire = numberOfJobsAttemptedToAcquire;
  }

  public List<List<String>> getJobIdBatches() {
    return acquiredJobBatches;
  }

  public void addJobIdBatch(List<String> jobIds) {
    if (!jobIds.isEmpty()) {
      acquiredJobBatches.add(jobIds);
      acquiredJobs.addAll(jobIds);
    }
  }

  public void addJobIdBatch(String jobId) {
    ArrayList<String> list = new ArrayList<String>();
    list.add(jobId);

    addJobIdBatch(list);
  }

  public boolean contains(String jobId) {
    return acquiredJobs.contains(jobId);
  }

  public int size() {
    return acquiredJobs.size();
  }

  public void removeJobId(String id) {
    numberOfJobsFailedToLock++;

    acquiredJobs.remove(id);

    Iterator<List<String>> batchIterator = acquiredJobBatches.iterator();
    while (batchIterator.hasNext()) {
      List<String> batch = batchIterator.next();
      batch.remove(id);

      // remove batch if it is now empty
      if(batch.isEmpty()) {
        batchIterator.remove();
      }

    }
  }

  public int getNumberOfJobsFailedToLock() {
    return numberOfJobsFailedToLock;
  }

  public int getNumberOfJobsAttemptedToAcquire() {
    return numberOfJobsAttemptedToAcquire;
  }

}
