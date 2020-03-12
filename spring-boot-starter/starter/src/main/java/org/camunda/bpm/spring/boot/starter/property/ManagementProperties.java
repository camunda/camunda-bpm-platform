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
