package org.camunda.bpm.cycle.configuration;

import org.camunda.bpm.security.SecurityConfiguration;
import org.springframework.stereotype.Component;


/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
@Component
public class CycleConfiguration extends SecurityConfiguration {
  
  private String mailSessionName;
  private String emailFrom = "cycle@localhost";
  private String defaultCommitMessage = "Changed using camunda fox cycle";
  
  /**
   * The mail session name is used for looking up a mail session in JNDI
   * 
   * @return the JNDI name for looking up a mail session
   */
  public String getMailSessionName() {
    return mailSessionName;
  }
  
  public void setMailSessionName(String mailSessionUrl) {
    this.mailSessionName = mailSessionUrl;
  }

  /**
   * The name that is used by cycle to send emails.
   */
  public String getEmailFrom() {
    return emailFrom;    
  }
  
  public void setEmailFrom(String emailFrom) {
    this.emailFrom = emailFrom;
  }
  
  public String getDefaultCommitMessage() {
    return defaultCommitMessage;
  }
  
  public void setDefaultCommitMessage(String defaultCommitMessage) {
    this.defaultCommitMessage = defaultCommitMessage;
  }

}
