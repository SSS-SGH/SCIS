package ch.speleo.scis.business.utils;

import org.apache.commons.lang.StringUtils;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

public class ConditionalRequiredValidator<T> implements IValidator {
	
	private static final long serialVersionUID = 6459200280132747025L;

	private Boolean required;
	private T value;
	private String conditionName;
	private String valueName;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public String getValueName() {
		return valueName;
	}

	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	public void validate(Messages errors) throws Exception {
		if (Boolean.TRUE.equals(required) && (
				value== null || StringUtils.isEmpty(value.toString()))) {
			errors.add("required_on_condition", conditionName, valueName);
		}
	
	}

}
