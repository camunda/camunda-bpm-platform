"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("common.directives", [
    "common/directives/errorPanel",
    "common/directives/help",
    "common/directives/requestAware"
  ]);
});
