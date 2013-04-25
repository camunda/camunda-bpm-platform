"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("cockpit.resources", [
    "cockpit/resources/processDefinitionDiagramResource",
    "cockpit/resources/processDefinitionResource",
    "cockpit/resources/processInstanceResource"
  ]);
});
