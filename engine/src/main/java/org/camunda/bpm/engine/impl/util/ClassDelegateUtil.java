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
package org.camunda.bpm.engine.impl.util;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Roman Smirnov
 *
 */
public class ClassDelegateUtil {

  private static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }

  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    ArtifactFactory artifactFactory = Context.getProcessEngineConfiguration().getArtifactFactory();

    try {
      Class<?> clazz = ReflectUtil.loadClass(className);

      Object object = artifactFactory.getArtifact(clazz);

      applyFieldDeclaration(fieldDeclarations, object);
      return object;
    }
    catch (Exception e) {
      throw LOG.exceptionWhileInstantiatingClass(className, e);
    }

  }

  public static void applyFieldDeclaration(List<FieldDeclaration> fieldDeclarations, Object target) {
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        applyFieldDeclaration(declaration, target);
      }
    }
  }

  public static void applyFieldDeclaration(FieldDeclaration declaration, Object target) {
    Method setterMethod = ReflectUtil.getSetter(declaration.getName(),
      target.getClass(), declaration.getValue().getClass());

    if(setterMethod != null) {
      try {
        setterMethod.invoke(target, declaration.getValue());
      }
      catch (Exception e) {
        throw LOG.exceptionWhileApplyingFieldDeclatation(declaration.getName(), target.getClass().getName(), e);
      }
    }
    else {
      Field field = ReflectUtil.getField(declaration.getName(), target);
      ensureNotNull("Field definition uses unexisting field '" + declaration.getName() + "' on class " + target.getClass().getName(), "field", field);
      // Check if the delegate field's type is correct
      if (!fieldTypeCompatible(declaration, field)) {
        throw LOG.incompatibleTypeForFieldDeclaration(declaration, target, field);
      }
      ReflectUtil.setField(field, target, declaration.getValue());
    }
  }

  public static boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if(declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {
      // Null can be set any field type
      return true;
    }
  }

}
