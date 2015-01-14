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
package org.camunda.bpm.engine.cdi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Annotation for qualifying injection points using either
 * {@link ProcessVariable} or {@link ProcessVariableLocal} to denote that a
 * {@link TypedValue} should be injected instead of a normal Java object.
 * <ul>
 * <li>{@code @Inject @Typed @ProcessVariable TypedValue accountNumber}</li>
 * <li>{@code @Inject @Typed @ProcessVariable("accountNumber") TypedValue account}</li>
 * </ul>
 * In both cases, the process variable with the name 'accountNumber' is injected
 * as TypedValue. NOTE: injection points must be of type 'TypedValue'.
 * 
 * @author Michael Scholz
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Typed {
}