"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  module.factory("EngineApi", function ($resource, Uri) {
      function EngineApi() {
      };

      EngineApi.prototype.getTasklist = function () {
        var Tasklist = $resource(Uri.restUri("task/:id/:op"), {}, {
          claim: {method:'POST', params : {op : "claim"}}
        });

        return Tasklist;
      };

      EngineApi.prototype.getTaskCount = function () {
        var Count = $resource(Uri.restUri("task/count"));
        return Count;
      };

      EngineApi.prototype.getGroups = function (userId) {
        var Groups = $resource(Uri.restUri("task/groups"));
        return Groups.get({"userId": userId});
      };

      return new EngineApi();
    }
  );

  return module;
});