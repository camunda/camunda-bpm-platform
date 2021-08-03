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
package org.camunda.bpm.engine.impl.incident;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CompositeIncidentHandlerTest {

    @Test
    public void shouldUseCompositeIncidentHandlerWithMainIncidentHandlerAddNullHandler() {
        CompositeIncidentHandler compositeIncidentHandler = new CompositeIncidentHandler(new DefaultIncidentHandler(""));
        try {
            compositeIncidentHandler.add(null);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handler is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithNullMainHandler() {
        try {
            new CompositeIncidentHandler(null);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handler is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithNullVarargs() {
        IncidentHandler incidentHandler = null;
        try {
            new CompositeIncidentHandler(null, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handlers contains null value");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithNullList() {
        List<IncidentHandler> incidentHandler = null;
        try {
            new CompositeIncidentHandler(null, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handler is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithMainHandlersAndNullVarargValue() {
        IncidentHandler mainIncidentHandler = new DefaultIncidentHandler("failedJob");
        IncidentHandler incidentHandler = null;
        try {
            new CompositeIncidentHandler(mainIncidentHandler, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handlers contains null value");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithMainHandlersAndNullVarargs() {
        IncidentHandler mainIncidentHandler = new DefaultIncidentHandler("failedJob");
        IncidentHandler[] incidentHandler = null;
        try {
            new CompositeIncidentHandler(mainIncidentHandler, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handlers is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithMainHandlersAndNullList() {
        IncidentHandler mainIncidentHandler = new DefaultIncidentHandler("failedJob");

        List<IncidentHandler> incidentHandler = null;
        try {
            new CompositeIncidentHandler(mainIncidentHandler, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handlers is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerArgumentConstructorWithMainHandlersAndListWithNulls() {
        IncidentHandler mainIncidentHandler = new DefaultIncidentHandler("failedJob");

        List<IncidentHandler> incidentHandler = new ArrayList<>();
        incidentHandler.add(null);
        incidentHandler.add(null);
        try {
            new CompositeIncidentHandler(mainIncidentHandler, incidentHandler);
            fail("NullValueException expected");
        } catch (NullValueException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incident handler is null");
        }
    }

    @Test
    public void shouldUseCompositeIncidentHandlerWithAnotherIncidentType() {
        CompositeIncidentHandler compositeIncidentHandler = new CompositeIncidentHandler(new DefaultIncidentHandler("failedJob"));
        try {
            compositeIncidentHandler.add(new DefaultIncidentHandler("failedExternalTask"));
            fail("Non expected message expected");
        } catch (ProcessEngineException e) {
            assertThat(e.getMessage()).containsIgnoringCase("Incorrect incident type handler in composite handler with type: failedJob");
        }
    }
}

