package com.iwdnb.blqs.core.handler.swagger.schema;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tag {

    String name;
    String description;

    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
