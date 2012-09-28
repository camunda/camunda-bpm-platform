package com.camunda.fox.platform.test.util;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author christian.lipphardt
 */
public class JndiConstants {
  
  private static Logger logger = Logger.getLogger(JndiConstants.class.getName());
  
  /**
   * AppName is empty for all application servers except websphere.
   * WAS-8 Arquillian Remote Connector packages all WARs into EARs so we need to add an additional AppName to JNDI lookups,
   * which is the same as the WAR one.
   */
  public static final String getAppName() {
    Properties properties = new Properties();
    
    try {
      properties.load(JndiConstants.class.getResourceAsStream("/appname.properties"));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to load appname.properties", e);
    }
    
    // either return lookuped property value or nothing
    return properties.getProperty("appname", "");
  }
  
  public static <T> T lookup(String jndi) {
    try {
      return (T) new InitialContext().lookup(jndi);
    } catch (NamingException e) {
      e.printStackTrace();
      return null;
    }
  }
  
}
