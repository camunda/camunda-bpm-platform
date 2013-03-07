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
package org.camunda.bpm.engine.test.cfg;

import junit.framework.TestCase;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutorImpl;
import org.camunda.bpm.engine.impl.interceptor.RetryInterceptor;

/**
 * 
 * @author Daniel Meyer
 */
public class RetryInterceptorTest extends TestCase {

  protected class CommandThrowingOptimisticLockingException implements Command<Void> {
    public Void execute(CommandContext commandContext) {
      throw new OptimisticLockingException("");
    }
  }

  public void testRetryInterceptor() {
    RetryInterceptor retryInterceptor = new RetryInterceptor();
    retryInterceptor.setNext(new CommandExecutorImpl());
    try {
      retryInterceptor.execute(new CommandThrowingOptimisticLockingException());
      fail("ProcessEngineException expected.");
    }catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains(retryInterceptor.getNumOfRetries()+" retries failed"));
    }
  }
}
