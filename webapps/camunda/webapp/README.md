camunda webapp
==============

The camunda webapplication.


Development Setup
-----------------

For developing the application you can use the `develop` profile.
It will bootstrap the application in an embedded tomcat and allows it to reload web resources on the fly.

Start the application on an embedded tomcat by executing `mvn clean tomcat:run -Pdevelop` from within the `camunda-webapp` folder.


Test Suite
----------


### Server side

Run tests via `mvn clean test`.


### Client side

> Requires [NodeJS](http://nodejs.org/) and [karma](http://karma-runner.github.com).
>
> Install karma `npm -g install karma@canary` + dependencies `npm update --dev`.
>
> Additionally paths to browser runtimes may need to be defined in environment variables:
> <code>PHANTOMJS_BIN</code>, <code>FIREFOX_BIN</code>, <code>CHROME_BIN</code>.

Run unit tests via `karma start src/test/js/config/karma.unit.js`.

Run end-to-end tests via `karma start src/test/js/config/karma.e2e.js` (requires the develop environment to be running).
