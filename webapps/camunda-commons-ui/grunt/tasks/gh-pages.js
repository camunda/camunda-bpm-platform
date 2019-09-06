/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

function commonsConf() {
  var conf = commons.requirejs({ pathPrefix: '' });
  conf.baseUrl = '/';
  conf.packages.push({
    name: 'camunda-commons-ui/widgets',
    location: 'lib/widgets',
    main: 'index'
  });
  return conf;
  // return JSON.stringify(conf, null, 2);
}

var commons = require('../../index');

module.exports = function(grunt) {
  var path = require('path');
  var projectRoot = path.resolve(__dirname, '../../');

  var marked = require('marked');
  var amdConf = commonsConf();
  var pkg = grunt.file.readJSON(projectRoot +'/package.json');

  var gitDir = projectRoot + '/gh-pages';
  var generatedDir = gitDir + '/' + pkg.version;


  function cloneGhPages(done) {
    if (grunt.file.isDir(gitDir)) {
      grunt.file.delete(gitDir);
    }

    grunt.util.spawn({
      cmd: 'git',
      args: [
        'clone',
        'git@github.com:camunda/camunda-commons-ui.git',
        gitDir
      ]
    }, function(err) {
      if (err) { return done(err); }

      checkoutGhPages(function(err) {
        if (err) { return done(err); }

        grunt.log.writeln('Repository checked out on "gh-pages" branch');
        done();
      });
    });
  }


  function checkoutGhPages(done, orphan) {
    var args = ['checkout'];
    if (orphan) { args.push('--orphan'); }
    args.push('gh-pages');

    grunt.util.spawn({
      opts: {cwd: gitDir},
      cmd: 'git',
      args: args
    }, function(err) {
      if (!orphan && err && err.message === 'error: pathspec \'gh-pages\' did not match any file(s) known to git.') {
        grunt.log.writeln('Create orphan branch');

        return checkoutGhPages(function(err) {
          if (err) { return done(err); }

          grunt.util.spawn({
            opts: {cwd: gitDir},
            cmd: 'git',
            args: [
              'rm',
              '-rf',
              '.'
            ]
          }, done);
        }, true);
      }

      done(err);
    });
  }


  function copyToVersion(version) {
    version = version || '';
    var versionPath = version ? ('/' + version) : '';
    var textPluginExp = new RegExp('\'node_modules/requirejs-text/text\'', 'g');

    grunt.file.expand([
      generatedDir + '/*.html'
    ]).forEach(function(filepath) {
      if (!grunt.file.isFile(filepath)) { return; }

      var destination = filepath.replace('/' + pkg.version, versionPath);

      grunt.file.copy(filepath, destination, {
        process: function(content) {
          grunt.log.writeln('Rewrite URLs for ' + destination);

          var textPluginPath = '\'' + pkg.name + versionPath + '/node_modules/requirejs-text/text\'';

          return content
            .replace(new RegExp(pkg.version, 'g'), version)
            .replace(textPluginExp, textPluginPath);
        }
      });
    });

    grunt.file.expand([
      generatedDir + '/**',
      '!*.html'
    ]).forEach(function(filepath) {
      if (!grunt.file.isFile(filepath)) { return; }

      grunt.file.copy(filepath, filepath
        .replace('/' + pkg.version, '/' + version)
      );
    });

    grunt.log.writeln('Copied to the "' + (version ? version : 'root') + '" directory');
  }


  function compileGhPages() {
    grunt.file.expand([
      generatedDir +'/{**/,}*',
      '!' + generatedDir +'/.git'
    ]).forEach(function(filepath) {
      if (grunt.file.isFile(filepath)) {
        grunt.file.delete(filepath);
      }
    });

    var sources = grunt.file.expand([
      'lib/widgets/*/test/*.spec.html'
    ]);

    var destinations = sources.map(function(filepath) {
      var destination = filepath.slice(filepath.lastIndexOf('/test/') + '/test/'.length)
        .split('.spec').shift();
      return destination;
    });

    var menuTemplate = require('lodash').template([
      '<header class="cam-brand-header">',
      '<span class="navbar-brand" href="./index.html" title="Camunda Corporate Styles">',
      '<span class="brand-logo"></span>',
      '<a href="./">Camunda commons UI</a>',
      '<small><%- version %></small>',
      '</span>',
      '</header>',
      '<div class="page-wrapper">',
      '<nav>',
      '<h4>Widgets</h4>',
      '<ul class="list-inline">',
      '<% destinations.forEach(function (destination, i) { %>',
      '<li<% if (destination === current) { %> class="active"<% } %>>',
      '<a href="./<%- destination %>.html">',
      '<%- destination.replace("cam-widget-", "") %>',
      '</a>',
      '</li><% }); %>',
      '</ul>',
      '</nav>'
    ].join('\n'));

    function ghPagesMenu(current) {
      return menuTemplate({
        version: pkg.version,
        destinations: destinations,
        current: current
      });
    }

    var footerTemplate = require('lodash').template([
      '</div>',
      '<footer class="cam-brand-footer">',
      '<nav>',
      '<ul class="list-inline">',
      '<li><a href="//camunda.org">Camunda BPM</a></li>',
      '<li><a href="//github.com/camunda/camunda-commons-ui">commons UI lib</a></li>',
      '</ul>',
      '</nav>',
      '</footer>'
    ].join('\n'));

    sources.forEach(function(source, i) {
      grunt.file.copy(source, generatedDir + '/' + destinations[i] + '.html', {
        process: function(content) {
          return content
            .replace('<!-- gh-pages-menu -->', ghPagesMenu(destinations[i]))
            .replace('<!-- gh-pages-footer -->', footerTemplate())
            .replace('<body class="', '<body class="gh-pages ')
            .replace('<body>', '<body class="gh-pages">')
            .replace(new RegExp('<base href="/" />', 'g'), '')
          ;
        }
      });
    });


    var readme = marked(grunt.file.read(projectRoot + '/README.md').toString());
    readme = readme.replace(/<h1 id="camunda-commons-ui.*<\/h1>/, '');
    var d = new Date();
    grunt.file.write(generatedDir + '/index.html', [
      '<html>',
      '<!-- ' + d + ' -->',
      '<head>',
      '<meta charset="utf-8" />',
      '<title>Camunda commons UI library</title>',
      '<link rel="icon" href="resources/img/favicon.ico" />',
      '<link type="text/css" rel="stylesheet" href="./styles.css" />',
      '<link type="text/css" rel="stylesheet" href="./test-styles.css" />',
      '</head>',
      '<body class="gh-pages readme">',
      ghPagesMenu(),
      '<section>',
      '<div class="content">'+ readme +'</div>',
      '</section>',
      footerTemplate(),
      '</body>',
      '</html>'
    ].join('\n'));



    grunt.file.expand([
      'resources/img/*'
    ]).forEach(function(filepath) {
      var fileDestination = generatedDir + '/' + filepath;
      grunt.verbose.writeln('copy ' + filepath + ' to ' + fileDestination);
      grunt.file.copy(filepath, fileDestination);
    });



    grunt.file.expand([
      'node_modules/dmn-js/dist/assets/dmn-font/font/*.{eot,svg,ttf,woff,woff2}',
      'node_modules/bpmn-font/dist/font/*.{eot,svg,ttf,woff,woff2}',
      'node_modules/bootstrap/fonts/*.{eot,svg,ttf,woff,woff2}',
      'vendor/fonts/*.{eot,svg,ttf,woff,woff2}'
    ]).forEach(function(filepath) {
      var fileDestination = generatedDir + '/vendor/fonts/' + path.basename(filepath);
      grunt.verbose.writeln('copy ' + filepath + ' to ' + fileDestination);
      grunt.file.copy(filepath, fileDestination);
    });




    var paths = {};
    Object.keys(amdConf.paths).forEach(function(lib) {
      var libPath = amdConf.paths[lib];

      paths[lib] = libPath.replace(/^\//, '');

      grunt.file.expand([
        amdConf.paths[lib].slice(1) +'{*,/**/*}'
      ]).forEach(function(filepath) {
        if (!grunt.file.isFile(filepath)) { return; }
        grunt.file.copy(filepath, generatedDir + '/' + filepath);
      });
    });
    amdConf.paths = paths;

    amdConf.packages = amdConf.packages.map(function(info) {
      if (info.location) {
        info.location = info.location.replace(/^\//, '');
      }
      return info;
    });

    amdConf.baseUrl = './';
    grunt.file.write(generatedDir + '/test-conf.json', JSON.stringify(amdConf, null, 2));




    grunt.file.copy('test-styles.css', generatedDir + '/test-styles.css');
    grunt.file.copy('lib/widgets/index.js', generatedDir + '/index.js');
  }


  function pushGhPages(done) {
    grunt.util.spawn({
      opts: {cwd: gitDir},
      cmd: 'git',
      args: [
        'add',
        '--all',
        '.'
      ]
    }, function(err) {
      if (err) {
        console.info.apply(console, arguments);
        return done(err);
      }
      grunt.verbose.writeln('Added changed files');

      grunt.util.spawn({
        opts: {cwd: gitDir},
        cmd: 'git',
        args: [
          'commit',
          '-m',
          '"gh-pages update"'
        ]
      }, function(err) {
        if (err) {
          console.info.apply(console, arguments);
          return done(err);
        }
        grunt.verbose.writeln('Commited changed files');

        // done();
        // return;

        grunt.util.spawn({
          opts: {cwd: gitDir},
          cmd: 'git',
          args: [
            'push',
            // '--force',
            'origin',
            'gh-pages'
          ]
        }, function(err) {
          if (err) { return done(err); }
          grunt.log.writeln('Pushed to gh-pages branch');

          grunt.file.delete(generatedDir);

          done();
        });
      });
    });
  }

  grunt.registerTask('gh-pages-compile', compileGhPages);

  grunt.registerTask('gh-pages', function() {
    var done = this.async();
    cloneGhPages(function(err) {
      if (err) { return done(err); }
      compileGhPages();

      copyToVersion();

      copyToVersion('latest');

      grunt.log.writeln('Compilation of gh-pages completed');

      pushGhPages(done);
    });
  });
};
