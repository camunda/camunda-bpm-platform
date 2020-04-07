This is an example of how to migrate an existing angularJS Plugin to the new Plugin system. The original plugin can be found in `original-plugin.js`.

Note that `localConf` is a service that was provided by the Camunda framework and was removed. You can implement the service yourself if you desire the functionality.

To build, run `rollup -c`. The result will be in the `/dist` folder.
