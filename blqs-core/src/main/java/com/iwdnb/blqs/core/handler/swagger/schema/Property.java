package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwdnb.blqs.core.common.Element;

import lombok.Data;

/**
 * @date: 2019-05-29 20:02
 */
@Data
public class Property {

    @JsonIgnore
    String                name;
    String                type;
    String                format;
    boolean               required = false;
    String                description;
    String                title;
    @JsonProperty("$ref")
    String                ref;
    Map<String, Property> properties;

    public Property(){
    }

    public Property(Element element){
        this.name = element.getName();
        this.type = element.getType();
        this.title = element.getTitle();
        this.description = element.getComment();
    }
}
