# Configuring XML Handling

Spin can be configured to change XML parsing, writing and mapping settings. Spin uses JAXB and DOM to handle XML. The XML data format therefore uses instances of `javax.xml.parsers.DocumentBuilderFactory`, `javax.xml.transform.TransformerFactory` and `javax.xml.bind.JAXBContext` that can be configured using Spin's [configuration mechanism][configuring-data-formats]. For example, a custom application may provide an implementation of `org.camunda.spin.spi.DataFormatConfigurator` that exchanges the `JAXBContext` Spin uses and caches the context to improve performance.

The data format class to register a configurator for is `org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat`. An instance of this class provides setter methods for the above-mentioned entities that can be used to replace the default object mapper. Please refer to the [JDK documentation][jdk-7-doc] on what configuration can be applied.

[jdk-7-doc]: http://docs.oracle.com/javase/7/docs/api/
[configuring-data-formats]: ../extend/configuring-data-formats.md