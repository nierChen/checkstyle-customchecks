package com.dce.checkstyle.bean.errorcode;

import java.util.List;

public class Module {

    private String name;

    private List<Package> packages;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Package> getPackages() {
        return this.packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }
}
