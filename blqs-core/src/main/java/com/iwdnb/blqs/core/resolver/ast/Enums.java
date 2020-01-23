package com.iwdnb.blqs.core.resolver.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration;
import com.iwdnb.blqs.core.common.Element;

public class Enums {

    public static String getNames(ResolvedEnumDeclaration enumDeclaration) {
        StringBuilder sb = new StringBuilder();
        for (ResolvedEnumConstantDeclaration resolvedEnumConstantDeclaration : enumDeclaration.getEnumConstants()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(resolvedEnumConstantDeclaration.getName());
        }
        return sb.toString();
    }

    public static List<Element> toDetails(EnumDeclaration declaration) {
        List<Element> elements = new ArrayList<>();
        for (EnumConstantDeclaration constant : declaration.getEntries()) {
            Element element = new Element();
            element.setName(constant.getNameAsString());
            for (Expression expression : constant.getArguments()) {
                Object value = Expressions.getValue(expression);
                element.setName(String.valueOf(value));
            }
            elements.add(element);
        }
        return elements;
    }

}
