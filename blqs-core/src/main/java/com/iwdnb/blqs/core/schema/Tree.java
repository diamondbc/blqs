package com.iwdnb.blqs.core.schema;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tree extends Node {

    String              version;
    String              readme;
    /**
     * 默认的桶
     */
    Bucket              bucket;
    /**
     * 其他的桶
     */
    Map<String, Bucket> buckets    = Maps.newTreeMap(String::compareTo);
    List<Appendix>      appendices = new LinkedList<>();

    public List<Appendix> getAppendices() {
        appendices.sort(COMPARATOR);
        return appendices;
    }

    public Bucket getBucket(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return bucket;
        }
        if (!buckets.containsKey(name)) {
            buckets.put(name, new Bucket(name));
        }
        return buckets.get(name);
    }

}
