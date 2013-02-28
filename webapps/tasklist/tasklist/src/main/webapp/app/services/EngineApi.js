"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  var EngineApiFactory = function($resource, Uri) {

    var baseUri = Uri.restRoot();

    function EngineApi() {

      this.taskList = $resource(Uri.build(baseUri, "task/:taskId/:op"), {taskId: "@id"} , {
        claim : {method:'POST', params : {op:"claim"}},
        complete : {method:'POST', params : {op:"complete"}}
      });

      this.taskCount = $resource(Uri.build(baseUri, "task/count"));
      this.processDefinitions = $resource(Uri.build(baseUri, "process-definition"));
      this.groups = $resource(Uri.build(baseUri, "task/groups"));
    };

    EngineApi.prototype.getProcessDefinitions = function() {
      return this.processDefinitions;
    };

    EngineApi.prototype.getTasklist = function () {
      return this.taskList;
    };

    EngineApi.prototype.getTaskCount = function () {
      return this.taskCount;
    };

    EngineApi.prototype.getGroups = function(userId) {
      return this.groups.get({ userId: userId });
    };

    return new EngineApi();
  };

  EngineApiFactory.$inject = ["$resource", "Uri"];

  module.factory("EngineApi", EngineApiFactory);

  return module;
});