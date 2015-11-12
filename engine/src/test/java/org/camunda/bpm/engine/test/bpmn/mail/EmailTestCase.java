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

package org.camunda.bpm.engine.test.bpmn.mail;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.test.TestLogger;
import org.slf4j.Logger;
import org.subethamail.wiser.Wiser;


/**
 * @author Joram Barrez
 */
public abstract class EmailTestCase extends PluggableProcessEngineTestCase {

  private final static Logger LOG = TestLogger.TEST_LOGGER.getLogger();

  protected Wiser wiser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    int port = processEngineConfiguration.getMailServerPort();

    boolean serverUpAndRunning = false;
    while (!serverUpAndRunning) {
      wiser = new Wiser();
      wiser.setPort(port);

      try {
        LOG.info("Starting Wiser mail server on port: " + port);
        wiser.start();
        serverUpAndRunning = true;
        LOG.info("Wiser mail server listening on port: " + port);
      } catch (RuntimeException e) { // Fix for slow port-closing Jenkins
        if (e.getMessage().toLowerCase().contains("BindException")) {
          Thread.sleep(250L);
        }
      }
    }
  }

  @Override
  protected void tearDown() throws Exception {
    wiser.stop();

    // Fix for slow Jenkins
    Thread.sleep(250L);

    super.tearDown();
  }

}
