package org.camunda.bpm.engine.impl.db;

public interface EntityLoadListener {
  
  void onEntityLoaded(DbEntity entity);

}
