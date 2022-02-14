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
package org.camunda.bpm.engine.authorization;

/**
 * <p>A permission represents an authorization to interact with a given 
 * resource in a specific way. See {@link Permissions} for a set of built-in 
 * permissions and {@link Authorization} for general overview on authorizations.</p>
 *  
 * <p>In Camunda Platform, multiple permissions are grouped into an {@link Authorization}.
 * For efficient storage and checking of authorizations, the permissons that make
 * up an authorization are coded into a single integer.
 * The implication of this design is that a permission must have a unique integer value 
 * and it must be a power of two, ie 2^0, 2^1, 2^2, 2^3, 2^4 ...
 * 
 * The permission can then be added to an authorization using bitwise OR: 
 * <pre>
 *        Auth: 0000001001001
 * Perm to add: 0000000010000 
 * bit OR (|) : 0000001011001 
 * </pre> 
 * 
 * and removed using bitwise AND of the inverted value: 
 * <pre>
 *        Auth: 0000001001001
 * Perm to rem: 0000000001000 
 * invert (~) : 1111111110111
 * bit AND (&): 0000001000001    
 * </pre>
 * 
 * <h2>Defining a custom Permission</h2>
 * The XxxPermissions classes contains the values of the  built-in
 * permissions (i.e. {@link Permissions}, {@link ProcessDefinitionPermissions},
 * {@link ProcessInstancePermissions}, {@link TaskPermissions},
 * all can be found in {@link org.camunda.bpm.engine.authorization authorization} package).
 * In order to define a custom permission, you must provide
 * an implementation of this interface such that the {@link #getValue()} 
 * method returns an integer which is a power of two and not yet used by any of the
 * built-in Permissions. Keep the Permission's names unique as well.
 * You must implement also {@link #getTypes()} and make sure that
 * the permission values are not already reserved for the desired
 * {@link Resource}.</p>
 * 
 * 
 * @author Daniel Meyer
 * @since 7.0
 */
public interface Permission {
  
  /** returns the name of the permission, ie. 'UPDATE' */
  String getName();
  
  /** returns the unique numeric value of the permission.
   * Must be a power of 2. ie 2^0, 2^1, 2^2, 2^3, 2^4 ... */
  int getValue();
  
  /** returns the resource types which are allowed for this permission */
  Resource[] getTypes();

}
