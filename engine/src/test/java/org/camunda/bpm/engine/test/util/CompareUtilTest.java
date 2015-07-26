package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Filip Hrisafov
 */
public class CompareUtilTest {

  @Test
  public void testValidateOrderDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2015, Calendar.MARCH, 15);
    Date first = calendar.getTime();
    calendar.set(2015, Calendar.AUGUST, 15);
    Date second = calendar.getTime();
    Date nullDate = null;
    assertThat(CompareUtil.validateOrder(null, first, null, second), is(true));
    assertThat(CompareUtil.validateOrder(null, first, null, first), is(true));
    assertThat(CompareUtil.validateOrder(null, second, null, first), is(false));
    assertThat(CompareUtil.validateOrder(nullDate, nullDate, nullDate), is(true));

    assertThat(CompareUtil.validateOrder(Arrays.asList(first, second)), is(true));
    assertThat(CompareUtil.validateOrder(Arrays.asList(first, first)), is(true));
    assertThat(CompareUtil.validateOrder(Arrays.asList(second, first)), is(false));
  }

  @Test
  public void testValidateContains() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.validateContains(element, values), is(true));
    assertThat(CompareUtil.validateContains(element, values2), is(false));
    assertThat(CompareUtil.validateContains(null, values), is(true));
    assertThat(CompareUtil.validateContains(null, nullValues), is(true));
    assertThat(CompareUtil.validateContains(element, nullValues), is(true));

    assertThat(CompareUtil.validateContains(element, Arrays.asList(values)), is(true));
    assertThat(CompareUtil.validateContains(element, Arrays.asList(values2)), is(false));
    assertThat(CompareUtil.validateContains(null, Arrays.asList(values)), is(true));
    assertThat(CompareUtil.validateContains(null, nullList), is(true));
    assertThat(CompareUtil.validateContains(element, nullList), is(true));
  }

  @Test
  public void testValidateNotContains() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.validateNotContains(element, values), is(false));
    assertThat(CompareUtil.validateNotContains(element, values2), is(true));
    assertThat(CompareUtil.validateNotContains(null, values), is(true));
    assertThat(CompareUtil.validateNotContains(null, nullValues), is(true));
    assertThat(CompareUtil.validateNotContains(element, nullValues), is(true));

    assertThat(CompareUtil.validateNotContains(element, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.validateNotContains(element, Arrays.asList(values2)), is(true));
    assertThat(CompareUtil.validateNotContains(null, Arrays.asList(values)), is(true));
    assertThat(CompareUtil.validateNotContains(null, nullList), is(true));
    assertThat(CompareUtil.validateNotContains(element, nullList), is(true));
  }
}
