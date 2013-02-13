package com.camunda.fox.cycle.service.mail;

import java.util.Arrays;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.configuration.CycleConfiguration;
import com.camunda.fox.cycle.service.mail.spi.MailSessionProvider;

/**
 * <p>Default implementation of the {@link MailSessionProvider} SPI.</p>
 * 
 * <p>This implemenation looks up a mail session in JNDI using the name configured in {@link CycleConfiguration#getMailSessionName()}.</p>
 * 
 * <p>If that name is null, a set of default mail session names is used in order to autodetect a mail session.</p> 
 * 
 * @author Daniel Meyer
 */
@Component
public class DefaultMailSessionProvider implements MailSessionProvider {

  @Inject
  private CycleConfiguration configuration;

  /* (non-Javadoc)
   * @see com.camunda.fox.cycle.service.MailServiceProvider#lookupMailSession()
   */
  @Override
  public Session lookupMailSession() {
    String mailSessionName = configuration.getMailSessionName();

    if (mailSessionName == null) {
      return tryAutoDetectMailSession();
    } else {
      return performLookup(mailSessionName);
    }
  }
  
  protected Session performLookup(String mailSessionName) {
    try {
      return InitialContext.doLookup(mailSessionName);
    } catch (NamingException e) {
      throw new MailServiceException("Could not lookup mail session with name '"+mailSessionName+"'");
    }
  }

  protected Session tryAutoDetectMailSession() {

    String[] mailSessionNames = getKnownDefaultMailSessionNames();
    
    for (int i = 0; i < mailSessionNames.length; i++) {
      String mailSessionName = mailSessionNames[i];
      
      try {
        Session session = performLookup(mailSessionName);
        configuration.setMailSessionName(mailSessionName);
        return session;

      } catch (MailServiceException e) {
        // ignore (we try all names)
      }      
    }
    
    throw new MailServiceException("No mail session URL configured and could not autodetect mail session using names "+Arrays.toString(mailSessionNames));
   
  }

  protected String[] getKnownDefaultMailSessionNames() {
    
    String mailSessionNames[] = { 
      "java:comp/env/mail/Session", 
      "java:jboss/mail/Default" 
    };
    
    return mailSessionNames;
  }

  @Override
  public void sendMail(Message msg, Session session) {
    try {
      Transport.send(msg);
    } catch (MessagingException e) {
      throw new MailServiceException("Could not send message using the default Transport", e);
    }
  }
  
}
