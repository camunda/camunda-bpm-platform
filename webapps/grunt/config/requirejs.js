module.exports = function(config) {
  'use strict';
  var _ = require('underscore');
  var fs = require('fs');
  var path = require('path');
  var grunt = config.grunt;
  var rjsConfPath = path.resolve('./client/scripts/require-conf');
  var rjsConf = require(rjsConfPath);

  var deps = [
    'scripts/require-conf',
    './../node_modules/requirejs/require',
    'jquery',
    'angular',
    //'moment',
    'angular-bootstrap',
    'angular-route',
    'angular-animate',
    'camunda-commons-ui',
    //'angular-moment',
    'angular-resource',
    'angular-sanitize',
    'angular-ui',
    'ngDefine',
    'jquery-ui/ui/jquery.ui.draggable',
    'domReady!',
    'bpmn',
    'bpmn/Bpmn',
    'bpmn/Transformer',
    'dojo',
    'dojox/gfx',
    'angular-data-depend'
  ];

  _.extend(rjsConf.paths, {
    'require-conf': 'scripts/require-conf',
    'cockpit/util/routeUtil': 'scripts/util/routeUtil',
    //'bpmn': 'empty:',
    //'bpmn/Bpmn': 'empty:',
    'dojox/gfx': 'empty:',
    'cockpit-plugin-base': 'plugin'
  });


  var rConf = {
    options: {
      //stubModules: ['text'],

      optimize: '<%= (buildTarget === "dist" ? "uglify2" : "none") %>',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.clientDir %>',
      // baseUrl: config.clientDir,

      paths: rjsConf.paths,
      shim: rjsConf.shim,
      packages: rjsConf.packages
    },


    dependencies: {
      options: {
        create: true,
        name: '<%= pkg.name %>-deps',
        out: '<%= buildTarget %>/scripts/deps.js',
        include: deps
      }
    },

    scripts: {
      options: {
        name: 'camunda-cockpit',
        out: '<%= buildTarget %>/scripts/<%= pkg.name %>.js',
        exclude: deps,
        include: rjsConf.shim['camunda-cockpit']
      }
    }
  };

  return rConf;
};
