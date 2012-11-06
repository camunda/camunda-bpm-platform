package com.camunda.fox.cycle.service.mail;

import java.util.Properties;

import javax.mail.Session;

import com.camunda.fox.cycle.service.mail.spi.MailServiceProvider;

/**
 * SPI-Implementation of {@link MailServiceProvider} for unittesting. 
 * 
 * @author Daniel Meyer
 *
 */
public class TestMailServiceProvider extends DefaultMailServiceProvider {
  
  protected Properties mailServerProperties;

  public TestMailServiceProvider(Properties mailServerProperties) {
    this.mailServerProperties = mailServerProperties;
  }

  @Override
  public Session lookupMailSession() {    
    return Session.getDefaultInstance(mailServerProperties);
  }

}
