module.exports = function(config) {
  'use strict';
  var _ = require('underscore');
  var fs = require('fs');
  var path = require('path');

  config = config || {};

  var deps = [
    './../node_modules/requirejs/require',
    'angular',
    'moment',
    'angular-bootstrap',
    'angular-moment'
  ];

  var rjsConfPath = path.resolve('./client/scripts/rjsconf');
  var rjsConf = require(rjsConfPath);

  _.extend(rjsConf.paths, {
    rjsconf: 'scripts/rjsconf'
  });

  var rConf = {
    options: {
      stubModules: ['text'],

      optimize: 'uglify2',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      // optimize: 'none',
      // preserveLicenseComments: true,
      // generateSourceMaps: false,


      baseUrl: './client',
      paths: rjsConf.paths,
      shim: rjsConf.shim,
      packages: rjsConf.packages,

      onModuleBundleComplete: function (data) {
        /*
        data.name: the bundle name.
        data.path: the bundle path relative to the output directory.
        data.included: an array of items included in the build bundle.
        If a file path, it is relative to the output directory. Loader
        plugin IDs are also included in this array, but depending
        on the plugin, may or may not have something inlined in the
        module bundle.
        */
        console.info('onModuleBundleComplete', data.path+':\n'+data.included.join('\n'));

        // // add a timestamp to the sourcemap URL to prevent caching
        // fs.readFile(data.path, {encoding: 'utf8'}, function(err, content) {
        //   // console.info('onModuleBundleComplete', data.name, content);
        //   content = content + '?' + (new Date()).getTime();
        //   fs.writeFileSync(data.path, content);
        // });
      }
    },


    dependencies: {
      options: {
        create: true,
        name: '<%= pkg.name %>-deps',
        out: 'dist/scripts/deps.js',
        // include: deps
        include: deps.concat([
          'camunda-tasklist/rjsconf',
          'angular-route'
        ])
      }
    },



    mocks: {
      options: {
        // name: 'scripts/index',
        name: '<%= pkg.name %>/mocks',
        out: 'dist/scripts/deps-n-mocks.js',
        exclude: [],
        include: deps.concat([
          'camunda-tasklist/rjsconf',
          'angular-route'
        ], rjsConf.shim['camunda-tasklist/mocks'], ['camunda-tasklist/mocks'])
      }
    },



    scripts: {
      options: {
        // name: 'scripts/index',
        name: 'camunda-tasklist',
        out: 'dist/scripts/<%= pkg.name %>.js',
        // exclude: deps,
        exclude: deps.concat([
          'camunda-tasklist/rjsconf',
          // 'camunda-tasklist/mocks',
          'angular-route'
        ]),
        include: rjsConf.shim['camunda-tasklist']
      }
    }
  };

  return rConf;
};
