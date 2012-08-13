package com.proofpoint.discovery.monitor;

public enum DiscoveryEventType
{
    SERVICEQUERY("v1/service", "GET"),
    STATICANNOUNCEMENT("v1/announcement/static", "POST"),
    STATICANNOUNCEMENTLIST("v1/announcement/static", "GET"),
    STATICANNOUNCEMENTDELETE("v1/announcement/static", "DELETE"),
    DYNAMICANNOUNCEMENT("v1/announcement", "PUT"),
    DYNAMICANNOUNCEMENTDELETE("v1/announcement", "DELETE");
    
    private final String path;
    private final String method;
    
    DiscoveryEventType(String path, String method)
    {
        this.path = path;
        this.method = method;
    }

    public String getPath()
    {
        return path;
    }

    public String getMethod()
    {
        return method;
    }

    public static DiscoveryEventType get(String path, String method)
    {
        for (DiscoveryEventType type : values()) {
            if (path.startsWith(type.getPath()) && type.getMethod().equals(method)) {
                return type;
            }
        }

        return null;
    }
}
