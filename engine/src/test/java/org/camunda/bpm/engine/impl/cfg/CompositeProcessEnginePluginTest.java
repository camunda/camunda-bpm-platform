package org.camunda.bpm.engine.impl.cfg;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class CompositeProcessEnginePluginTest {

  private static final ProcessEnginePlugin PLUGIN_A = processEnginePlugin("PluginA");
  private static final ProcessEnginePlugin PLUGIN_B = processEnginePlugin("PluginB");
  private static final InOrder ORDER = inOrder(PLUGIN_A, PLUGIN_B);

  private static final ProcessEngineConfigurationImpl CONFIGURATION = mock(ProcessEngineConfigurationImpl.class);
  private static final ProcessEngine ENGINE = mock(ProcessEngine.class);
  private InOrder inOrder;

  @Test
  public void addPlugin() throws Exception {
    CompositeProcessEnginePlugin composite = new CompositeProcessEnginePlugin(PLUGIN_A);

    assertThat(composite.getPlugins().size(), is(1));
    assertThat(composite.getPlugins().get(0), is(PLUGIN_A));

    composite.addProcessEnginePlugin(PLUGIN_B);
    assertThat(composite.getPlugins().size(), is(2));
    assertThat(composite.getPlugins().get(1), is(PLUGIN_B));

  }

  @Test
  public void addPlugins() throws Exception {
    CompositeProcessEnginePlugin composite = new CompositeProcessEnginePlugin(PLUGIN_A);
    composite.addProcessEnginePlugins(Arrays.asList(PLUGIN_B));

    assertThat(composite.getPlugins().size(), is(2));
    assertThat(composite.getPlugins().get(0), is(PLUGIN_A));
    assertThat(composite.getPlugins().get(1), is(PLUGIN_B));

  }

  @Test
  public void allPluginsOnPreInit() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).preInit(CONFIGURATION);

    ORDER.verify(PLUGIN_A).preInit(CONFIGURATION);
    ORDER.verify(PLUGIN_B).preInit(CONFIGURATION);
  }

  @Test
  public void allPluginsOnPostInit() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).postInit(CONFIGURATION);

    ORDER.verify(PLUGIN_A).postInit(CONFIGURATION);
    ORDER.verify(PLUGIN_B).postInit(CONFIGURATION);
  }

  @Test
  public void allPluginsOnPostProcessEngineBuild() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).postProcessEngineBuild(ENGINE);

    ORDER.verify(PLUGIN_A).postProcessEngineBuild(ENGINE);
    ORDER.verify(PLUGIN_B).postProcessEngineBuild(ENGINE);
  }

  @Test
  public void verifyToString() throws Exception {
    assertThat(new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).toString(), is("CompositeProcessEnginePlugin[PluginA, PluginB]"));
  }

  private static ProcessEnginePlugin processEnginePlugin(final String name) {
    ProcessEnginePlugin plugin = Mockito.mock(ProcessEnginePlugin.class);
    when(plugin.toString()).thenReturn(name);

    return plugin;
  }
}
