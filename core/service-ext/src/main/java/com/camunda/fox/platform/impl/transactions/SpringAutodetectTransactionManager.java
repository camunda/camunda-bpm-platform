package com.camunda.fox.platform.impl.transactions;

import java.util.logging.Logger;

import org.springframework.transaction.config.JtaTransactionManagerBeanDefinitionParser;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.camunda.fox.platform.impl.transactions.spi.TransactionManagerFactory;

/**
 * <p>Autodetects a Spring JtaTransactionManager and returns an instance.</p>
 * 
 * <p>Supports autodetection of platform-specific transaction managers for
 * WebSphere / WebLogic.</p>
 * 
 * @author Daniel Meyer
 */
public class SpringAutodetectTransactionManager extends JtaTransactionManagerBeanDefinitionParser implements TransactionManagerFactory {

  private static Logger log = Logger.getLogger(SpringAutodetectTransactionManager.class.getName());

  public JtaTransactionManager getTransactionManager() {
    String classname = getBeanClassName(null);
    log.info("Autodetected JtaTransactionManager class: " + classname);
    Class<JtaTransactionManager> txManagerClass = loadTransactionManagerClass(classname);
    JtaTransactionManager txManager = instantiateTransactionManager(txManagerClass);
    txManager.afterPropertiesSet();
    return txManager;
  }

  protected JtaTransactionManager instantiateTransactionManager(Class<JtaTransactionManager> txManagerClass) {
    try {
      return txManagerClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not instantiate JtaTransactionManager class: " + txManagerClass + ": " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Class<JtaTransactionManager> loadTransactionManagerClass(String classname) {
    try {
      Class<JtaTransactionManager> clazz = (Class<JtaTransactionManager>) Class.forName(classname);
      return clazz;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load transaction manager class: " + classname + ": " + e.getMessage(), e);
    }
  }

}
