"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("common.services", [
    "common/services/debouncer",
    "common/services/uri",
    "common/services/requestStatus",
    "common/services/httpStatusInterceptor",
    "common/services/errors"
  ]);
});