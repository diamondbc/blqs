package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request {

    Url             url        = new Url();
    Method          method;
    String          description;
    List<Header>    header     = new ArrayList<>();
    Body            body       = new Body();
    List<Parameter> parameters = new ArrayList<>();

}
