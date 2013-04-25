"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("cockpit.pages", [
    "cockpit/pages/dashboard",
    "cockpit/pages/processDefinition"
  ]);
});