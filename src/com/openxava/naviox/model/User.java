package com.openxava.naviox.model;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javax.naming.*;
import javax.naming.directory.*;
import javax.persistence.*;

import org.apache.commons.collections.*;
import org.apache.commons.lang3.*;
import org.apache.commons.logging.*;
import org.openxava.annotations.*;
import org.openxava.application.meta.*;
import org.openxava.calculators.*;
import org.openxava.controller.meta.*;
import org.openxava.jpa.*;
import org.openxava.model.meta.*;
import org.openxava.util.*;

/**
 * 
 * @author Javier Paniza 
 */

@Entity
@Table(name="OXUSERS", indexes={
	@Index(columnList="email"),
	@Index(columnList="passwordRecoveringCode")
})
@View(members=
	"#name, active;" +
	"password, repeatPassword;" + 
	"creationDate, lastLoginDate;" +
	"forceChangePassword, authenticateWithLDAP;" +
	"allowedIP;" + 
	"personalData [" +
	"	email;" +
	"	givenName;" +
	"	familyName;" +
	"	jobTitle;" +
	"	middleName;" +
	"	nickName;" +
	"	birthDate;" +
	"];" + 
	"roles;" + 
	"modules;"  +
	"sessionsRecord"
)
@View(name="PersonalData", members=
	"email;" +
	"givenName;" +
	"familyName;" +
	"jobTitle;" +
	"middleName;" +
	"nickName;" +
	"birthDate;" 
)
@View(name="ForOrganizationWithSharedUsers", members=
	"name, active;" +
	"roles;" + 
	"modules;"  
)
public class User implements java.io.Serializable {
	
	private static final long serialVersionUID = 2355223287420733687L;
	
	private final static String PROPERTIES_FILE = "naviox.properties";
	private static Log log = LogFactory.getLog(User.class);
	private static Properties properties;
	private static Map<String, Boolean> actionsByModules; 

	@Transient
	private Map<MetaModule, Collection<MetaMember>> excludedMetaMembersForMetaModules; 

	@Transient
	private Map<MetaModule, Collection<MetaMember>> readOnlyMetaMembersForMetaModules; 
	
	@Transient
	private Map<MetaModule, Collection<String>> excludedCollectionActionsForMetaModules; 
	
	@Transient
	private Map<MetaModule, Collection<MetaAction>> excludedMetaActionsForMetaModules; 
	
	
	public static User find(String name) {
		User user = XPersistence.getManager().find(User.class, name);
		if (user != null) return user;
		if (Configuration.getInstance().isUseEmailAsUserName()) {
			if (name.contains("@")) {
				return findByEmail(name);
			}
		}
		if (!Configuration.getInstance().isCaseSensitiveUserName()) {
			Query query = XPersistence.getManager()
				.createQuery("from User u where lower(u.name) = :name");
			query.setParameter("name", name.toLowerCase());
			List<User> users = query.getResultList();
			if (users.size() == 1) {
				return users.get(0);
			}
		}
		return null;
	}
	
	public static User findByEmail(String email) { 
		try {
			Query query = XPersistence.getManager().createQuery("from User f where lower(f.email) = :email"); 
			query.setParameter("email", email.toLowerCase()); 
	 		return (User) query.getSingleResult();
		}
		catch (NoResultException ex) {
			return null;
		}
	}
	
	public static User findByPasswordRecoveringCode(String passwordRecoveringCode) { 
		try {
	 		Query query = XPersistence.getManager().createQuery(
	 			"from User f where f.passwordRecoveringCode = :passwordRecoveringCode");
	 		query.setParameter("passwordRecoveringCode", passwordRecoveringCode);
	 		return (User) query.getSingleResult();
		}
		catch (NoResultException ex) {
			return null;
		}
	}

	public static int count() {
 		Query query = XPersistence.getManager().createQuery(
 			"select count(*) from User");
 		return ((Number) query.getSingleResult()).intValue();  		 		
	}
	
	static void resetCache() {
		actionsByModules = null;
	}
	
	@Id @Column(length=30) 
	private String name;

	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")	
	private String password;

	@Column(length=41)  
	private String recentPassword1;
	@Column(length=41)  
	private String recentPassword2;
	@Column(length=41)  
	private String recentPassword3;
	@Column(length=41)  
	private String recentPassword4;
	
	@ReadOnly
	private Date creationDate; 
	
	@ReadOnly
	private Date lastLoginDate;   
	
	@Hidden
	private Date lastPasswordChangeDate;  

	@Transient
	@Column(length=41) @DisplaySize(30) 
	@Stereotype("PASSWORD")		
	private String repeatPassword; 
	
	@org.hibernate.annotations.Type(type="org.hibernate.type.YesNoType")
	@DefaultValueCalculator(TrueCalculator.class) 
	@Column(columnDefinition="varchar(1) default 'Y' not null") 
	private boolean active = true;

	@org.hibernate.annotations.Type(type="org.hibernate.type.YesNoType")
	@Column(columnDefinition="varchar(1) default 'N' not null") 
	private boolean forceChangePassword; 
	
	@org.hibernate.annotations.Type(type="org.hibernate.type.YesNoType")
	@Column(columnDefinition="varchar(1) default 'N' not null") 
	private boolean authenticateWithLDAP; 

	@Column(length=60) @Stereotype("EMAIL") 
	private String email;
	
	@Column(length=30)
	private String givenName;
		
	@Column(length=30)
	private String familyName;
	
	@Column(length=30)
	private String jobTitle;
	
	@Column(length=30)
	private String middleName;
	
	@Column(length=30)
	private String nickName;
	
	private Date birthDate;
	
	private int failedLoginAttempts;
	
	@Column(length=32)
	private String passwordRecoveringCode; 
	
	private Date passwordRecoveringDate; 
	
	private Date privacyPolicyAcceptanceDate; 
	
	@Column(length=15) @Stereotype("IP")
	private String allowedIP;  	
	
	@ManyToMany
	@ReadOnly 
	@JoinTable( // Though we use default names because a JPA bug with IN clause in queries
		name="OXUSERS_OXORGANIZATIONS",
		joinColumns=
			@JoinColumn(name="OXUSERS_NAME", referencedColumnName="NAME"),
		inverseJoinColumns=
			@JoinColumn(name="ORGANIZATIONS_ID", referencedColumnName="ID")
	)
	private Collection<Organization> organizations; 
	
	@ManyToMany
	@JoinTable( // Though we use default names because a JPA bug generating schema
		name="OXUSERS_OXROLES",
		joinColumns=
			@JoinColumn(name="OXUSERS_NAME", referencedColumnName="NAME"),
		inverseJoinColumns=
			@JoinColumn(name="ROLES_NAME", referencedColumnName="NAME")
	)	
	private Collection<Role> roles;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	@OrderBy("singInTime desc")
	@ReadOnly 
	private Collection<SessionRecord> sessionsRecord; 
	
	@ReadOnly
	@ListProperties("localizedName, unrestricted, hidden") 
	public Collection<Module> getModules() {
		// IF YOU CHANGE THIS LOGIC TEST THAT A LDAP USER CANNOT ACCESS TO ChangePassword module
		//   BOTH WITH THE URL IN BROWSER AND IT HAS NOT BE PRESENT IN MODULES MENU
		Collection<Module> modules = getModules((r) -> r.getModules());
		if (isAuthenticateWithLDAP()) {
			return modules.stream()
				.filter(m -> !m.getName().equals(ChangePassword.class.getSimpleName()))
				.collect(Collectors.toList());
		}
		return modules;
	}
	
	/** @since 6.5 */
	public Collection<Module> getModulesNotInMenu() { 
		return getModules((r) -> r.getModulesNotInMenu()); 
	}
	
	private Collection<Module> getModules(Function<Role, Collection<Module>> modulesGetter) { 
		if (roles == null) return Collections.<Module>emptyList();
		Collection<Module> modules = new ArrayList<Module>();
		for (Role role: roles) {
			modules.addAll(modulesGetter.apply(role));
		}
		return modules;
	}

	
	private boolean hasModule(MetaModule metaModule) { 
		for (Module module: getModules()) {
			if (metaModule.getName().equals(module.getName()) && 
				metaModule.getMetaApplication().getName().equals(module.getApplication()))
			{
				return true;
			}
		}
		return false;
	}
	
	public void generatePasswordRecoveringCode() {
		passwordRecoveringCode = UUID.randomUUID().toString().replace("-", "");
		passwordRecoveringDate = new Date();
	}
	
	public void addDefaultRole() { 
		if (roles == null || roles.isEmpty()) {
			Role userRole = Role.find("user");
			if (userRole != null) {
				addRole(userRole);
			}
		}
	}
	
	@PrePersist
	private void prePersit() { 
		creationDate = new Date();
		verifyPasswordsMatch();
	}
	
	@PreUpdate
	private void verifyPasswordsMatch() {
		if (repeatPassword == null) return;
		if (!repeatPassword.equals(password)) {
			Messages errors = new Messages();
			errors.add("passwords_not_match", "password");
			throw new org.openxava.validators.ValidationException(errors);
		}
	}
	
	private void encryptPassword() {
		password = encrypt(password);
	}
	
	private void encryptRepeatPassword() { 
		repeatPassword = encrypt(repeatPassword);
	}
	
	public boolean isAuthorized(String password) { 
		if (!isActive()) return false;
		return passwordMatches(password);
	}
	
	public boolean passwordMatches(String password) {
		if (isAuthenticateWithLDAP()) return isValidLoginWithLDAP(password);
		return encrypt(password).equals(this.password);
	}
	
	private boolean isValidLoginWithLDAP(String password) { 
        Hashtable<String, String> props = new Hashtable<String, String>();        
        String ldapDomain = getProperties().getProperty("ldapDomain", "").trim();
        String ldapHost = getProperties().getProperty("ldapHost", "").trim();
        String ldapPort = getProperties().getProperty("ldapPort", "").trim();
        String ldapDN =  getProperties().getProperty("ldapDN", "").trim();
        String ldapProtocol = "636".equals(ldapPort)?"ldaps":"ldap"; 
        
        String ldapURL;        
        String securityPrincipal;
        if (Is.emptyString(ldapDomain)) {  
        	ldapURL = String.format("%s://%s:%s", ldapProtocol, ldapHost, ldapPort); 
        	securityPrincipal = String.format("%s%s%s", "uid=" + this.name, 		
   														ldapDN.equals("")?"":",", 
   														ldapDN);	
        }
        else {     
        	ldapURL = String.format("%s://%s:%s/%s", ldapProtocol, ldapHost, ldapPort, ldapDN); 
	        securityPrincipal = String.format("%s%s%s", ldapDomain, 
	     	                                           ldapDomain.equals("")?"":"\\", 
	     	                                           this.name);
        }
        
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapURL);
        props.put(Context.SECURITY_AUTHENTICATION, "simple");
        props.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        props.put(Context.SECURITY_CREDENTIALS, password);
        try {
            DirContext ctx = new InitialDirContext(props);
            ctx.close();
            return true;
        } catch (NamingException ex) {
            log.error(XavaResources.getString("ldap_authentication_error"), ex);  
        } finally {
			log.info("javax.naming.Context.PROVIDER_URL: " + ldapURL);
			log.info("javax.naming.Context.SECURITY_PRINCIPAL: " + securityPrincipal);
		}
        return false;
    }
	
	private String encrypt(String source) {
		if (!isEncryptPassword()) return source;
		try {
	      MessageDigest md = MessageDigest.getInstance("SHA");
	      byte[] bytes = source.getBytes();
	      md.update(bytes);
	      byte[] encrypted= md.digest();
	      if (isStorePasswordAsHex()) {
	    	  return new BigInteger(encrypted).toString(16);
	      }
	      else {
	    	  return new String(encrypted);
	      } 
		}
		catch (Exception ex) {
			log.error(XavaResources.getString("encrypting_password_problem"), ex); 
			throw new RuntimeException(XavaResources.getString("encrypting_password_problem"), ex);  
		}
	}
	
	private static boolean isEncryptPassword() {
		return "true".equalsIgnoreCase(getProperties().getProperty(
				"encryptPassword", "true").trim());
	}
	
	private static boolean isStorePasswordAsHex() { 
		return "true".equalsIgnoreCase(getProperties().getProperty(
				"storePasswordAsHex", "true").trim());
	}
	
	// We don't use NaviOXProperties here to not create a compilation dependency from naviox jar from User.java
	private static Properties getProperties() {
		if (properties == null) {
			PropertiesReader reader = new PropertiesReader(
					User.class, PROPERTIES_FILE);
			try {
				properties = reader.get();
			} catch (IOException ex) {
				log.error(XavaResources.getString("properties_file_error",
						PROPERTIES_FILE), ex);
				properties = new Properties();
			}
		}
		return properties;
	}
		
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (Is.equal(this.email, email)) return;
		if (!Is.emptyString(email) && findByEmail(email) != null) {
			throw new org.openxava.validators.ValidationException("email_already_in_use");
		}
		this.email = email;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return "********";
	}

	public void setPassword(String password) {
		if (getPassword().equals(password)) return;
		validatePassword(password);
		rememberPassword(); 
		this.setLastPasswordChangeDate(new Date()); 
		this.password = password;
		encryptPassword(); 
		passwordRecoveringCode = null;
		passwordRecoveringDate = null;
	}

	private void rememberPassword() { 
		recentPassword4 = recentPassword3;
		recentPassword3 = recentPassword2;
		recentPassword2 = recentPassword1;
		recentPassword1 = password;
	}

	private void validatePassword(String password) { 
		Messages errors = new Messages();
		if (password == null) errors.add("not_null", "password", "User"); 
		else {
			if (password.length() < Configuration.getInstance().getPasswordMinLength()) errors.add("password_too_short", "password", Configuration.getInstance().getPasswordMinLength()); 
			if (Configuration.getInstance().isForceLetterAndNumbersInPassword()) {
				if (!StringUtils.isAlphanumeric(password)) errors.add("password_numbers_letters", "password");  
				if (StringUtils.isAlpha(password) || Strings.isNumeric(password)) errors.add("password_numbers_letters", "password");
			}
			String encriptedPassword = encrypt(password);
			if (Configuration.getInstance().isRecentPasswordsNotAllowed() &&
				(encriptedPassword.equals(this.password) ||
				encriptedPassword.equals(recentPassword1) || encriptedPassword.equals(recentPassword2) ||
				encriptedPassword.equals(recentPassword3) || encriptedPassword.equals(recentPassword4))) 
			{
				errors.add("password_already_used", "password");
			}
		}
		if (errors.contains()) throw new org.openxava.validators.ValidationException(errors);
	}
	
	
	@PostLoad
	private void postLoad() {
		updateForceChangePassword();
		resetForMetaModulesCache();
	}
	
	private void updateForceChangePassword() { 
		if (getLastPasswordChangeDate() == null) {
			setLastPasswordChangeDate(new Date());
			return;
		}
		int forceChangePasswordDays = Configuration.getInstance().getForceChangePasswordDays();
		if (forceChangePasswordDays == 0) return;
		int days = Dates.daysInterval(getLastPasswordChangeDate(), new Date(), false);
		if (days >= forceChangePasswordDays) {
			forceChangePassword = true;
		}		
	}	
	
	private void resetForMetaModulesCache() {
		excludedMetaMembersForMetaModules = null; 
		readOnlyMetaMembersForMetaModules = null;
		excludedCollectionActionsForMetaModules = null;
		excludedMetaActionsForMetaModules = null;
	}
	
	public boolean addOrganization(Organization organization) { 
		if (organizations == null) organizations = new ArrayList<Organization>();
		if (!organizations.contains(organization)) return organizations.add(organization);
		else return false;
	}
	
	/** @since 6.0 */
	public boolean addRole(String roleName) { 
		if (roles == null) roles = new ArrayList<Role>();
		Role role = Role.find(roleName);
		if (role != null) {
			addRole(role);
			return true;
		}
		return false;
	}

	
	public void addRole(Role role) { 
		if (roles == null) roles = new ArrayList<Role>();
		roles.add(role);
	}
	
	/** @since 5.7 */
	public boolean hasRole(String roleName) { 
		if (roles == null) return false;
		for (Role role: roles) {
			if (role.getName().equals(roleName)) return true;
		}
		return false;
	}
	
	public Collection<Role> getRoles() {
		return roles;
	}

	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}


	public Collection<Module> getNotHiddenModules() {
		Collection<Module> result = new ArrayList<Module>();
		for (Module module: getModules()) {
			if (!module.isHidden()) {
				result.add(module);
			}
		}
		return result;
	}
	
	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public boolean isAuthenticateWithLDAP() {
		return authenticateWithLDAP;
	}

	public void setAuthenticateWithLDAP(boolean authenticateWithLDAP) {
		this.authenticateWithLDAP = authenticateWithLDAP;
	}

	public String getRepeatPassword() {
		return getPassword();
	}

	public void setRepeatPassword(String repeatPassword) {
		if (repeatPassword == null) return; 
		if (repeatPassword.equals(getPassword())) return; 
		if (repeatPassword.equals(this.repeatPassword)) return; 
		this.repeatPassword = repeatPassword;
		encryptRepeatPassword(); 
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) failedLoginAttempts = 0; 
	}
	
	/**
	 * @since 5.3
	 */
	static public boolean isActionForMetaModule(String userName, String actionName, MetaModule metaModule) { 
		if (actionsByModules == null) actionsByModules = new HashMap<String, Boolean>();
		String key = userName + ":" + actionName + ":" + metaModule.getMetaApplication().getName() + ":" + metaModule.getName();
		Boolean result = actionsByModules.get(key);
		if (result == null) {
			User user = User.find(userName);
			if (user.hasModule(metaModule)) {
				result = true;
				Collection<MetaAction> excludedActions =  user.getExcludedMetaActionsForMetaModule(metaModule);				
				for (MetaAction action: excludedActions) {		
					if (action.getName().equals(actionName)) {
						result = false;
						break;
					}
				}
			}
			else {
				result = false;
			}
			actionsByModules.put(key, result);
		}
		return result; 
	}
	
	public Collection<MetaAction> getExcludedMetaActionsForMetaModule(MetaModule metaModule) {
		// We make cache in order refine and polish from View do not do a lot duplicated SQLs 
		if (excludedMetaActionsForMetaModules == null) excludedMetaActionsForMetaModules = new HashMap<>();
		Collection<MetaAction> result = excludedMetaActionsForMetaModules.get(metaModule);
		if (result == null) {
			result = collectFromRights(metaModule, new IRightsCollectionExtractor() {
				public Collection get(ModuleRights rights) {
					return rights.getExcludedMetaActions();
				}
			});
			excludedMetaActionsForMetaModules.put(metaModule, result);
		}
		return result;		
	}
	
	public Collection<String> getExcludedCollectionActionsForMetaModule(MetaModule metaModule) {
		// We make cache in order refine and polish from View do not do a lot duplicated SQLs
		if (excludedCollectionActionsForMetaModules == null) excludedCollectionActionsForMetaModules = new HashMap<>();
		Collection<String> result = excludedCollectionActionsForMetaModules.get(metaModule);
		if (result == null) {
			result = collectFromRights(metaModule, new IRightsCollectionExtractor() {
				public Collection get(ModuleRights rights) {
					return rights.getExcludedCollectionActions();
				}
			});
			for (MetaCollection collection: MetaModel.get(metaModule.getModelName()).getMetaCollections()) {
				String referencedModel = collection.getMetaReference().getReferencedModelName();
				MetaModule referencedMetaModule = metaModule.getMetaApplication().getMetaModule(referencedModel);
				if (!isActionForMetaModule(name, "new", referencedMetaModule)) {
					result = add(result, collection.getName() + ":Collection.new"); 
				}
				if (!isActionForMetaModule(name, "save", referencedMetaModule)) {
					result = add(result, collection.getName() + ":Collection.new");
					result = add(result, collection.getName() + ":Collection.edit"); 
				}
				if (!isActionForMetaModule(name, "delete", referencedMetaModule)) {
					result = add(result, collection.getName() + ":Collection.removeSelected"); 
				}								
			}
			excludedCollectionActionsForMetaModules.put(metaModule, result);
		}
		return result;
	}
	
	private Collection<String> add(Collection<String> collection, String string) { 
		try {
			collection.add(string);
			return collection;
		}
		catch (UnsupportedOperationException ex) {
			Collection<String> result = new HashSet(collection);
			result.add(string);
			return result;
		}
	}
	
	public Collection<MetaMember> getExcludedMetaMembersForMetaModule(MetaModule metaModule) {
		// We make cache in order refine and polish from View do not do a lot duplicated SQLs
		if (excludedMetaMembersForMetaModules == null) excludedMetaMembersForMetaModules = new HashMap<>();
		Collection<MetaMember> result = excludedMetaMembersForMetaModules.get(metaModule);
		if (result == null) {
			result = collectFromRights(metaModule, new IRightsCollectionExtractor() {			
				public Collection get(ModuleRights rights) {
					return rights.getExcludedMetaMembers();
				}
			});
			excludedMetaMembersForMetaModules.put(metaModule, result);
		}
		return result;
	}
	
	public Collection<MetaMember> getReadOnlyMetaMembersForMetaModule(MetaModule metaModule) {
		// We make cache in order refine and polish from View do not do a lot duplicated SQLs
		if (readOnlyMetaMembersForMetaModules == null) readOnlyMetaMembersForMetaModules = new HashMap<>();
		Collection<MetaMember> result = readOnlyMetaMembersForMetaModules.get(metaModule);
		if (result == null) {
			result = collectFromRights(metaModule, new IRightsCollectionExtractor() {			
				public Collection get(ModuleRights rights) {
					return rights.getReadOnlyMetaMembers();
				}
			});
			readOnlyMetaMembersForMetaModules.put(metaModule, result);
		}
		return result;
	}
		
	public Collection collectFromRights(MetaModule metaModule, IRightsCollectionExtractor extractor) { 
		Collection result = null; 
		for (Role role: roles) {
			ModuleRights rights = role.getModulesRightsForMetaModule(metaModule);
			if (rights == null) continue;
			if (result == null) result = extractor.get(rights);
			else result = CollectionUtils.intersection(result, extractor.get(rights));
		}
		return result==null?Collections.EMPTY_LIST:result;
	}	

	private interface IRightsCollectionExtractor {
		
		Collection get(ModuleRights rights);
	
	}

	public boolean isForceChangePassword() {
		return forceChangePassword;
	}

	public void setForceChangePassword(boolean forceChangePassword) {
		this.forceChangePassword = forceChangePassword;
	}

	public Date getLastPasswordChangeDate() {
		return lastPasswordChangeDate;
	}

	public void setLastPasswordChangeDate(Date lastPasswordChangeDate) {
		this.lastPasswordChangeDate = lastPasswordChangeDate;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
		if (lastLoginDate != null) recordSession(); 
	}
	
	private void recordSession() { 
		SessionRecord r = new SessionRecord();
		r.setUser(this);
		r.setSingInTime(new java.sql.Timestamp(lastLoginDate.getTime()));
		if (sessionsRecord == null) sessionsRecord = new ArrayList<SessionRecord>();
		sessionsRecord.add(r);
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Collection<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Collection<Organization> organizations) {
		this.organizations = organizations;
	}

	public Collection<SessionRecord> getSessionsRecord() {
		return sessionsRecord;
	}

	public void setSessionsRecord(Collection<SessionRecord> sessionsRecord) {
		this.sessionsRecord = sessionsRecord;
	}
	
	public Date getPasswordRecoveringDate() {
		return passwordRecoveringDate;
	}

	public void setPasswordRecoveringDate(Date passwordRecoveringDate) {
		this.passwordRecoveringDate = passwordRecoveringDate;
	}

	public void setPasswordRecoveringCode(String passwordRecoveringCode) {
		this.passwordRecoveringCode = passwordRecoveringCode;
	}

	public String getPasswordRecoveringCode() {
		return passwordRecoveringCode;
	}

	public Date getPrivacyPolicyAcceptanceDate() {
		return privacyPolicyAcceptanceDate;
	}

	public void setPrivacyPolicyAcceptanceDate(Date privacyPolicyAcceptanceDate) {
		this.privacyPolicyAcceptanceDate = privacyPolicyAcceptanceDate;
	}

	public String getAllowedIP() {
		return allowedIP;
	}

	public void setAllowedIP(String allowedIP) {
		this.allowedIP = allowedIP;
	}

}
