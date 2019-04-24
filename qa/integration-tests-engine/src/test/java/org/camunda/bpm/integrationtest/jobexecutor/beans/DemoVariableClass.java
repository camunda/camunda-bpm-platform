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
package org.camunda.bpm.integrationtest.jobexecutor.beans;


import java.io.Serializable;
import java.util.Map;

public class DemoVariableClass implements Serializable {
  
  private static final long serialVersionUID = 1L;
  private boolean booleanProperty;
  private byte byteProperty;
  private short shortProperty;
  private int intProperty;
  private long longProperty;   
  private float floatProperty;
  private double doubleProperty;
  private char charProperty;
  
  private Boolean booleanObjectProperty;
  private Byte byteObjectProperty;
  private Short shortObjectProperty;
  private Integer integerObjectProperty;
  private Long longObjectProperty;
  private Float floatObjectProperty;
  private Double doubleObjectProperty;
  
  private String stringProperty;
  private int[] intArrayProperty;
  private Map<Object,Object> mapProperty;
  
  public boolean isBooleanProperty() {
    return booleanProperty;
  }
  
  public void setBooleanProperty(boolean booleanProperty) {
    this.booleanProperty = booleanProperty;
  }
  
  public byte getByteProperty() {
    return byteProperty;
  }
  
  public void setByteProperty(byte byteProperty) {
    this.byteProperty = byteProperty;
  }
  
  public short getShortProperty() {
    return shortProperty;
  }
  
  public void setShortProperty(short shortProperty) {
    this.shortProperty = shortProperty;
  }
  
  public int getIntProperty() {
    return intProperty;
  }
  
  public void setIntProperty(int intProperty) {
    this.intProperty = intProperty;
  }
  
  public long getLongProperty() {
    return longProperty;
  }
  
  public void setLongProperty(long longProperty) {
    this.longProperty = longProperty;
  }
  
  public float getFloatProperty() {
    return floatProperty;
  }
  
  public void setFloatProperty(float floatProperty) {
    this.floatProperty = floatProperty;
  }
  
  public double getDoubleProperty() {
    return doubleProperty;
  }
  
  public void setDoubleProperty(double doubleProperty) {
    this.doubleProperty = doubleProperty;
  }
  
  public char getCharProperty() {
    return charProperty;
  }
  
  public void setCharProperty(char charProperty) {
    this.charProperty = charProperty;
  }
  
  public Boolean getBooleanObjectProperty() {
    return booleanObjectProperty;
  }
  
  public void setBooleanObjectProperty(Boolean booleanObjectProperty) {
    this.booleanObjectProperty = booleanObjectProperty;
  }
  
  public Byte getByteObjectProperty() {
    return byteObjectProperty;
  }
  
  public void setByteObjectProperty(Byte byteObjectProperty) {
    this.byteObjectProperty = byteObjectProperty;
  }
  
  public Short getShortObjectProperty() {
    return shortObjectProperty;
  }
  
  public void setShortObjectProperty(Short shortObjectProperty) {
    this.shortObjectProperty = shortObjectProperty;
  }
  
  public Integer getIntegerObjectProperty() {
    return integerObjectProperty;
  }
  
  public void setIntegerObjectProperty(Integer integerObjectProperty) {
    this.integerObjectProperty = integerObjectProperty;
  }
  
  public Long getLongObjectProperty() {
    return longObjectProperty;
  }
  
  public void setLongObjectProperty(Long longObjectProperty) {
    this.longObjectProperty = longObjectProperty;
  }
  
  public Float getFloatObjectProperty() {
    return floatObjectProperty;
  }
  
  public void setFloatObjectProperty(Float floatObjectProperty) {
    this.floatObjectProperty = floatObjectProperty;
  }
  
  public Double getDoubleObjectProperty() {
    return doubleObjectProperty;
  }
  
  public void setDoubleObjectProperty(Double doubleObjectProperty) {
    this.doubleObjectProperty = doubleObjectProperty;
  }
  
  public String getStringProperty() {
    return stringProperty;
  }
  
  public void setStringProperty(String stringProperty) {
    this.stringProperty = stringProperty;
  }
  
  public int[] getIntArrayProperty() {
    return intArrayProperty;
  }
  
  public void setIntArrayProperty(int[] intArrayProperty) {
    this.intArrayProperty = intArrayProperty;
  }
  
  public Map<Object, Object> getMapProperty() {
    return mapProperty;
  }
  
  public void setMapProperty(Map<Object, Object> mapProperty) {
    this.mapProperty = mapProperty;
  }
  
}