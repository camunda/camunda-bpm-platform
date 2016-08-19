package org.camunda.bpm.engine.form;

import org.camunda.bpm.engine.impl.form.FormFieldImpl;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Askar Akhmerov
 */
public class FormFieldTest {

  @Test
  public void isBusinessKey() throws Exception {
    FormField toTest = new FormFieldImpl();
    assertThat(toTest.isBusinessKey(),is(false));
    ((FormFieldImpl)toTest).setBusinessKey(true);
    assertThat(toTest.isBusinessKey(),is(true));
  }

}