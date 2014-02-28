/* global ngDefine: false */
ngDefine('tasklist.services', [
  'angular'
], function(module, angular) {
  'use strict';
  var EMBEDDED_KEY = 'embedded:',
      APP_KEY = 'app:',
      ENGINE_KEY = 'engine:';

  function compact(arr) {
    var a = [];
    for (var ay in arr) {
      if (arr[ay]) {
        a.push(arr[ay]);
      }
    }
    return a;
  }

  var FormsProducer = function(Uri) {

    var booleanTypeConverter = function(value) {
      if(!value) {
        return false;
      } else {
        return true === value ||
               'true' === value ||
               'TRUE' === value;
      }
    };

    var numberTypeConverter = function(value) {
      return parseInt(value);
    };

    var stringTypeConverter = function(value) {
      return value.toString();
    };

    var typeConverters = {
      'boolean' : booleanTypeConverter,
      'number' : numberTypeConverter,
      'Integer' : numberTypeConverter,
      'string' : stringTypeConverter
    };

    function convertValue(variable) {
      var converter = typeConverters[variable.type];
      if(!!converter) {
        return converter(variable.value);
      } else {
        return variable.value;
      }
    }

    var Forms = {
      /**
       *
       * @param variables {Array<Variable>} the variables to convert to a variable map
       */
      variablesToMap: function(variables) {
        var variablesMap = {};

        for (var i = 0, variable; !!(variable = variables[i]); i++) {

          // read-only variables should not be submitted.
          if(!variable.readOnly) {

            var name = variable.name;
            var value = convertValue(variable);

            variablesMap[name] = {'value' : value};
          }
        }
        return variablesMap;
      },

      mapToVariablesArray: function(variables_map) {
        var variablesArray = [];

        angular.forEach(variables_map, function(variable, name) {
          variablesArray.push({ name : name, value : variable.value, type : variable.type });
        });

        return variablesArray;
      },

      /**
       * Parses the form data into the given form
       *
       * @param data {object}
       * @param form {object} optional
       */
      parseFormData: function(data, form) {
        var key = data.key,
          applicationContextPath = data.contextPath;

        form = form || {};

        // structure may be [embedded:][app:]formKey

        if (!key) {
          return;
        }

        if (key.indexOf(EMBEDDED_KEY) === 0) {
          key = key.substring(EMBEDDED_KEY.length);
          form.embedded = true;
        } else {
          form.external = true;
        }

        if (key.indexOf(APP_KEY) === 0) {
          if (applicationContextPath) {
            key = compact([applicationContextPath, key.substring(APP_KEY.length)])
              .join('/')
              // prevents multiple "/" in the URI
              .replace(/\/([\/]+)/, '/');
          }
        }

        if(key.indexOf(ENGINE_KEY) === 0) {
          // resolve relative prefix
          key = Uri.appUri(key);
        }

        form.key = key;

        return form;
      }
    };

    return Forms;
  };


  FormsProducer.$inject = ['Uri'];

  module.factory('Forms', FormsProducer);
});
