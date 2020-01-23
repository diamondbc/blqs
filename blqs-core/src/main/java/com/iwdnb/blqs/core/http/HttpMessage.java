package com.iwdnb.blqs.core.http;

import com.iwdnb.blqs.core.resolver.ast.Comments;
import com.iwdnb.blqs.core.schema.Group;
import com.iwdnb.blqs.core.schema.Node;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An class that defines a HTTP message, providing common properties and method
 */
@Getter
@Setter
@Slf4j
public class HttpMessage extends Node {

    HttpVersion  version  = HttpVersion.DEFAULT;
    HttpRequest  request  = new HttpRequest();
    HttpResponse response = new HttpResponse();

    @Ignore
    Group        parent;

    @Override
    public void accept(Comments comments) {
        super.accept(comments);

        // //解析@return标签
        // for (Tag tag : comments.getTags()) {
        // if ("return".equals(tag.getName()) && !Strings.isNullOrEmpty(tag.getContent())) {
        // Types types = TypeResolvers.tryParse(tag.getContent());
        // if(types.isResolved()){
        // response.setBody(types.getValue());
        // response.setTypes(types);
        // }
        //
        // }
        // }

    }

}
