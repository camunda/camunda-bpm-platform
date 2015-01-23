module.exports = function(config) {
  'use strict';
  var grunt = config.grunt;
  var commons = require('camunda-commons-ui');
  var _ = commons.utils._;
  var rjsConf = commons.requirejs();

  var deps = [
    'requirejs',
    'angular',
    'angular-resource',
    'angular-sanitize',
    'angular-bootstrap',
    'ngDefine',
    'domReady'
  ];

  var rConf = {
    options: {
      stubModules: ['text'],

      optimize: '<%= (buildTarget === "dist" ? "uglify2" : "none") %>',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.clientDir %>',

      paths: _.extend(rjsConf.paths, {
        'cockpit':            'scripts',
        'camunda-cockpit-ui': 'scripts/camunda-cockpit-ui'
      }),

      shim: _.extend(rjsConf.shim, {
      }),

      packages: rjsConf.packages.concat([
        {
          name: 'bpmn',
          location : '../node_modules/camunda-bpmn.js/src/bpmn',
          main: 'Bpmn'
        },
        {
          name: 'dojo',
          location : 'vendor/dojo/dojo'
        },
        {
          name: 'dojox',
          location : 'vendor/dojo/dojox'
        },

        {
          name: 'services',
          location: './scripts/services',
        },
        {
          name: 'pages',
          location: './scripts/pages',
        },
        {
          name: 'directives',
          location: './scripts/directives',
        },
        {
          name: 'filters',
          location: './scripts/filters',
        },
        {
          name: 'resources',
          location: './scripts/resources',
        },
        {
          name: 'util',
          location: './scripts/util',
          main: 'routeUtil'
        }
      ])
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
        name: '<%= pkg.name %>',
        out: '<%= buildTarget %>/scripts/<%= pkg.name %>.js',
        exclude: deps,
        include: [],

        onModuleBundleComplete: commons.livereloadSnippet(grunt)
      }
    }
  };

  return rConf;
};
