package ch.speleo.scis.model.common;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.text.StrBuilder;
import org.hibernate.envers.Audited;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.LabelFormat;
import org.openxava.annotations.LabelFormatType;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;

import ch.speleo.scis.persistence.utils.SimpleQueries;

/**
 * Class representing a karstologist (a caver for example) using Hibernate
 * Annotation.
 * 
 * @author miguel
 * @version 1.0
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
     * Empty constructor.
     */
    public Karstologist() { }
    /**
     * @return initials of the karstologist.
     */
    public String getInitials() {
        return initials;
    }
    /**
     * @param initials initials of the karstologist.
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }
    /**
	 * @return first name of the karstologist.
	 */
	public String getFirstname() {
	    return firstname;
	}
	/**
	 * @param firstname first name of the karstologist.
	 */
	public void setFirstname(String firstname) {
	    this.firstname = firstname;
	}
	/**
     * @return name of the karstologist.
     */
    public String getLastname() {
        return lastname;
    }
    /**
     * @param name name of the karstologist.
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    /**
     * @return initials, first and last name of the karstologist.
     */
	@Depends("initials, firstname, lastname")
	@LabelFormat(value = LabelFormatType.NO_LABEL, forViews = "Short")
    public String getInitialsAndName() {
        StrBuilder text = new StrBuilder();
        text.append("<").append(initials).append("> ");
        text.append(firstname).append(" ").append(lastname);
        return text.toString();
    }
	@Depends("initials, firstname, lastname")
	@Hidden
	public String getBusinessId() {
		return getInitialsAndName();
	}
   /**
     * @return club in which the karstologist is registred.
     */
    public String getClub() {
        return club;
    }
    /**
     * @param club club in which the karstologist is registred.
     */
    public void setClub(String club) {
        this.club = club;
    }
    /**
     * @return comments related to a karstologist.
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comments comments related to a karstologist.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @ListProperties("revision.modificationDate, revision.username, deleted, initials, firstname, lastname, club, comment")
    @ReadOnly
    public List<Karstologist> getAuditedValues() {
    	return loadAuditedValues();
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
    	StrBuilder msg = new StrBuilder();
    	msg.append(" while searching not deleted ").append(Karstologist.class.getSimpleName());
    	msg.append(" with ").append("initials").append(" = " ).append(initials);
    	return SimpleQueries.getSingleResult(msg.toString(), Karstologist.class, "initials = ? and deleted = false", initials);
    }
    
}
