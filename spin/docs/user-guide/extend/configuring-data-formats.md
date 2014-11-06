# Configuring Data Formats

The data formats available to Spin may not always suit your needs. Sometimes, it is necessary to provide configuration. For example, when using Spin to map Java objects to JSON, a format for how dates are serialized has to specified. While the Spin data formats use reasonable default values, they can also be changed.

To configure a data format detected by Spin, the SPI `org.camunda.spin.spi.DataFormatConfigurator` can be implemented. A configurator specifies which classes it can configure. Spin discovers a configurator by employing Java's service loader mechanism and will then provide it with all data formats that match the specified class (or are a subclass thereof). The concrete configuration options depend on the actual data format. For example, a Jackson-based JSON data format can modify the `ObjectMapper` that the data format uses.

In order to provide a custom dataformat, you have to

* Provide a custom implementation of `org.camunda.spin.spi.DataFormatConfigurator`
* Add the configurator's fully qualified classname to a file named `META-INF/services/org.camunda.spin.spi.DataFormatConfigurator`
* Ensure that the artifact containing the configurator is reachable from Spin's classloader
