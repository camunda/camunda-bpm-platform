package com.camunda.fox.platform.impl.service;

import org.activiti.engine.ProcessEngine;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.service.SimplePlatformService;
import com.camunda.fox.platform.spi.ProcessArchive;


public abstract class PlatformServiceTest { 

  protected static final String ENGINE_DS1 = "engineDs1";
  protected static final String ENGINE_DS2 = "engineDs2";
  
  protected ProcessEngineService processEngineService;
  protected ProcessArchiveService processArchiveService;
  protected SimplePlatformService platformService;
  
  @Before
  public void setupProcessEngineSerivce() throws Exception {    
    platformService = new SimplePlatformService();
    processArchiveService = platformService;
    processEngineService = platformService;
    
    BasicDataSource engineDs1 = new BasicDataSource();
    engineDs1.setDriverClassName("org.h2.Driver");
    engineDs1.setUrl("jdbc:h2:mem:engineDs1;DB_CLOSE_DELAY=-1;MVCC=TRUE");
    engineDs1.setUsername("sa");
    engineDs1.setPassword("sa");
    
    BasicDataSource engineDs2 = new BasicDataSource();
    engineDs2.setDriverClassName("org.h2.Driver");
    engineDs2.setUrl("jdbc:h2:mem:engineDs2;DB_CLOSE_DELAY=-1;MVCC=TRUE");
    engineDs2.setUsername("sa");
    engineDs2.setPassword("sa");
    
    SimpleNamingContextBuilder emptyActivatedContextBuilder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();    
    emptyActivatedContextBuilder.bind(ENGINE_DS1, engineDs1);
    emptyActivatedContextBuilder.bind(ENGINE_DS2, engineDs2);    
  }
  
  @After
  public void cleanup() throws Exception {
    // uninstall all process archives
    for (ProcessArchive processArchive : platformService.getInstalledProcessArchives()) {
      platformService.unInstallProcessArchive(processArchive);
    }
    // stop all process engines
    for (ProcessEngine processEngine : platformService.getProcessEngines()) {      
      platformService.stopProcessEngine(processEngine);
    }
  }

}
