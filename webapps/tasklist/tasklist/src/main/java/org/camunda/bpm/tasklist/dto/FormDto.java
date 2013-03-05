package org.camunda.bpm.tasklist.dto;

/**
 * @author drobisch
 * @author nico.rehwaldt
 */
public class FormDto {

  private String formKey;
  private String applicationContextPath;

  public FormDto(String formKey, String applicationContextPath) {
    this.formKey = formKey;
    this.applicationContextPath = applicationContextPath;
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
}
