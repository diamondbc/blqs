package com.iwdnb.blqs.core.http;

import java.util.ArrayList;
import java.util.List;

import com.iwdnb.blqs.core.common.ObjectMappers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwdnb.blqs.core.common.Element;
import com.iwdnb.blqs.core.resolver.Types;

import lombok.Getter;
import lombok.Setter;

/**
 * a http request
 */
@Setter
@Getter
public class HttpRequest {

    HttpRequestMethod method;
    List<String>      uris     = new ArrayList<>();
    HttpHeaders       headers  = new HttpHeaders();

    Object            body;

    List<Element>     elements = new ArrayList<>();
    Types             types;

    public Object queryString() {
        if (HttpRequestMethod.GET.equals(method)) {
            if (elements.size() > 0) {
                String queryString = join(elements);
                if (queryString.length() > 0) {
                    return "?" + queryString;
                }
            }
        }
        return "";
    }

    public boolean hasBody() {
        return !HttpRequestMethod.GET.equals(method);
    }

    public String bodyString() {
        if (getBody() != null && getBody() instanceof JsonNode) {
            return ObjectMappers.toPretty(getBody());
        } else {
            return join(elements);
        }
    }

    public boolean hasParameter() {
        return CollectionUtils.isNotEmpty(elements);
    }

    /**
     * 根据请求方法，参数等 设置合理的content-Type 最低是 form_urlencoded，如果已经是json，并不会覆盖
     * 
     * @return
     */
    public void checkContentType() {
        if (hasBody()) {
            headers.setContentType(HttpHeaders.ContentType.APPLICATION_X_WWW_FORM_URLENCODED);
        }
    }

    private String join(List<Element> elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            if (StringUtils.isBlank(element.getValue())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(element.getName()).append("=").append(element.getValue());
        }
        return sb.toString();
    }

}
