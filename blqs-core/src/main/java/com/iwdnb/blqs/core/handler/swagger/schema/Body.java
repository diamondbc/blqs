package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Body {

    BodyMode        mode;
    String          raw;
    List<Parameter> urlencoded = new ArrayList<>();
    List<Parameter> formdata   = new ArrayList<>();

}
