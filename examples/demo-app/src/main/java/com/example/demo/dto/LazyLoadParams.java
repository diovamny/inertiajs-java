package com.example.demo.dto;

import java.util.Map;

public class LazyLoadParams {

    public int page;
    public int size = 25;
    public String sortField;
    public int sortOrder = 1;
    public Map<String, FilterConstraint> filters;

    public int getOffset() {
        return page * size;
    }

    public static class FilterConstraint {
        public Object value;
        public String matchMode;
        public Object constraints;
    }
}
