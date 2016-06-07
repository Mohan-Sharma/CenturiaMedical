package com.nzion.zkoss.dto;

/**
 * Created by Mohan Sharma on 9/26/2015.
 */
public class RoleDto {
    private String roleName;
    private Long roleValue;
    public RoleDto(String roleName, Long roleValue){
        this.roleName = roleName;
        this.roleValue = roleValue;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(Long roleValue) {
        this.roleValue = roleValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleDto)) return false;

        RoleDto role = (RoleDto) o;

        if (roleName != null ? !roleName.equals(role.roleName) : role.roleName != null) return false;
        if (roleValue != null ? !roleValue.equals(role.roleValue) : role.roleValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roleName != null ? roleName.hashCode() : 0;
        result = 31 * result + (roleValue != null ? roleValue.hashCode() : 0);
        return result;
    }
}
