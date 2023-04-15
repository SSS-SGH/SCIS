package com.openxava.naviox.model;

import javax.persistence.*;

/**
 * 
 * @author Javier Paniza
 */

public class ModuleRightsKey implements java.io.Serializable {
	
	
	@Id @ManyToOne
	@JoinColumn(name="roles_name")
	private Role role;
	
	@Id @ManyToOne
	@JoinColumns({
		@JoinColumn(name="modules_application", referencedColumnName="application"), 
		@JoinColumn(name="modules_name", referencedColumnName="name")
	})	
	private Module module;

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleRightsKey other = (ModuleRightsKey) obj;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}
		
}
