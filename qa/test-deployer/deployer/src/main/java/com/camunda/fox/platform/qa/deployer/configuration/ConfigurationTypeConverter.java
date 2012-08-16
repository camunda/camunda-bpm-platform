package com.camunda.fox.platform.qa.deployer.configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * 
*/
class ConfigurationTypeConverter {

  /**
   * A helper boxing method. Returns boxed class for a primitive class
   *
   * @param primitive A primitive class
   * @return Boxed class if class was primitive, unchanged class in other cases
   */
  public Class<?> box(Class<?> primitive) {
    if (!primitive.isPrimitive()) {
      return primitive;
    }

    if (int.class.equals(primitive)) {
      return Integer.class;
    } else if (long.class.equals(primitive)) {
      return Long.class;
    } else if (float.class.equals(primitive)) {
      return Float.class;
    } else if (double.class.equals(primitive)) {
      return Double.class;
    } else if (short.class.equals(primitive)) {
      return Short.class;
    } else if (boolean.class.equals(primitive)) {
      return Boolean.class;
    } else if (char.class.equals(primitive)) {
      return Character.class;
    } else if (byte.class.equals(primitive)) {
      return Byte.class;
    }

    throw new IllegalArgumentException("Unknown primitive type " + primitive);
  }

  /**
   * A helper converting method.
   *
   * Converts string to a class of given type
   *
   * @param value String value to be converted
   * @param to Type of desired value
   *
   * @param <T> Type of returned value
   * @return Value converted to a appropriate type
   */
  public <T> T convert(String value, Class<T> to) {
    if (String.class.equals(to)) {
      return to.cast(value);
    } else if (Integer.class.equals(to)) {
      return to.cast(Integer.valueOf(value));
    } else if (Double.class.equals(to)) {
      return to.cast(Double.valueOf(value));
    } else if (Long.class.equals(to)) {
      return to.cast(Long.valueOf(value));
    } else if (Boolean.class.equals(to)) {
      return to.cast(Boolean.valueOf(value));
    } else if (URL.class.equals(to)) {
      try {
        return to.cast(new URI(value).toURL());
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
      }
    } else if (URI.class.equals(to)) {
      try {
        return to.cast(new URI(value));
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
      }
    }

    throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + to.getName());
  }
}
