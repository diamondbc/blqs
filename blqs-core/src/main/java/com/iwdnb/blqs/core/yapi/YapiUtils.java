package com.iwdnb.blqs.core.yapi;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.iwdnb.blqs.core.common.ObjectMappers;
import com.iwdnb.blqs.core.handler.swagger.schema.*;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YapiUtils {

    public static void yapiUpload(Swagger swagger, String synDataUrl, String token, int yapiBatch) {
        try {
            if (StringUtils.isAnyBlank(synDataUrl, token)) {
                return;
            }
            int size = swagger.getPaths().size();
            log.info("YapiUtils.sysData,token:{} count:{}", token, size);
            Map<String, Map<String, Item>> paths = swagger.getPaths();
            Map<String, Model> definitions = swagger.getDefinitions();
            int count = 0;
            Map<String, Map<String, Item>> tempPath = new HashMap<>();
            for (Map.Entry<String, Map<String, Item>> entry : paths.entrySet()) {
                tempPath.put(entry.getKey(), entry.getValue());
                count++;
                if (count % yapiBatch == 0) {
                    swagger.setPaths(tempPath);
                    swagger.setDefinitions(getPathDefinations(tempPath, definitions));
                    synData(swagger, synDataUrl, token);
                    tempPath = new HashMap<>();
                }
            }
            if (!tempPath.isEmpty()) {
                swagger.setPaths(tempPath);
                swagger.setDefinitions(getPathDefinations(tempPath, definitions));
                synData(swagger, synDataUrl, token);
            }
        } catch (Exception e) {
            log.warn("yapiUpload error:" + e.getLocalizedMessage());
        }
    }

    private static Map<String, Model> getPathDefinations(Map<String, Map<String, Item>> paths,
                                                         Map<String, Model> definations) {
        Set<String> definationkeys = new HashSet<>();
        for (Map.Entry<String, Map<String, Item>> entry : paths.entrySet()) {
            Map<String, Item> itemMap = entry.getValue();
            for (Map.Entry<String, Item> itemEntry : itemMap.entrySet()) {
                Item item = itemEntry.getValue();
                // 请求参数解析
                List<Parameter> parameters = item.getParameters();
                for (Parameter parameter : parameters) {
                    if (parameter instanceof BodyParameter) {
                        BodyParameter bodyParameter = (BodyParameter) parameter;
                        Schema schema = bodyParameter.getSchema();
                        buildDependencyDefinations(schema, definationkeys, definations);
                    }
                }
                // 响应参数解析
                Map<String, Response> responses = item.getResponses();
                for (Map.Entry<String, Response> responseEntry : responses.entrySet()) {
                    Response response = responseEntry.getValue();
                    Schema schema = response.getSchema();
                    buildDependencyDefinations(schema, definationkeys, definations);
                }
            }
        }
        Map<String, Model> tempMap = new HashMap<>();
        for (String key : definationkeys) {
            tempMap.put(key, definations.get(key));
        }
        return tempMap;
    }

    /**
     * 获取本次依赖的对象定义
     * 
     * @param schema
     * @param definationkeys
     * @param definations
     */
    private static void buildDependencyDefinations(Schema schema, Set<String> definationkeys,
                                                   Map<String, Model> definations) {
        if (schema != null && StringUtils.isNotBlank(schema.getRef())) {
            String key = schema.getRef().substring(SwaggerConstants.DEFINITION.length());
            if (!definationkeys.contains(key)) {
                recursePath(key, definations, definationkeys);
            }
        }
    }

    private static void recursePath(String definationkey, Map<String, Model> definations, Set<String> definationkeys) {
        definationkeys.add(definationkey);
        Model model = definations.get(definationkey);
        if (model == null) {
            return;
        }
        Map<String, Property> propertyMap = model.getProperties();
        if (propertyMap == null || propertyMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Property> propertyEntry : propertyMap.entrySet()) {
            Property property = propertyEntry.getValue();
            if (property instanceof RefProperty) {
                String key = ((RefProperty) property).get$ref();
                key = key.substring(SwaggerConstants.DEFINITION.length());
                if (definationkeys.contains(key)) {
                    continue;
                } else {
                    recursePath(key, definations, definationkeys);
                }
            } else if (property instanceof ArrayProperty) {
                Property tempProperty = ((ArrayProperty) property).getItems();
                if (tempProperty instanceof RefProperty) {
                    String key = ((RefProperty) tempProperty).get$ref();
                    key = key.substring(SwaggerConstants.DEFINITION.length());
                    if (definationkeys.contains(key)) {
                        continue;
                    } else {
                        recursePath(key, definations, definationkeys);
                    }
                }
            }
        }

    }

    public static void synData(Swagger swagger, String synDataUrl, String token) {
        String json = ObjectMappers.toString(swagger);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("type", "swagger");
        map.add("merge", "good");
        map.add("token", token);
        map.add("json", json);
        RestTemplate restTemplate = RestTemplateConfig.restTemplate();
        String responseEntity = restTemplate.postForEntity(synDataUrl, map, String.class).getBody();
        if (responseEntity.contains("413")) {
            log.info("synData413:" + json.length());
        }
        log.info("YapiUtils.synData,result:" + responseEntity);
    }

}
