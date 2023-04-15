package ch.speleo.scis.model.karst;

import java.io.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang.*;
import org.hibernate.envers.*;
import org.openxava.annotations.*;
import org.openxava.calculators.*;

import ch.speleo.scis.model.common.*;
import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import lombok.*;

@Entity
@Table(name = "KARST_OBJECT_DOCUMENT")
@Audited
@Views({
	@View(name = "Short", members = "transmissionDate, rolledMap, suspensionFolder; contact; remarks; "), 
	@View(members = "transmissionDate, rolledMap, suspensionFolder; object; contact; remarks; ")
})
@Getter @Setter
public class KarstObjectDocument
extends GenericIdentity implements Serializable {
	
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

    	
	@Hidden
	public boolean isEmpty() {
		return contact == null && 
		       transmissionDate == null && 
		       StringUtils.isBlank(remarks) && 
		       !rolledMap && 
		       !suspensionFolder;
	}
    
	@PrePersist @PreUpdate @PreDelete
    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
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
