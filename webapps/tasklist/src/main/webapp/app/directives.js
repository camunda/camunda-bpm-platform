"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("tasklist.directives", [
    "tasklist/directives/taskForm",
    "tasklist/directives/sortable"
  ]);

});