package com.dce.checkstyle.bean.errorcode;

import java.util.List;

public class Package {

    private String name;

    private List<Clazz> clazzes;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Clazz> getClazzes() {
        return this.clazzes;
    }

    public void setClazzes(List<Clazz> clazzes) {
        this.clazzes = clazzes;
    }
}
