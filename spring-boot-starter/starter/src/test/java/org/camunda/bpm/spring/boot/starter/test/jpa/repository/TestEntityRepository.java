package org.camunda.bpm.spring.boot.starter.test.jpa.repository;

import org.camunda.bpm.spring.boot.starter.test.jpa.domain.TestEntity;
import org.springframework.data.repository.CrudRepository;

public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}
