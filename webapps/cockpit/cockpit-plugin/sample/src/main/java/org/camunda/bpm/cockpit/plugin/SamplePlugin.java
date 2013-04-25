package org.camunda.bpm.cockpit.plugin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.camunda.bpm.cockpit.plugin.api.Plugin;

/**
 *
 * @author nico.rehwaldt
 */
@WebListener
public class SamplePlugin implements ServletContextListener {
  private PluginInfo plugin;

  private class PluginInfo implements Plugin {

    private final String contextPath;
    private final String id;

    private PluginInfo(String id, String contextPath) {
      this.id = id;
      this.contextPath = contextPath;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getContextPath() {
      return contextPath;
    }

  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("Context initialized: " + sce);
    this.plugin = new PluginInfo("sample-plugin", sce.getServletContext().getContextPath());

    CockpitPlugins.getRegistry().registerPlugin(plugin);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("Context destroyed: " + sce);
    CockpitPlugins.getRegistry().unregisterPlugin(plugin.getId());
  }
}
