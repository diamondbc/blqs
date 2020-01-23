package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item {

    String                operationId = UUID.randomUUID().toString();
    String                summary;
    String                description;
    List<String>          tags        = new ArrayList<>();
    List<String>          consumes    = new ArrayList<>();
    @JsonIgnore
    Url                   url         = new Url();
    @JsonIgnore
    Method                method;
    List<Header>          header      = new ArrayList<>();
    Body                  body        = new Body();
    List<Parameter>       parameters  = new ArrayList<>();

    /**
     * Map<code,Response>
     */
    Map<String, Response> responses   = new HashMap<>();
    // List<Response> response = new ArrayList<>();

}
