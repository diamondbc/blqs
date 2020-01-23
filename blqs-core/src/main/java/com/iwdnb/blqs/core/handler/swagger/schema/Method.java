package com.iwdnb.blqs.core.handler.swagger.schema;

import com.iwdnb.blqs.core.http.HttpRequestMethod;

/**
 * 支持的http method
 */
public enum Method {

                    GET,
                    POST,
                    PUT,
                    DELETE,
                    OPTIONS,
                    PATCH,
                    COPY,
                    HEAD,
                    LINK,
                    UNLINK,
                    PURGE,
                    LOCK,
                    UNLOCK,
                    PROPFIND,
                    VIEW;

    public static Method of(HttpRequestMethod method) {
        return valueOf(method.name());
    }
}
