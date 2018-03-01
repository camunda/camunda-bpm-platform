package org.camunda.bpm.spring.boot.starter.test.nonpa.service;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.domain.TestEntity;
import org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.repository.TestEntityRepository;
import org.camunda.bpm.spring.boot.starter.test.nonpa.service.TransactionalTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionalTestServiceImpl implements TransactionalTestService {

  @Autowired
  private TestEntityRepository testEntityRepository;

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public ProcessInstance doOk() {
    TestEntity entity = new TestEntity();
    entity.setText("text");
    TestEntity testEntity = testEntityRepository.save(entity);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("test", testEntity);
    return runtimeService.startProcessInstanceByKey("TestProcess", variables);
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  public void doThrowing() {
    doOk();
    throw new IllegalStateException();
  }
}
