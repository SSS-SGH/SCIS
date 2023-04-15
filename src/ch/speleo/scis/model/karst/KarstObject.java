package ch.speleo.scis.model.karst;

import java.io.*;
import java.sql.*;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.envers.*;
import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.util.*;

import ch.speleo.scis.model.common.*;
import ch.speleo.scis.persistence.audit.*;
import ch.speleo.scis.persistence.audit.ScisUserUtils.*;
import lombok.*;

/**
 * Any object to document on or in the cave (generic object like an entrance, a cave system, a spring, etc).
 */
@Entity
@Table(name = "KARST_OBJECT")
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
@Tab(properties = "name, translatedType, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@View(name = "Short", members = "name, translatedType, deleted")
@Getter @Setter
public class KarstObject 
extends GenericIdentityWithDeleted implements Serializable, Identifiable {

	private static final long serialVersionUID = 11992881712762616L;

    /**
     * Name of the karst object.
     */
    @Column(name = "NAME", length=100, nullable = false)
	@DisplaySize(value=50, forViews="Short, ShortWithId") 
    @Required
    private String name;
    /**
     * Comment about the karst object.
     */
    @Column(name = "COMMENT", length=50000, nullable = true)
    @Stereotype("BIGTEXT_AREA")
    private String comment;
    /**
     * Manager of the karst object.
     */
    @ManyToOne
    @JoinColumn(name = "MANAGER_ID", nullable = true)
    @ReferenceView(value = "Short")
    private Karstologist manager;
    /**
     * Date when the karst object ...
     */
    @Column(name = "CREATED_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    @DefaultValueCalculator(CurrentDateCalculator.class)
    private Date creationDate;
    /**
     * Date of the last modification
     */
    @Column(name = "LAST_MODIF_DATE", nullable = true)
    @Version // An OpenXava editor and formater with miliseconds is required. 
    @NotAudited
    @ReadOnly
    @Stereotype("TIMESTAMP")
    private Timestamp lastModifDate;
    /**
     * Literature, references, publication
     */
    @Column(name = "LITERATURE", nullable = true, length=500)
	@DisplaySize(value=100) 
    private String literature;
    /**
     * History of the available data
     */
    @Column(name = "DATA_HISTORY", nullable = true, length=500)
	@DisplaySize(value=100) 
    private String dataHistory;
    /**
     * Document(s) available for this object. 
     */
    @OneToOne(mappedBy = "object", optional = true, cascade = {CascadeType.ALL}, orphanRemoval=true)
    @ReferenceView(value = "Short")
    @AsEmbedded
    @NoSearch @NoCreate @NoModify
    private KarstObjectDocument document;
    /**
     * Any files and folders
     */
    //@Column(name = "E_FOLDER_NAME", nullable = true)
    //@Stereotype("SCIS_DOCUMENT_LIBRARY") 
    @Transient // for future use 
    private String edataFolderName;
    /**
     * Has been verified after the import.
     */
    @Column(name = "VERIFIED", nullable = false)
    @DefaultValueCalculator(TrueCalculator.class)
    private Boolean verified;

	@Depends("name")
	@Hidden
	public String getBusinessId() {
		return getName();
	}
	public void setDocument(KarstObjectDocument document) {
		if (this.document != null) {
			this.document.setObject(null);
		}
		this.document = document;
		if (document != null) {
			document.setObject(this); // so that the link works
		}
	}
	
	/**
	 * @return The translation of the object's type, if it exists, otherwise of the class name
	 */
	public String getTranslatedType() {
		String text = getClass().getSimpleName();
		return Labels.get(text);
	}
	
	@PrePersist @PreUpdate @PreDelete
	public void onPrePersistAndUpdate() {
		removeEmptyDocument();
		handlePermissionsOnWrite();
	}
	
	public void removeEmptyDocument() {
		if (document != null && document.isEmpty()) {
			document = null;
		}
	}

    public void handlePermissionsOnWrite() {
        ScisUserUtils.checkRoleInCurrentUser(ScisRole.SGH_ARCHIVAR);
    }
    
	@Override
	protected void writeFields(StringBuilder builder) {
		super.writeFields(builder);
		builder.append(", name=");
		builder.append(name);
		builder.append(", comment=");
		builder.append(comment);
		builder.append(", manager.id=");
		builder.append((manager==null)?"null":manager.getId());
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", lastModifDate=");
		builder.append(lastModifDate);
		builder.append(", literature=");
		builder.append(literature);
		builder.append(", dataHistory=");
		builder.append(dataHistory);
		builder.append(", document.id=");
		builder.append((document==null)?"null":document.getId());
		builder.append(", edataFolderName=");
		builder.append(edataFolderName);
		builder.append(", verified=");
		builder.append(verified);
	}
	
}
