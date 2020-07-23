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
package org.camunda.bpm.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cmd.SetLicenseKeyCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

public class CompetingLicenseKeyAccessTest extends ConcurrencyTest {

  private ThreadControl asyncThread;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    managementService.setLicenseKey("testLicenseKey");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    managementService.deleteLicenseKey();
  }

  /**
   * thread1:
   *  t=1: fetch license key
   *  t=4: update license key
   *
   * thread2:
   *  t=2: fetch and delete license key
   *  t=3: commit transaction
   */
  public void testConcurrentlyDeleteAndSetLicense() {
    managementService.setLicenseKey("testLicenseKey");

    asyncThread = executeControllableCommand(new FetchAndUpdateLicenseCmd());

    asyncThread.waitForSync();

    managementService.deleteLicenseKey();

    asyncThread.reportInterrupts();
    asyncThread.waitUntilDone();

    Throwable exception = asyncThread.getException();
    assertThat(exception).isNotNull();
    assertThat(exception instanceof OptimisticLockingException).isTrue();
  }

  /**
   * thread1:
   *  t=1: fetch license key
   *  t=4: update license key
   *
   * thread2:
   *  t=2: fetch and update license key
   *  t=3: commit transaction
   */
  public void testConcurrentlyAlterLicense() {
    managementService.setLicenseKey("testLicenseKey");

    asyncThread = executeControllableCommand(new FetchAndUpdateLicenseCmd());

    asyncThread.waitForSync();

    managementService.setLicenseKey("updatedTestLicenseKey");

    asyncThread.reportInterrupts();
    asyncThread.waitUntilDone();

    Throwable exception = asyncThread.getException();
    assertThat(exception).isNotNull();
    assertThat(exception instanceof OptimisticLockingException).isTrue();
  }

  private static class FetchAndUpdateLicenseCmd extends ControllableCommand<Long> {

    @Override
    public Long execute(CommandContext commandContext) {
      ResourceEntity licenseKey = commandContext.getResourceManager().findLicenseKeyResource();
      assertNotNull("license key is expected to be not null", licenseKey);

      monitor.sync();

      licenseKey.setBytes("updatedTestLicenseKeyBySecondThread".getBytes());
      new SetLicenseKeyCmd(new String(licenseKey.getBytes())).execute(commandContext);
      return null;
    }
  }
}
