package ch.speleo.scis.model.common;

import java.io.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.apache.commons.text.*;
import org.hibernate.envers.*;
import org.openxava.annotations.*;

import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import ch.speleo.scis.persistence.utils.*;
import lombok.*;

/**
 * A karstologist in a very broad sense (a caver for example).
 */
@Entity
@Table(name = "KARSTOLOGIST",
    uniqueConstraints = {})
@Audited
@Tab(properties = "initials, firstname, lastname, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@Views({
	@View(name = "Short", members = "initials, firstname, lastname"),
	@View(members = "initials; firstname; lastname; club; comment; deleted"),
	@View(name=GenericIdentityWithRevision.AUDIT_VIEW_NAME, members = " auditedValues")
	})
@Getter @Setter
public class Karstologist 
extends GenericIdentityWithDeleted implements Serializable, Identifiable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8464339762940905332L;
    
    /**
     * Initials of the karstologist.
     */
    @Column(name = "INITIALS", nullable = false, length=10)
    @Required
    @Size(max=10)
    private String initials;
    
    /**
	 * First name of the karstologist.
	 */
	@Column(name = "FIRSTNAME", nullable = true, length=50)
	@DisplaySize(30) 
	private String firstname;

	/**
     * Name of the karstologist.
     */
    @Column(name = "LASTNAME", nullable = true, length=50)
	@DisplaySize(30) 
    private String lastname;
    
    /**
     * Club in which the karstologist is registred.
     */
    @Column(name = "CLUB", nullable = true, length=50)
    private String club;
    
    /**
     * Comments related to a karstologist.
     */
    @Column(name = "COMMENT", nullable = true)
    private String comment;
    
    /**
     * @return initials, first and last name of the karstologist.
     */
	@Depends("initials, firstname, lastname")
	@LabelFormat(value = LabelFormatType.NO_LABEL, forViews = "Short")
    public String getInitialsAndName() {
		TextStringBuilder text = new TextStringBuilder();
        text.append("<").append(initials).append("> ");
        text.append(firstname).append(" ").append(lastname);
        return text.toString();
    }
	@Depends("initials, firstname, lastname")
	@Hidden
	public String getBusinessId() {
		return getInitialsAndName();
	}
    
    @ListProperties("revision.modificationDate, revision.username, deleted, initials, firstname, lastname, club, comment")
    @ReadOnly
    public List<Karstologist> getAuditedValues() {
    	return loadAuditedValues();
    }

	@PrePersist @PreUpdate @PreDelete
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
    @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", initials=");
		builder.append(initials);
		builder.append(", lastname=");
		builder.append(lastname);
		builder.append(", firstname=");
		builder.append(firstname);
		builder.append(", club=");
		builder.append(club);
		builder.append(", comment=");
		builder.append(comment);
	}
	
    public static Karstologist getByInitials(String initials) {
    	if (initials == null) 
    		throw new IllegalArgumentException("initials of "+Karstologist.class.getSimpleName()+" to search for should not be null");
    	TextStringBuilder msg = new TextStringBuilder();
    	msg.append(" while searching not deleted ").append(Karstologist.class.getSimpleName());
    	msg.append(" with ").append("initials").append(" = " ).append(initials);
    	return SimpleQueries.getSingleResult(msg.toString(), Karstologist.class, "initials = ?1 and deleted = false", initials);
    }
    
}
