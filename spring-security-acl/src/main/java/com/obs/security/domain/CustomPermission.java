package com.obs.security.domain;

import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.model.Permission;

public class CustomPermission implements Permission {

    private int permissionId;
    private String permission;
    public CustomPermission(int permissionId, String permission) {
        this.permissionId = permissionId;
        this.permission = permission;
    }

    @Override
    public int getMask() {
        return permissionId;
    }

    @Override
    public String getPattern() {
        return permission;
    }
}
