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
package org.camunda.bpm.client.spi;

/**
 * Maps a java object to the data format's internal representation and vice versa.
 */
public interface DataFormat {

  /**
   * Returns the data format name.
   */
  String getName();

  /**
   * Returns true if this data format can map the provided Java Object.
   *
   * @param parameter the java object to check
   * @return true if this object can be mapped.
   */
  public boolean canMap(Object value);

  /**
   * Writes a java object to a data format's internal data representation.
   *
   * @param value object that is written into internal data representation
   * @return the data format's internal representation of that object
   */
  public String writeValue(Object value);

  /**
   * Reads the internal representation of a data format to a java object of the
   * desired class.
   *
   * @param value the object to be read
   * @param typeIdentifier the class to map the object to
   * @return a java object of the specified class that was populated with the input
   * parameter
   */
  public <T> T readValue(String value, String typeIdentifier);

  /**
   * Reads the internal representation of a data format to a java object of the
   * desired class.
   *
   * @param value the object to be read
   * @param cls a data-format-specific type identifier that describes
   *   the class to map to
   * @return a java object of the specified class that was populated with the input
   * parameter
   */
  public <T> T readValue(String value, Class<T> cls);

  /**
   * Returns a data-format-specific canonical type name.
   */
  String getCanonicalTypeName(Object value);

}
