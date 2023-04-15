package ch.speleo.scis.business.imports;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.DataReader;

public class ExcelReader extends DataReader {
	
	private static final Log logger = LogFactory.getLog(ExcelReader.class);
	
	private File inputFile;
	private Workbook workbook;
	private int nbHeaderRows;
	
	public ExcelReader(File inputFile) {
		this.inputFile = inputFile;
	}
	public ExcelReader(File inputFile, int nbHeaderRows) {
		this.inputFile = inputFile;
		this.nbHeaderRows = nbHeaderRows;
	}
	public ExcelReader(String inputFileName) {
		this.inputFile = new File(inputFileName);
	}
	public ExcelReader(String inputFileName, int nbHeaderRows) {
		this.inputFile = new File(inputFileName);
		this.nbHeaderRows = nbHeaderRows;
	}

	@Override
	public Iterator<String[]> open() throws Exception {
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setEncoding("Cp1252");
        logger.debug(this.getClass().getSimpleName()+" is importing file "+inputFile.getCanonicalPath());
        try {
        	workbook = Workbook.getWorkbook(inputFile, workbookSettings);
    	} catch (IOException e) {
    		throw new IOException("error reading "+inputFile.getCanonicalPath(), e);
    	}
        final Sheet sheet = workbook.getSheet(0);
        final int nbRows = sheet.getRows();
        Iterator<String[]> iterator = new Iterator<String[]>() {
        	private int iRow = nbHeaderRows;

			public boolean hasNext() {
				return iRow < nbRows;
			}

			public String[] next() {
				Cell[] inRow = sheet.getRow(iRow);
				String[] outRow = new String[inRow.length];
				for (int iCell = 0 ; iCell < inRow.length ; iCell++) {
					if (inRow[iCell] != null) {
						outRow[iCell] = inRow[iCell].getContents();
					}
				}
				iRow++;
				return outRow;
			}

			public void remove() {
				throw new UnsupportedOperationException("can't remove because this reader is read-only");
			}
        	
        };
        return iterator;
	}

	@Override
	public void close() {
		workbook.close();
	}

	@Override
	public String getDatasourceName() {
		return inputFile.getName();
	}

}
