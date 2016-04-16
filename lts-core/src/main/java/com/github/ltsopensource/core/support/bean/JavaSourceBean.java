package com.github.ltsopensource.core.support.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class JavaSourceBean {

    private String packageName;

    private Set<String> importNameList = new HashSet<String>();

    private String classDefinition;

    private Set<String> methodCodeList = new HashSet<String>();

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClassDefinition(String classDefinition) {
        this.classDefinition = classDefinition;
    }

    public void addImport(String importName){
        importNameList.add(importName);
    }

    public void addMethod(String methodCode){
        methodCodeList.add(methodCode);
    }

    public String toString(){
        StringBuilder code = new StringBuilder();
        code.append("package ").append(packageName).append(";\n");

        for (String importName : importNameList) {
            code.append("import ").append(importName).append(";\n");
        }

        code.append(classDefinition).append(" {\n");

        for (String methodCode : methodCodeList) {
            code.append(methodCode);
        }

        code.append("}");

        return code.toString();
    }
}
