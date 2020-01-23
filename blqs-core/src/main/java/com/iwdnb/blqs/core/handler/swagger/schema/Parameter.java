package com.iwdnb.blqs.core.handler.swagger.schema;

import com.iwdnb.blqs.core.common.Element;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Parameter {

    String  name;
    String  type;
    Object  value;
    String  description;
    String  in       = "query";
    boolean required = false;

    public Parameter(Element element){
        this.name = element.getName();
        this.type = element.getType();
        this.value = element.getValue();
        this.description = element.getComment();
    }

}
