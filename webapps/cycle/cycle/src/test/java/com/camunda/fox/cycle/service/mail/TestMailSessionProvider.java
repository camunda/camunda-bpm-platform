package com.camunda.fox.cycle.service.mail;

import java.util.Properties;

import javax.mail.Session;

import com.camunda.fox.cycle.service.mail.spi.MailSessionProvider;

/**
 * SPI-Implementation of {@link MailSessionProvider} for unit-testing. 
 * 
 * @author Daniel Meyer
 *
 */
public class TestMailSessionProvider extends DefaultMailSessionProvider {
  
  protected Properties mailServerProperties;

  public TestMailSessionProvider(Properties mailServerProperties) {
    this.mailServerProperties = mailServerProperties;
  }

  @Override
  public Session lookupMailSession() {    
    return Session.getDefaultInstance(mailServerProperties);
  }

}
