package com.iwdnb.blqs.core.visitor.springmvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.google.common.collect.Sets;
import com.iwdnb.blqs.core.Options;
import com.iwdnb.blqs.core.common.Element;
import com.iwdnb.blqs.core.resolver.TypeResolvers;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Annotations;
import com.iwdnb.blqs.core.resolver.ast.Comments;
import com.iwdnb.blqs.core.resolver.ast.Defaults;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Mvc 参数类型解析
 */
@Setter
@Getter
@Slf4j
public class Parameters {

    public static final String REQUEST_BODY = "RequestBody";
    public static final String REQUEST_PARAM = "RequestParam";
    public static final String REQUEST_HEADER = "RequestHeader";
    public static final String PATH_VARIABLE = "PathVariable";
    public static final String JPARAM_BODY = "JParam";
    public static final String SPRING_JSON_BODY = "SpringJsonParam";

    public static final String MVC_MODEL = "MODEL";

    public static final Set<String> MVCS = Sets.newHashSet(MVC_MODEL);
    /**
     * 是否基本类型(包括string)
     */
    boolean primitive;
    /**
     * 是否路径字段
     */
    boolean pathVariable;
    /**
     * 是否请求体接收字段
     */
    boolean requestBody;
    /**
     * 是否是header
     */
    boolean header;
    /**
     * 是否文件字段
     */
    boolean file;
    /**
     * 是否spring mvc 保留字段
     */
    boolean mvc;
    /**
     * 参数忽略解析
     */
    boolean ignore;

    String name;
    String type;
    Object value;
    String description;
    List<Element> elements = new ArrayList<>();
    /**
     * body的参数类型
     */
    private Types types;

    public static Parameters of(Parameter param) {
        Parameters parameters = new Parameters();

        // TODO file mvc ?

        if (param.isAnnotationPresent(PATH_VARIABLE)) {
            parameters.setPathVariable(true);
            parameters.resolvePath(param);
        } else if (param.isAnnotationPresent(REQUEST_HEADER)) {
            parameters.setHeader(true);
            parameters.resolveHeader(param);
        } else {
            if (param.isAnnotationPresent(REQUEST_BODY)) {
                parameters.setRequestBody(true);
            }
            parameters.tryResolve(param);
        }

        String typeName = parameters.getTypes() != null ? parameters.getTypes().getName() : "";
        if (Options.IGNORE_TYPES.contains(typeName)) {
            parameters.setIgnore(true);
        }
        return parameters;
    }

    private void resolvePath(Parameter param) {
        Element element = new Element(param.getNameAsString(), param.getTypeAsString(), "",
                String.valueOf(Defaults.get(param.getTypeAsString())),
                Comments.getCommentFromMethod(param));
        elements.add(element);
    }

    private void resolveHeader(Parameter param) {
        name = param.getNameAsString();
        type = param.getTypeAsString();
        value = Defaults.get(type);
        // 解析RequestParam 获取字段名和默认值
        Object valueAttr = Annotations.getAttr(param.getAnnotationByName(REQUEST_HEADER), "value");
        Object defaultValueAttr = Annotations.getAttr(param.getAnnotationByName(REQUEST_HEADER), "defaultValue");
        if (valueAttr != null) {
            name = String.valueOf(valueAttr);
        }
        if (defaultValueAttr != null) {
            value = defaultValueAttr;
        }
        Element element = new Element(name, type, "", String.valueOf(value), Comments.getCommentFromMethod(param));
        elements.add(element);
    }

    private void tryResolve(Parameter param) {
        try {
            ResolvedParameterDeclaration parameterDeclaration = param.resolve();
            Types astResolvedType = TypeResolvers.of(parameterDeclaration.getType(), null);
            setPrimitive(astResolvedType.isPrimitive());
            setValue(astResolvedType.getValue());
            this.types = astResolvedType;
            if (astResolvedType.isPrimitive()) {
                String name = param.getNameAsString();
                String value = String.valueOf(astResolvedType.getValue());
                // 解析RequestParam 获取字段名和默认值
                Object valueAttr = Annotations.getAttr(param.getAnnotationByName(REQUEST_PARAM), "value");
                if (valueAttr != null) {
                    name = String.valueOf(valueAttr);
                }
                Object defaultValueAttr = Annotations.getAttr(param.getAnnotationByName(REQUEST_PARAM), "defaultValue");
                if (defaultValueAttr != null) {
                    value = String.valueOf(defaultValueAttr);
                }
                elements.add(new Element(name, astResolvedType.getName(), "", value,
                        Comments.getCommentFromMethod(param)));
            }
            resolveTypeParameter(astResolvedType, elements, "", new ArrayList<>());

        } catch (Exception e) {
            log.debug("parameters parse fail:{}", param.toString());
        }
    }

    private void resolveTypeParameter(Types astResolvedType, List<Element> elements, String paramName,
                                      List<String> typeNames) {
        try {
            for (Element element : astResolvedType.getElements()) {
                if (StringUtils.isBlank(element.getTag())) {
                    element.setTag("");
                }
                String type = element.getType();
                String name = element.getName();
                // 循环自己
                if (astResolvedType.getName().equals(element.getTag()) || typeNames.contains(element.getTag())) {
                    element.setName(paramName + name);
                    element.setTag(element.getTag());
                    elements.add(element);
                } else if ("object".equalsIgnoreCase(type)) {
                    Types types = Types.get(element.getTag());
                    if (types == null) {
                        element.setName(paramName + name);
                        elements.add(element);
                        continue;
                    } else {
                        types = types.duplicate();
                        typeNames.add(types.getName());
                        resolveTypeParameter(types, elements, paramName + name + ".", typeNames);
                    }
                } else if ("array".equalsIgnoreCase(type)) {
                    Types types = Types.get(element.getTag());
                    if (types == null) {
                        continue;
                    }
                    types = types.duplicate();
                    if (types.isPrimitive()) {
                        element.setName(paramName + name);
                        elements.add(element);
                    } else {
                        resolveTypeParameter(types, elements, paramName + name + "[0].", typeNames);
                    }
                } else {
                    element.setName(paramName + name);
                    elements.add(element);
                }
            }
        } catch (Exception e) {
            log.warn("parameters parse fail:{}:{}", e.getLocalizedMessage(), astResolvedType);
        }
    }

}
