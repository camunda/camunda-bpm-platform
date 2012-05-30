package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlParser;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

/**
 * @author roman.smirnov
 */
public class TestJobExecutorXmlParser {
  
  private JobExecutorXmlParser parser;

  @Before
  public void init() {
    this.parser = new JobExecutorXmlParserImpl();
  }
  
  @Test
  public void testEmptyRootTag() {
    List<JobExecutorXml> jobExecutorXmls = parser.parseJobExecutorXml("com/camunda/fox/platform/impl/jobexecutor/ext/config/emptyRootTag.xml");
    Assert.assertNotNull(jobExecutorXmls);
    Assert.assertEquals(1, jobExecutorXmls.size());
    Assert.assertEquals(0, jobExecutorXmls.get(0).getJobAcquisitions().size());
  }
  
  @Test
  public void testSingleJobAcquisition() {
    List<JobExecutorXml> jobExecutorXmls = parser.parseJobExecutorXml("com/camunda/fox/platform/impl/jobexecutor/ext/config/singleJobAcquisition.xml");
    Assert.assertNotNull(jobExecutorXmls);
    Assert.assertEquals(1, jobExecutorXmls.size());
    JobExecutorXml jobExecutorXml = jobExecutorXmls.get(0);
    Assert.assertEquals(1, jobExecutorXml.getJobAcquisitions().size());
    
    JobAcquisitionConfiguration jobAcquisitionConfiguration = jobExecutorXml.getJobAcquisitions().get(0);
    
    Assert.assertEquals("default", jobAcquisitionConfiguration.getAcquisitionName());
    Assert.assertEquals("SEQUENTIAL", jobAcquisitionConfiguration.getJobAcquisitionStrategy());
    
    Assert.assertEquals(10000, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS));
    Assert.assertEquals(50000, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS));
    Assert.assertEquals(10, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION));
  }
  
  @Test
  public void testMultipleJobAcquisition() {
    List<JobExecutorXml> jobExecutorXmls = parser.parseJobExecutorXml("com/camunda/fox/platform/impl/jobexecutor/ext/config/multipleJobAcquisitions.xml");
    Assert.assertNotNull(jobExecutorXmls);
    Assert.assertEquals(1, jobExecutorXmls.size());
    JobExecutorXml jobExecutorXml = jobExecutorXmls.get(0);
    Assert.assertEquals(2, jobExecutorXml.getJobAcquisitions().size());
    
    JobAcquisitionConfiguration firstJobAcquisitionConfiguration = jobExecutorXml.getJobAcquisitions().get(0);
    
    Assert.assertEquals("jobAcquisition1", firstJobAcquisitionConfiguration.getAcquisitionName());
    Assert.assertEquals("SEQUENTIAL", firstJobAcquisitionConfiguration.getJobAcquisitionStrategy());
    
    Assert.assertEquals(10000, firstJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS));
    Assert.assertEquals(50000, firstJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS));
    Assert.assertEquals(10, firstJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION));
    
    JobAcquisitionConfiguration secondJobAcquisitionConfiguration = jobExecutorXml.getJobAcquisitions().get(1);
    
    Assert.assertEquals("jobAcquisition2", secondJobAcquisitionConfiguration.getAcquisitionName());
    Assert.assertEquals("SEQUENTIAL", secondJobAcquisitionConfiguration.getJobAcquisitionStrategy());
    
    Assert.assertEquals(50000, secondJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS));
    Assert.assertEquals(10000, secondJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS));
    Assert.assertEquals(50, secondJobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION));
  }
  
  @Test
  public void testJobAcquisitionDefaultValues() {
    List<JobExecutorXml> jobExecutorXmls = parser.parseJobExecutorXml("com/camunda/fox/platform/impl/jobexecutor/ext/config/defaultValues.xml");
    Assert.assertNotNull(jobExecutorXmls);
    Assert.assertEquals(1, jobExecutorXmls.size());
    JobExecutorXml jobExecutorXml = jobExecutorXmls.get(0);
    Assert.assertEquals(1, jobExecutorXml.getJobAcquisitions().size());
    
    JobAcquisitionConfiguration jobAcquisitionConfiguration = jobExecutorXml.getJobAcquisitions().get(0);
    
    Assert.assertEquals("default", jobAcquisitionConfiguration.getAcquisitionName());
    Assert.assertEquals("SEQUENTIAL", jobAcquisitionConfiguration.getJobAcquisitionStrategy());
    
    Assert.assertEquals(300000, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS));
    Assert.assertEquals(5000, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS));
    Assert.assertEquals(3, jobAcquisitionConfiguration.getJobAcquisitionProperties().get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION));
  }

}
