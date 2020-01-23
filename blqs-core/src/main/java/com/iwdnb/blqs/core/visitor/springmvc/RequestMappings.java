package com.iwdnb.blqs.core.visitor.springmvc;

import java.util.*;

import com.iwdnb.blqs.core.common.URL;
import com.iwdnb.blqs.core.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.iwdnb.blqs.core.http.HttpRequestMethod;
import com.iwdnb.blqs.core.resolver.ast.Annotations;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

/**
 * Spring @RequestMapping 解析工具
 */
@Setter
@Getter
public class RequestMappings {

    private static Logger                              log               = LoggerFactory.getLogger(RequestMappings.class);

    public static final String                         GET_MAPPING       = "GetMapping";
    public static final String                         POST_MAPPING      = "PostMapping";
    public static final String                         PUT_MAPPING       = "PutMapping";
    public static final String                         PATCH_MAPPING     = "PatchMapping";
    public static final String                         DELETE_MAPPING    = "DeleteMapping";
    public static final String                         REQUEST_MAPPING   = "RequestMapping";

    public static final List<String>                   ANNOTATIONS       = Lists.newArrayList(GET_MAPPING, POST_MAPPING,
                                                                                              PUT_MAPPING,
                                                                                              PATCH_MAPPING,
                                                                                              DELETE_MAPPING,
                                                                                              REQUEST_MAPPING);

    public static final Map<String, HttpRequestMethod> ANNOTATION_METHOD = new HashMap<>(ANNOTATIONS.size());

    static {
        ANNOTATION_METHOD.put(GET_MAPPING, HttpRequestMethod.GET);
        ANNOTATION_METHOD.put(POST_MAPPING, HttpRequestMethod.POST);
        ANNOTATION_METHOD.put(PUT_MAPPING, HttpRequestMethod.PUT);
        ANNOTATION_METHOD.put(PATCH_MAPPING, HttpRequestMethod.PATCH);
        ANNOTATION_METHOD.put(DELETE_MAPPING, HttpRequestMethod.DELETE);
        ANNOTATION_METHOD.put(REQUEST_MAPPING, HttpRequestMethod.POST);
    }

    public static final String                         REQUEST_METHOD_GET    = "RequestMethod.GET";
    public static final String                         REQUEST_METHOD_POST   = "RequestMethod.POST";
    public static final String                         REQUEST_METHOD_PUT    = "RequestMethod.PUT";
    public static final String                         REQUEST_METHOD_PATCH  = "RequestMethod.PATCH";
    public static final String                         REQUEST_METHOD_DELETE = "RequestMethod.DELETE";

    public static final Map<String, HttpRequestMethod> ATTRS_METHOD          = new HashMap<>();

    static {
        ATTRS_METHOD.put(REQUEST_METHOD_GET, HttpRequestMethod.GET);
        ATTRS_METHOD.put(REQUEST_METHOD_POST, HttpRequestMethod.POST);
        ATTRS_METHOD.put(REQUEST_METHOD_PUT, HttpRequestMethod.PUT);
        ATTRS_METHOD.put(REQUEST_METHOD_PATCH, HttpRequestMethod.PATCH);
        ATTRS_METHOD.put(REQUEST_METHOD_DELETE, HttpRequestMethod.DELETE);
    }

    public static boolean accept(NodeList<AnnotationExpr> nodes) {
        for (AnnotationExpr node : nodes) {
            if (accept(node)) {
                return true;
            }
        }
        return false;
    }

    public static boolean accept(AnnotationExpr n) {
        return ANNOTATIONS.contains(n.getNameAsString());
    }

    HttpRequestMethod method;
    List<String>      path    = new ArrayList<>();
    HttpHeaders headers = new HttpHeaders();

    public static RequestMappings of(AnnotationExpr n) {
        if (!accept(n)) {
            throw new IllegalArgumentException("annotationExpr not accept:" + n.getNameAsString());
        }
        // 解析注解各个属性
        Map<String, Object> annotationAttrs = Annotations.getAttrs(n);

        RequestMappings requestMappings = new RequestMappings();
        // 解析并设置http请求方法
        if (annotationAttrs.containsKey("method")) {
            Object methodString = annotationAttrs.get("method");
            if (annotationAttrs.get("method") instanceof List) {
                methodString = ((List) annotationAttrs.get("method")).get(0);
            }
            HttpRequestMethod m = ATTRS_METHOD.get(methodString);
            if (m != null) {
                requestMappings.setMethod(m);
            }
        } else {
            requestMappings.setMethod(ANNOTATION_METHOD.get(n.getNameAsString()));
        }
        // 解析并设置http请求路径
        if (annotationAttrs.containsKey("value")) {
            Object value = annotationAttrs.get("value");
            if (value instanceof List) {
                for (Object o : (List) value) {
                    requestMappings.getPath().add(String.valueOf(o));
                }
            } else {
                requestMappings.getPath().add(String.valueOf(value));
            }
        } else {
            requestMappings.getPath().add("");
        }
        // TODO 解析headers

        return requestMappings;
    }

    /**
     * 解析Class的@RequestMapping注解 Class上只能使用@RequestMapping
     *
     * @param n
     * @return
     */
    public static Optional<RequestMappings> of(ClassOrInterfaceDeclaration n) {

        RequestMappings requestMappings = null;

        Optional<AnnotationExpr> requestMapping = n.getAnnotationByName(REQUEST_MAPPING);
        if (requestMapping.isPresent()) {
            requestMappings = RequestMappings.of(requestMapping.get());
            requestMappings.setParent(getParentPath(n));
        }
        return Optional.ofNullable(requestMappings);
    }

    public static String getParentPath(ClassOrInterfaceDeclaration n) {
        String parent = "";
        try {
            for (ClassOrInterfaceType classOrInterfaceType : n.getExtendedTypes()) {
                ResolvedReferenceType resolve = classOrInterfaceType.resolve();
                for (ResolvedReferenceType referenceType : resolve.getDirectAncestors()) {
                    Optional<AnnotationExpr> requestMapping = ((JavaParserClassDeclaration) referenceType.getTypeDeclaration()).getWrappedNode().getAnnotationByName(REQUEST_MAPPING);
                    if (requestMapping.isPresent()) {
                        RequestMappings extend = RequestMappings.of(requestMapping.get());
                        parent = URL.normalize(parent, extend.getPath().get(0));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("parse super fail:{}", e.getMessage());
        }
        return parent;
    }

    public void setParent(String parent) {
        for (int i = 0; i < getPath().size(); i++) {
            getPath().set(i, URL.normalize(parent, getPath().get(i)));
        }
    }

    public static boolean isRequestBody(MethodDeclaration n) {
        return n.isAnnotationPresent("ResponseBody");
    }
}
