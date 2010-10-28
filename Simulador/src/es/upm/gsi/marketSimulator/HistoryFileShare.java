package es.upm.gsi.marketSimulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryFileShare extends Share {
	private FileReader fileReader;
	private Double values[];
	private int iteration = 0;
	
	HistoryFileShare (String companyName, String stockCategory, String path) {
		this.name = companyName;
		this.category = stockCategory;
		try {
			fileReader = new FileReader(path);
			values = readFile();
			setNextValue();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Double[] readFile() {
		//double values[] = new double[7000];
		List<Double> values = new ArrayList<Double>();
		//int num = 0;
		BufferedReader bufReader = new BufferedReader(fileReader);
		String line;
		try {
			while((line = bufReader.readLine()) != null) {
				//values[num] = Double.parseDouble(line.split(",")[1]);
				values.add(Double.parseDouble(line.split(",")[1]));
				//num++;
			}			
			System.out.println(values.size()+" lines read for "+name);			
		} catch (NumberFormatException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return values.toArray(new Double[values.size()]);
	}
	
	void setNextValue() {
		value = values[++iteration];
		addHistory((value - values[iteration-1])/values[iteration-1]);
	}

}