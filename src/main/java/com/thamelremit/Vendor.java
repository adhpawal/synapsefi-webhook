package com.thamelremit;


public enum Vendor {
    THAMELREMIT("TR"), MUNCHAMONEY("MC");

    String value;

    Vendor(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
 }
