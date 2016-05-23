package ch.speleo.scis.model.karst;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.Editor;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.annotations.PropertyValue;

import ch.speleo.scis.business.utils.PrivacyValidator;
import ch.speleo.scis.model.common.GenericIdentity;

/**
 * Class representing a privacy (restriction of access to an information) using Hibernate
 * Annotation.
 * 
 * @author miguel
 * @version 1.0
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
public class Privacy 
extends GenericIdentity implements Serializable {
    /**
     * Serial version UID.
     */
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
     * Empty constructor.
     */
    public Privacy() { }
    /**
     * @return date when the karst object started being protected.
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate date when the karst object started being protected.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return date when the protected is going to end.
     */
    public Date getEndDate() {
        return endDate;
    }
    /**
     * @param endDate date when the protected is going to end.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    /**
     * @return why we decided to protect the given karst object.
     */
    public String getReason() {
        return reason;
    }
    /**
     * @param reason why we decided to protect the given karst object.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    /**
     * @return who decided to protect the karst object.
     */
    public String getProtector() {
        return protector;
    }
    /**
     * @param protector who decided to protect the karst object.
     */
    public void setProtector(String protector) {
        this.protector = protector;
    }
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
