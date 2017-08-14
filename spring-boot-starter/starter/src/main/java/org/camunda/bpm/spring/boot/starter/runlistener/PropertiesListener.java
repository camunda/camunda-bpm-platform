package org.camunda.bpm.spring.boot.starter.runlistener;

import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Adds camunda.bpm.version properties to environment.
 */
public class PropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  private final CamundaBpmVersion version;

  /**
   * Default constructor, used when initializing via spring.factories.
   *
   * @see PropertiesListener#PropertiesListener(CamundaBpmVersion)
   */
  public PropertiesListener() {
    this(new CamundaBpmVersion());
  }

  /**
   * Initialize with version.
   *
   * @param version the current camundaBpmVersion instance.
   */
  PropertiesListener(CamundaBpmVersion version) {
    this.version = version;
  }

  @Override
  public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event) {
    event.getEnvironment()
      .getPropertySources()
      .addFirst(version.getPropertiesPropertySource());
  }

}
