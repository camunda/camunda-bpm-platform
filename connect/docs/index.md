# Connect Documentation

Welcome to the camunda Connect documentation

* [User Guide](user-guide/index.md)

## camunda BPM platform and camunda Connect

To use Connect to enable connector service tasks in the camunda BPM platform you have to
add the camunda Connect process engine plugin to your process engine. For more information
of the plugin mechanism please see the [docs][plugins].  For example, a bpm-platform.xml file with
the plugin enabled would look as follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpm-platform xmlns="http://www.camunda.org/schema/1.0/BpmPlatform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.camunda.org/schema/1.0/BpmPlatform http://www.camunda.org/schema/1.0/BpmPlatform ">

  ...

  <process-engine name="default">
    ...

    <plugins>
      <plugin>
        <class>org.camunda.connect.plugin.ConnectProcessEnginePlugin</class>
      </plugin>
    </plugins>

    ...
  </process-engine>

</bpm-platform>
```

After you enabled the plugin you can use connector services tasks as described in the [user
guide][].


[plugins]: http://docs.camunda.org/latest/guides/user-guide/#process-engine-process-engine-plugins
[user guide]: http://stage.docs.camunda.org/guides/user-guide/#process-engine-connectors
