package ch.speleo.scis.ui.editors;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TabViewableOnMap {

	String eastColumnName();
	String northColumnName();
	int coordsSystemEpsgNr();
}
