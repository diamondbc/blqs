package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Clazz;
import com.iwdnb.blqs.core.resolver.ast.Defaults;

public class DateResolver extends ReferenceResolver {

    @Override
    public boolean accept(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return Clazz.Dates.isAssignableBy(typeDeclaration);
    }

    @Override
    public void resolveSpecial(Types types, ResolvedType resolvedType, ResolvedReferenceTypeDeclaration typeDeclaration,
                               List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        types.setValue(Defaults.DEFAULT_STRING);
        types.setType("string");
    }
}
