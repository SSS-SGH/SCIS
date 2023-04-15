package com.openxava.naviox.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.TrueCalculator;
import org.openxava.filters.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

/**
 * 
 * @since 5.2
 * @author Javier Paniza
 */

@Entity
@Table(name="OXORGANIZATIONS") 
@View(members="name, url; active; theme") 
@Tabs({	 
	@Tab(properties="name, active, theme"), 
	@Tab(name="OfCurrentUser", filter=UserFilter.class, 
		properties="name", editors="Cards", 
		baseCondition="from Organization e, in (e.users) u where u.name = ?") 
})
public class Organization implements java.io.Serializable {
	
	private static final long serialVersionUID = -5904310527593026919L;

	private static Map<String, String> names; 
	private static Set<String> deactivatedIds;
	private static Map<String, String> themes; 
	
	@Id @Hidden @Column(length=50) 
	private String id;
	
	@Column(length=50) @Required
	private String name;
	
	@org.hibernate.annotations.Type(type="org.hibernate.type.YesNoType")
	@DefaultValueCalculator(TrueCalculator.class) 	
	@Column(columnDefinition="varchar(1) default 'Y' not null") 
	private boolean active; 
	
	@Column(length=30)
	private String theme;
	
	@ManyToMany(mappedBy="organizations")
	@ReadOnly
	private Collection<User> users; 

	
	/** @since 5.6 */
	public static Organization find(String id) { 
		return XPersistence.getManager().find(Organization.class, id);
	}
	
	/** @since 5.6.1 */
	public static boolean existsWithName(String name) { 
		return XPersistence.getManager().find(Organization.class, normalize(name)) != null;
	}
	
	@LabelFormat(LabelFormatType.NO_LABEL)
	public String getUrl() {  
		return "/o/" + getId();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String normalize(String name) {
		return Strings.naturalLabelToIdentifier(name); 
	}
	
	public static String getName(String id) {
		if (id == null) return null;
		return names.get(id);
	}
	
	/** @since 6.4 */
	public static String getTheme(String id) { 
		if (id == null) return null;
		if (themes == null) return null;
		return themes.get(id);
	}
	
	/** @since 6.2 */
	public static boolean exists(String id) { 
		if (names == null) setUp();
		return names.containsKey(id);
	}
	
	/** @since 6.2 */
	public static boolean isActive(String id) { 
		if (names == null) setUp();
		return deactivatedIds == null || !deactivatedIds.contains(id);
	}	
		
	/**
	 * 
	 * @since 5.3.2
	 */
	public static Collection<String> getAllIds() { 
		if (names == null) setUp();
		return names.keySet();
	}
	
	@PrePersist
	private void normalizeId() {
		this.id = normalize(name);
	}
	
	public static void resetCache() {
		names = null;
		deactivatedIds = null;
		themes = null; 
	}

	public static void setUp() { 
		if (names != null) return;
		names = new HashMap<String, String>();
		for (Organization o: findAll()) {
			names.put(o.getId(), o.getName());
			if (!o.isActive()) {
				if (deactivatedIds == null) deactivatedIds = new HashSet<>();
				deactivatedIds.add(o.getId());
			}
			if (!Is.emptyString(o.getTheme())) {
				if (themes == null) themes = new HashMap<>();
				themes.put(o.getId(), o.getTheme());
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	private static Collection<Organization> findAll() {
		return XPersistence.getManager().createQuery("from Organization").getResultList();
	}

	public static int count() {
		if (names == null) setUp();
		return names.size();
	}

	public Collection<User> getUsers() {
		return users;
	}

	public void setUsers(Collection<User> users) {
		this.users = users;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Organization)) return false;
		return getId().equals(((Organization) obj).getId());
	}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	/** @since 6.2 */
	public boolean isActive() {
		return active;
	}

	/** @since 6.2 */
	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

}
