package ch.speleo.scis.persistence.audit;

import java.util.*;

import org.openxava.filters.*;
import org.openxava.util.*;

import com.openxava.naviox.model.*;

import lombok.*;

public class ScisUserUtils {
	
	public static String getCurrentUserName() {
		return Users.getCurrent(); 
		// on Liferay, this gives a technical internal id ... better gives the nickname as bellow!
		//UserInfo userinfo = Users.getCurrentUserInfo();
		//return StringUtils.isNotBlank(userinfo.getNickName()) ? userinfo.getNickName() : userinfo.getId();
	}
	
	public static boolean hasRoleInCurrentUser(ScisRole... roles) {
		User currentUser = User.find(getCurrentUserName());
		for (ScisRole role: roles) {
			if (currentUser.hasRole(role.name())) {
				return true;
			}
		}
		return false;
	}
	
	public static void checkRoleInCurrentUser(ScisRole... roles) {
		if (!hasRoleInCurrentUser(roles)) {
			throw new NoPermissionException("missing one of the roles " + Arrays.toString(roles) + " for current user " + getCurrentUserName());
		}
	}
	
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ScisRole {
		public static final ScisRole SGH_ARCHIVAR = new ScisRole("SGH_ARCHIVAR"); 
		public static final ScisRole SGH_MEMBER = new ScisRole("SGH_MEMBER"); 
		public static final ScisRole VISITOR = new ScisRole("VISITOR"); 
		public static final ScisRole ADMIN = new ScisRole("admin"); 
		
		private final String name;
		
		public String name() {
			return name;
		}
		public String toString() {
			return name();
		}
	}
	
	public static class NoPermissionException extends RuntimeException {

		private static final long serialVersionUID = -7171481631668666151L;

		public NoPermissionException(String message) {
			super(message);
		}
	}
	
    /**
     * Combines the filters from OpenXava with those from other conditions (typically an implemented {@link IFilter}). 
     * @param baseFilters Filters from OpenXava search, can be null, an array or unlikely a single object.
     * @param otherFilters Filters for additional conditions. 
     * @return Combined filters, with first {@code otherFitlers} then {@code baseFilters} to please OpenXava.
     */
	public static Object[] combineFilters(Object baseFilters, Object... otherFilters) {
		if (baseFilters == null) {
			return otherFilters;
		}
		if (baseFilters instanceof Object[]) {
			int baseLength = ((Object[]) baseFilters).length;
			Object[] result = new Object[otherFilters.length + baseLength];
			System.arraycopy(otherFilters, 0, result, 0, otherFilters.length);
			System.arraycopy(baseFilters, 0, result, otherFilters.length, baseLength);
			return result;
		} else {
			Object[] result = new Object[otherFilters.length + 1];
			System.arraycopy(otherFilters, 0, result, 0, otherFilters.length);
			result[otherFilters.length] = baseFilters;
			return result;
		}
	}

}
