package ch.speleo.scis.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.formatters.BooleanFormatter;
import org.openxava.model.meta.MetaModel;
import org.openxava.model.meta.MetaProperty;
import org.openxava.tab.Tab;
import org.openxava.tab.impl.IXTableModel;
import org.openxava.util.Messages;
import org.openxava.view.View;
import org.openxava.web.WebEditors;

import ch.speleo.scis.ui.editors.MapScisEditorHelper.ItemWithCoordinates;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MapScisEditorHelper implements Iterable<ItemWithCoordinates>, Iterator<ItemWithCoordinates> {

	private static Log log = LogFactory.getLog(MapScisEditorHelper.class);

	private final View view;
	private final MetaModel metaModel;
	private final HttpServletRequest request;
	private final Messages errors;
	private final Tab tab;
	private final Class<?> entityClass;
	private final TabViewableOnMap tabViewableOnMap;
	private final IXTableModel model;
	private final int indexColumnEast;
    private final int indexColumnNorth;
    private final List<String> fieldsLabel;
    private final BooleanFormatter booleanFormatter;

    private int currentRow = 0;
    private int finalIndex = Integer.MAX_VALUE;

	public MapScisEditorHelper(Tab tab, View view, HttpServletRequest request, Messages errors) throws Exception {
		this.tab = tab;
		this.view = view;
		this.request = request;
		this.errors = errors;
		this.model = tab.getTableModel();//tab.getAllDataTableModel();
		this.metaModel = tab.getMetaTab().getMetaModel();//view.getMetaModel();
		this.entityClass = metaModel.getPOJOClass();
		this.tabViewableOnMap = entityClass.getAnnotation(TabViewableOnMap.class);
        this.booleanFormatter = new BooleanFormatter();
		if (tabViewableOnMap == null) {
			log.error("entity " + entityClass.getCanonicalName() + " cannot be displayed on a map cause it misses the annotation @" + TabViewableOnMap.class.getSimpleName());
			errors.add("missing_map_annotation");
	        this.indexColumnEast = -1;
	        this.indexColumnNorth = -1;
	        this.fieldsLabel = List.of();
	        return;
		}
        int[] indexColumnsCoord = getIndexColumnsCoord();
        this.indexColumnEast = indexColumnsCoord[0];
        this.indexColumnNorth = indexColumnsCoord[1];
        this.fieldsLabel = transformNonCoordColumns(model::getColumnName);
	}
	
	private int[] getIndexColumnsCoord() {
        int indexColumnEast = -1;
        int indexColumnNorth = -1;
        for (int h = 0 ; h < tab.getMetaProperties().size() ; h++) {
        	String columnName = tab.getMetaProperty(h).getName();
        	if (columnName.equals(getNameColumnEast())) {
        		indexColumnEast = h;
        	} else if (columnName.equals(getNameColumnNorth())) {
        		indexColumnNorth = h;
        	}
        }
        if (indexColumnEast == -1) {
        	errors.add("missing_coord_column", getNameColumnEast());
        }
        if (indexColumnNorth == -1) {
        	errors.add("missing_coord_column", getNameColumnNorth());
        }
        return new int[]{indexColumnEast, indexColumnNorth};
	}

	public boolean isViewableOnMap() {
		return tabViewableOnMap != null && indexColumnEast != -1 && indexColumnNorth != -1;
	}
	
	public Class<?> getAnnotationForViewableOnMap() {
		return TabViewableOnMap.class;
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public int getCoordsSystemEpsgNr() {
		return tabViewableOnMap == null ? -1 : tabViewableOnMap.coordsSystemEpsgNr();
	}
	
	public int getIndexColumnEast() {
		return indexColumnEast;
	}
	
	public int getIndexColumnNorth() {
		return indexColumnNorth;
	}
	
	public String getNameColumnEast() {
		return tabViewableOnMap.eastColumnName();
	}
	
	public String getNameColumnNorth() {
		return tabViewableOnMap.northColumnName();
	}

	public String getLabelColumnEast() {
		return model.getColumnName(indexColumnEast);
	}
	
	public String getLabelColumnNorth() {
		return model.getColumnName(indexColumnNorth);
	}

	public String getLabelOfField(int index) {
		return fieldsLabel.get(index);
	}
	
	public int getCurrentRow() {
		return currentRow - 1;
	}

	public Iterator<ItemWithCoordinates> iterator() {
		return this;
	}
	
	public Iterable<ItemWithCoordinates> iterateOverAll() {
		currentRow = 0;
		finalIndex = Integer.MAX_VALUE;
		return this;
	}
	
	public Iterable<ItemWithCoordinates> iterateFromTo(int initialIndex, int finalIndex) {
		currentRow = initialIndex;
		this.finalIndex = finalIndex;
		return this;
	}
	
	public boolean hasNext() {
		return currentRow < model.getRowCount() && currentRow < finalIndex;
	}

	public ItemWithCoordinates next() {
    	Number coordEast = (Number) model.getValueAt(currentRow, indexColumnEast);
    	Number coordNorth = (Number) model.getValueAt(currentRow, indexColumnNorth);
    	List<String> fields = transformNonCoordColumns(indexColumn -> format(indexColumn, model.getValueAt(currentRow, indexColumn)));
    	String cssStyle = tab.getStyle(currentRow);
    	currentRow++;
    	return new ItemWithCoordinates(coordEast, coordNorth, fields, cssStyle==null?"":cssStyle);
    }
	
	private <T> List<T> transformNonCoordColumns(Function<Integer, T> transformer) {
		int size = (indexColumnEast == -1 || indexColumnNorth == -1) ? model.getColumnCount() : model.getColumnCount() - 2;
		List<T> result = new ArrayList<>(size);
        for (int c = 0 ; c < model.getColumnCount() ; c++) {
        	if (c != indexColumnEast && c != indexColumnNorth) {
        		result.add(transformer.apply(c));
        	}
        }
        return result;
	}
	
	// copied from OpenXava's CardIterator
	private String format(int column, Object value) {
		MetaProperty p = tab.getMetaProperty(column);
		if (p.hasValidValues()) {
			return p.getValidValueLabel(value);
		}
		else if (p.getType().equals(boolean.class) || p.getType().equals(Boolean.class)) {
			return booleanFormatter.format(null, value);
		}
		else {
			return WebEditors.format(request, p, value, errors, view.getViewName(), true);
		}
	}
	
	public String sanitizeForJavascript(String input) {
		return input.replace("\\", "\\\\").replace("'", "\\'");
	}
	
	@RequiredArgsConstructor
	@Getter
	public static class ItemWithCoordinates {
		private final Number coordEast;
		private final Number coordNorth;
		private final List<String> fields;
		private final String cssStyle;
	}
}
