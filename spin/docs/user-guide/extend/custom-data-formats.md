# Implementing custom Data Formats

A Spin data format is an implementation of the interface `org.camunda.spin.spi.DataFormat`. An implementation of this interface can be registered by implementing the SPI `org.camunda.spin.spi.DataFormatProvider`. Spin uses the Java platform's service loader mechanism to lookup provider implementations at runtime. 

In order to provide a custom dataformat, you have to

* Provide a custom implementation of `org.camunda.spin.spi.DataFormat`
* Provide a custom implementation of `org.camunda.spin.spi.DataFormatProvider`
* Add the provider's fully qualified classname to a file named `META-INF/services/org.camunda.spin.spi.DataFormatProvider`
* Ensure that the artifact containing the provider is reachable from Spin's classloader

If you now call `org.camunda.spin.DataFormats.getAvailableDataFormats()`, then the custom dataformat is returned along with the built-in dataformats. Furthermore, `org.camunda.spin.DataFormats.getDataFormatByName(String dataFormatName)` can be used to explicity retrieve the data format by a specific provider.
