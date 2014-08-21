package org.camunda.spin.impl.xml.dom;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Stefan Hentschel.
 */
public interface XmlDomConfigurable {

  XmlDomMapperConfiguration mapper();

  XmlDomDataFormat done();

  void applyTo(Marshaller marshaller);
  void applyTo(Unmarshaller unmarshaller);
}
