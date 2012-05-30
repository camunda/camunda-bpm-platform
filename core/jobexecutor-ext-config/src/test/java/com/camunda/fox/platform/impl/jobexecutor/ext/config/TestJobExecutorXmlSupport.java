package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.camunda.fox.platform.impl.jobexecutor.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.DefaultPlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.acquisition.JobAcquisition;

/**
 * @author roman.smirnov
 */
public class TestJobExecutorXmlSupport {
  
  private MyJobExecutorXmlSupport jobExecutorXmlSupport;
  private PlatformJobExecutorService platformJobExecutorService;
  
  @Before
  public void init() {
    this.jobExecutorXmlSupport = new MyJobExecutorXmlSupport();
    this.platformJobExecutorService = ServiceLoaderUtil.loadService(PlatformJobExecutorService.class, DefaultPlatformJobExecutor.class);
    PlatformJobExecutor jobExecutor = (PlatformJobExecutor) this.platformJobExecutorService;
    jobExecutor.start();
  }
  
  @Test
  public void testStartEmptyRootTag() {
    String jobExecutorXmlLocation = "com/camunda/fox/platform/impl/jobexecutor/ext/config/emptyRootTag.xml";
    this.jobExecutorXmlSupport.setJobExecutorXmlLocation(jobExecutorXmlLocation);
    this.jobExecutorXmlSupport.startJobExecutor(this.platformJobExecutorService);
    
    Assert.assertTrue(this.jobExecutorXmlSupport.getJobAcquisitions().isEmpty());
  }
  
  @Test
  public void testStartSingleJobAcquisition() {
    String jobExecutorXmlLocation = "com/camunda/fox/platform/impl/jobexecutor/ext/config/singleJobAcquisition.xml";
    this.jobExecutorXmlSupport.setJobExecutorXmlLocation(jobExecutorXmlLocation);
    this.jobExecutorXmlSupport.startJobExecutor(this.platformJobExecutorService);
    
    Map<String, List<JobAcquisition>> jobAcquisitions = this.jobExecutorXmlSupport.getJobAcquisitions();
    Assert.assertEquals(1, jobAcquisitions.size());
    for (Entry<String, List<JobAcquisition>> entry : jobAcquisitions.entrySet()) {
      String key = entry.getKey();
      Assert.assertTrue(key.contains(jobExecutorXmlLocation));
      
      List<JobAcquisition> startedJobAcquisitions = entry.getValue();
      Assert.assertEquals(1, startedJobAcquisitions.size());
    }
    
    this.jobExecutorXmlSupport.stopJobExecutor(this.platformJobExecutorService);
  }
  
  @Test
  public void testStartMultipleJobAcquisition() {
    String jobExecutorXmlLocation = "com/camunda/fox/platform/impl/jobexecutor/ext/config/multipleJobAcquisitions.xml";
    this.jobExecutorXmlSupport.setJobExecutorXmlLocation(jobExecutorXmlLocation);
    this.jobExecutorXmlSupport.startJobExecutor(this.platformJobExecutorService);
    
    Map<String, List<JobAcquisition>> jobAcquisitions = this.jobExecutorXmlSupport.getJobAcquisitions();
    Assert.assertEquals(1, jobAcquisitions.size());
    for (Entry<String, List<JobAcquisition>> entry : jobAcquisitions.entrySet()) {
      String key = entry.getKey();
      Assert.assertTrue(key.contains(jobExecutorXmlLocation));
      
      List<JobAcquisition> startedJobAcquisitions = entry.getValue();
      Assert.assertEquals(2, startedJobAcquisitions.size());
    }
    
    this.jobExecutorXmlSupport.stopJobExecutor(this.platformJobExecutorService);
  }
  
  private class MyJobExecutorXmlSupport extends JobExecutorXmlSupportImpl {
    
    private String jobExecutorXmlLocation;
    
    @Override
    protected String getJobExecutorXmlLocation() {
      return jobExecutorXmlLocation;
    }
    
    public void setJobExecutorXmlLocation(String jobExecutorXmlLocation) {
      this.jobExecutorXmlLocation = jobExecutorXmlLocation;
    }
    
  }
  
}
