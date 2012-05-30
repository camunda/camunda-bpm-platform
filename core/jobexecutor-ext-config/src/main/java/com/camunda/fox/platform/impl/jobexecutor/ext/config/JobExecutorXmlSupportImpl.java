package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlParser;
import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlSupport;
import com.camunda.fox.platform.impl.jobexecutor.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.acquisition.JobAcquisition;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;


public class JobExecutorXmlSupportImpl implements JobExecutorXmlSupport {
  
  private static final String META_INF_JOB_EXECUTOR_XML = "META-INF/job-executor.xml";
  private Logger log = Logger.getLogger(JobExecutorXmlSupportImpl.class.getName());
  /** process engines by filename */
  private Map<String, List<JobAcquisition>> jobAcquisitions = new HashMap<String, List<JobAcquisition>>();

  @Override
  public void startJobExecutor(PlatformJobExecutorService platformJobExecutorService) {
    List<JobExecutorXml> parsedJobExecutorXml = this.parseConfigurationFiles();
    for (JobExecutorXml jobExecutorXml : parsedJobExecutorXml) {
      this.startJobExecutor(jobExecutorXml, platformJobExecutorService);
    }
  }
  
  protected void startJobExecutor(JobExecutorXml jobExecutorXml, PlatformJobExecutorService platformJobExecutorService) {
    List<JobAcquisition> startedJobAcquisitions = new ArrayList<JobAcquisition>();
    log.info("Found '"+jobExecutorXml.getJobAcquisitions().size()+"' job-acquisition definitions in file "+jobExecutorXml.getResourceName());
    
    for (JobAcquisitionConfiguration jobAcquisitionConfiguration : jobExecutorXml.getJobAcquisitions()) {
      try {
        startedJobAcquisitions.add((JobAcquisition) platformJobExecutorService.startJobAcquisition(jobAcquisitionConfiguration));
      } catch (Exception e) {
        log.log(Level.SEVERE, "Exception while staring job-acquisition '" + jobAcquisitionConfiguration.getAcquisitionName() + "' defined in file '"+jobExecutorXml.getResourceName()+"'.", e);
      }
    }
    if (!startedJobAcquisitions.isEmpty()) {
      this.jobAcquisitions.put(jobExecutorXml.getResourceName(), startedJobAcquisitions);
    }
  }

  @Override
  public void stopJobExecutor(PlatformJobExecutorService platformJobExecutorService) {
    List<String> jobAcquisitionsFailedToStop = new ArrayList<String>();
    
    for (Entry<String, List<JobAcquisition>> jobAcquisitionsByFilename : jobAcquisitions.entrySet()) {
      String filename = jobAcquisitionsByFilename.getKey();
      List<JobAcquisition> jobAcquisitions = jobAcquisitionsByFilename.getValue();
      log.info("Stopping job-acquisition defined in '"+filename+"'");
      for (JobAcquisition jobAcquisition : jobAcquisitions) {
        String name = jobAcquisition.getJobAcquisitionConfiguration().getAcquisitionName();
        try {
          platformJobExecutorService.stopJobAcquisition(name);
        }catch (Exception e) {
          log.log(Level.SEVERE, "Exception while stopping job-acquisition with name '"+name+"' defined in file '"+filename, e);
          jobAcquisitionsFailedToStop.add(name);
        }
      }
    }
    if(jobAcquisitionsFailedToStop.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      errorMessage.append("The following job-acquisitions failed to stop and might still be running:");
      errorMessage.append("\n");
      for (String engineName : jobAcquisitionsFailedToStop) {
        errorMessage.append("   ");
        errorMessage.append(engineName);
      }
      errorMessage.append("Consider restarting the fox platform.");
      log.severe(errorMessage.toString());      
    }
  }
  
  protected List<JobExecutorXml> parseConfigurationFiles() {
    JobExecutorXmlParser parser = getParser();    
    String jobExecutorXml = getJobExecutorXmlLocation();
    return parser.parseJobExecutorXml(jobExecutorXml);
  }
  
  protected JobExecutorXmlParser getParser() {
    return ServiceLoaderUtil.loadService(JobExecutorXmlParser.class, JobExecutorXmlParserImpl.class);
  }
  
  protected String getJobExecutorXmlLocation() {
    return META_INF_JOB_EXECUTOR_XML;
  }
  
  protected Map<String, List<JobAcquisition>> getJobAcquisitions() {
    return this.jobAcquisitions;
  }

}
