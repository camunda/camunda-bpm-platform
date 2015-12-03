package org.camunda.bpm.engine.test.api.cfg;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;


public class IdGeneratorDataSourceTest extends ResourceProcessEngineTestCase {

  public IdGeneratorDataSourceTest() {
    super("org/camunda/bpm/engine/test/api/cfg/IdGeneratorDataSourceTest.camunda.cfg.xml");
  }

  @Deployment
  public void testIdGeneratorDataSource() {
    List<Thread> threads = new ArrayList<Thread>();
    for (int i=0; i<20; i++) {
      Thread thread = new Thread() {
        public void run() {
          for (int j = 0; j < 5; j++) {
            runtimeService.startProcessInstanceByKey("idGeneratorDataSource");
          }
        }
      };
      thread.start();
      threads.add(thread);
    }

    for (Thread thread: threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
