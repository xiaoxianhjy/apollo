package com.ctrip.framework.apollo.portal.entity.vo;

public class ItemInfo {

    private String appName;
    private String envName;
    private String clusterName;
    private String namespaceName;
    private String status;
    private String key;
    private String value;

    // Default constructor (no-args constructor)
    public ItemInfo() {
    }

    // Constructor with parameters
    public ItemInfo(String appName, String envName, String clusterName, String namespaceName, String status, String key, String value) {
        this.appName = appName;
        this.envName = envName;
        this.clusterName = clusterName;
        this.namespaceName = namespaceName;
        this.status = status;
        this.key = key;
        this.value = value;
    }

    // Getters and Setters
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // toString() method
    @Override
    public String toString() {
        return "ItemInfo{" +
                "appName='" + appName + '\'' +
                ", envName='" + envName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", namespaceName='" + namespaceName + '\'' +
                ", status='" + status + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
