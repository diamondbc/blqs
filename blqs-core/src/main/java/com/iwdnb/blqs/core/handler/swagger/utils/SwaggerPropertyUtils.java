package com.iwdnb.blqs.core.handler.swagger.utils;

/*
 * Copyright 2015-2017 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

import static com.google.common.base.Functions.forMap;
import static com.google.common.base.Strings.nullToEmpty;
import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Types.isVoid;

import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import io.swagger.models.properties.*;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.ModelReference;

public class SwaggerPropertyUtils {

    private static final List<String>                                      simpleTypes = Arrays.asList("byte", "short",
                                                                                                       "int", "integer",
                                                                                                       "long", "float",
                                                                                                       "double",
                                                                                                       "string",
                                                                                                       "boolean",
                                                                                                       "date",
                                                                                                       "date-time",
                                                                                                       "bigdecimal",
                                                                                                       "biginteger");

    private static final Map<String, Function<String, ? extends Property>> typeFactory = ImmutableMap.<String, Function<String, ? extends Property>> builder()
                                                                                                     //
                                                                                                     .put("short",
                                                                                                          newInstanceOf(IntegerProperty.class))
                                                                                                     //
                                                                                                     .put("int",
                                                                                                          newInstanceOf(IntegerProperty.class))
                                                                                                     //
                                                                                                     .put("integer",
                                                                                                          newInstanceOf(IntegerProperty.class))
                                                                                                     //
                                                                                                     .put("long",
                                                                                                          newInstanceOf(LongProperty.class))
                                                                                                     //
                                                                                                     .put("float",
                                                                                                          newInstanceOf(FloatProperty.class))
                                                                                                     //
                                                                                                     .put("double",
                                                                                                          newInstanceOf(DoubleProperty.class))
                                                                                                     //
                                                                                                     .put("string",
                                                                                                          newInstanceOf(StringProperty.class))
                                                                                                     //
                                                                                                     .put("boolean",
                                                                                                          newInstanceOf(BooleanProperty.class))
                                                                                                     //
                                                                                                     .put("date",
                                                                                                          newInstanceOf(DateProperty.class))
                                                                                                     //
                                                                                                     .put("date-time",
                                                                                                          newInstanceOf(DateTimeProperty.class))
                                                                                                     //
                                                                                                     .put("bigdecimal",
                                                                                                          newInstanceOf(DecimalProperty.class))
                                                                                                     //
                                                                                                     .put("biginteger",
                                                                                                          newInstanceOf(BaseIntegerProperty.class))
                                                                                                     //
                                                                                                     .put("uuid",
                                                                                                          newInstanceOf(UUIDProperty.class))
                                                                                                     //
                                                                                                     .put("object",
                                                                                                          newInstanceOf(ObjectProperty.class))
                                                                                                     //
                                                                                                     .put("byte",
                                                                                                          bytePropertyFactory())
                                                                                                     //
                                                                                                     .put("__file",
                                                                                                          filePropertyFactory()).build();

    private SwaggerPropertyUtils(){
        throw new UnsupportedOperationException();
    }

    public static Property buildProperty(final String typeName) {
        String safeTypeName = nullToEmpty(typeName);
        Function<String, Function<String, ? extends Property>> propertyLookup = forMap(typeFactory,
                                                                                       voidOrRef(safeTypeName));
        return propertyLookup.apply(safeTypeName.toLowerCase()).apply(safeTypeName);
    }

    public static Property property(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        if (simpleType(type)) {
            return buildProperty(formatSimpleType(type));
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(type);
        } catch (ClassNotFoundException e) {
            return null;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return new MapProperty();
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return new ArrayProperty();
        }
        return null;
    }

    public static String formatSimpleType(String type) {
        return type.replaceAll("java.lang.", "").replaceAll("java.util.", "").replaceAll("java.math.",
                                                                                         "").toLowerCase();
    }

    public static boolean simpleType(String type) {
        if (StringUtils.isBlank(type)) {
            return true;
        }
        return simpleTypes.contains(formatSimpleType(type));
    }

    private static Function<? super ModelReference, String> toTypeName() {
        return new Function<ModelReference, String>() {

            @Override
            public String apply(ModelReference input) {
                return input.getType();
            }
        };
    }

    private static <T extends Property> Function<String, T> newInstanceOf(final Class<T> clazz) {
        return new Function<String, T>() {

            @Override
            public T apply(String input) {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    // This is bad! should never come here
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    static Ordering<String> defaultOrdering(Map<String, ModelProperty> properties) {
        return Ordering.from(byPosition(properties)).compound(byName());
    }

    private static Function<String, ? extends Property> voidOrRef(final String typeName) {
        return new Function<String, Property>() {

            @Override
            public Property apply(String input) {
                if (typeName.equalsIgnoreCase("void")) {
                    return null;
                }
                return new RefProperty(typeName);
            }
        };
    }

    private static Function<String, ? extends Property> bytePropertyFactory() {
        return new Function<String, Property>() {

            @Override
            public Property apply(String input) {
                final IntegerProperty integerProperty = new IntegerProperty();
                integerProperty.setFormat("int32");
                integerProperty.setMaximum(BigDecimal.valueOf(Byte.MAX_VALUE));
                integerProperty.setMinimum(BigDecimal.valueOf(Byte.MIN_VALUE));
                return integerProperty;
            }
        };
    }

    private static Function<String, ? extends Property> filePropertyFactory() {
        return new Function<String, Property>() {

            @Override
            public Property apply(String input) {
                return new FileProperty();
            }
        };
    }

    private static Comparator<String> byName() {
        return new Comparator<String>() {

            @Override
            public int compare(String first, String second) {
                return first.compareTo(second);
            }
        };
    }

    private static Comparator<String> byPosition(final Map<String, ModelProperty> modelProperties) {
        return new Comparator<String>() {

            @Override
            public int compare(String first, String second) {
                ModelProperty p1 = modelProperties.get(first);
                ModelProperty p2 = modelProperties.get(second);
                return Ints.compare(p1.getPosition(), p2.getPosition());
            }
        };
    }

    static Predicate<Map.Entry<String, ModelProperty>> voidProperties() {
        return new Predicate<Map.Entry<String, ModelProperty>>() {

            @Override
            public boolean apply(Map.Entry<String, ModelProperty> input) {
                return isVoid(input.getValue().getType()) || collectionOfVoid(input.getValue().getType())
                       || arrayTypeOfVoid(input.getValue().getType().getArrayElementType());
            }
        };
    }

    private static boolean arrayTypeOfVoid(ResolvedType arrayElementType) {
        return arrayElementType != null && isVoid(arrayElementType);
    }

    private static boolean collectionOfVoid(ResolvedType type) {
        return isContainerType(type) && isVoid(collectionElementType(type));
    }
}
