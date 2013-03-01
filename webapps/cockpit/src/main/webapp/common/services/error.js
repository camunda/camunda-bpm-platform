"use strict";

(function() {

  var module = angular.module("common.services");
  
  var Service = function() {
    return {
      errors : [],
      errorConsumers : [],
      addError: function (error) {
        this.errors.push(error);
        if (this.errorConsumers.length > 0 ) {
          this.errorConsumers[this.errorConsumers.length-1](error);
        }
      },
      removeError: function(error) {
        var idx = this.errors.indexOf(error);
        this.errors.splice(idx,1);
      },
      removeAllErrors: function() {
        // not assigning a new [], because it still can be referenced somewhere => memory leak
        this.errors.length = 0;
      },
      registerErrorConsumer: function(callback) {
        this.errorConsumers.push(callback);
      },
      unregisterErrorConsumer: function(callback) {
        this.errorConsumers.splice(this.errorConsumers.indexOf(callback),1);
      }
    };
  };
  
  module
    .service("Error", Service);

})();
