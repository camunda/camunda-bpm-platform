module.exports = function(grunt) {
  'use strict';
  var path = require('path');

  grunt.registerMultiTask('localescompile', function() {
    var lang, file, f, loaded, key, filepath;
    var done = this.async();
    var languages = {};
    var dest = grunt.config.get(this.name +'.'+ this.target +'.options.dest');
    dest = dest || grunt.config.get(this.name +'.options.dest');

    for (f in this.filesSrc) {
      file = this.filesSrc[f];
      lang = path.basename(file, '.json');

      languages[lang] = languages[lang] || {labels:{}};

      loaded = grunt.file.readJSON(file);
      for (key in loaded.labels) {
        if(typeof loaded.labels[key] === "string") {
          if(languages[lang].labels[key]) {
            throw grunt.util.error("Duplicate entry " + key + " for translation file " + lang + ".");
          }
          languages[lang].labels[key] = loaded.labels[key];
        } else {
          if(languages[lang].labels[key]) {
            throw grunt.util.error("Duplicate entry " + key + " for translation file " + lang + ".");
          }
          languages[lang].labels[key] = loaded.labels[key];
        }

      }
    }

    for (lang in languages) {
      filepath = path.join(dest, lang +'.json');
      grunt.file.write(filepath, JSON.stringify(languages[lang], null, 2));
      grunt.log.writeln('Wrote "%s" translations in %s', lang, filepath);
    }

    done();
  });
};
