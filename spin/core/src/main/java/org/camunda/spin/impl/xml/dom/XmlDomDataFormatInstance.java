package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatInstance;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;

public class XmlDomDataFormatInstance implements DataFormatInstance<SpinXmlTreeElement> {

  public XmlDomDataFormat dataFormat;
  
  public XmlDomDataFormatInstance(XmlDomDataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public DataFormat<SpinXmlTreeElement> getDataFormat() {
    return dataFormat;
  }

  public DataFormatReader getReader() {
    return new XmlDomDataFormatReader();
  }

}
