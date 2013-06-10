"use strict";

define([ "angular" ], function(angular) {

  var module = angular.module("tasklist.services");

  var EMBEDDED_KEY = "embedded:",
      APP_KEY = "app:";

  var FormsProducer = function() {

    var Forms = {
      /**
       *
       * @param variables {Array<Variable>} the variables to convert to a variable map
       */
      variablesToMap: function(variables) {
        var variablesMap = {};

        for (var i = 0, variable; !!(variable = variables[i]); i++) {
          var name = variable.name,
              value = variable.value,
              type = variable.type;

          if (!value && type == "boolean") {
            value = false;
          }

          variablesMap[name] = {"value" : value};
        }
        return variablesMap;
      },
      
      mapToVariablesArray: function(variables_map) {
        var variablesArray = [];
        
        $.each(variables_map, function(name, value) {
          variablesArray.push({name : name, value : value.value, type : type});
        });
        
        return variablesArray;
      },

      /**
       * Parses the form date into the given form
       *
       * @param data {object}
       * @param form {object} optional
       */
      parseFormData: function(data, form) {
        var key = data.formKey,
          applicationContextPath = data.applicationContextPath;

        form = form || {};

        // structure may be [embedded:][app:]formKey

        if (!key) {
          return;
        }

        if (key.indexOf(EMBEDDED_KEY) == 0) {
          key = key.substring(EMBEDDED_KEY.length);
          form.embedded = true;
        } else {
          form.external = true;
        }

        if (key.indexOf(APP_KEY) == 0) {
          if (applicationContextPath) {
            key = applicationContextPath + "/" + key.substring(APP_KEY.length);
          }
        }

        form.key = key;

        return form;
      }
    };

    return Forms;
  };

  module.factory("Forms", FormsProducer);

  return module;
});