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

var fs = require('fs');
var path = require('path');

var walk = function(dir, done) {
  // console.log(dir);
  if (
    dir.includes('node_modules') ||
    // dir.includes('camunda-commons-ui') ||
    dir.includes('.git') ||
    dir.includes('target')
  ) {
    return done(null, []);
  }
  var results = [];
  fs.readdir(dir, function(err, list) {
    if (err) return done(err);
    var pending = list.length;
    if (!pending) return done(null, results);
    list.forEach(function(file) {
      file = path.resolve(dir, file);
      fs.stat(file, function(err, stat) {
        if (stat && stat.isDirectory()) {
          walk(file, function(err, res) {
            results = results.concat(res);
            if (!--pending) done(null, results);
          });
        } else {
          results.push(file);
          if (!--pending) done(null, results);
        }
      });
    });
  });
};

walk(__dirname, (err, res) => {
  // console.log(res);
  var root_dir_depth = __dirname.match(/\//g).length;
  res.forEach(file => {
    if (!file.includes('.js')) {
      return;
    }
    // if (file.includes('index.js')) {
    //   console.log(file);
    // }
    fs.readFile(file, 'utf8', function(err, data) {
      if (err) {
        return console.log(err);
      }
      var depth = file.match(/\//g).length - root_dir_depth;

      var replacementString = "require('";

      // ui / welcome / client / scripts;;

      depth--;

      for (let i = 0; i < depth; i++) {
        replacementString += '../';
      }

      if (
        file.includes(
          '/home/stamm/test/merged_repo/camunda-bpm-webapp/camunda-commons-ui/lib/util/index.js'
        )
      ) {
        console.log(file);
        console.log(depth, root_dir_depth, replacementString, __dirname);
      }

      if (
        file.includes(
          '/home/stamm/test/merged_repo/camunda-bpm-webapp/ui/welcome/client/scripts/camunda-welcome-bootstrap.js'
        )
      ) {
        console.log(file);
        console.log(depth, root_dir_depth, replacementString, __dirname);
      }

      var result = data;

      // replacementString += 'camunda-commons-ui';
      result = data.replace(
        /require\('camunda-commons-ui/g,
        replacementString + 'camunda-commons-ui'
      );

      result = result.replace(
        /require\('camunda-bpm-sdk-js/g,
        replacementString + 'camunda-bpm-sdk-js'
      );

      fs.writeFile(file, result, 'utf8', function(err) {
        if (err) return console.log(err);
      });
    });
  });
});
