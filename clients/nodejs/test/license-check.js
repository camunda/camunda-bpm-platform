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

import checker from 'license-checker';

const ALLOWED_LICENSES = [
  'MIT',
  'MIT*',
  'ISC',
  'BSD',
  'BSD-2-Clause',
  'BSD-3-Clause',
  'Apache-2.0',
  'Apache-2.0 WITH LLVM-exception',
  '(MIT OR CC0-1.0)'
];

console.log(
  `\n\nChecking licenses...`
);

checker.init(
  {
    start: '.',
    production: true,
    excludePrivatePackages: true
  },
  function(err, packages) {
    if (err) {
      throw err;
    } else {
      const entries = Object.entries(packages);
      let licenseWarning = '';

      for (const [p, info] of entries) {
        const licenses =
          typeof info.licenses === 'object'
            ? info.licenses
            : [info.licenses];

        licenses.forEach(license => {
          if (!ALLOWED_LICENSES.includes(license)) {
            licenseWarning += `${p} uses ${license}\n`;
          }
        });
      }

      if (licenseWarning) {
        console.error(
          `These Packages use unknown licenses:\n${licenseWarning}`
        );
        process.exit(1);
      }
      console.log(
        `all good!`
      );
      process.exit(0);
    }
  }
);
