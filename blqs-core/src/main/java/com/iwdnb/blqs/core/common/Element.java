package com.iwdnb.blqs.core.common;

/**
 * 多个数据的组合
 */
public class Element {

    protected String name;
    protected String type;
    protected String title;
    protected String value;
    protected String comment;
    protected String tag;

    public Element(){
    }

    public Element(String name, String type, String title, String value, String comment){
        this.value = value;
        this.name = name;
        this.type = type;
        this.title = title;
        this.comment = comment;
    }

    public Element duplicate() {
        Element element = new Element();
        element.setTag(tag);
        element.setName(name);
        element.setType(type);
        element.setTitle(title);
        element.setValue(value);
        element.setComment(comment);
        return element;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
