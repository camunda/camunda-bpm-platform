package org.camunda.bpm.cockpit.impl.web.filter.engine;

import static org.fest.assertions.Assertions.assertThat;
import java.util.regex.Matcher;

import java.util.regex.Pattern;

import org.camunda.bpm.webapp.impl.filter.ProcessEnginesFilter;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class EnginesFilterTest {

  @Test
  public void testHTML_FILE_PATTERN() throws Exception {

    // given
    Pattern pattern = ProcessEnginesFilter.HTML_FILE_PATTERN;

    // when
    Matcher matcher1 = pattern.matcher("/app/cockpit/");
    Matcher matcher2 = pattern.matcher("/app/cockpit/engine1/");
    Matcher matcher3 = pattern.matcher("/app/cockpit/engine1/something/asd.html");

    // then

    assertThat(matcher1.matches()).isTrue();
    assertThat(matcher1.groupCount()).isEqualTo(3);
    assertThat(matcher1.group(1)).isNull();

    assertThat(matcher2.matches()).isTrue();
    assertThat(matcher2.group(1)).isEqualTo("engine1");
    assertThat(matcher2.group(2)).isEmpty();

    assertThat(matcher3.matches()).isTrue();
    assertThat(matcher3.group(1)).isEqualTo("engine1");
    assertThat(matcher3.group(2)).isEqualTo("something/asd.html");
  }
}
