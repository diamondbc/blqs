package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Url {

    String          raw;
    String          protocol = "http";
    String          host     = "{{host}}";
    String          path;
    String          port;
    List<Parameter> query    = new ArrayList<>();

}
