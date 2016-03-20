package ch.speleo.scis.model.common;

import java.io.Serializable;
import java.util.Date;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.View;

@View(members = "modificationDate, username")
public class RevisionInfo implements Serializable {

	private static final long serialVersionUID = -6167475284113314078L;

	@Stereotype("DATETIME")
	@Hidden
    private Date modificationDate;

	@Hidden
    private String username;
    
    public RevisionInfo() {	
    }

    public RevisionInfo(Revision revision) {
    	this.modificationDate = revision.getModificationDate();
    	this.username = revision.getUsername();
    }

	/**
	 * @return The time of the modification.
	 */
	public Date getModificationDate() {
		return modificationDate;
	}
	/**
	 * @param modificationDate The time of the modification.
	 */
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	/**
	 * @return Username of the user that does this change.
	 */
	public String getUsername() { 
		return username; 
	}
	/**
	 * @param username Username of the user that does this change.
	 */
	public void setUsername(String username) { 
		this.username = username; 
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(RevisionInfo.class.getSimpleName());
		builder.append(" [modificationDate=");
		builder.append(modificationDate);
		builder.append(", username=");
		builder.append(username);
		builder.append("]");
		return builder.toString();
	}
	
}
