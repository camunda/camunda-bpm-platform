ngDefine('tasklist.services', [
  'angular'
], function(module, angular) {

  var EMBEDDED_KEY = "embedded:",
      APP_KEY = "app:",
      ENGINE_KEY = "engine:";

  var FormsProducer = function(Uri) {

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

        if(key.indexOf(ENGINE_KEY) == 0) {
          // resolve relative prefix
          key = Uri.appUri(key);
        }

        form.key = key;

        return form;
      }
    };

    return Forms;
  };


  FormsProducer.$inject = ["Uri"];

  module.factory("Forms", FormsProducer);
});
