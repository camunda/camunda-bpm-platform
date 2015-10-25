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
  public void testHasEcludingOrderDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2015, Calendar.MARCH, 15);
    Date first = calendar.getTime();
    calendar.set(2015, Calendar.AUGUST, 15);
    Date second = calendar.getTime();
    Date nullDate = null;
    assertThat(CompareUtil.hasExcludingOrder(null, first, null, second), is(false));
    assertThat(CompareUtil.hasExcludingOrder(null, first, null, first), is(false));
    assertThat(CompareUtil.hasExcludingOrder(null, second, null, first), is(true));
    assertThat(CompareUtil.hasExcludingOrder(nullDate, nullDate, nullDate), is(false));

    assertThat(CompareUtil.hasExcludingOrder(Arrays.asList(first, second)), is(false));
    assertThat(CompareUtil.hasExcludingOrder(Arrays.asList(first, first)), is(false));
    assertThat(CompareUtil.hasExcludingOrder(Arrays.asList(second, first)), is(true));
  }

  @Test
  public void testHasExcludingContains() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.hasExcludingContains(element, values), is(false));
    assertThat(CompareUtil.hasExcludingContains(element, values2), is(true));
    assertThat(CompareUtil.hasExcludingContains(null, values), is(false));
    assertThat(CompareUtil.hasExcludingContains(null, nullValues), is(false));
    assertThat(CompareUtil.hasExcludingContains(element, nullValues), is(false));

    assertThat(CompareUtil.hasExcludingContains(element, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.hasExcludingContains(element, Arrays.asList(values2)), is(true));
    assertThat(CompareUtil.hasExcludingContains(null, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.hasExcludingContains(null, nullList), is(false));
    assertThat(CompareUtil.hasExcludingContains(element, nullList), is(false));
  }

  @Test
  public void testHasExcludingNotContains() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.hasExcludingNotContains(element, values), is(true));
    assertThat(CompareUtil.hasExcludingNotContains(element, values2), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(null, values), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(null, nullValues), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(element, nullValues), is(false));

    assertThat(CompareUtil.hasExcludingNotContains(element, Arrays.asList(values)), is(true));
    assertThat(CompareUtil.hasExcludingNotContains(element, Arrays.asList(values2)), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(null, Arrays.asList(values)), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(null, nullList), is(false));
    assertThat(CompareUtil.hasExcludingNotContains(element, nullList), is(false));
  }
}
