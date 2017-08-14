package org.camunda.bpm.spring.boot.starter.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

@ConfigurationProperties("management")
public class ManagementProperties {

  private Health health = new Health();

  /**
   * @return the health
   */
  public Health getHealth() {
    return health;
  }

  /**
   * @param health the health to set
   */
  public void setHealth(Health health) {
    this.health = health;
  }

  @Override
  public String toString() {
    return "ManagementProperties [health=" + health + "]";
  }

  public static class Health {

    private Camunda camunda = new Camunda();

    /**
     * @return the camunda
     */
    public Camunda getCamunda() {
      return camunda;
    }

    /**
     * @param camunda the camunda to set
     */
    public void setCamunda(Camunda camunda) {
      this.camunda = camunda;
    }

    @Override
    public String toString() {
      return joinOn(this.getClass())
        .add("camunda=" + camunda)
        .toString();
    }

    public class Camunda {
      private boolean enabled = true;

      /**
       * @return the enabled
       */
      public boolean isEnabled() {
        return enabled;
      }

      /**
       * @param enabled the enabled to set
       */
      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      @Override
      public String toString() {
        return joinOn(this.getClass())
          .add("enabled=" + enabled)
          .toString();
      }

    }
  }

}
