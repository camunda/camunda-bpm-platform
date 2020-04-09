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
package org.camunda.bpm.engine.test.bpmn.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.subethamail.wiser.WiserMessage;


/**
 * @author Joram Barrez
 */
public class EmailServiceTaskTest extends EmailTestCase {

  @Deployment
  @Test
  public void testSimpleTextMail() throws Exception {
    String procId = runtimeService.startProcessInstanceByKey("simpleTextOnly").getId();

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "camunda@localhost",
            Arrays.asList("kermit@camunda.org"), null);
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testSimpleTextMailMultipleRecipients() {
    runtimeService.startProcessInstanceByKey("simpleTextOnlyMultipleRecipients");

    // 3 recipients == 3 emails in wiser with different receivers
    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(3, messages.size());

    // sort recipients for easy assertion
    List<String> recipients = new ArrayList<String>();
    for (WiserMessage message : messages) {
      recipients.add(message.getEnvelopeReceiver());
    }
    Collections.sort(recipients);

    assertEquals("fozzie@camunda.org", recipients.get(0));
    assertEquals("kermit@camunda.org", recipients.get(1));
    assertEquals("mispiggy@camunda.org", recipients.get(2));
  }

  @Deployment
  @Test
  public void testTextMailExpressions() throws Exception {

    String sender = "mispiggy@activiti.org";
    String recipient = "fozziebear@activiti.org";
    String recipientName = "Mr. Fozzie";
    String subject = "Fozzie, you should see this!";

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sender", sender);
    vars.put("recipient", recipient);
    vars.put("recipientName", recipientName);
    vars.put("subject", subject);

    runtimeService.startProcessInstanceByKey("textMailExpressions", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, subject, "Hello " + recipientName + ", this is an e-mail",
            sender, Arrays.asList(recipient), null);
  }

  @Deployment
  @Test
  public void testCcAndBcc() throws Exception {
    runtimeService.startProcessInstanceByKey("ccAndBcc");

    List<WiserMessage> messages = wiser.getMessages();
    assertEmailSend(messages.get(0), false, "Hello world", "This is the content", "camunda@localhost",
            Arrays.asList("kermit@camunda.org"), Arrays.asList("fozzie@camunda.org"));

    // Bcc is not stored in the header (obviously)
    // so the only way to verify the bcc, is that there are three messages send.
    assertEquals(3, messages.size());
  }

  @Deployment
  @Test
  public void testHtmlMail() throws Exception {
    runtimeService.startProcessInstanceByKey("htmlMail", CollectionUtil.singletonMap("gender", "male"));

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    assertEmailSend(messages.get(0), true, "Test", "Mr. <b>Kermit</b>", "camunda@localhost", Arrays.asList("kermit@camunda.org"), null);
  }

  @Deployment
  @Test
  public void testSendEmail() throws Exception {

    String from = "ordershipping@activiti.org";
    boolean male = true;
    String recipientName = "John Doe";
    String recipient = "johndoe@alfresco.com";
    Date now = new Date();
    String orderId = "123456";

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sender", from);
    vars.put("recipient", recipient);
    vars.put("recipientName", recipientName);
    vars.put("male", male);
    vars.put("now", now);
    vars.put("orderId", orderId);

    runtimeService.startProcessInstanceByKey("sendMailExample", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    MimeMessage mimeMessage = message.getMimeMessage();

    assertEquals("Your order " + orderId + " has been shipped", mimeMessage.getHeader("Subject", null));
    assertEquals(from, mimeMessage.getHeader("From", null));
    assertTrue(mimeMessage.getHeader("To", null).contains(recipient));
  }

  // Helper

  public static void assertEmailSend(WiserMessage emailMessage, boolean htmlMail, String subject, String message,
          String from, List<String> to, List<String> cc) throws IOException {
    try {
      MimeMessage mimeMessage = emailMessage.getMimeMessage();

      if (htmlMail) {
        assertTrue(mimeMessage.getContentType().contains("multipart/mixed"));
      } else {
        assertTrue(mimeMessage.getContentType().contains("text/plain"));
      }

      assertEquals(subject, mimeMessage.getHeader("Subject", null));
      assertEquals(from, mimeMessage.getHeader("From", null));
      assertTrue(getMessage(mimeMessage).contains(message));

      for (String t : to) {
        assertTrue(mimeMessage.getHeader("To", null).contains(t));
      }

      if (cc != null) {
        for (String c : cc) {
          assertTrue(mimeMessage.getHeader("Cc", null).contains(c));
        }
      }

    } catch (MessagingException e) {
      fail(e.getMessage());
    }

  }

  public static String getMessage(MimeMessage mimeMessage) throws MessagingException, IOException {
    DataHandler dataHandler = mimeMessage.getDataHandler();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    dataHandler.writeTo(baos);
    baos.flush();
    return baos.toString();
  }

}
