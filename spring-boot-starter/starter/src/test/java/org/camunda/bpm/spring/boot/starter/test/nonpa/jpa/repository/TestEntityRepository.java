package org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.repository;

import org.camunda.bpm.spring.boot.starter.test.nonpa.jpa.domain.TestEntity;
import org.springframework.data.repository.CrudRepository;

public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}
