package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;
import java.util.Objects;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.resolver.TypeResolvers;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Clazz;

public class CollectionResolver extends ReferenceResolver {

    @Override
    public boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return Clazz.Collections.isAssignableBy(typeDeclaration);
    }

    @Override
    public void resolveSpecial(Types types, ResolvedType resolvedType, ResolvedReferenceTypeDeclaration typeDeclaration,
                               List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> collectionTypeParametersMap = resolvedType.asReferenceType().getTypeParametersMap();
        if (collectionTypeParametersMap != null && collectionTypeParametersMap.size() == 1) {
            if (!"?".equals(collectionTypeParametersMap.get(0).b.describe())) {
                Types componentType = TypeResolvers.getTypeVariable(collectionTypeParametersMap.get(0).b,
                                                                    typeParametersMap).duplicate();
                types.setTag(componentType.getName());
                if (componentType.isPrimitive()) {
                    types.setTag(componentType.getName());
                } else if (componentType.isResolved()) {
                    types.setTag(componentType.getTag());
                }
            }
        }
        types.setType("array");
    }

    public static Types of(ResolvedType type,
                           List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        if (type.isTypeVariable()) {
            // 泛型解析
            ResolvedTypeVariable resolvedType = type.asTypeVariable();
            if (typeParametersMap != null) {
                for (int i = 0; i < typeParametersMap.size(); i++) {
                    Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair = typeParametersMap.get(i);
                    if (Objects.equals(resolvedType.asTypeParameter(), pair.a)) {
                        return TypeResolvers.of(pair.b, typeParametersMap);
                    }
                }
            }
        }
        return TypeResolvers.of(type, typeParametersMap);
    }
}
