package com.iwdnb.blqs.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.utils.Pair;
import com.google.common.collect.Lists;
import com.iwdnb.blqs.core.Context;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.resolver.ast.Clazz;
import com.iwdnb.blqs.core.resolver.impl.*;

public class TypeResolvers {

    private static List<Resolver>          resolvers          = Lists.newArrayList(new ArrayResolver(),
                                                                                   new CharSequenceResolver(),
                                                                                   new PrimitiveResolver());

    private static List<ReferenceResolver> referenceResolvers = Lists.newArrayList(new PojoResolver(),
                                                                                   new CollectionResolver(),
                                                                                   new DateResolver(),
                                                                                   new EnumResolver(),
                                                                                   new MapResolver());

    static {
        resolvers.addAll(referenceResolvers);
    }

    /**
     * 解析类型
     *
     * @param type
     * @return
     */
    public static Types of(Type type) {
        String typeName = Clazz.getName(type);
        if (!Types.contain(typeName)) {
            try {
                ResolvedType resolvedType = type.resolve();
                Types.put(typeName, of(resolvedType, null));
            } catch (UnsolvedSymbolException e) {
                // 解析失败 查找泛型参数
                Types.put(typeName, tryResolveTypeArguments(type));
            }
        }
        return Types.get(typeName);
    }

    public static Types of(ResolvedType resolvedType,
                           List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        if (typeParametersMap == null) {
            typeParametersMap = new ArrayList<>();
        }
        String typeName = Clazz.getName(resolvedType, typeParametersMap);
        if (!Types.contain(typeName)) {
            Types types = Types.get(typeName);
            Types.put(typeName, types);
            types.setName(Clazz.getName(resolvedType, typeParametersMap));
            for (Resolver resolver : resolvers) {
                if (resolver.accept(resolvedType)) {
                    resolver.resolve(types, resolvedType, typeParametersMap);
                    types.setResolved(true);
                    break;
                }
            }
        }
        return Types.get(typeName);
    }

    /**
     * 解析泛型的参数类型，需结合外部环境
     *
     * @param type
     * @param typeParametersMap
     * @return
     */
    public static Types getTypeVariable(ResolvedType type,
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
        Types types = TypeResolvers.of(type, typeParametersMap);
        if (!types.resolved) {
            types.setType("object");
        }
        return types;
    }

    /**
     * 解析泛型参数 只有一个泛型时，直接返回解析这一个泛型 如果有多个泛型，解析为{?0:T,?1:E}
     *
     * @param type
     */
    private static Types tryResolveTypeArguments(Type type) {
        String typeName = Clazz.getName(type);
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
            if (classType.getTypeArguments().isPresent()) {
                NodeList<Type> typeNodeList = classType.getTypeArguments().get();
                List<Types> typesList = new ArrayList<>();
                for (Type typeArgument : typeNodeList) {
                    Types types = of(typeArgument);
                    if (types.isResolved()) {
                        if (typeNodeList.size() == 1) {
                            return types;
                        }
                        typesList.add(types);
                    }
                }
                Types result = Types.get(typeName);
                ArrayNode arrayNode = ObjectMappers.instance().createArrayNode();
                int i = 0;
                for (Types types : typesList) {
                    String field = "?" + i++;
                    Types duplicate = types.duplicate();
                    duplicate.prefix(field + ".");
                    arrayNode.addPOJO(duplicate.getValue());
                    result.resolved = true;
                    result.elements.addAll(duplicate.elements);
                }
                result.value = arrayNode;
                return result;

            }
        }
        return Types.get(typeName);
    }

    public static Types tryParse(String name) {
        SymbolReference<ResolvedReferenceTypeDeclaration> symbolReference = Context.getContext().getTypeSolver().tryToSolveType(name);
        if (symbolReference.isSolved()) {
            ResolvedReferenceTypeDeclaration typeDeclaration = symbolReference.getCorrespondingDeclaration();

            for (ReferenceResolver referenceResolver : referenceResolvers) {
                if (referenceResolver.accept(typeDeclaration)) {
                    Types types = Types.get(name);
                    referenceResolver.resolveSpecial(types, null, typeDeclaration, null);
                    types.setResolved(true);
                    Types.put(name, types);
                    break;
                }
            }
        }
        return Types.get(name);
    }

}
