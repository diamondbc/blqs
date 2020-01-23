package com.iwdnb.blqs.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iwdnb.blqs.core.common.Element;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Types {

    /**
     * 已解析类型结果池，防止循环递归
     */
    private static Map<String, Types> POOL = new ConcurrentHashMap<>();

    public Types(){
    }

    public static Map<String, Types> values() {
        return POOL;
    }

    public static boolean contain(String obj) {
        return POOL.containsKey(obj);
    }

    public static Types get(String obj) {
        if (StringUtils.isBlank(obj)) {
            return null;
        }
        if (POOL.containsKey(obj)) {
            return POOL.get(obj);
        }
        return new Types();
    }

    public static void put(String obj, Types types) {
        POOL.put(obj, types);
    }

    /**
     * 获取解析结果前，应判断是否已解析
     */
    @JsonIgnore
    boolean       resolved;
    /**
     * 是否基本类型
     */
    @JsonIgnore
    boolean       primitive;
    @JsonIgnore
    String        tag;
    String        type;
    @JsonProperty("title")
    String        name;
    @JsonIgnore
    Object        value;
    @JsonIgnore
    List<Element> elements = new ArrayList<>();

    public Types duplicate() {
        Types types = new Types();
        types.name = this.name;
        types.type = this.type;
        types.tag = this.tag;
        types.resolved = this.resolved;
        types.primitive = this.primitive;
        types.value = this.value;
        for (Element element : this.elements) {
            types.elements.add(element.duplicate());
        }
        return types;
    }

    public void prefix(String prefix) {
        for (Element element : elements) {
            element.setName(prefix + element.getName());
        }
    }

    @Override
    public String toString() {
        return "Types{" + "resolved=" + resolved + ", primitive=" + primitive + ", tag='" + tag + '\'' + ", type='"
               + type + '\'' + ", name='" + name + '\'' + ", value=" + value + ", elements=" + elements + '}';
    }
}
