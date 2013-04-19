package org.camunda.bpm.cycle.service.mail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.cycle.service.mail.spi.MailSessionProvider;
import org.camunda.bpm.cycle.util.IoUtil;
import org.springframework.stereotype.Service;


/**
 * 
 * @author Daniel Meyer
 * 
 */
@Service
public class MailService {

  private static final String WELCOME_EMAIL_TEMPLATE = "welcomeEmailTemplate.txt";
  private static final String WELCOME_EMAIL_NAME = "welcome email";
  private static String WELCOME_EMAIL_TEMPLATE_CACHED;

  @Inject
  MailSessionProvider mailSessionProvider;

  public void sendWelcomeEmail(String username, String password, String from, String receiver) {
    
    validateParameters(username, password, from, receiver, WELCOME_EMAIL_NAME);
    
    String foxType = "cycle"; 

    Session mailSession = mailSessionProvider.lookupMailSession();
    Message msg = new MimeMessage(mailSession);
    
    setFrom(msg, from, WELCOME_EMAIL_NAME);
    setRecipients(msg, receiver, WELCOME_EMAIL_NAME);
    setSubject(msg, foxType, WELCOME_EMAIL_NAME);
    
    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("\\$\\{username\\}", username);
    replacements.put("\\$\\{password\\}", password);
    replacements.put("\\$\\{foxType\\}", foxType);
    
    if(WELCOME_EMAIL_TEMPLATE_CACHED == null) {
      WELCOME_EMAIL_TEMPLATE_CACHED = readEmailTemplate(WELCOME_EMAIL_NAME, WELCOME_EMAIL_TEMPLATE);
    }
    
    setText(msg, WELCOME_EMAIL_TEMPLATE_CACHED, replacements, WELCOME_EMAIL_NAME);

    mailSessionProvider.sendMail(msg, mailSession);

  }

  protected void validateParameters(String username, String password, String from, String receiver, String emailName) {
    if(username == null) {
      throw new MailServiceException("Cannot send "+emailName+ "; username is null");
    }
    if(password == null) {
      throw new MailServiceException("Cannot send "+password+ "; password is null");
    }
    if(from == null) {
      throw new MailServiceException("Cannot send "+from+ "; sender is null");
    }
    if(receiver == null) {
      throw new MailServiceException("Cannot send "+receiver+ "; recipient is null");
    }
  }

  protected void setText(Message msg, String template, Map<String, String> replacements, String emailName) {

    String templateInstance = new String(template);
    
    for (Entry<String, String> replacement : replacements.entrySet()) {
      templateInstance = templateInstance.replaceAll(replacement.getKey(), replacement.getValue());
    }

    try {
      msg.setText(templateInstance);
    } catch (MessagingException e) {
      throw new MailServiceException("Could not set 'text' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setSubject(Message msg, String foxType, String emailName) {
    try {
      msg.setSubject("Welcome to camunda " + foxType);
    } catch (MessagingException e) {
      throw new MailServiceException("Could not set 'subject' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setRecipients(Message msg, String receiver, String emailName) {
    try {
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver, false));
    } catch (Exception e) {
      throw new MailServiceException("Could not set 'recipients' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setFrom(Message msg, String from, String emailName) {
    try {
      msg.setFrom(new InternetAddress(from));
    } catch (Exception e) {
      throw new MailServiceException("Could not set 'from' field in "+emailName+": "+e.getMessage(), e);
    }
  }

  protected String readEmailTemplate(String emailName, String templateLocation) {
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = MailService.class.getResourceAsStream(templateLocation);
      return new String(IoUtil.readInputStream(resourceAsStream, emailName), "utf-8");
      
    } catch (Exception e) {
      throw new MailServiceException("Could not load "+emailName+": " + e.getMessage(), e);
      
    } finally {
      IoUtil.closeSilently(resourceAsStream);
    }
  }

}
