package com.iwdnb.blqs.core.handler.swagger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.iwdnb.blqs.core.Options;
import com.iwdnb.blqs.core.common.Element;
import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.handler.TreeHandler;
import com.iwdnb.blqs.core.handler.swagger.schema.*;
import com.iwdnb.blqs.core.handler.swagger.utils.MockProperty;
import com.iwdnb.blqs.core.handler.swagger.utils.SwaggerPropertyUtils;
import com.iwdnb.blqs.core.http.HttpHeaders;
import com.iwdnb.blqs.core.http.HttpMessage;
import com.iwdnb.blqs.core.resolver.Types;
import com.iwdnb.blqs.core.schema.Group;
import com.iwdnb.blqs.core.schema.Tree;
import com.iwdnb.blqs.core.yapi.YapiUtils;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Swagger v2.1 json文件构建
 */
@Slf4j
public class SwaggerTreeHandler implements TreeHandler {

    @Override
    public void handle(Tree tree, Options options) {
        Swagger swagger = buildSwagger(tree);
        swagger.setDefinitions(buildDefinitions(Types.values(), options));
        Path file = options.getOutPath().resolve(options.getId() + "-swagger.json");
        String json = ObjectMappers.toPretty(swagger);
        write(file, json, Charsets.UTF_8);
        log.info("Build swagger {}", file);
        YapiUtils.yapiUpload(swagger, options.getYapiSynDataUrl(), options.getYapiToken(), options.getYapiBatch());
    }

    private Swagger buildSwagger(Tree tree) {
        Swagger swagger = new Swagger();
        Info info = new Info();
        info.setVersion(tree.getId());
        info.setName(tree.getName());
        info.setDescription(tree.getDescription());
        info.setTitle(tree.getName());
        swagger.setInfo(info);
        for (Group group : tree.getBucket().getGroups()) {
            swagger.getTags().add(new Tag(group.getName(), group.getDescription()));
            for (HttpMessage httpMessage : group.getNodes()) {
                Item item = buildItem(httpMessage);
                item.getTags().add(group.getName());
                Map<String, Item> map = new HashMap<>();
                map.put(item.getMethod().toString().toLowerCase(), item);
                swagger.getPaths().put(item.getUrl().getPath(), map);
            }
        }
        // for (Group group : tree.getBucket().getGroups()) {
        // Tag tag = new Tag(group.getName(), group.getDescription());
        // swagger.getTags().add(tag);
        // Folder folder = new Folder();
        // folder.getTags().add(tag);
        // for (HttpMessage httpMessage : group.getNodes()) {
        // folder.getItem().add(buildItem(httpMessage));
        // }
        // swagger.getItem().add(folder);
        // }

        return swagger;
    }

    private Item buildItem(HttpMessage httpMessage) {
        Item item = new Item();
        item.setSummary(httpMessage.getName());
        item.setDescription(httpMessage.getDescription());

        item.getUrl().setPath(httpMessage.getRequest().getUris().get(0));
        item.setMethod(Method.of(httpMessage.getRequest().getMethod()));
        httpMessage.getRequest().getHeaders().forEach((key, value) -> item.getHeader().add(new Header(key, value)));
        httpMessage.getRequest().getElements().forEach((key) -> item.getParameters().add(new Parameter(key)));
        if (Method.GET.equals(item.getMethod())) {
            for (Element element : httpMessage.getRequest().getElements()) {
                item.getUrl().getQuery().add(new Parameter(element));
            }
        } else
            if (HttpHeaders.ContentType.APPLICATION_JSON.equals(httpMessage.getRequest().getHeaders().getContentType())) {
                item.getConsumes().add("application/json");
                Element element = new Element("root", null, "root", null, "root");
                BodyParameter bodyParameter = new BodyParameter(element);
                bodyParameter.setIn("body");
                bodyParameter.setSchema(buildSchema(httpMessage.getRequest().getTypes()));
                item.getParameters().clear();
                item.getParameters().add(bodyParameter);
                item.setBody(null);
            } else
                if (HttpHeaders.ContentType.APPLICATION_X_WWW_FORM_URLENCODED.equals(httpMessage.getRequest().getHeaders().getContentType())) {
                    item.getBody().setMode(BodyMode.urlencoded);
                    for (Element element : httpMessage.getRequest().getElements()) {
                        item.getBody().getUrlencoded().add(new Parameter(element));
                    }
                } else
                    if (HttpHeaders.ContentType.MULTIPART_FORM_DATA.equals(httpMessage.getRequest().getHeaders().getContentType())) {
                        item.getBody().setMode(BodyMode.formdata);
                        for (Element element : httpMessage.getRequest().getElements()) {
                            item.getBody().getFormdata().add(new Parameter(element));
                        }
                    }
        // build response
        Response response = new Response();
        response.setDescription(httpMessage.getResponse().getStatus().toString());
        response.setSchema(buildSchema(httpMessage.getResponse().getTypes()));

        item.getResponses().put(httpMessage.getResponse().getStatus().code() + "", response);

        return item;
    }

    private Schema buildSchema(Types types) {
        Schema schema = new Schema();
        if (types == null) {
            return schema;
        }
        if (types.isPrimitive()) {
            schema.setType(types.getType());
        } else {
            schema.setRef(types.getTag());
        }
        return schema;
    }

    private Map<String, Model> buildDefinitions(Map<String, Types> typesMap, Options options) {
        Map<String, Model> map = new HashMap<>(typesMap.size());
        for (Map.Entry<String, Types> entry : typesMap.entrySet()) {
            if (needBuildModel(entry.getValue())) {
                map.put(entry.getKey(), mapProperties(entry.getValue(), options));
            }

        }
        return map;
    }

    private boolean needBuildModel(Types types) {
        String typeName = types.getName();
        if (SwaggerPropertyUtils.simpleType(typeName)) {
            return false;
        }
        if (StringUtils.isBlank(types.getType())) {
            return false;
        }
        if (types.getType().equalsIgnoreCase("array")) {
            return false;
        }
        return true;
    }

    private Model mapProperties(Types types, Options options) {
        ModelImpl model = new ModelImpl()
                                         // .description(types.getTag())
                                         // .discriminator(source.getDiscriminator())
                                         // .example(source.getExample())
                                         .name(types.getName());
        // .xml(mapXml(source.getXml()));
        Map<String, Property> modelProperties = mapProperties(types.getElements(), options);
        model.setProperties(modelProperties);

        model.setSimple(false);
        model.setType(ModelImpl.OBJECT);
        // model.setTitle(types.getName());
        if (isMapType(types)) {
            // TODO map映射
            // Optional<Class> clazz = typeOfValue(source);
            // if (clazz.isPresent()) {
            // model.additionalProperties(property(clazz.get().getSimpleName()));
            // } else {
            model.additionalProperties(new ObjectProperty());
            // }
        }
        return model;
    }

    private Map<String, Property> mapProperties(List<Element> elements, Options options) {
        Map<String, Property> propertyMap = new LinkedHashMap<>(elements.size());
        for (Element element : elements) {
            Property property;
            if ("array".equalsIgnoreCase(element.getType())) {
                property = new ArrayProperty();
                if (StringUtils.isNotBlank(element.getTag())) {
                    RefProperty items = new RefProperty();
                    items.set$ref(element.getTag());
                    ((ArrayProperty) property).setItems(items);
                } else {
                    ObjectProperty items = new ObjectProperty();
                    items.setType("object");
                    ((ArrayProperty) property).setItems(items);
                }

            } else
                if (StringUtils.isNotBlank(element.getTag()) && !SwaggerPropertyUtils.simpleType(element.getType())) {
                    property = new RefProperty();
                    ((RefProperty) property).set$ref(element.getTag());
                } else {
                    property = SwaggerPropertyUtils.property(element.getType());
                    if (property != null) {
                        property.setDescription(element.getComment());
                        // 设置默认mock参数
                        property = mockProperty(element.getName(), property, options);
                    } else {
                        property = new ObjectProperty();
                        ((ObjectProperty) property).setType("object");
                    }
                }
            propertyMap.put(element.getName(), property);
        }
        return propertyMap;
    }

    private Property mockProperty(String propertyName, Property property, Options options) {
        Map<String, String> yapiMockParam = options.getYapiMockParam();
        if (yapiMockParam.isEmpty()) {
            return property;
        }
        if (yapiMockParam.containsKey(propertyName)) {
            MockProperty mockProperty = new MockProperty(property);
            mockProperty.setMock(yapiMockParam.get(propertyName));
            mockProperty.setDefault(yapiMockParam.get(propertyName));
            return mockProperty;
        }
        return property;
    }

    private boolean isMapType(Types types) {
        if (types.isPrimitive()) {
            return false;
        }
        if (StringUtils.isBlank(types.getTag())) {
            return false;
        }
        try {
            return Map.class.isAssignableFrom(Class.forName(types.getTag()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
