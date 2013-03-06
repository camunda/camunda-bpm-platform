"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("tasklist.services", [
    "tasklist/services/EngineApi",
    "tasklist/services/Authentication",
    "tasklist/services/Forms"
  ]);

});