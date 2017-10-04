package org.camunda.bpm.spring.boot.starter.test.nonpa.service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.camunda.bpm.engine.runtime.ProcessInstance;

public interface TransactionalTestService {

  ProcessInstance doOk();

  @Transactional(TxType.REQUIRES_NEW)
  void doThrowing();

}
