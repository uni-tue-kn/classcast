/*
 * Copyright 2018 University of Tuebingen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.regression.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import parser.CSVParser;

public class Forecast {
	
	private String[] header;

	private String[] colType;

	private String[] handle;

	private LinkedList<String[]> csvCells;

	private LinkedList<String[]> forecastedCsvCells;

	private final Boolean CASTED;
	
	public Forecast(String filepath) {
		CSVParser parser = new CSVParser();
		csvCells = parser.parseCsv(filepath);

		if (!parser.PARSED) {
			CASTED = null;
			return;
		}
		
		header = csvCells.pollFirst();
		String[] firstRow = csvCells.peekFirst();
		colType = new String[header.length];

		// Label the Columns with their respective datatype
		int i = 0;
		for (String string : firstRow) {
			try {
				// datatype is integer?
				Integer.parseInt(string);
				colType[i] = "Integer";
				i++;
			} catch (NumberFormatException e) {
				try {
					// if not integer, maybe double?
					Double.parseDouble(string);
					colType[i] = "Double";
					i++;
				} catch (Exception e2) {
					// then has to be string
					colType[i] = "String";
					i++;
				}
			}
		}
		// first Value of Row is must double
		colType[0] = "Double";

		try {
			for (String[] strings : csvCells) {
				if (strings.length != header.length)
					throw new IllegalArgumentException("Illegal rowlength at: " + Arrays.toString(strings));
				for (int j = 0; j < strings.length; j++) {
					if (colType[j] == "Integer")
						Integer.parseInt(strings[j]);
					else if (colType[j] == "Double")
						if (!(strings[j].isEmpty()))
							Double.parseDouble(strings[j]);
						else if (j != 0)
							throw new NullPointerException("NullPointerException at " + Arrays.toString(strings));
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			CASTED = false;
			return;
		}

		CASTED = true;
	}

	public void forecastRegProcess(int regLoc, double floatingInterval) {

		int h = 0;
		int[] doubleLoc = new int[colType.length];
		for (int j = 1; j < colType.length; j++) {
			if (colType[j] == "Double") {
				doubleLoc[h] = j;
				h++;
			}
		}
		doubleLoc = Arrays.copyOf(doubleLoc, h);

		for (int j = 0; j < forecastedCsvCells.size(); j++) {
			String[] currentRow = forecastedCsvCells.get(j);
			for (int k = 0; k < doubleLoc.length; k++) {
				if(doubleLoc[k]== regLoc) {
					continue;
				}
				double colValue = Double.parseDouble(currentRow[doubleLoc[k]]);
				double val = colValue / 0.3;
				double lowerBound = Math.round(Math.floor((val)) * floatingInterval * Math.pow(10, 4))
						/ Math.pow(10, 4);
				double upperBound = Math.round((Math.floor((val)) + 1) * floatingInterval * Math.pow(10, 4))
						/ Math.pow(10, 4);
				currentRow[doubleLoc[k]] = "(" + Double.toString(lowerBound) + "," + Double.toString(upperBound) + ")";
			}
			forecastedCsvCells.set(j, currentRow);
		}

		int[] ignoreCols = new int[handle.length];
		String[] replaceValues = new String[handle.length];
		int ignoreColsCount = 0;
		for (int i = 1; i < handle.length; i++) {
			if (handle[i] == "Ignore") {
				ignoreCols[ignoreColsCount] = i;
				replaceValues[ignoreColsCount] = forecastedCsvCells.peek()[i];
				ignoreColsCount++;
			}
		}
		if (ignoreColsCount != 0) {
			ignoreCols = Arrays.copyOfRange(ignoreCols, 0, ignoreColsCount);
		}

		Map<String, SimpleRegression> classRegressor = new HashMap<String, SimpleRegression>();
		for (String[] strings : forecastedCsvCells) {
			for (int i = 0; i < ignoreColsCount; i++) {
				strings[ignoreCols[i]] = replaceValues[i];
			}
			if (strings[0].isEmpty())
				continue;
			String factorComb;
			if (regLoc == 1) {
				factorComb = String.join("|", Arrays.copyOfRange(strings, 2, strings.length));
			} else if (regLoc == strings.length-1) {
				factorComb = String.join("|", Arrays.copyOfRange(strings, 1, strings.length - 1));
			} else {
				String leftPart = String.join("|", Arrays.copyOfRange(strings, 1, regLoc - 1));
				String rightPart = String.join("|", Arrays.copyOfRange(strings, regLoc + 1, strings.length));

				factorComb = leftPart + "|" + rightPart;
			}
			if (classRegressor.containsKey(factorComb)) {
				classRegressor.get(factorComb).addData(Double.parseDouble(strings[regLoc]),
						Double.parseDouble(strings[0]));
			} else {
				classRegressor.put(factorComb, new SimpleRegression(true));
				classRegressor.get(factorComb).addData(Double.parseDouble(strings[regLoc]),
						Double.parseDouble(strings[0]));
			}
//			System.out.println(factorComb);
		}

		try {
			for (String[] strings : forecastedCsvCells) {
				if (strings[0].isEmpty()) {
					String factorComb;
					if (regLoc == 1) {
						factorComb = String.join("|", Arrays.copyOfRange(strings, 2, strings.length));
					} else if (regLoc == strings.length-1) {
						factorComb = String.join("|", Arrays.copyOfRange(strings, 1, strings.length - 1));
					} else {
						String leftPart = String.join("|", Arrays.copyOfRange(strings, 1, regLoc - 1));
						String rightPart = String.join("|", Arrays.copyOfRange(strings, regLoc + 1, strings.length));

						factorComb = leftPart + "|" + rightPart;
					}
					strings[0] = Double.toString(classRegressor.get(factorComb).predict(Double.parseDouble(strings[regLoc])));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Abbruch!!");
			System.out.println(e.toString());
		}
//
//		// Show Data
//		for (String[] strings : csvCells) {
//			System.out.println(Arrays.toString(strings));
//		}
//		
//		// Show Data
//		for (String[] strings : forecastedCsvCells) {
//			System.out.println(Arrays.toString(strings));
//		}
//		
	}

	public void forecastMeanProcess(double floatingInterval) {

		int h = 0;
		int[] doubleLoc = new int[colType.length];
		for (int j = 1; j < colType.length; j++) {
			if (colType[j] == "Double") {
				doubleLoc[h] = j;
				h++;
			}
		}
		doubleLoc = Arrays.copyOf(doubleLoc, h);

		for (int j = 0; j < forecastedCsvCells.size(); j++) {
			String[] currentRow = forecastedCsvCells.get(j);
			for (int k = 0; k < doubleLoc.length; k++) {
				double colValue = Double.parseDouble(currentRow[doubleLoc[k]]);
				double val = colValue / 0.3;
				double lowerBound = Math.round(Math.floor((val)) * floatingInterval * Math.pow(10, 4))
						/ Math.pow(10, 4);
				double upperBound = Math.round((Math.floor((val)) + 1) * floatingInterval * Math.pow(10, 4))
						/ Math.pow(10, 4);
				currentRow[doubleLoc[k]] = "(" + Double.toString(lowerBound) + "," + Double.toString(upperBound) + ")";
			}
			forecastedCsvCells.set(j, currentRow);
		}

		int[] ignoreCols = new int[handle.length];
		String[] replaceValues = new String[handle.length];
		int ignoreColsCount = 0;
		for (int i = 1; i < handle.length; i++) {
			if (handle[i] == "Ignore") {
				ignoreCols[ignoreColsCount] = i;
				replaceValues[ignoreColsCount] = forecastedCsvCells.peek()[i];
				ignoreColsCount++;
			}
		}
		if (ignoreColsCount != 0) {
			ignoreCols = Arrays.copyOfRange(ignoreCols, 0, ignoreColsCount);
		}

		Map<String, List<Double>> classValues = new HashMap<String, List<Double>>();
		for (String[] strings : forecastedCsvCells) {
			for (int i = 0; i < ignoreColsCount; i++) {
				strings[ignoreCols[i]] = replaceValues[i];
			}
			if (strings[0].isEmpty())
				continue;
			String factorComb = String.join("|", Arrays.copyOfRange(strings, 1, strings.length));
			if (classValues.containsKey(factorComb)) {
				classValues.get(factorComb).add(Double.parseDouble(strings[0]));
			} else {
				classValues.put(factorComb, new LinkedList<Double>());
				classValues.get(factorComb).add(Double.parseDouble(strings[0]));
			}
		}

		Map<String, Double> classMean = new HashMap<String, Double>();
		for (String key : classValues.keySet()) {
			List<Double> values = classValues.get(key);
			double sum = 0;
			double div = 0;
			for (Double val : values) {
				sum += val;
				div++;
			}
			classMean.put(key, sum / div);
		}
		try {
			for (String[] strings : forecastedCsvCells) {
				if (strings[0].isEmpty()) {
					String factorComb = String.join("|", Arrays.copyOfRange(strings, 1, strings.length));
					strings[0] = classMean.get(factorComb).toString();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Abbruch!!");
			System.out.println(e.toString());
		}
//
//		// Show data
//		for (String[] strings : csvCells) {
//			System.out.println(Arrays.toString(strings));
//		}
//		
//		//Show forecasted data
//		for (String[] strings : forecastedCsvCells) {
//			System.out.println(Arrays.toString(strings));
//		}
	}

	public void forecast(boolean regMethod, int regLoc, double floatingInterval) {
		forecastedCsvCells = new LinkedList<>();
		for (String[] strings : csvCells) {
			forecastedCsvCells.add(strings.clone());
		}

		if (regMethod) {
			forecastRegProcess(regLoc, floatingInterval);
		} else {
			forecastMeanProcess(floatingInterval);
		}

	}

	public void validate(boolean regMethod, int regLoc, double validateDataSize, double floatingInterval) {
		forecastedCsvCells = new LinkedList<>();
		for (String[] strings : csvCells) {
			forecastedCsvCells.add(strings.clone());
		}
		for (int i = (int) (forecastedCsvCells.size() * validateDataSize); i < forecastedCsvCells.size(); i++) {
			forecastedCsvCells.get(i)[0] = "";
		}
//		for (String[] strings : forecastedCsvCells) {
//			System.out.println(Arrays.toString(strings));
//		}
		if (regMethod) {
			forecastRegProcess(regLoc, floatingInterval);
		} else {
			forecastMeanProcess(floatingInterval);
		}
	}


	public LinkedList<String[]> getCsvCells() {
		return csvCells;
	}

	public LinkedList<String[]> getForecastedCsvCells() {
		return forecastedCsvCells;
	}

	public String[] getHeader() {
		return header;
	}

	public String[] getColType() {
		return colType;
	}

	public Boolean isCASTED() {
		return CASTED;
	}

	public String[] getHandle() {
		return handle;
	}

	public void setHandle(String[] handle) {
		this.handle = handle;
	}
}
