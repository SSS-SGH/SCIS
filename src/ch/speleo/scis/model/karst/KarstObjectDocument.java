package ch.speleo.scis.model.karst;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.calculators.FalseCalculator;

import ch.speleo.scis.model.common.GenericIdentity;
import ch.speleo.scis.model.common.Karstologist;

@Entity
@Table(name = "KARST_OBJECT_DOCUMENT")
@Audited
@Views({
	@View(name = "Short", members = "transmissionDate, rolledMap, suspensionFolder; contact; remarks; "), 
	@View(members = "transmissionDate, rolledMap, suspensionFolder; object; contact; remarks; ")
})
public class KarstObjectDocument
extends GenericIdentity implements Serializable {
	
	/**
     * Serial version UID.
	 */
	private static final long serialVersionUID = 3695081938559929106L;

	/**
	 * The object to which this document refers. 
	 */
	@OneToOne() //optional = false to temporary save it
    @JoinColumn(name = "OBJECT_ID") // nullable=true to temporary save it
    @ReferenceView(value = "Short")
	private KarstObject object;
	
	/**
	 * The person who transmitted this document or the contact for this document. 
	 */
    @ManyToOne
    @JoinColumn(name = "CONTACT_ID", nullable = true)
    @DescriptionsList(forViews="Short", descriptionProperties="initials, firstname, lastname")
    @ReferenceView(value = "Short")
	private Karstologist contact;
    
    @Column(name = "TRANSMISSION_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date transmissionDate;
    
    @Column(name = "REMARKS", nullable = true)
    @Stereotype("TEXT_AREA")
    private String remarks;
    
    /**
     * if this document is (among others) a rolled map. 
     */
    @Column(name = "ROLLED_MAP", nullable = true)
    @DefaultValueCalculator(FalseCalculator.class)
    private Boolean rolledMap;
    
    /**
     * if this document is (among others) in a suspension folder. 
     */
    @Column(name = "SUSPENSION_FOLDER", nullable = true)
    @DefaultValueCalculator(FalseCalculator.class)
    private Boolean suspensionFolder;

    
	/**
	 * @return The object to which this document refers. 
	 */
	public KarstObject getObject() {
		return object;
	}
	public void setObject(KarstObject object) {
		this.object = object;
	}

	/**
	 * @return The person who transmitted this document or the contact for this document. 
	 */
	public Karstologist getContact() {
		return contact;
	}
	public void setContact(Karstologist contact) {
		this.contact = contact;
	}

	public Date getTransmissionDate() {
		return transmissionDate;
	}
	public void setTransmissionDate(Date transmissionDate) {
		this.transmissionDate = transmissionDate;
	}

	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

    /**
     * @return if this document is (among others) a rolled map. 
     */
	public Boolean getRolledMap() {
		return rolledMap;
	}
	public void setRolledMap(Boolean rolledMap) {
		this.rolledMap = rolledMap;
	}

    /**
     * @return if this document is (among others) in a suspension folder. 
     */
	public Boolean getSuspensionFolder() {
		return suspensionFolder;
	}
	public void setSuspensionFolder(Boolean suspensionFolder) {
		this.suspensionFolder = suspensionFolder;
	}
	
	@Hidden
	public boolean isEmpty() {
		return contact == null && 
		       transmissionDate == null && 
		       StringUtils.isBlank(remarks) && 
		       !rolledMap && 
		       !suspensionFolder;
	}
    
    @Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", object.id=");
		builder.append((object==null)?"null":object.getId());
		builder.append(", contact.id=");
		builder.append((contact==null)?"null":contact.getId());
		builder.append(", transmissionDate=");
		builder.append(transmissionDate);
		builder.append(", remarks=");
		builder.append(remarks);
		builder.append(", rolledMap=");
		builder.append(rolledMap);
		builder.append(", suspensionFolder=");
		builder.append(suspensionFolder);
	}
    
}
