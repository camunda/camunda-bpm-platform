# Implementing custom Data Formats

Spin detects all available dataformats on the classpath. A dataformat is an implementation of the
`org.camunda.spin.spi.DataFormat` SPI.

In order to provide a custom dataformat, you have to

* Provide a custom implementation of `org.camunda.spin.spi.DataFormat`.
* Add the fully qualified classname to a file named
`META-INF/services/org.camunda.spin.spi.DataFormat`

If you now call `org.camunda.spin.DataFormats.getAvailableDataFormats()`, then the custom dataformat
is returned along with the built-in dataformats.

