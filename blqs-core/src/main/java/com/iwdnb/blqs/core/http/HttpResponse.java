package com.iwdnb.blqs.core.http;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.resolver.Types;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HttpResponse {

    HttpResponseStatus status  = HttpResponseStatus.DEFAULT;
    HttpHeaders        headers = new HttpHeaders();
    Object             body;

    private Types      types;

    public boolean isEmpty() {
        return Objects.isNull(body) && headers.isEmpty();
    }

    public boolean hasBody() {
        return body != null;
    }

    public String bodyString() {
        if (getBody() != null && getBody() instanceof JsonNode) {
            return ObjectMappers.toPretty(getBody());
        }
        return "";
    }

}
