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
package org.camunda.bpm.engine.test.concurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingExternalTaskFetchingTest extends PluggableProcessEngineTestCase {

  public class ExternalTaskFetcherThread extends ControllableThread {

    protected String workerId;
    protected int results;
    protected String topic;

    protected List<LockedExternalTask> fetchedTasks;
    protected OptimisticLockingException exception;

    public ExternalTaskFetcherThread(String workerId, int results, String topic) {
      this.workerId = workerId;
      this.results = results;
      this.topic = topic;
    }

    public void run() {
      Map<String, TopicFetchInstruction> instructions = new HashMap<String, TopicFetchInstruction>();

      TopicFetchInstruction instruction = new TopicFetchInstruction(topic, 10000L);
      instructions.put(topic, instruction);

      try {
        fetchedTasks = processEngineConfiguration.getCommandExecutorTxRequired().execute(
            new FetchExternalTasksCmd(workerId, results, instructions));
      } catch (OptimisticLockingException e) {
        exception = e;
      }
    }
  }

  @Deployment
  public void testCompetingExternalTaskFetching() {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExternalTaskFetcherThread thread1 = new ExternalTaskFetcherThread("thread1", 5, "externalTaskTopic");
    ExternalTaskFetcherThread thread2 = new ExternalTaskFetcherThread("thread2", 5, "externalTaskTopic");

    // both threads fetch the same task and wait before flushing the lock
    thread1.startAndWaitUntilControlIsReturned();
    thread2.startAndWaitUntilControlIsReturned();

    // thread1 succeeds
    thread1.proceedAndWaitTillDone();
    assertNull(thread1.exception);
    assertEquals(1, thread1.fetchedTasks.size());

    // thread2 does not succeed in locking the job
    thread2.proceedAndWaitTillDone();
    assertEquals(0, thread2.fetchedTasks.size());
    // but does not fail with an OptimisticLockingException
    assertNull(thread2.exception);
  }
}
