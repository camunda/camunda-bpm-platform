package org.camunda.bpm.engine.test.standalone.util;

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
  public void testDateNotInAnAscendingOrder() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2015, Calendar.MARCH, 15);
    Date first = calendar.getTime();
    calendar.set(2015, Calendar.AUGUST, 15);
    Date second = calendar.getTime();
    Date nullDate = null;
    assertThat(CompareUtil.areNotInAscendingOrder(null, first, null, second), is(false));
    assertThat(CompareUtil.areNotInAscendingOrder(null, first, null, first), is(false));
    assertThat(CompareUtil.areNotInAscendingOrder(null, second, null, first), is(true));
    assertThat(CompareUtil.areNotInAscendingOrder(nullDate, nullDate, nullDate), is(false));

    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(first, second)), is(false));
    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(first, first)), is(false));
    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(second, first)), is(true));
  }

  @Test
  public void testIsNotContainedIn() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.elementIsNotContainedInArray(element, values), is(false));
    assertThat(CompareUtil.elementIsNotContainedInArray(element, values2), is(true));
    assertThat(CompareUtil.elementIsNotContainedInArray(null, values), is(false));
    assertThat(CompareUtil.elementIsNotContainedInArray(null, nullValues), is(false));
    assertThat(CompareUtil.elementIsNotContainedInArray(element, nullValues), is(false));

    assertThat(CompareUtil.elementIsNotContainedInList(element, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.elementIsNotContainedInList(element, Arrays.asList(values2)), is(true));
    assertThat(CompareUtil.elementIsNotContainedInList(null, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.elementIsNotContainedInList(null, nullList), is(false));
    assertThat(CompareUtil.elementIsNotContainedInList(element, nullList), is(false));
  }

  @Test
  public void testIsContainedIn() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.elementIsContainedInArray(element, values), is(true));
    assertThat(CompareUtil.elementIsContainedInArray(element, values2), is(false));
    assertThat(CompareUtil.elementIsContainedInArray(null, values), is(false));
    assertThat(CompareUtil.elementIsContainedInArray(null, nullValues), is(false));
    assertThat(CompareUtil.elementIsContainedInArray(element, nullValues), is(false));

    assertThat(CompareUtil.elementIsContainedInList(element, Arrays.asList(values)), is(true));
    assertThat(CompareUtil.elementIsContainedInList(element, Arrays.asList(values2)), is(false));
    assertThat(CompareUtil.elementIsContainedInList(null, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.elementIsContainedInList(null, nullList), is(false));
    assertThat(CompareUtil.elementIsContainedInList(element, nullList), is(false));
  }
}
