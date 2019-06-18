package org.camunda.bpm.engine.impl.form.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.junit.Assert;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class TestDateFormType {
  private final String datePatterns[] = new String[] { "MM-dd-yyyy", "yyyy-MM-dd" };
  private final String dateStr = "2019-04-06";
  private final Date date;
  private final DateValue dateValue;

  public TestDateFormType() throws ParseException {
    date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
    dateValue = Variables.dateValue(date);
  }
  
  @Test public void testConvertToModelValue() {
    for (String datePattern : datePatterns ) {
      final DateFormType dateFormType = new DateFormType(datePattern);
      final StringValue stringValue = Variables.stringValue(new SimpleDateFormat(datePattern).format(date));
      final TypedValue modelValue = dateFormType.convertToModelValue(stringValue);
      Assert.assertEquals(date, modelValue.getValue());
    }
  }

  @Test public void testConvertToFormValue() {
    for (String datePattern : datePatterns ) {
      final DateFormType dateFormType = new DateFormType(datePattern);
      final TypedValue formValue = dateFormType.convertToFormValue(dateValue);
      Assert.assertEquals(new SimpleDateFormat(datePattern).format(date), formValue.getValue());
    }
  }
}
