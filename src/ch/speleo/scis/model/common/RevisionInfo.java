package ch.speleo.scis.model.common;

import java.io.*;
import java.util.*;

import org.openxava.annotations.*;

import lombok.*;

@View(members = "modificationDate, username")
@Getter @Setter
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
