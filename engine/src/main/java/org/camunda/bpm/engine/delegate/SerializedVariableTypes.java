/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.delegate;

/**
 * <p>
 * This enum provides the variable types that the process engine uses.
 * A variable type is responsible for persisting a variable in a specific format.
 * Thus, a type consists of a name, the java types it can persist, the java types
 * of the serialized representation this type creates and additional variable
 * configuration options.
 * </p>
 *
 * <table>
 *   <tr>
 *   <th>Type Name</th>
 *   <th>Type of Persisted Value</th>
 *   <th>Type of Serialized Value</th>
 *   <th>Configuration Settings</th>
 *   <th>Additional Remarks</th>
 *   </tr>
 *
 *   <tr>
 *   <td>boolean</td>
 *   <td>Boolean</td>
 *   <td>Boolean</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>bytes</td>
 *   <td>byte[]</td>
 *   <td>byte[]</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>short</td>
 *   <td>Short</td>
 *   <td>Short</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>integer</td>
 *   <td>Integer</td>
 *   <td>Integer</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>long</td>
 *   <td>Long</td>
 *   <td>Long</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>double</td>
 *   <td>Double</td>
 *   <td>Double</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>date</td>
 *   <td>Date</td>
 *   <td>Long</td>
 *   <td>No configuration</td>
 *   <td>Serialized value is time in millis since 1/1/1970</td>
 *   </tr>
 *
 *   <tr>
 *   <td>serializable</td>
 *   <td>&lt;Custom Class&gt; implements Serializable</td>
 *   <td>byte[]</td>
 *   <td>No configuration</td>
 *   <td>Applies standard Java object serialization</td>
 *   </tr>
 *
 *   <tr>
 *   <td>null</td>
 *   <td>null</td>
 *   <td>null</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>string</td>
 *   <td>String</td>
 *   <td>String</td>
 *   <td>No configuration</td>
 *   <td></td>
 *   </tr>
 *
 *   <tr>
 *   <td>jpa-entity</td>
 *   <td>&lt;JPA Entity&gt;</td>
 *   <td>null</td>
 *   <td>
 *     <ul>
 *       <li>{@link #JPA_TYPE_CONFIG_CLASS_NAME}</li>
 *       <li>{@link #JPA_TYPE_CONFIG_ENTITY_ID}</li>
 *     </ul>
 *   </td>
 *   <td>The serialized value is <code>null</code> as the variable only stores
 *   a reference to a JPA Entity</td>
 *   </tr>
 *
 *   <tr>
 *   <td>SpinSerialization</td>
 *   <td>&lt;Custom Class&gt;</td>
 *   <td>String</td>
 *   <td>
 *     <ul>
 *       <li>{@link #SPIN_TYPE_DATA_FORMAT_ID}</li>
 *       <li>{@link #SPIN_TYPE_CONFIG_ROOT_TYPE}</li>
 *     </ul>
 *   </td>
 *   <td>This type persists variables using the camunda Spin library.
 *   The serialized value corresponds to the serialization formats it offers
 *   and that the format that is configured in the process engine.</td>
 *   </tr>
 * </table>
 *
 * @author Thorben Lindhauer
 */
public enum SerializedVariableTypes {

  /**
   * Requires no configuration.
   * Value has to be boolean.
   */
  Boolean("boolean"),

  /**
   * Requires no configuration.
   * Value has to be a byte array.
   */
  ByteArray("bytes"),

  /**
   * Requires no configuration.
   * Value has to be short.
   */
  Short("short"),

  /**
   * Requires no configuration.
   * Value has to be long.
   */
  Long("long"),

  /**
   * Requires no configuration.
   */
  Double("double"),

  /**
   * Requires no configuration.
   * Value has to be a long timestamp of milliseconds since 1/1/1970.
   */
  Date("date"),

  /**
   * Requires no configuration.
   * Value has to be a byte array.
   */
  Serializable("serializable"),

  /**
   * Requires no configuration.
   * Value has to be <code>null</code>.
   */
  Null("null"),

  /**
   * Requires no configuration.
   * Value has to be String.
   */
  String("string"),

  /**
   * Requires no configuration.
   * Value has to be int.
   */
  Integer("integer"),

  /**
   * Required configuration parameters are:
   * {@link JPA_TYPE_CONFIG_CLASS_NAME} and
   * {@link JPA_TYPE_CONFIG_ENTITY_ID}.
   * Value has to be <code>null</code>.
   */
  JPA("jpa-entity"),

  /**
   * Required configuration parameters are:
   * {@link SPIN_TYPE_DATA_FORMAT_ID} and
   * {@link SPIN_TYPE_CONFIG_ROOT_TYPE}.
   * Value has to be a String.
   */
  Spin("SpinSerialization");

  /**
   * The name of the Spin data format that can handle the serialized input; String.
   */
  public static final String SPIN_TYPE_DATA_FORMAT_ID = "dataFormatId";

  /**
   * The java type identifier that represents the root of this object.
   * Has to correspond to the spin data format's canonical type name.
   * Has to be a String value.
   */
  public static final String SPIN_TYPE_CONFIG_ROOT_TYPE = "rootType";

  /**
   * The class name of the JPA entity as a String.
   */
  public static final String JPA_TYPE_CONFIG_CLASS_NAME = "class";

  /**
   * The id of the JPA entity as a String value.
   */
  public static final String JPA_TYPE_CONFIG_ENTITY_ID = "id";

  protected String name;

  SerializedVariableTypes(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
