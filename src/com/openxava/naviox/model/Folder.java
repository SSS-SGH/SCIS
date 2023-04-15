package com.openxava.naviox.model;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.logging.*;
import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

/**
 * 
 * @author Javier Paniza
 */

@Entity
@Table(name="OXFOLDERS", indexes={
	@Index(columnList="name"), 
})
@View(members="name, parent, icon; subfolders; modules") 
@Tab(defaultOrder="${name} asc") 
public class Folder implements java.io.Serializable {
	
	private static Log log = LogFactory.getLog(Folder.class); 
	
	@Transient
	private boolean creatingROOT = false; 
	
	@Id @Hidden 
	@Column(length=32)
	private String id; 
		
	@Column(length=25) @Required
	private String name; 
	
	@Column(length=40) 
	@Stereotype("ICON") 
	private String icon; 
	
	@ManyToOne 
	@DescriptionsList
	private Folder parent;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="parent")
	@OrderColumn(name="orderInFolder") 
	@SaveAction("Folder.saveSubfolder") 
	private List<Folder> subfolders; 
	
	@OneToMany(mappedBy="folder")
	@OrderColumn(name="orderInFolder")  
	private List<Module> modules; 
	
	@Hidden
	private Integer orderInFolder = 0; 
	
	@Hidden
	public String getLabel() {
		String id = Strings.naturalLabelToIdentifier(getName());
		if (Labels.existsExact(id, Locales.getCurrent())) return Labels.get(id);
		return getName();
	}
	
	@Hidden
	public boolean isRoot() { 
		return "ROOT".equals(name);
	}
	
	@PreCreate
	public void preCreate() { 
		if (isRoot()) id = "ROOT"; // ROOT id for ROOT to avoid duplicate ROOT
		else if (Is.emptyString(id)) {
			id = UUID.randomUUID().toString().replace("-", ""); 
		}
		if (creatingROOT) return;
		if (isRoot()) {
			throw new ValidationException("root_folder_already_exists"); 
		}
	}
	
	@PostLoad
	private void resetCreatingROOT() { 
		creatingROOT = false;
	}
	
	public static Folder find(String oid) {
		return XPersistence.getManager().find(Folder.class, oid);
	}
	
	public static Folder findByName(String name) { 
 		Query query = XPersistence.getManager().createQuery("from Folder f where f.name = :name");
 		query.setParameter("name", name);
 		List<Folder> folders = query.getResultList();
 		if (folders.isEmpty()) throw new NoResultException();
 		int count = folders.size();
 		if (count > 1) {
 			log.warn(XavaResources.getString("non_unique_folder_name", name, count));
 		}
 		return folders.get(0);
	}
	
	public static Collection<Folder> findAll() { 
		Query query = XPersistence.getManager().createQuery("from Folder f"); 
	 	return query.getResultList();  		 		
	}
	
	public static Folder getROOT() { 
		try {
			return findByName("ROOT");
		}
		catch (NoResultException ex) {
			return null;
		}		
	}
	
	public static List<Folder> findByParent(Folder parent) {
		if (parent == null || parent.isRoot()) {
			String condition = "(f.parent is null and not f = :parent) or f.parent = :parent";
			try {
				if (parent == null) parent = findByName("ROOT");
			}
			catch (NoResultException ex) {
				condition = "f.parent is null";
			}
			Query query = XPersistence.getManager().createQuery(
				"from Folder f where " + condition + " order by f.orderInFolder"); 
		 	if (parent != null) query.setParameter("parent", parent);
		 	return query.getResultList();				

		}
		else {
			return parent.getSubfolders();
		}
	}
	
	public static void updateROOT() {
		Folder root = findByName("ROOT");
		root.setModules(Module.findInRoot());
		root.setSubfolders(findByParent(null));
	}
	
	public static void createROOT() { 
		Folder root = new Folder(); 
		root.setName("ROOT");
		root.creatingROOT = true;
		XPersistence.getManager().persist(root);
	}
		
	@PreRemove
	private void annulModulesReferences() {
		// Because some database does not annul by default
		if (modules == null) return;
		for (Module m: modules) m.setFolder(null);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (isRoot()) {
			if (!name.equals("ROOT")) {
				throw new ValidationException("cannot_change_root_folder_name"); 
			}
		}
		this.name = name;
	}

	public Folder getParent() {
		return parent;
	}

	public void setParent(Folder parent) {
		if (areEqual(this.parent, parent)) return; 
		this.parent = parent;
		if (parent != null && parent.subfolders != null) {
			parent.getSubfolders().add(this);
		}
	}
	
	static boolean areEqual(Folder a, Folder b) {  
		if (a == b) return true;
		if (a == null || b == null) return false;
		return Is.equal(a.getId(), b.getId());
	}


	public List<Folder> getSubfolders() {
		removeNulls(subfolders); 
		return subfolders;
	}

	public void setSubfolders(List<Folder> subfolders) { 
		this.subfolders = subfolders;
		if (this.subfolders != null) { 
			this.subfolders.stream().filter(s -> !s.isRoot()).forEach(s -> s.parent = this); 
		}
	}

	public List<Module> getModules() {
		removeNulls(modules); 
		return modules;
	}
	
	private void removeNulls(Collection collection) {
		if (collection == null) return; 
		for (Iterator it = collection.iterator(); it.hasNext();) {
			Object element = it.next();
			if (element == null) it.remove();
		}
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
		if (this.modules != null) { 
			this.modules.stream().forEach(m -> m.folder = this); 
		}
	}

	public Integer getOrderInFolder() {
		return orderInFolder;
	}

	public void setOrderInFolder(Integer orderInFolder) {
		this.orderInFolder = orderInFolder;
	}

	public String getIcon() {
		return Is.emptyString(icon)?"folder":icon; 
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
