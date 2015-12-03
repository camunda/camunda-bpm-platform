package org.camunda.bpm.engine.test.api.cfg;

import org.apache.ibatis.datasource.pooled.PooledDataSource;


public class IdGeneratorDataSource extends PooledDataSource {

  public IdGeneratorDataSource() {
    setDriver("org.h2.Driver");
    setUrl("jdbc:h2:mem:IdGeneratorDataSourceTest");
    setUsername("sa");
    setPassword("");
    setPoolMaximumActiveConnections(2);
  }
}
