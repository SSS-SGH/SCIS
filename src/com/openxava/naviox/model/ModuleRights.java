package com.openxava.naviox.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.controller.meta.*;
import org.openxava.jpa.*;
import org.openxava.model.meta.*;
import org.openxava.util.*;

/**
 * 
 * @author Javier Paniza
 */

@Entity
@Table(name="OXROLES_OXMODULES")
@IdClass(ModuleRightsKey.class)
@View(members="module, notInMenu; excludedActions; excludedMembers; readOnlyMembers") 
public class ModuleRights {
	
	public static int countForApplication(String application) {
 		Query query = XPersistence.getManager().createQuery(
 			"select count(*) from ModuleRights r where r.module.application = :application");
 		query.setParameter("application", application);
 		return ((Number) query.getSingleResult()).intValue();  		 		
	}

	@Id @ManyToOne
	@JoinColumn(name="roles_name")
	private Role role;
	
	@Id @ManyToOne 
	@JoinColumns({
		@JoinColumn(name="modules_application", referencedColumnName="application"), 
		@JoinColumn(name="modules_name", referencedColumnName="name")
	})
	@ReferenceView("OnlyName") @NoFrame
	private Module module;
	
	@Column(length=1000)
	private String excludedActions;
	
	@Column(length=1000)
	private String excludedMembers; 
	
	@Column(length=1000)
	private String readOnlyMembers; 
	
	@org.hibernate.annotations.Type(type="org.hibernate.type.YesNoType")
	@Column(columnDefinition="varchar(1) default 'N' not null") 
	private boolean notInMenu; 
	
	@PostPersist @PostUpdate @PostRemove 
	private void resetCache() { 
		User.resetCache();
	}

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

	public String getExcludedActions() {
		return excludedActions;
	}

	public void setExcludedActions(String excludedActions) {
		this.excludedActions = excludedActions;
	}

	public Collection<MetaAction> getExcludedMetaActions() { 
		if (Is.emptyString(excludedActions)) return Collections.<MetaAction>emptyList();
		Collection<MetaAction> result = new ArrayList<MetaAction>();
		for (String action: excludedActions.split(",")) {
			if (action.contains(":")) continue;
			result.add(MetaControllers.getMetaAction(action));
		}
		return result;
	}	
	
	public Collection<String> getExcludedCollectionActions() {
		if (Is.emptyString(excludedActions)) return Collections.<String>emptyList();
		Collection<String> result = new ArrayList<String>();
		for (String action: excludedActions.split(",")) {
			if (!action.contains(":")) continue;
			result.add(action);
		}
		return result;
	}
	
	
	public Collection<MetaMember> getExcludedMetaMembers() {
		return toMetaMembers(excludedMembers);
	}
	
	public Collection<MetaMember> getReadOnlyMetaMembers() {
		return toMetaMembers(readOnlyMembers);
	}

	private Collection<MetaMember> toMetaMembers(String members) { 
		if (Is.emptyString(members)) return Collections.<MetaMember>emptyList();
		Collection<MetaMember> result = new ArrayList<MetaMember>();
		MetaModel metaModel = null;
		for (String member: members.split(",")) {
			if (metaModel == null) {
				metaModel = MetaModel.get(member);
			} else {
				try {
					MetaMember metaMember = metaModel.getMetaMember(member);
					if (member.contains(".") && metaMember instanceof MetaProperty) {
						metaMember = ((MetaProperty) metaMember).cloneMetaProperty();
						metaMember.setName(member);
					}
					result.add(metaMember);
				} catch(ElementNotFoundException ex) {} // In the Role administration module, 
														// a warning is printed in log				
			}
		}
		return result;
	}
	
	public String getExcludedMembers() {
		return excludedMembers;
	}

	public void setExcludedMembers(String excludedMembers) {
		this.excludedMembers = excludedMembers;
	}

	public String getReadOnlyMembers() {
		return readOnlyMembers;
	}

	public void setReadOnlyMembers(String readOnlyMembers) {
		this.readOnlyMembers = readOnlyMembers;
	}

	public boolean isNotInMenu() {
		return notInMenu;
	}

	public void setNotInMenu(boolean notInMenu) {
		this.notInMenu = notInMenu;
	}

}
