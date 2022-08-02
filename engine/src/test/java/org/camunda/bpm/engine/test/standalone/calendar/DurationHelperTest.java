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
package org.camunda.bpm.engine.test.standalone.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.engine.impl.calendar.DurationHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.After;
import org.junit.Test;

public class DurationHelperTest {

  @After
  public void tearDown() {
    ClockUtil.reset();
  }

  @Test
  public void shouldNotExceedNumber() throws Exception {
    // given
    ClockUtil.setCurrentTime(new Date(0));

    // when
    DurationHelper dh = new DurationHelper("R2/PT10S");

    // then
    ClockUtil.setCurrentTime(new Date(15000));
    assertThat(dh.getDateAfter().getTime()).isEqualTo(20000);

    ClockUtil.setCurrentTime(new Date(30000));
    assertThat(dh.getDateAfter()).isNull();
  }

  @Test
  public void shouldNotExceedNumberWithStartDateExpression() throws Exception {
    // given
    ClockUtil.setCurrentTime(parse("19700101-00:00:00"));

    // when
    DurationHelper dh = new DurationHelper("R2/1970-01-01T00:01:00/PT10S");

    // then should trigger twice
    ClockUtil.setCurrentTime(parse("19700101-00:00:05"));
    assertThat(dh.getDateAfter()).isEqualTo(parse("19700101-00:01:00"));

    ClockUtil.setCurrentTime(parse("19700101-00:01:05"));
    assertThat(dh.getDateAfter()).isEqualTo(parse("19700101-00:01:10"));

    ClockUtil.setCurrentTime(parse("19700101-00:01:15"));
    assertThat(dh.getDateAfter()).isNull();
  }

  @Test
  public void shouldNotExceedNumberPeriods() throws Exception {
    // given
    ClockUtil.setCurrentTime(parse("19700101-00:00:00"));

    // when
    DurationHelper dh = new DurationHelper("R2/1970-01-01T00:00:00/1970-01-01T00:00:10");

    // then
    ClockUtil.setCurrentTime(parse("19700101-00:00:15"));
    assertThat(dh.getDateAfter()).isEqualTo(parse("19700101-00:00:20"));

    ClockUtil.setCurrentTime(parse("19700101-00:00:30"));
    assertThat(dh.getDateAfter()).isNull();
  }

  @Test
  public void shouldNotExceedNumberNegative() throws Exception {
    // given
    ClockUtil.setCurrentTime(parse("19700101-00:00:00"));

    // when
    DurationHelper dh = new DurationHelper("R2/PT10S/1970-01-01T00:00:50");

    // then
    ClockUtil.setCurrentTime(parse("19700101-00:00:20"));
    assertThat(dh.getDateAfter()).isEqualTo(parse("19700101-00:00:30"));

    ClockUtil.setCurrentTime(parse("19700101-00:00:35"));
    assertThat(dh.getDateAfter()).isEqualTo(parse("19700101-00:00:40"));
  }

  @Test
  public void shouldNotExceedNumberWithStartDate() throws Exception {
    // given

    // when
    DurationHelper dh = new DurationHelper("R2/PT10S", new Date(0));

    // then
    assertThat(dh.getDateAfter(new Date(15000)).getTime()).isEqualTo(20000);
    assertThat(dh.getDateAfter(new Date(30000))).isNull();
  }

  @Test
  public void shouldNotExceedNumberPeriodsWithStartDate() throws Exception {
    // given

    // when
    DurationHelper dh = new DurationHelper("R2/1970-01-01T00:00:00/1970-01-01T00:00:10", parse("19700101-00:00:00"));

    // then
    assertThat(dh.getDateAfter(parse("19700101-00:00:15"))).isEqualTo(parse("19700101-00:00:20"));
    assertThat(dh.getDateAfter(parse("19700101-00:00:30"))).isNull();
  }

  @Test
  public void shouldNotExceedNumberNegativeWithStartDate() throws Exception {
    // given

    // when
    DurationHelper dh = new DurationHelper("R2/PT10S/1970-01-01T00:00:50", parse("19700101-00:00:00"));

    // then
    assertThat(dh.getDateAfter(parse("19700101-00:00:20"))).isEqualTo(parse("19700101-00:00:30"));
    assertThat(dh.getDateAfter(parse("19700101-00:00:35"))).isEqualTo(parse("19700101-00:00:40"));
  }

  @Test
  public void shouldParseAllSupportedISO8601DurationPatterns() throws Exception {
    // given

    // when
    DurationHelper PnYnMnDTnHnMnS = new DurationHelper("P1Y5M21DT19H47M55S", parse("19700101-00:00:00"));
    DurationHelper PnW = new DurationHelper("P2W", parse("19700101-00:00:00"));

    // then
    assertThat(PnYnMnDTnHnMnS.getDateAfter()).isEqualTo(parse("19710622-19:47:55"));
    assertThat(PnW.getDateAfter()).isEqualTo(parse("19700115-00:00:00"));
  }

  @Test
  public void shouldParseP4W() throws Exception {
    // given

    // when
    DurationHelper PnW = new DurationHelper("P4W", parse("19700101-00:00:00"));

    // then
    assertThat(PnW.getDateAfter()).isEqualTo(parse("19700129-00:00:00"));
  }

  @Test
  public void shouldParseP5W() throws Exception {
    // given

    // when
    DurationHelper PnW = new DurationHelper("P5W", parse("19700101-00:00:00"));

    // then
    assertThat(PnW.getDateAfter()).isEqualTo(parse("19700205-00:00:00"));
  }

  @Test
  public void shouldParseP22W() throws Exception {
    // given

    // when
    DurationHelper PnW = new DurationHelper("P22W", parse("19700101-00:00:00"));

    // then
    assertThat(PnW.getDateAfter()).isEqualTo(parse("19700604-00:00:00"));
  }

  private Date parse(String str) throws Exception {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    return simpleDateFormat.parse(str);
  }


}
