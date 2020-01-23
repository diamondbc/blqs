package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.resolver.Types;

public class EnumResolver extends ReferenceResolver {

    @Override
    public boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return typeDeclaration.isEnum();
    }

    @Override
    public void resolveSpecial(Types types, ResolvedType resolvedType, ResolvedReferenceTypeDeclaration typeDeclaration,
                               List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        types.setPrimitive(true);
        types.setValue("");
    }
}
