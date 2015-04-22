module.exports = function(config, requireJsConfig) {
  'use strict';
  var grunt = config.grunt;
  var commons = require('camunda-commons-ui');
  var _ = commons.utils._;
  var rjsConf = commons.requirejs();

  var deps = [
    'requirejs',
    'angular-route',
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

    baseUrl: './<%= pkg.gruntConfig.adminSourceDir %>',

    paths: _.extend(rjsConf.paths, {
      'admin':            'scripts',
      'camunda-admin-ui': 'scripts/camunda-admin-ui'
    }),

    shim: _.extend(rjsConf.shim, {
    }),

    packages: rjsConf.packages.concat([
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


  requireJsConfig.admin_dependencies = {
    options: _.merge({}, options, {
      create: true,
      name: 'camunda-admin-ui-deps',
      out: '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/deps.js',
      include: deps
    })
  };

  requireJsConfig.admin_scripts = {
    options: _.merge({}, options, {
      name: 'camunda-admin-ui',
      out: '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js',
      exclude: deps,
      include: [],

      onModuleBundleComplete: commons.livereloadSnippet(grunt)
    })
  };

};
