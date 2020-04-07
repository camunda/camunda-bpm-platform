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

module.exports = `<html>
  <head>
    <title>Camunda commons UI library</title>
    <base href="/" />
    <!--[if IE]><script type="text/javascript">
        // Fix for IE ignoring relative base tags.
        // See http://stackoverflow.com/questions/3926197/html-base-tag-and-local-folder-path-with-internet-explorer
        (function() {
            var baseTag = document.getElementsByTagName('base');
            if (baseTag[0]) { baseTag[0].href = baseTag[0].href; }
        })();
    </script><![endif]-->
    <link rel="icon" href="resources/img/favicon.ico" />
    <link href="styles.css" rel="stylesheet" />
    <link href="test-styles.css" rel="stylesheet" />
  </head>
  <body class="cam-widget-debug-test-page">
    <!-- gh-pages-menu -->

    <header>
      <div>
        <h1>Debug</h1>
      </div>
    </header>

        <section class="widget-description">
      <header>
        <h2>Description</h2>
      </header>
      <p>Simple utility for development puproses that prints a variable.
      Can become useful when you get lost in the scopes madness.</p>
    </section>

    <section class="widget-reference">
      <header>
        <h2>Usage</h2>
      </header>

      <h3>Options</h3>
      <dl>
        <dt><span class="badge">@</span> open</dt>
        <dd>can be used to determine the initial state (open or not) of the debug widget</dd>

        <dt><span class="badge">=</span> debugged</dt>
        <dd>can be used to pass the variable to debug</dd>

        <dt><span class="badge">=</span> extended</dt>
        <dd>Indicate that additional info box should be displayed</dd>

        <dt><span class="badge">=</span> extension-name</dt>
        <dd>Define a label displayed on top of extended information</dd>

        <dt><span class="badge">=</span> extended-info</dt>
        <dd>variable containing extended information value</dd>
      </dl>
    </section>

    <section class="widget-examples">
      <header>
        <h2>Examples</h2>
      </header>

      <div class="widget-example"
           id="default"
           ng-controller="testController">
        <h3>Default usage</h3>
        <pre ng-non-bindable>
          &lt;div
            cam-widget-debug
            debugged=&quot;varToDebug&quot;&gt;
          &lt;/div&gt;
        </pre>
        <div class="test-container"
             id="test-container">
          <div cam-widget-debug
               debugged="varToDebug"></div>

        </div><!-- /.test-container -->

        <div class="widget-example"
             id="extended"
             ng-controller="testController">
          <h3>Extended usage</h3>
          <pre ng-non-bindable>
            &lt;div
              cam-widget-debug
              extended
              extension-name=&quot;test&quot;
              extended-info=&quot;info&quot;
              debugged=&quot;varToDebug&quot;&gt;
            &lt;/div&gt;
          </pre>
          <div class="test-container"
               id="test-container2">
            <div cam-widget-debug
                 extended
                 extension-name="test"
                 extended-info="info"
                 debugged="varToDebug"></div>
        </div>
      </div><!-- /.widget-example -->
    </section>

    <!-- gh-pages-footer -->

    <script src="lib/widgets/debug/test/cam-widget-debug.build.js"></script>
  </body>
</html>
`;
