Cockpit README
==============


Plugins
-------

Cockpit may be extended using plugin-jars.
Those jars can contain server-side resources (such as REST interfaces)
as well as client-side resources (html views and javascript).

A cockpit plugin publishes those resources via a `META-INF/services` implementation
of the type `org.camunda.bpm.cockpit.spi.CockpitPlugin`.

The basic skeleton for a cockpit plugin looks as follows: