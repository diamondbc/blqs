package com.iwdnb.blqs.core.resolver.ast;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tag {

    String              name;
    String              key;
    String              content;
    Map<String, String> inline = new HashMap<>();

}
