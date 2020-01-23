package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.resolver.Types;

public abstract class ReferenceResolver extends Resolver {

    @Override
    public boolean accept(ResolvedType resolvedType) {
        return super.accept(resolvedType) && resolvedType.isReferenceType()
               && accept(resolvedType.asReferenceType().getTypeDeclaration());
    }

    @Override
    public void resolve(Types types, ResolvedType resolvedType,
                        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();
        ResolvedReferenceTypeDeclaration typeDeclaration = referenceType.getTypeDeclaration();
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap2 = referenceType.getTypeParametersMap();
        typeParametersMap.addAll(typeParametersMap2);
        resolveSpecial(types, resolvedType, typeDeclaration, typeParametersMap);
    }

    public abstract boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration);

    public abstract void resolveSpecial(Types types, ResolvedType resolvedType,
                                        ResolvedReferenceTypeDeclaration typeDeclaration,
                                        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap);
}
