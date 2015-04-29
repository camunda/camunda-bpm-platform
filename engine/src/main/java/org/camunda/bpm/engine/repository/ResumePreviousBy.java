package org.camunda.bpm.engine.repository;


/**
 * Contains the constants for the possible values the property {@link ProcessApplicationDeploymentBuilder#resumePreviousVersionsBy(String)}.
 */
public enum ResumePreviousBy {
  ;

  /**
   * Resume previous deployments that contain processes with the same key as in the new deployment
   */
  public static final String RESUME_BY_PROCESS_DEFINITION_KEY = "process-definition-key";

  /**
   * Resume previous deployments that have the same name as the new deployment
   */
  public static final String RESUME_BY_DEPLOYMENT_NAME = "deployment-name";
}