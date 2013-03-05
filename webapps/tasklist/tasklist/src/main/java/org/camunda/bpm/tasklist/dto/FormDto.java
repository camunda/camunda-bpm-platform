package org.camunda.bpm.tasklist.dto;

/**
 * @author drobisch
 * @author nico.rehwaldt
 */
public class FormDto {

  private String formKey;
  private String applicationContextPath;

  private String formSuffix;

  public FormDto(String formKey, String applicationContextPath, String formSuffix) {
    this.formKey = formKey;
    this.applicationContextPath = applicationContextPath;
    this.formSuffix = formSuffix;
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public String getApplicationContextPath() {
    return applicationContextPath;
  }

  public void setApplicationContextPath(String applicationContextPath) {
    this.applicationContextPath = applicationContextPath;
  }

  public String getFormSuffix() {
    return formSuffix;
  }

  public void setFormSuffix(String formSuffix) {
    this.formSuffix = formSuffix;
  }
}
