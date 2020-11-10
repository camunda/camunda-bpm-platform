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

module.exports = `<form cam-form role="form">
  <div class="form-group">
    <label for="stringVar">A string</label>
    <input cam-variable-name="stringVar"
           cam-variable-type="String"
           type="text"
           class="form-control"
           id="stringVar">
  </div>

  <div class="form-group">
    <label for="longStringVar">A longer string</label>
    <textarea cam-variable-name="longStringVar"
              cols="30"
              rows="10"
              class="form-control"
              id="longStringVar"></textarea>
  </div>

  <div class="form-group">
    <label for="singleChoiceSelect">1 choice</label>
    <select cam-variable-name="singleChoiceSelect"
            class="form-control"
            id="singleChoiceSelect">
      <option> - None - </option>
      <option value="val1">Value 1</option>
      <option value="val2">Value 2</option>
    </select>
  </div>

  <div class="form-group">
    <label for="multipleChoicesSelect">Multple choices</label>
    <select cam-variable-name="multipleChoicesSelect"
            class="form-control"
            id="multipleChoicesSelect"
            multiple>
      <option value="val1">Value 1</option>
      <option value="val2">Value 2</option>
    </select>
  </div>

<!-- unsupported (for now): radios and checkboxes -->
<!--
  <div class="checkbox">
    <label>
      <input cam-variable-name="checkboxChoices"
             type="checkbox"
             value="">
      Option one is this and that&mdash;be sure to include why it's great
    </label>
  </div>
  <div class="checkbox disabled">
    <label>
      <input cam-variable-name="checkboxChoices"
             type="checkbox"
             value=""
             disabled>
      Option two is disabled
    </label>
  </div>

  <div class="radio">
    <label>
      <input cam-variable-name="radioChoice"
             type="radio"
             name="radioChoice"
             id="optionsRadios1"
             value="option1"
             checked>
      Option one is this and that&mdash;be sure to include why it's great
    </label>
  </div>
  <div class="radio">
    <label>
      <input cam-variable-name="radioChoice"
             type="radio"
             name="radioChoice"
             id="optionsRadios2"
             value="option2">
      Option two can be something else and selecting it will deselect option one
    </label>
  </div>
  <div class="radio disabled">
    <label>
      <input cam-variable-name="radioChoice"
             type="radio"
             name="radioChoice"
             id="optionsRadios3"
             value="option3"
             disabled>
      Option three is disabled
    </label>
  </div>

 -->

</form>
`;
