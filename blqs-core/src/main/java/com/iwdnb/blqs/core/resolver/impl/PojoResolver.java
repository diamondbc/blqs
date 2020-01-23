package com.iwdnb.blqs.core.resolver.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistFactory;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistFieldDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.common.Element;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.resolver.TypeResolvers;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.*;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PojoResolver extends ReferenceResolver {

    @Override
    public boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return !Clazz.Langs.isAssignableBy(typeDeclaration) && !Clazz.Collections.isAssignableBy(typeDeclaration);
    }

    @Override
    public void resolveSpecial(Types types, ResolvedType pojoResolvedType,
                               ResolvedReferenceTypeDeclaration typeDeclaration,
                               List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        ObjectNode objectNode = ObjectMappers.instance().createObjectNode();
        // 先解析父类的字段
        try {
            typeDeclaration.getAncestors().forEach(direct -> {
                Types ancestor = TypeResolvers.of(direct, typeParametersMap);
                if (ancestor.isResolved() && !ancestor.isPrimitive()) {
                    if (ancestor.getValue() instanceof ObjectNode) {
                        ObjectNode directValue = (ObjectNode) ancestor.getValue();
                        objectNode.setAll(directValue);
                    }
                    types.getElements().addAll(ancestor.getElements());
                }
            });
        } catch (Exception e) {
            log.warn("PojoResolver exception:" + e.getLocalizedMessage());
        }
        // 解析各字段
        for (ResolvedFieldDeclaration next : typeDeclaration.getDeclaredFields()) {

            ResolvedType resolvedType = null;
            try {
                resolvedType = next.getType();
            } catch (Exception e) {
                if (e instanceof RuntimeException && e.getCause() instanceof NotFoundException) {
                    // 从Classpool获取类型
                    resolvedType = resolvedTypeByClassPool(next);
                    if (resolvedType == null) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            if (resolvedType == null || next.isStatic() || typeDeclaration.equals(resolvedType)) {
                continue;
            }
            String description = null;
            String name = Fields.getName(next);
            // 处理类字段的默认值
            if (pojoResolvedType == null) {
                log.warn("pojoResolvedType is null,types:" + JSON.toJSONString(types));
                continue;
            }

            Types resolvedTypes = TypeResolvers.getTypeVariable(resolvedType,
                                                                pojoResolvedType.asReferenceType().getTypeParametersMap());
            if (resolvedTypes == null) {
                continue;
            }
            resolvedTypes = resolvedTypes.duplicate();
            resolvedTypes.prefix(name + ".");

            String condition = "";

            if (next instanceof JavaParserFieldDeclaration) {
                JavaParserFieldDeclaration field = (JavaParserFieldDeclaration) next;
                if (Comments.isIgnore(field.getWrappedNode())) {
                    continue;
                }
                condition = Validations.of(field.getWrappedNode().getAnnotations()).getResults();

                Object value = Fields.getInitializer(field);
                if (value != null) {
                    resolvedTypes.setValue(value);
                }

                Optional<Comments> comments = Comments.of(field.getWrappedNode().getComment());
                if (comments.isPresent()) {
                    description = comments.get().getContent();
                    for (Tag tag : comments.get().getTags()) {
                        if (tag.getName().equals(Tags.value.name())) {
                            resolvedTypes.setValue(tag.getContent());
                        }
                    }
                }

            }

            Element element = new Element(name, noNull(resolvedTypes.getName()), noNull(condition), null, null);
            if (resolvedTypes.isPrimitive()) {
                element.setValue(noNull(resolvedTypes.getValue()));
            }
            if ("array".equalsIgnoreCase(resolvedTypes.getType())) {
                element.setTag(resolvedTypes.getTag());
                element.setType(resolvedTypes.getType());
            } else {
                element.setTag(resolvedTypes.getTag());
                element.setValue("");
                element.setType(resolvedTypes.getType());
            }
            element.setComment(noNull(description));
            if (Objects.nonNull(resolvedTypes.getValue())) {
                objectNode.putPOJO(name, resolvedTypes.getValue());
            }
            addElement(types, element);
            // types.getElements().addAll(resolvedTypes.getElements());

        }
        types.setType("object");
        types.setValue(objectNode);
        types.setTag(types.getName());
    }

    private void addElement(Types types, Element element) {
        for (Element e : types.getElements()) {
            if (e.getName().equals(element.getName())) {
                return;
            }
        }
        types.getElements().add(element);
    }

    private ResolvedType resolvedTypeByClassPool(ResolvedFieldDeclaration next) {
        ClassPool pool = ClassPool.getDefault();
        try {
            JavassistFieldDeclaration javassistFieldDeclaration = (JavassistFieldDeclaration) next;
            CtField ctField = (CtField) getFieldValue(javassistFieldDeclaration, "ctField");
            TypeSolver typeSolver = (TypeSolver) getFieldValue(javassistFieldDeclaration, "typeSolver");
            String classname = ctField.getFieldInfo().getDescriptor();
            if (classname.charAt(0) == 'L') {
                classname = classname.substring(1, classname.indexOf(";")).replace('/', '.');
            }
            CtClass ctClass = pool.get(classname);
            return JavassistFactory.typeUsageFor(ctClass, typeSolver);
        } catch (NotFoundException e1) {
            log.warn("resolvedTypeByClassPool class not found:" + e1.getLocalizedMessage());
        }
        return null;
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            try {
                if (f.getName().equals(fieldName)) {
                    return f.get(obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 没有查到时返回空字符串
        return "";
    }

    private String noNull(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

}
