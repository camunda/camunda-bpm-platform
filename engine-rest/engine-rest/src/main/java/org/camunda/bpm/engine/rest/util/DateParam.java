/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DateParam {

    private final Date date;

    //Since SimpleDateFormat is not thread safe we need one formater each thread.
    private static final ThreadLocal<SimpleDateFormat> FORMATTER = new ThreadLocal<SimpleDateFormat>() {
      @Override
      protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      }
    };

    public DateParam(String dateStr) throws WebApplicationException {
      if (dateStr == null || dateStr.isEmpty()) {
        date = null;
      } else {
        try {
          this.date = FORMATTER.get().parse(dateStr);
        } catch (ParseException pe) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't parse date string: " + pe.getMessage()).build());
        }
      }
    }

    //is called from jax-rs
    public static DateParam valueOf(String string) {
      return new DateParam(string);
    }

    public Date getDate() {
      return date;
    }

  }