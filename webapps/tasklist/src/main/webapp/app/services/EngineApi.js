"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  var EngineApiFactory = function($resource, Uri) {

    var baseUri = Uri.restRoot();
    var apiUri = Uri.appRoot() + "api/";

    function EngineApi() {

      this.taskList = $resource(Uri.build(baseUri, "task/:id/:operation"), { id: "@id" } , {
        claim : { method: 'POST', params : { operation: "claim" }},
        unclaim : { method: 'POST', params : { operation: "unclaim" }},
        delegate : { method: 'POST', params : { operation: "delegate" }},
        resolve : { method: 'POST', params : { operation: "resolve" }},
        complete : { method: 'POST', params : { operation: "complete" }}
      });

      var forms = $resource(Uri.build(apiUri, "forms/:context/:id"), { id: "@id" } , {
        startForm : { method: 'GET', params : { context: "process-definition" }},
        taskForm : { method: 'GET', params : { context: "task" }}
      });

      this.taskList.getForm = function(data, fn) {
        return forms.taskForm(data, fn);
      };

      this.taskCount = $resource(Uri.build(baseUri, "task/count"));
      this.processDefinitions = $resource(Uri.build(baseUri, "process-definition/:id/:operation"), { id: "@id" }, {
          xml : { method: 'GET', params : { operation: "xml" }}
      });

      this.processDefinitions.getStartForm = function(data, fn) {
        return forms.startForm(data, fn);
      };

      this.processDefinitions.startInstance = function(data, fn) {
        data = angular.extend(data, { operation : "start" });

        return this.save(data, fn);
      };

      this.groups = $resource(Uri.build(baseUri, "task/groups"));

      this.processInstance = $resource(Uri.build(baseUri, "process-instance/:id/:operation"), { id: "@id" } , {
        variables : { method: 'GET', params : { operation: "variables" }}
      });

    };

    EngineApi.prototype.getProcessDefinitions = function() {
      return this.processDefinitions;
    };

    EngineApi.prototype.getTaskList = function () {
      return this.taskList;
    };

    EngineApi.prototype.getTaskCount = function () {
      return this.taskCount;
    };

    EngineApi.prototype.getGroups = function(userId) {
      return this.groups.get({ userId: userId });
    };

    EngineApi.prototype.getProcessInstance = function() {
      return this.processInstance;
    };

    return new EngineApi();
  };

  EngineApiFactory.$inject = ["$resource", "Uri"];

  module.factory("EngineApi", EngineApiFactory);

  return module;
});