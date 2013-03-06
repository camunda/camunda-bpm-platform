package org.camunda.bpm.engine.rest.dto.task;

import org.camunda.bpm.engine.form.FormData;

/**
 *
 * @author nico.rehwaldt
 */
public class FormDto {

  private String key;

  public void setKey(String form) {
    this.key = form;
  }

  public String getKey() {
    return key;
  }

  public static FormDto fromFormData(FormData formData) {
    FormDto dto = new FormDto();

    if (formData != null) {
      dto.key = formData.getFormKey();
    }

    return dto;
  }
}
