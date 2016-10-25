package org.camunda.bpm.webapp.impl.engine;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.DefaultCockpitRuntimeDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.webapp.impl.IllegalWebAppConfigurationException;
import org.junit.After;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class EnginesFilterTest {

  @Test
  public void testHTML_FILE_PATTERN() throws Exception {

    // given
    Pattern pattern = ProcessEnginesFilter.APP_PREFIX_PATTERN;

    // when
    Matcher matcher1 = pattern.matcher("/app/cockpit/");
    Matcher matcher2 = pattern.matcher("/app/cockpit/engine1/");
    Matcher matcher3 = pattern.matcher("/app/cockpit/engine1/something/asd.html");
    Matcher matcher4 = pattern.matcher("/app/admin/engine1/something/asd.html");
    Matcher matcher5 = pattern.matcher("/app/cockpit/index.html");
    Matcher matcher6 = pattern.matcher("/app/tasklist/spring-engine/");

    // then
    assertThat(matcher1.matches()).isTrue();
    assertThat(matcher1.group(1)).isEqualTo("cockpit");
    assertThat(matcher1.groupCount()).isEqualTo(3);
    assertThat(matcher1.group(2)).isNull();

    assertThat(matcher2.matches()).isTrue();
    assertThat(matcher2.group(1)).isEqualTo("cockpit");
    assertThat(matcher2.group(2)).isEqualTo("engine1");
    assertThat(matcher2.group(3)).isEmpty();

    assertThat(matcher3.matches()).isTrue();
    assertThat(matcher3.group(1)).isEqualTo("cockpit");
    assertThat(matcher3.group(2)).isEqualTo("engine1");
    assertThat(matcher3.group(3)).isEqualTo("something/asd.html");

    assertThat(matcher4.matches()).isTrue();
    assertThat(matcher4.group(1)).isEqualTo("admin");
    assertThat(matcher4.group(2)).isEqualTo("engine1");
    assertThat(matcher4.group(3)).isEqualTo("something/asd.html");

    assertThat(matcher5.matches()).isTrue();
    assertThat(matcher5.group(1)).isEqualTo("cockpit");
    assertThat(matcher5.group(2)).isEqualTo("index.html");
    assertThat(matcher5.group(3)).isEmpty();

    assertThat(matcher6.matches()).isTrue();
    assertThat(matcher6.group(1)).isEqualTo("tasklist");
    assertThat(matcher6.group(2)).isEqualTo("spring-engine");
    assertThat(matcher6.group(3)).isEmpty();
  }

  @Test
  public void testGetDefaultProcessEngine() {

    // see https://app.camunda.com/jira/browse/CAM-2126

    // runtime delegate returns single, non-default-named process engine engine

    Cockpit.setCockpitRuntimeDelegate(new DefaultCockpitRuntimeDelegate() {

      protected ProcessEngineProvider loadProcessEngineProvider() {
        return null;
      }

      public Set<String> getProcessEngineNames() {
        return Collections.singleton("foo");
      }
      public ProcessEngine getDefaultProcessEngine() {
        return null;
      }
    });

    ProcessEnginesFilter processEnginesFilter = new ProcessEnginesFilter();
    String defaultEngineName = processEnginesFilter.getDefaultEngineName();
    assertThat(defaultEngineName).isEqualTo("foo");


    // now it returns 'null'

    Cockpit.setCockpitRuntimeDelegate(new DefaultCockpitRuntimeDelegate() {

      protected ProcessEngineProvider loadProcessEngineProvider() {
        return null;
      }

      public Set<String> getProcessEngineNames() {
        return Collections.emptySet();
      }
      public ProcessEngine getDefaultProcessEngine() {
        return null;
      }
    });

    try {
      defaultEngineName = processEnginesFilter.getDefaultEngineName();
      fail();
    } catch(IllegalWebAppConfigurationException e) {
      // expected
    }

  }

  @After
  public void cleanup() {
    Cockpit.setCockpitRuntimeDelegate(null);
  }
}
