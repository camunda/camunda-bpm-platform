package com.camunda.fox.cycle.service.mail;

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

import org.springframework.stereotype.Service;

import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.service.mail.spi.MailSessionProvider;
import com.camunda.fox.cycle.util.IoUtil;

/**
 * 
 * @author Daniel Meyer
 * 
 */
@Service
public class MailService {

  private static final String PASSWORD_CONFIRM_TEMPLATE = "passwordConfirmationEmailTemplate.txt";
  private static final String PASSWORD_CONFIRM_NAME = "password confirmation email";
  private static String PASSWORD_CONFIRM_TEMPLATE_CACHED;

  @Inject
  MailSessionProvider mailSessionProvider;

  public void sendPasswordConfirmationMail(String username, String password, String from, String receiver) {
    
    validateParameters(username, password, from, receiver, PASSWORD_CONFIRM_NAME);
    
    String foxType = "cycle"; 

    Session mailSession = mailSessionProvider.lookupMailSession();
    Message msg = new MimeMessage(mailSession);
    
    setFrom(msg, from, PASSWORD_CONFIRM_NAME);
    setRecipients(msg, receiver, PASSWORD_CONFIRM_NAME);
    setSubject(msg, foxType, PASSWORD_CONFIRM_NAME);
    
    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("\\$\\{username\\}", username);
    replacements.put("\\$\\{password\\}", password);
    replacements.put("\\$\\{foxType\\}", foxType);
    
    if(PASSWORD_CONFIRM_TEMPLATE_CACHED == null) {
      PASSWORD_CONFIRM_TEMPLATE_CACHED = readEmailTemplate(PASSWORD_CONFIRM_NAME, PASSWORD_CONFIRM_TEMPLATE);
    }
    
    setText(msg, PASSWORD_CONFIRM_TEMPLATE_CACHED, replacements, PASSWORD_CONFIRM_NAME);

    mailSessionProvider.sendMail(msg, mailSession);

  }

  protected void validateParameters(String username, String password, String from, String receiver, String emailName) {
    if(username == null) {
      throw new CycleException("Cannot send "+emailName+ "; username is null");
    }
    if(password == null) {
      throw new CycleException("Cannot send "+password+ "; username is null");
    }
    if(from == null) {
      throw new CycleException("Cannot send "+from+ "; username is null");
    }
    if(receiver == null) {
      throw new CycleException("Cannot send "+receiver+ "; username is null");
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
      throw new CycleException("Could not set 'text' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setSubject(Message msg, String foxType, String emailName) {
    try {
      msg.setSubject("Welcome to camunda fox " + foxType);
    } catch (MessagingException e) {
      throw new CycleException("Could not set 'subject' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setRecipients(Message msg, String receiver, String emailName) {
    try {
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver, false));
    } catch (Exception e) {
      throw new CycleException("Could not set 'recipients' field in "+emailName+": "+e.getMessage(), e);
    }
    
  }

  protected void setFrom(Message msg, String from, String emailName) {
    try {
      msg.setFrom(new InternetAddress(from));
    } catch (Exception e) {
      throw new CycleException("Could not set 'from' field in "+emailName+": "+e.getMessage(), e);
    }
  }

  protected String readEmailTemplate(String emailName, String templateLocation) {
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = MailService.class.getResourceAsStream(templateLocation);
      return new String(IoUtil.readInputStream(resourceAsStream, emailName), "utf-8");
      
    } catch (Exception e) {
      throw new CycleException("Could not load "+emailName+": " + e.getMessage(), e);
      
    } finally {
      IoUtil.closeSilently(resourceAsStream);
    }
  }

}
