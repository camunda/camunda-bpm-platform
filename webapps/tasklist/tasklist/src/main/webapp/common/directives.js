"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("common.directives", [
    "common/directives/errorPanel",
    "common/directives/multiSelect",
    "common/directives/requestAware",
    "common/directives/errorPanel"
  ]);
});
