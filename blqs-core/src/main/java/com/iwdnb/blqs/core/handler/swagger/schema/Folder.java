package com.iwdnb.blqs.core.handler.swagger.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Folder {

    List<Tag>  tags = new ArrayList<>();
    List<Item> item = new ArrayList<>();

}
