package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.resolver.TypeResolvers;
import com.iwdnb.blqs.core.resolver.Types;

public class ArrayResolver extends Resolver {

    @Override
    public boolean accept(ResolvedType resolvedType) {
        return super.accept(resolvedType) && resolvedType.isArray();
    }

    @Override
    public void resolve(Types types, ResolvedType resolvedType,
                        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        ArrayNode arrayNode = ObjectMappers.instance().createArrayNode();
        Types componentType = TypeResolvers.of(resolvedType.asArrayType().getComponentType(),
                                               typeParametersMap).duplicate();
        if (componentType.isResolved()) {
            componentType.prefix("[].");
            arrayNode.addPOJO(componentType.getValue());
            types.setValue(arrayNode);
            types.getElements().addAll(componentType.getElements());
            types.setType("array");
        }
    }
}
