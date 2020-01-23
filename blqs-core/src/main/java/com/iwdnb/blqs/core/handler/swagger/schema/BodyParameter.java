package com.iwdnb.blqs.core.handler.swagger.schema;

import com.iwdnb.blqs.core.common.Element;

import lombok.Data;

/**
 * body格式参数
 */
@Data
public class BodyParameter extends Parameter {

    private Schema schema;

    public BodyParameter(Element element){
        super(element);
    }
}
