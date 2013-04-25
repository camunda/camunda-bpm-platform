package org.camunda.bpm.cockpit.resources;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.camunda.bpm.cockpit.CockpitApplication;
import org.camunda.bpm.cockpit.plugin.CockpitPlugins;
import org.camunda.bpm.cockpit.plugin.api.Plugin;

/**
 *
 * @author nico.rehwaldt
 */
@Path("/plugins")
public class PluginsResource {

  @GET
  public List<Plugin> getPlugins() {

    return CockpitPlugins.getPlugins();
  }
}
