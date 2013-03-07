package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.util;


/**
 * Code portions taken from {@link org.springframework.util.ClassUtils}
 * @author christian.lipphardt@camunda.com
 */
public class ClassUtils {

  /**
   * Determine whether the {@link Class} identified by the supplied name is present
   * and can be loaded. Will return <code>false</code> if either the class or
   * one of its dependencies is not present or cannot be loaded.
   * @param className the name of the class to check
   * @param classLoader the class loader to use
   * (may be <code>null</code>, which indicates the default class loader)
   * @return whether the specified class is present
   */
  public static boolean isPresent(String className, ClassLoader classLoader) {
    try {
      forName(className, classLoader);
      return true;
    }
    catch (Throwable ex) {
      // Class or one of its dependencies is not present...
      return false;
    }
  }
  
  /**
   * Replacement for <code>Class.forName()</code>.
   * It is capable of resolving inner class names in Java source
   * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
   * @param name the name of the Class
   * @param classLoader the class loader to use
   * (may be <code>null</code>, which indicates the default class loader)
   * @return Class instance for the supplied name
   * @throws ClassNotFoundException if the class was not found
   * @throws LinkageError if the class file could not be loaded
   * @see Class#forName(String, boolean, ClassLoader)
   */
  public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
    ClassLoader classLoaderToUse = classLoader;
    if (classLoaderToUse == null) {
      classLoaderToUse = getDefaultClassLoader();
    }
    try {
      return classLoaderToUse.loadClass(name);
    }
    catch (ClassNotFoundException ex) {
      int lastDotIndex = name.lastIndexOf('.');
      if (lastDotIndex != -1) {
        String innerClassName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
        try {
          return classLoaderToUse.loadClass(innerClassName);
        }
        catch (ClassNotFoundException ex2) {
          // swallow - let original exception get through
        }
      }
      throw ex;
    }
  }
  
  /**
   * Return the default ClassLoader to use: typically the thread context
   * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
   * class will be used as fallback.
   * <p>Call this method if you intend to use the thread context ClassLoader
   * in a scenario where you absolutely need a non-null ClassLoader reference:
   * for example, for class path resource loading (but not necessarily for
   * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
   * reference as well).
   * @return the default ClassLoader (never <code>null</code>)
   * @see java.lang.Thread#getContextClassLoader()
   */
  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    }
    catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back to system class loader...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = ClassUtils.class.getClassLoader();
    }
    return cl;
  }
  
}
