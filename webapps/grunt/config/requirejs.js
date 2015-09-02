module.exports = function(config, requireJsConfig) {
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

  var options = {
      stubModules: ['text'],

      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.cockpitSourceDir %>',

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
  };


  requireJsConfig.cockpit_dependencies = {
      options: _.merge({}, options, {
        create: true,
        name: 'camunda-cockpit-ui-deps',
        out: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/deps.js',
        include: deps
      })
    };

  requireJsConfig.cockpit_scripts = {
      options: _.merge({}, options, {
        name: 'camunda-cockpit-ui',
        out: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js',
        exclude: deps,
        include: [],

        onModuleBundleComplete: commons.livereloadSnippet(grunt)
    })
  };
};
