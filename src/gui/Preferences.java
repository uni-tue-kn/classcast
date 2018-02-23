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

package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Preferences {
	
	private double interval = 0.3;
	
	private char seperator = ';';
	
	public static Stage windowPreferences() throws Exception {

		GridPane gridPane = new GridPane();
		gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		Text floatingText = new Text("Interval for floating numbers:");
		Text seperatorText = new Text("Seperator on export:");
		floatingText.setFont(Font.font("Lucida Sans", 13));
		seperatorText.setFont(Font.font("Lucida Sans", 13));

		TextField floatingField = new TextField();
		TextField seperatorField = new TextField();

		gridPane.add(floatingText, 0, 0, 1, 1);
		gridPane.add(floatingField, 1, 0, 1, 1);
		gridPane.add(seperatorText, 0, 1, 1, 1);
		gridPane.add(seperatorField, 1, 1, 1, 1);

		Stage prefWindow = new Stage();
		prefWindow.setScene(new Scene(gridPane, 300, 300));

		prefWindow.setTitle("Preferences");
		return(prefWindow);
	}
	
//	public void showPrefereces() {
//		prefWindow.showAndWait();
//		return;
//	}
}
