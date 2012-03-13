package com.camunda.fox.platform.impl.transactions.spi;

import org.springframework.transaction.jta.JtaTransactionManager;

public interface TransactionManagerFactory {

  public JtaTransactionManager getTransactionManager();

}