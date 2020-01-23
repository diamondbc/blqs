package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.resolver.TypeResolvers;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Clazz;

public class MapResolver extends ReferenceResolver {

    @Override
    public boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return Clazz.Maps.isAssignableBy(typeDeclaration);
    }

    @Override
    public void resolveSpecial(Types types, ResolvedType resolvedType, ResolvedReferenceTypeDeclaration typeDeclaration,
                               List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        if (typeParametersMap != null && typeParametersMap.size() == 2) {
            ObjectNode objectNode = ObjectMappers.instance().createObjectNode();
            Object key = null;
            Object value = null;
            if (!"?".equals(typeParametersMap.get(0).b.describe())) {
                Types componentType = TypeResolvers.of(typeParametersMap.get(0).b, typeParametersMap);
                if (componentType.isResolved()) {
                    key = componentType.getValue();
                }
            }
            if (!"?".equals(typeParametersMap.get(1).b.describe())) {
                Types componentType = TypeResolvers.of(typeParametersMap.get(1).b, typeParametersMap);
                if (componentType.isResolved()) {
                    value = componentType.getValue();
                }
            }
            if (key != null && value != null) {
                objectNode.putPOJO(String.valueOf(key), value);
                types.setValue(objectNode);
            }
        }
    }
}
