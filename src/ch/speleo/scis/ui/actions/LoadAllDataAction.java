package ch.speleo.scis.ui.actions;

import org.openxava.actions.TabBaseAction;
import org.openxava.tab.impl.IXTableModel;

public class LoadAllDataAction extends TabBaseAction {
	
	public void execute() throws Exception {
		IXTableModel allData = getTab().getAllDataTableModel();
		getTab().setTableModel(allData);
	}

}
