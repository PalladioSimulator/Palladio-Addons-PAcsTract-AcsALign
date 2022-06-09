package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


public class RoleModelPermissionList {

	String role;
	String[] permissions;
	ArrayList<String> inheritFrom;
	
	public ArrayList<String> getInheritFrom() {
		return inheritFrom;
	}

	public void setInheritFrom(ArrayList<String> inheritFrom) {
		this.inheritFrom = inheritFrom;
	}

	public RoleModelPermissionList(String role, String[] permissions) {
		super();
		this.role = role;
		this.permissions = permissions;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String[] getPermissions() {
		return permissions;
	}
	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}
	
	public String print() {
		return "Rolle: " + getRole() + " ### Berechtigungen: " + Arrays.toString(getPermissions()) + " ### Erbt von: " +  Arrays.asList(getInheritFrom()).stream()
                .map(Object::toString)
                .collect(Collectors.joining("\t"));
	}

	
}
