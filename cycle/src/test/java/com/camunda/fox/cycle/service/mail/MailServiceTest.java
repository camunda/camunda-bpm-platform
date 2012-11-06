package com.camunda.fox.cycle.service.mail;

import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;



/**
 * 
 * @author Daniel Meyer
 *
 */
public class MailServiceTest {
  
  private static final int MAIL_SERVER_PORT = 5026;
  protected Wiser wiser;
  protected MailService mailService;
  
  @Before
  public void setUp() throws Exception {
    
    // setup wiser mail for testing
    
    boolean serverUpAndRunning = false;
    while (!serverUpAndRunning) {
      wiser = new Wiser();
      wiser.setPort(MAIL_SERVER_PORT);
      
      try {
        wiser.start();
        serverUpAndRunning = true;
      } catch (RuntimeException e) { // Fix for slow port-closing Jenkins
        if (e.getMessage().toLowerCase().contains("BindException")) {
          Thread.sleep(250L);
        }
      }
    }
    
    // setup the mail service
    
    Properties mailServerProperties = getMailServerProperties();      
    TestMailSessionProvider testMailServiceProvider = new TestMailSessionProvider(mailServerProperties);
    mailService = new MailService();
    mailService.mailSessionProvider = testMailServiceProvider;
    
  }
  
  @After
  public void tearDown() throws Exception {
    wiser.stop();
    
    // Fix for slow Jenkins
    Thread.sleep(250L);
  }
  
  @Test
  public void testSendWelcomeEmail() throws Exception {

    String from = "test@fox.camunda.com";
    String username = "testUserName";
    String password = "testPassword";
    String recipient = "testUser@camunda.com";

    // send the email
    mailService.sendWelcomeEmail(username, password, from, recipient);

    // make sure it was received by the mail 
    List<WiserMessage> messages = wiser.getMessages();
    Assert.assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    MimeMessage mimeMessage = message.getMimeMessage();    
    
    // check that the recipient is correct
    Address[] recipients = mimeMessage.getRecipients(RecipientType.TO);
    Assert.assertEquals(1, recipients.length);
    Assert.assertEquals(recipient, recipients[0].toString());
    
    // check that the from is correct
    Address[] froms = mimeMessage.getFrom();
    Assert.assertEquals(1, froms.length);
    Assert.assertEquals(from, froms[0].toString());
    
    // check taht the meessage contains the username and password:
    String content = (String) mimeMessage.getContent();
    Assert.assertNotNull(content);
    Assert.assertTrue(content.contains("Username: " + username));
    Assert.assertTrue(content.contains("Password: " + password));

  }
  
  @Test
  public void testSendWelcomeEmail_invalidParameters() {
    
    String from = "test@fox.camunda.com";
    String username = "testUserName";
    String password = "testPassword";
    String recipient = "testUser@camunda.com";
    
    try {
      mailService.sendWelcomeEmail(null, password, from, recipient);
      Assert.fail("Exception expected");
    }catch (MailServiceException e) {
      // expected
    }
    try {
      mailService.sendWelcomeEmail(username, null, from, recipient);
      Assert.fail("Exception expected");
    }catch (MailServiceException e) {
      // expected
    }
    try {
      mailService.sendWelcomeEmail(username, password, null, recipient);
      Assert.fail("Exception expected");
    }catch (MailServiceException e) {
      // expected
    }
    try {
      mailService.sendWelcomeEmail(username, password, from, null);
      Assert.fail("Exception expected");
    }catch (MailServiceException e) {
      // expected
    }
    
    // no emails were sent:
    List<WiserMessage> messages = wiser.getMessages();
    Assert.assertEquals(0, messages.size());
    
  }
  

  protected Properties getMailServerProperties() {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.host", "localhost");
    properties.setProperty("mail.smtp.auth", "false");
    properties.setProperty("mail.smtp.port", String.valueOf(MAIL_SERVER_PORT));
    return properties;
  }
  
}