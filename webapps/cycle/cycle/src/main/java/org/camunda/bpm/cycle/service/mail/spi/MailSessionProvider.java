package org.camunda.bpm.cycle.service.mail.spi;

import javax.mail.Message;
import javax.mail.Session;

import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;


/**
 * 
 * @author Daniel Meyer
 *
 */
public interface MailSessionProvider {

  /**
   * Looks up the mail session using the configured name in {@link CycleConfiguration#getMailSessionName()}. 
   *  
   * @return the mail {@link Session}
   * @throws CycleException if no mail session can be looked up
   * 
   */
  public abstract Session lookupMailSession();

  /**
   * Ends the message using a transport at the discretion of the {@link MailSessionProvider}.
   * @param mailSession 
   * 
   * @param the message
   * @throws CycleException if the Message cannot be send.
   * 
   */
  public abstract void sendMail(Message msg, Session mailSession);

}