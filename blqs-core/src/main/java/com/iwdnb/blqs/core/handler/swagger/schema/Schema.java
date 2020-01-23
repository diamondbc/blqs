package com.iwdnb.blqs.core.handler.swagger.schema;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 */
@Data
public class Schema {

    private String type;
    @JsonProperty("$ref")
    private String ref;

    public String getRef() {
        if (StringUtils.isNotBlank(ref) && !ref.startsWith(SwaggerConstants.DEFINITION)) {
            ref = SwaggerConstants.DEFINITION + ref;
        }
        return ref;
    }
}
