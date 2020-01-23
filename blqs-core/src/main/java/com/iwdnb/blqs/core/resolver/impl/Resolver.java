package com.iwdnb.blqs.core.resolver.impl;

import java.util.List;

import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.iwdnb.blqs.core.Context;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.resolver.ast.Clazz;

public abstract class Resolver {

    public boolean accept(ResolvedType resolvedType) {
        return !Context.getContext().getIgnoreTypes().contains(Clazz.getName(resolvedType, null));
    }

    public abstract void resolve(Types types, ResolvedType resolvedType,
                                 List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap);

}
