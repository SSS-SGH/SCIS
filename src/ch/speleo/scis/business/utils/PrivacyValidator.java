package ch.speleo.scis.business.utils;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

public class PrivacyValidator implements IValidator {

    private static final long serialVersionUID = -1293394815743214046L;
    
	public void validate(Messages errors) throws Exception {
		if (!isEmpty()) {
			if (startDate == null){
				errors.add("privacy_required_fields", "startDate");
			}
			if (StringUtils.isBlank(reason)){
				errors.add("privacy_required_fields", "reason");
			}
		}
	}

    private Date startDate;
    private Date endDate;
    private String reason;
    private String protector;
    
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getProtector() {
        return protector;
    }
    public void setProtector(String protector) {
        this.protector = protector;
    }
	public boolean isEmpty() {
		return startDate == null && 
		       endDate == null && 
		       StringUtils.isBlank(reason) && 
		       StringUtils.isBlank(protector);
	}
    

}
