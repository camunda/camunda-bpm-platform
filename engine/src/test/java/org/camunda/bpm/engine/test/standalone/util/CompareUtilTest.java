/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.standalone.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.junit.Test;

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
    assertThat(CompareUtil.areNotInAscendingOrder(null, first, null, second)).isFalse();
    assertThat(CompareUtil.areNotInAscendingOrder(null, first, null, first)).isFalse();
    assertThat(CompareUtil.areNotInAscendingOrder(null, second, null, first)).isTrue();
    assertThat(CompareUtil.areNotInAscendingOrder(nullDate, nullDate, nullDate)).isFalse();

    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(first, second))).isFalse();
    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(first, first))).isFalse();
    assertThat(CompareUtil.areNotInAscendingOrder(Arrays.asList(second, first))).isTrue();
  }

  @Test
  public void testIsNotContainedIn() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.elementIsNotContainedInArray(element, values)).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInArray(element, values2)).isTrue();
    assertThat(CompareUtil.elementIsNotContainedInArray(null, values)).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInArray(null, nullValues)).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInArray(element, nullValues)).isFalse();

    assertThat(CompareUtil.elementIsNotContainedInList(element, Arrays.asList(values))).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInList(element, Arrays.asList(values2))).isTrue();
    assertThat(CompareUtil.elementIsNotContainedInList(null, Arrays.asList(values))).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInList(null, nullList)).isFalse();
    assertThat(CompareUtil.elementIsNotContainedInList(element, nullList)).isFalse();
  }

  @Test
  public void testIsContainedIn() {
    String element = "test";
    String [] values = {"test", "test1", "test2"};
    String [] values2 = {"test1", "test2"};
    String [] nullValues = null;
    List<String> nullList = null;

    assertThat(CompareUtil.elementIsContainedInArray(element, values)).isTrue();
    assertThat(CompareUtil.elementIsContainedInArray(element, values2)).isFalse();
    assertThat(CompareUtil.elementIsContainedInArray(null, values)).isFalse();
    assertThat(CompareUtil.elementIsContainedInArray(null, nullValues)).isFalse();
    assertThat(CompareUtil.elementIsContainedInArray(element, nullValues)).isFalse();

    assertThat(CompareUtil.elementIsContainedInList(element, Arrays.asList(values))).isTrue();
    assertThat(CompareUtil.elementIsContainedInList(element, Arrays.asList(values2))).isFalse();
    assertThat(CompareUtil.elementIsContainedInList(null, Arrays.asList(values))).isFalse();
    assertThat(CompareUtil.elementIsContainedInList(null, nullList)).isFalse();
    assertThat(CompareUtil.elementIsContainedInList(element, nullList)).isFalse();
  }
}
