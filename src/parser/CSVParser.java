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

package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CSVParser {

	String[] csvSplitBy = { ";", ",", "\t", "|" };

	public Boolean PARSED;

	public boolean isPARSED() {
		return PARSED;
	}

	public LinkedList<String[]> parseCsv(String filepath) {
		BufferedReader br = null;
		LinkedList<String[]> csvCells = new LinkedList<String[]>();

		for (String delimiter : csvSplitBy) {
			try {
				br = new BufferedReader(new FileReader(filepath));
				String line;
				int cellLength = 0;
				boolean header = true;
				while ((line = br.readLine()) != null) {
					if (line.length() > 1) {
						String[] seperatedLine = line.split(delimiter);
						if (seperatedLine.length > 1) {
							if (header) {
								cellLength = seperatedLine.length;
								csvCells.add(seperatedLine);
								header = false;
							} else if (seperatedLine.length == cellLength) {
								csvCells.add(seperatedLine);
							} else {
								csvCells = new LinkedList<String[]>();
								break;
							}
						}
					}
				}
				if (csvCells.isEmpty()) {
					continue;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		report(csvCells);
		PARSED = csvCells.isEmpty() ? false : true ;
		return (csvCells);
	}

	public void report(List<String[]> text) {
		for (String[] strings : text) {
			System.out.println(Arrays.toString(strings));
		}

	}

}
