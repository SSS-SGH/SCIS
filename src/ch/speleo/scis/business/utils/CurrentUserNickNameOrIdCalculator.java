package ch.speleo.scis.business.utils;

import org.openxava.calculators.ICalculator;

import ch.speleo.scis.persistence.audit.ScisUserUtils;

public class CurrentUserNickNameOrIdCalculator implements ICalculator {

	private static final long serialVersionUID = -6656051521147852137L;

	public Object calculate() throws Exception {
		return ScisUserUtils.getCurrentUserName();
	}
	
}
