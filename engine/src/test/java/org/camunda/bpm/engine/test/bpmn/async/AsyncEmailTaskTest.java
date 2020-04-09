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
package org.camunda.bpm.engine.test.bpmn.async;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.mail.EmailServiceTaskTest;
import org.camunda.bpm.engine.test.bpmn.mail.EmailTestCase;
import org.subethamail.wiser.WiserMessage;

/**
 *
 *
 * @author Daniel Meyer
 */
public class AsyncEmailTaskTest extends EmailTestCase {

  // copied from org.camunda.bpm.engine.test.bpmn.mail.EmailServiceTaskTest
  @Deployment
  public void testSimpleTextMail() throws Exception {
    String procId = runtimeService.startProcessInstanceByKey("simpleTextOnly").getId();

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(0, messages.size());

    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    EmailServiceTaskTest.assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "camunda@localhost",
            Arrays.asList("kermit@camunda.org"), null);
    testRule.assertProcessEnded(procId);
  }

  // copied from org.camunda.bpm.engine.test.bpmn.mail.EmailSendTaskTest
  @Deployment
  public void testSimpleTextMailSendTask() throws Exception {
    runtimeService.startProcessInstanceByKey("simpleTextOnly");

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(0, messages.size());

    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    EmailServiceTaskTest.assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "camunda@localhost",
            Arrays.asList("kermit@camunda.org"), null);
  }

}
