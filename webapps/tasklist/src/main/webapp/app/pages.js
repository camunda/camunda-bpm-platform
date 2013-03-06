"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("tasklist.pages", [
    "tasklist/common/sitebar",
    "tasklist/common/header",
    "tasklist/pages/login",
    "tasklist/pages/overview",
    "tasklist/pages/start",
    "tasklist/pages/task"
  ]);
});