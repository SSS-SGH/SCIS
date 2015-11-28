package ch.speleo.scis.business.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.speleo.scis.business.imports.EntitiesReader.DataReader;

public class TabulatedTextReader extends DataReader {
	
	private static final Log logger = LogFactory.getLog(TabulatedTextReader.class);
	
	private File inputFile;
	private Charset inputCharset;
	BufferedReader reader;
	
	public TabulatedTextReader(File inputFile) {
		this.inputFile = inputFile;
	}
	public TabulatedTextReader(File inputFile, String inputCharset) {
		this(inputFile);
		this.inputCharset = Charset.forName(inputCharset);
	}
	public TabulatedTextReader(String inputFileName) {
		this(new File(inputFileName));
	}
	public TabulatedTextReader(String inputFileName, String inputCharset) {
		this(new File(inputFileName), inputCharset);
	}


	@Override
	public Iterator<String[]> open() throws Exception {
		if (inputCharset == null)
			reader = new BufferedReader(new FileReader(inputFile));
		else
			reader = new BufferedReader(new  InputStreamReader(new FileInputStream(inputFile), inputCharset));
        Iterator<String[]> iterator = new Iterator<String[]>() {
        	private String currentLine = null;
        	private String nextLine = reader.readLine();

			public boolean hasNext() {
				return nextLine != null;
			}

			public String[] next() {
				currentLine = nextLine;
				try {
					nextLine = reader.readLine();
				} catch (IOException e) {
					logger.error("error while reading "+inputFile.toString(), e);
					throw new RuntimeException(e);
				}
				return currentLine.split("\t", -1);
			}

			public void remove() {
				throw new UnsupportedOperationException("can't remove because this reader is read-only");
			}
        	
        };
        return iterator;
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}

	@Override
	public String getDatasourceName() {
		return inputFile.getName();
	}

}
