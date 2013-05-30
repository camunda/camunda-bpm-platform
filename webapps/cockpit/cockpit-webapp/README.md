cockpit webapp
==============

The real cockpit webapplication.

Development Setup
-----------------

For developing cockpit you can use the `dev` profile.
It will bootstrap cockpit in an embedded tomcat and allows it to reload web resources on the fly.

The cockpit on embedded tomcat can be started from within the `cockpit-webapp` folder via `mvn clean tomcat:run -Pdev`.


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

Run end-to-end tests via `karma start src/test/js/config/karma.e2e.js` (requires dev environment to be running).
