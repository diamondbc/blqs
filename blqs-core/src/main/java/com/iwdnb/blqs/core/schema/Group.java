package com.iwdnb.blqs.core.schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwdnb.blqs.core.http.HttpMessage;
import com.iwdnb.blqs.core.resolver.ast.Comments;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

/**
 * 请求组，如一个folder，一个Controller
 */
@Setter
@Getter
public class Group extends Node {

    /**
     * 忽略本组的接口信息
     */
    boolean           ignore = false;
    boolean           rest;
    String            bucketName;

    List<HttpMessage> nodes  = Lists.newLinkedList();

    @JsonIgnore
    Bucket            parent;

    public List<HttpMessage> getNodes() {
        nodes.sort(COMPARATOR);
        return nodes;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public void accept(Comments comments) {
        super.accept(comments);
        bucketName = Comments.getBucketName(comments);
    }
}
