'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;

function distFileProcessing(content, srcpath) {
  // removes the template comments
  content = content
            .split('\n').filter(function(line) {
              return !commentLineExp.test(line);
            }).join('\n');

  // var date = new Date();
  // var cacheBuster = [date.getFullYear(), date.getMonth(), date.getDate()].join('-');
  // content = content
  //           .replace(/\/\* cache-busting /, '/* cache-busting */')
  //           .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

  return content;
}

function devFileProcessing(content, srcpath) {
  /* jshint validthis: true */
  var liveReloadPort = this.config.get('pkg.gruntConfig.livereloadPort');
  /* jshint validthis: false */

  content = content
            .replace(/\/\* live-reload/, '/* live-reload */')
            .replace(/LIVERELOAD_PORT/g, liveReloadPort);

  // content = content
  //           .replace(/\/\* cache-busting/, '/* cache-busting */')
  //           .replace(/CACHE_BUSTER/g, (new Date()).getTime());

  return content;
}

module.exports = function(config) {
  var grunt = config.grunt;

  return {
    development: {
      options: {
        process: function() {
          return devFileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'index.html'
          ],
          dest: '<%= buildTarget %>/'
        }
      ]
    },

    dist: {
      options: {
        process: function() {
          return distFileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/WEB-INF',
          src: ['*'],
          dest: '<%= buildTarget %>/WEB-INF'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'index.html'
          ],
          dest: '<%= buildTarget %>/'
        }
      ]
    },

    assets: {
      files: [
        // images, fonts & stuff
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/',
          src:  [
            '{fonts,images}/**/*.*'
          ],
          dest: '<%= buildTarget %>/assets'
        },

        // bootstrap fonts
        {
          expand: true,
          cwd: 'node_modules/camunda-commons-ui/node_modules/bootstrap/fonts',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= buildTarget %>/fonts/'
        }
      ]
    }
  };
};
