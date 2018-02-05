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

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;

/**
 * <p>Tests two simultaneous attempts to login for the same user which results in OptimisticLockingException.</p>
 *
 */
public class ConcurrentLoginAttemptTest extends ConcurrencyTestCase {

  private static final String USER_ID = "johndoe";
  private static final String PASSWORD = "xxx";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    User user = identityService.newUser(USER_ID);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);
  }

  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser(USER_ID);
    super.tearDown();
  }

  public void test() throws InterruptedException {
    ThreadControl thread1 = executeControllableCommand(new ControllableCheckPasswordCommand());
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableCheckPasswordCommand());
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone();

    UserEntity user = (UserEntity) identityService.createUserQuery().userId(USER_ID).singleResult();
    assertEquals(0, user.getAttempts());
    assertNull(user.getLockExpirationTime());

    assertNull(thread1.exception);
    assertNull(thread2.exception);
  }

  protected static class ControllableCheckPasswordCommand extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called form main thread

      commandContext.getProcessEngineConfiguration().getIdentityService().checkPassword(USER_ID, PASSWORD);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }
}
