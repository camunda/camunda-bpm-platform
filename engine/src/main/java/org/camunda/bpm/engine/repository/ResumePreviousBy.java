package org.camunda.bpm.engine.repository;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;

/**
 * Enum to contain the constants for the possible values the property {@link ProcessArchiveXml#PROP_RESUME_PREVIOUS_BY} can take.
 */
public enum ResumePreviousBy{
  ;
  
  public static final String RESUME_BY_PROCESS_DEFINITION_KEY = "process-definition-key";
  
  public static final String RESUME_BY_DEPLOYMENT_NAME = "deployment-name";
}