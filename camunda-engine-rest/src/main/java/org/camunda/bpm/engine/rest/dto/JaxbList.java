package org.camunda.bpm.engine.rest.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="List")
public class JaxbList<T>{
    protected List<T> list;

    public JaxbList(){}

    public JaxbList(List<T> list){
      this.list=list;
    }

    @XmlElement(name="Item")
    public List<T> getList(){
      return list;
    }
}
