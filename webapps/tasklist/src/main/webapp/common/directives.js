"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("common.directives", [
    "common/directives/notificationsPanel",
    "common/directives/multiSelect",
    "common/directives/requestAware"
  ]);
});
