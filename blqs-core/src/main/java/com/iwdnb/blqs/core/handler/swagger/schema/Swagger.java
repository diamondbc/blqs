package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.models.Model;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Swagger {

    Info                           info  = new Info();
    List<Tag>                      tags  = new ArrayList<>();
    /**
     * Map<path,Map<method,Item>>
     */
    Map<String, Map<String, Item>> paths = new HashMap<>();

    protected Map<String, Model>   definitions;

}
