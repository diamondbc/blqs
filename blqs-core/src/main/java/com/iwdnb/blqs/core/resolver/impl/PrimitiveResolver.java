package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Clazz;
import com.iwdnb.blqs.core.resolver.ast.Defaults;

public class PrimitiveResolver extends Resolver {

    @Override
    public boolean accept(ResolvedType resolvedType) {
        return super.accept(resolvedType) && resolvedType.isPrimitive() || Clazz.Numbers.isAssignableBy(resolvedType);
    }

    @Override
    public void resolve(Types types, ResolvedType resolvedType,
                        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        types.setPrimitive(true);
        types.setValue(Defaults.DEFAULT_INTEGER);
        types.setType(Clazz.getName(resolvedType, typeParametersMap));
    }
}
