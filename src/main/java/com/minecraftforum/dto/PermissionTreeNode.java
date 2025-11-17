package com.minecraftforum.dto;

import com.minecraftforum.entity.Permission;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionTreeNode {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String description;
    private String router;
    private String apiurl;
    private String methodtype;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private List<PermissionTreeNode> children;
    
    public PermissionTreeNode() {
        this.children = new ArrayList<>();
    }
    
    public PermissionTreeNode(Permission permission) {
        this();
        this.id = permission.getId();
        this.code = permission.getCode();
        this.name = permission.getName();
        this.type = permission.getType();
        this.description = permission.getDescription();
        this.router = permission.getRouter();
        this.apiurl = permission.getApiurl();
        this.methodtype = permission.getMethodtype();
        this.parentId = permission.getParentId();
        this.sortOrder = permission.getSortOrder();
        this.status = permission.getStatus();
    }
}

