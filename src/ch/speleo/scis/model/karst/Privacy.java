package ch.speleo.scis.model.karst;

import java.io.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang.*;
import org.hibernate.envers.*;
import org.openxava.annotations.*;

import ch.speleo.scis.business.utils.*;
import ch.speleo.scis.model.common.*;
import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import lombok.*;

/**
 * Privacy, a restriction of access to an information.
 */
@Entity
@Table(name = "PRIVACY")
@Audited
@Views({
	@View(name = "Short", members = "privateNow, reason"), 
	@View(members = "startDate, endDate, privateNow; protector; reason")
})
@EntityValidator(value=PrivacyValidator.class, properties={
	@PropertyValue(name="startDate"),
	@PropertyValue(name="endDate"),
	@PropertyValue(name="reason"),
	@PropertyValue(name="protector")
})
@Getter @Setter
public class Privacy 
extends GenericIdentity implements Serializable {

	private static final long serialVersionUID = -1299394812743244046L;
    
    /**
     * Date when the karst object started being protected.
     */
    @Column(name = "START_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    //@Required
    private Date startDate;
    
    /**
     * Date when the protected is going to end.
     */
    @Column(name = "END_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    /**
     * Why we decided to protect the given karst object.
     */
    @Column(name = "REASON", nullable = true)    
    //@Required @NotEmpty
	@DisplaySize(value=100) 
    private String reason;
    
    /**
     * Who decided to protect the karst object.
     */
    //@ManyToOne
    //@JoinColumn(name = "PROTECTOR_ID", nullable = true)
    //@DescriptionsList(forViews="Short", descriptionProperties="initials, firstname, lastname")
    //@ReferenceView(value = "Short")
    @Column(name = "PROTECTOR", nullable = true, length=50)    
    private String protector;
    
    /**
     * @return if the protection is currently active.
     */
	@Depends("startDate, endDate")
	@Editor("BooleanYesNoCombo")
    public boolean isPrivateNow() {
		return isPrivate(Calendar.getInstance());
    }

    /**
     * @return if the protection is active at a given time.
     */
	@Depends("startDate, endDate")
    public boolean isPrivate(Calendar refTime) {
		if (startDate == null || startDate.after(refTime.getTime())) 
			return false; // not private yet
		if (endDate == null)
			return true; 
    	Calendar end = Calendar.getInstance();
    	end.setTime(this.endDate);
    	// privacy till end of day at 23:59:59
    	end.add(Calendar.DAY_OF_MONTH, 1);
    	end.add(Calendar.SECOND, -1);
    	return end.compareTo(refTime) > 0;
    }
	
	@Hidden
	public boolean isEmpty() {
		return startDate == null && 
		       endDate == null && 
		       StringUtils.isBlank(reason) && 
		       StringUtils.isBlank(protector);
	}
    
	@PrePersist @PreUpdate @PreDelete
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
   @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", protector=");
		builder.append(protector);
	}
    
}
