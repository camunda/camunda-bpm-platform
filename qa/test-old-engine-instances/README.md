Old Engine Instances Tests
========================

The test suite belongs to the rolling upgrade tests.

These modules test following scenario:

 * old engine creates process instance on old database schema (`previous-engine`)
 * upgrade of database schema to newer version (`upgrade-database`)
 * old engine end process instance on newer database schema (`test-old-engine`)


Executing Tests
---------------

Run `mvn clean install -Pold-engine-instances,${database-id}` where `${database-id}` is for example `h2`.

Project Structure
-----------------

 * `previous-engine` creates the old database schema and creates the process instance with the old engine
 * `upgrade-database` upgrades the old database schema to the newer one 
 * `test-old-engine` ends the process instance and drops the schema with the new scripts

