package ch.speleo.scis.model.karst;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.openxava.annotations.AsEmbedded;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DisplaySize;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.calculators.TrueCalculator;
import org.openxava.util.Labels;

import ch.speleo.scis.model.common.GenericIdentityWithDeleted;
import ch.speleo.scis.model.common.Identifiable;
import ch.speleo.scis.model.common.Karstologist;

/**
 * Class representing an object in the cave (generic object like an entrance, 
 * a cave system, a spring, etc.).
 * 
 * @author miguel
 * @version 1.0
 */
@Entity
@Table(name = "KARST_OBJECT")
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
@Tab(properties = "name, translatedType, deleted", 
	rowStyles = {@RowStyle(style="deletedData", property="deleted", value="true")})
@View(name = "Short", members = "name, translatedType, deleted")
public class KarstObject 
extends GenericIdentityWithDeleted implements Serializable, Identifiable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 11992881712762616L;

    /**
     * Name of the karst object.
     */
    @Column(name = "NAME", length=100, nullable = false)
	@DisplaySize(value=50, forViews="Short") 
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

    /**
     * Empty constructor.
     */
    public KarstObject() { }

	/**
     * @return name of the karst object.
     */
    public String getName() {
        return name;
    }
	@Depends("name")
	@Hidden
	public String getBusinessId() {
		return getName();
	}
    /**
     * @param name name of the karst object.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Comment about the karst object.
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comment Comment about the karst object.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return manager of the karst object.
     */
    public Karstologist getManager() {
        return manager;
    }
    /**
     * @param manager manager of the karst object.
     */
    public void setManager(Karstologist manager) {
        this.manager = manager;
    }
    
    /**
     * @return Date when the karst object ...
     */
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

    /**
     * @return Date of the last modification
     */
	public Timestamp getLastModifDate() {
		return lastModifDate;
	}
	public void setLastModifDate(Timestamp lastModifDate) {
		this.lastModifDate = lastModifDate;
	}

	/**
	 * @return Literature, references, publication
	 */
	public String getLiterature() {
		return literature;
	}
	public void setLiterature(String literature) {
		this.literature = literature;
	}

	/**
	 * @return History of the available data
	 */
	public String getDataHistory() {
		return dataHistory;
	}
	public void setDataHistory(String dataHistory) {
		this.dataHistory = dataHistory;
	}

    /**
     * @return Document(s) available for this object. 
     */
    public KarstObjectDocument getDocument() {
		return document;
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
	
	public String getEdataFolderName() {
		return edataFolderName;
	}

	public void setEdataFolderName(String eFolderName) {
		this.edataFolderName = eFolderName;
	}

	/**
	 * @return The translation of the object's type, if it exists, otherwise of the class name
	 */
	public String getTranslatedType() {
		String text = getClass().getSimpleName();
		return Labels.get(text);
	}
	
	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}
	
	@PrePersist @PreUpdate
	public void removeEmptyDocument() {
		if (document != null && document.isEmpty()) {
			document = null;
		}
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
