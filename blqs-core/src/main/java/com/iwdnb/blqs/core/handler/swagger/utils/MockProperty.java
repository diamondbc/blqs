package com.iwdnb.blqs.core.handler.swagger.utils;

import java.util.HashMap;
import java.util.Map;

import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.Property;

public class MockProperty extends AbstractProperty implements Property {

    private Map<String, String> mock = new HashMap<>();
    protected String            _default;

    public MockProperty(Property property){
        this.name = property.getName();
        this.type = property.getType();
        this.description = property.getDescription();
        this.format = property.getFormat();
        this.title = property.getTitle();
        this.required = property.getRequired();
        this.readOnly = property.getReadOnly();
        this.example = property.getExample();
    }

    public void setMock(String mockStr) {
        mock.put("mock", mockStr);
    }

    public Map<String, String> getMock() {
        return mock;
    }

    public void setMock(Map<String, String> mock) {
        this.mock = mock;
    }

    public String getDefault() {
        return _default;
    }

    @Override
    public void setDefault(String _default) {
        this._default = _default;
    }
}
