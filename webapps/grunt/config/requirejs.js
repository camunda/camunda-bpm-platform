module.exports = function(config) {
  'use strict';
  var _ = require('underscore');
  var fs = require('fs');
  var path = require('path');

  config = config || {};

  var deps = [
    './../node_modules/grunt-contrib-requirejs/node_modules/requirejs/require',
    'domready',
    'jquery-mockjax',
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
      // optimize: 'uglify2',
      // preserveLicenseComments: false,
      // generateSourceMaps: true,

      optimize: 'none',
      preserveLicenseComments: true,
      generateSourceMaps: false,

      // baseUrl: rjsConf.baseUrl,
      baseUrl: './client',
      paths: rjsConf.paths,
      shim: rjsConf.shim,
      packages: rjsConf.packages,


      //A function that is called for each JS module bundle that has been
      //completed. This function is called after all module bundles have
      //completed, but it is called for each bundle. A module bundle is a
      //"modules" entry or if just a single file JS optimization, the
      //optimized JS file.
      //Introduced in r.js version 2.1.6
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
        // console.info('onModuleBundleComplete', data);

        // // add a timestamp to the sourcemap URL to prevent caching
        // fs.readFile(data.path, {encoding: 'utf8'}, function(err, content) {
        //   // console.info('onModuleBundleComplete', data.name, content);
        //   content = content + '?' + (new Date()).getTime();
        //   fs.writeFileSync(data.path, content);
        // });
      },

      // //A function that if defined will be called for every file read in the
      // //build that is done to trace JS dependencies. This allows transforms of
      // //the content.
      // onBuildRead: function (moduleName, path, contents) {
      //   console.info('onBuildRead', moduleName);
      //   //Always return a value.
      //   //This is just a contrived example.
      //   return contents;//.replace(/foo/g, 'bar');
      // },

      //A function that will be called for every write to an optimized bundle
      //of modules. This allows transforms of the content before serialization.
      onBuildWrite: function (moduleName, path, contents) {
        console.info('onBuildWrite', moduleName);
        //Always return a value.
        //This is just a contrived example.
        return contents;//.replace(/bar/g, 'foo');
      }
    },


    dependencies: {
      options: {
        create: true,
        name: '<%= pkg.name %>-deps',
        out: 'dist/scripts/deps.js',
        // include: deps
        include: deps.concat(['camunda-tasklist/rjsconf'])
      }
    },



    // mocks: {
    //   options: {
    //     // name: 'scripts/index',
    //     name: 'camunda-tasklist/mocks',
    //     out: 'dist/scripts/<%= pkg.name %>/mocks.js',
    //     // exclude: deps.concat([
    //     //   'camunda-tasklist',
    //     //   'camunda-tasklist/controls',
    //     //   'camunda-tasklist/form',
    //     //   'camunda-tasklist/pile',
    //     //   'camunda-tasklist/task'
    //     // ]),
    //     include: [
    //       'uuid',
    //       'fixturer'
    //     ]
    //   }
    // },

    scripts: {
      options: {
        // name: 'scripts/index',
        name: 'camunda-tasklist',
        out: 'dist/scripts/<%= pkg.name %>.js',
        // exclude: deps,
        exclude: deps.concat(['camunda-tasklist/rjsconf']),
        include: rjsConf.shim['camunda-tasklist'].concat([
          // 'hyperagent',
          'camunda-tasklist/mocks'
        ])
      }
    }
  };

  return rConf;
};
