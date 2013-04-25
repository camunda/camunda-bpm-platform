"use strict";

define([ "angular" ], function(angular) {

  var module = angular.module("common.services");

  var SericeProducer = function() {
    return {
      errors : [],
      consumers : [],

      add: function(error) {
        var errors = this.errors;
        var consumers = this.consumers;

        errors.push(error);

        if (consumers.length > 0 ) {
          // add to top most consumer only
          consumers[consumers.length - 1].add(error);
        }
      },

      clear: function(error) {
        var errors = this.errors,
            consumers = this.consumers,
            removeCandidates = [];

        if (typeof error == "string") {
          error = { status: error };
        }

        angular.forEach(errors, function(e) {
          if (error.status == e.status) {
            removeCandidates.push(e);
          }
        });

        angular.forEach(removeCandidates, function(e) {
          errors.splice(errors.indexOf(e), 1);

          angular.forEach(consumers, function(consumer) {
            consumer.remove(e);
          });
        });
      },
      removeAllErrors: function() {
        var errors = this.errors;

        while (errors.length) {
          var error = errors.pop();
          this.clearError(error);
        }
      },

      registerConsumer: function(consumer) {
        this.consumers.push(consumer);
      },

      unregisterConsumer: function(consumer) {
        var consumers = this.consumers,
            idx = consumers.indexOf(consumer);

        if (idx != -1) {
          consumers.splice(idx, 1);
        }
      }
    };
  };

  module.service("Errors", SericeProducer);

  return module;
});
