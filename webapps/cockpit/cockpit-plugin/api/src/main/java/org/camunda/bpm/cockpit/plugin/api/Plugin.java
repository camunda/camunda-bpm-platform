package org.camunda.bpm.cockpit.plugin.api;

/**
 *
 * @author nico.rehwaldt
 */
public interface Plugin {

  /**
   * Returns the id of the plugin
   *
   * @return
   */
  public String getId();

  /**
   * Returns the context path to this plugin
   *
   * @return
   */
  public String getContextPath();
}
