"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  module.factory("EngineApi", function ($resource, Uri) {
      function EngineApi() {
      };

      EngineApi.prototype.getTasklist = function () {
        var Task = $resource(Uri.restUri("task"));
        return Task.get();
      }

      return new EngineApi();
    }
  );

  return module;
});