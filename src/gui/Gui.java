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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import tool.Forecast;

public class Gui extends Application implements EventHandler<ActionEvent> {

	Button forecastButton;

	CheckBox validateBox;

	MenuBar menuBar;

	Scene currentScene;

	File currentFile;

	Stage window;

	Slider slider;

	GridPane gridPane;

	Forecast forecaster;

	Stage preferences;

	char seperator = ';';

	double floatingInterval = 0.5;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;

		window.setTitle("ClassCast");

		window.setMinHeight(420);
		window.setMinWidth(600);
		window.setHeight(420);
		window.setWidth(600);
		currentScene = initLayout();
		window.setScene(currentScene);
		window.show();
	}

	@Override
	public void handle(ActionEvent event) {
		// If Open is clicked:
		if (event.getSource() == this.menuBar.getMenus().get(0).getItems().get(0)) {
			fileChooser();
			if (currentFile != null) {
				Forecast cast = new Forecast(currentFile.getAbsolutePath());
				if (cast.isCASTED() != null) {
					if (cast.isCASTED()) {
						forecaster = cast;
						// make new scene
						this.window.setMaximized(this.window.isMaximized());
						this.window.setScene(currentScene = fileInputLayout(cast));

					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error Dialog");
						alert.setHeaderText("Type Violation");
						alert.setContentText("There is a type violation in the given data!");

						alert.showAndWait();
					}
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Dialog");
					alert.setHeaderText("Format Error");
					alert.setContentText("The Format of the given file is inconsistent!");

					alert.showAndWait();
				}
			}
			this.window.setMaximized(true);
		}
		// If export is clicked...
		if (event.getSource() == this.menuBar.getMenus().get(0).getItems().get(1)) {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Export forecasted data to file");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV and Text Files", "*.txt", "*.csv"),
					new ExtensionFilter("All Files", "*.*"));
			currentFile = fileChooser.showSaveDialog(this.window);
			if (currentFile != null) {
				LinkedList<String[]> data = forecaster.getCsvCells();
				LinkedList<String[]> forecast = forecaster.getForecastedCsvCells();
				int lenRow = forecaster.getHeader().length;
				String sep = Character.toString(seperator);

				try {
					PrintWriter pw = new PrintWriter(currentFile);
					for (int i = 0; i < forecast.size(); i++) {
						String leftPart = forecast.get(i)[0];
						String rightPart = String.join(sep, Arrays.copyOfRange(data.get(i), 1, lenRow));
						pw.write(leftPart + sep + rightPart + "\n");
					}
					pw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Saving failed!");
					alert.setHeaderText("Saving data to file failed!");
					alert.setContentText("Unfortunatly saving the forecasted data to the given file has failed.");

					alert.showAndWait();
				}
			}
		}

		if (event.getSource() == this.menuBar.getMenus().get(1).getItems().get(0)) {
			GridPane prefrencesGridPane = new GridPane();
			prefrencesGridPane = new GridPane();
			prefrencesGridPane.setHgap(10);
			prefrencesGridPane.setVgap(5);
			prefrencesGridPane.setPadding(new Insets(10, 10, 10, 10));

			Text floatingText = new Text("Interval for floating numbers:");
			Text seperatorText = new Text("Seperator on export:");
			floatingText.setFont(Font.font("Lucida Sans", 13));
			seperatorText.setFont(Font.font("Lucida Sans", 13));

			Spinner floatingSpinner = new Spinner(0.1, 1, floatingInterval, 0.1);
			TextField seperatorField = new TextField(Character.toString(seperator));
			seperatorField.autosize();
			seperatorField.setTextFormatter(new TextFormatter<String>((Change change) -> {
				String newText = change.getControlNewText();
				if (newText.length() > 1) {
					return null;
				} else {
					return change;
				}
			}));

			prefrencesGridPane.add(floatingText, 0, 0, 1, 1);
			prefrencesGridPane.add(floatingSpinner, 1, 0, 1, 1);
			prefrencesGridPane.add(seperatorText, 0, 1, 1, 1);
			prefrencesGridPane.add(seperatorField, 1, 1, 1, 1);

			preferences = new Stage();
			preferences.setScene(new Scene(prefrencesGridPane, 300, 100));

			preferences.setTitle("Preferences");
			preferences.initModality(Modality.APPLICATION_MODAL);
			preferences.initOwner(window);
			preferences.showAndWait();
			floatingInterval = (double) floatingSpinner.getValue();
			seperator = seperatorField.getText().charAt(0);
		}

		if (event.getSource() == forecastButton) {
			String[] comboBoxContent = new String[forecaster.getHeader().length];
			ObservableList<Node> childrens = gridPane.getChildren();
			for (int i = 1; i < forecaster.getHeader().length; i++) {
				for (Node node : childrens) {
					if (gridPane.getRowIndex(node) == i && gridPane.getColumnIndex(node) == 1) {
						comboBoxContent[i] = ((ComboBox) node).getSelectionModel().getSelectedItem().toString();
						break;
					}
				}
			}
			boolean singleRegressor = false;
			int regLoc = Integer.MIN_VALUE;
			for (int i = 1; i < comboBoxContent.length; i++) {
				if (comboBoxContent[i] == "Regressor") {
					if (forecaster.getColType()[i] != "String") {
						if (!singleRegressor) {
							regLoc = i;
							singleRegressor = true;
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Multiple Regressor selected!");
							alert.setHeaderText("Multiple Regressor selected!");
							alert.setContentText("Only one independent variable can be selected\nas \"Regressor\"");

							alert.showAndWait();
							return;
						}
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Invalid Regressor selected!");
						alert.setHeaderText("Invalid Regressor selected!");
						alert.setContentText("Regressor must be numeric.");

						alert.showAndWait();
						return;
					}
				}
			}
			forecaster.setHandle(comboBoxContent);

			if (validateBox.isSelected()) {
				double validateDataSize = slider.getValue() / 100;

				XYChart.Series originalSeries = new XYChart.Series();
				try {
					originalSeries.setName("original series");
					// populating the series with original data

					LinkedList<String[]> data = forecaster.getCsvCells();
					for (int i = 0; i < data.size(); i++) {
						originalSeries.getData().add(new XYChart.Data(i, Double.parseDouble(data.get(i)[0])));
					}
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Data incomplete!");
					alert.setHeaderText("The dataset is incomplete!");
					alert.setContentText("Validating requiers a complete dataset.");

					alert.showAndWait();
					return;
				}
				forecaster.validate(singleRegressor, regLoc, validateDataSize, floatingInterval);
				NumberAxis xAxis = new NumberAxis();
				NumberAxis yAxis = new NumberAxis();
				xAxis.setLabel("Time-Index");
				yAxis.setLabel(forecaster.getHeader()[0]);
				// creating the chart
				LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

				lineChart.setTitle("Validation of: " + currentFile.getName()); // defining a series

				XYChart.Series series = new XYChart.Series();
				series.setName("forcasted series");
				// populating the series with data

				LinkedList<String[]> data = forecaster.getForecastedCsvCells();
				LinkedList<String[]> history = forecaster.getCsvCells();
				double sumPow2Deviation = 0;
				try {
					for (int i = (int) (data.size() * validateDataSize); i < data.size(); i++) {
						series.getData().add(new XYChart.Data(i, Double.parseDouble(data.get(i)[0])));
						sumPow2Deviation += Math
								.pow(Double.parseDouble(data.get(i)[0]) - Double.parseDouble(history.get(i)[0]), 2);
					}
				} catch (Exception e) {

					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Given data is insufficient for forecast ");
					alert.setHeaderText("The given dataset is insufficient to make a forecast!");
					alert.setContentText("Some of the classifications have no data.\n"
							+ "This can be solved by ignoring certain independent variables.");

					alert.showAndWait();
					return;

				}

				Scene scene = new Scene(lineChart, 800, 600);

				// XYChart.Series verticalLine = new XYChart.Series();
				// verticalLine.setName("delimiter");
				// verticalLine.getData().add(new XYChart.Data(1000, 100));
				// verticalLine.getData().add(new XYChart.Data(1000, 0));
				// verticalLine.setName("delimiter");
				lineChart.setCreateSymbols(false);
				lineChart.getData().addAll(originalSeries, series);

				lineChart.getStylesheets().add("LineChart.css");
				//
				// xAxis.setAutoRanging(false);
				// xAxis.setLowerBound(5000);
				// xAxis.setUpperBound(7000);
				// xAxis.setTickUnit(200);
				//
				//
				// yAxis.setAutoRanging(false);
				// yAxis.setLowerBound(0);
				// yAxis.setUpperBound(100);
				// yAxis.setTickUnit(25);

				ObservableList<Node> nodes = this.window.getScene().getRoot().getChildrenUnmodifiable();
				TabPane currentTabPane = null;
				for (Node child : nodes) {
					if (child instanceof TabPane) {
						currentTabPane = (TabPane) child;
					}
				}
				StackPane stackPane = new StackPane();

				Text sumPow2DeviationText = new Text(
						"Sum of squared deviation:\n" + Double.toString(Math.floor(sumPow2Deviation * 10000) / 10000));

				stackPane.getChildren().add(lineChart);
				sumPow2DeviationText.setLayoutX(30);
				sumPow2DeviationText.setLayoutY(30);
				sumPow2DeviationText.setX(1);
				sumPow2DeviationText.setY(1);
				sumPow2DeviationText.setTextAlignment(TextAlignment.CENTER);
				stackPane.setMargin(sumPow2DeviationText, new Insets(0, 20, 28, 0));
				stackPane.getChildren().add(sumPow2DeviationText);
				stackPane.setAlignment(sumPow2DeviationText, Pos.BOTTOM_RIGHT);
				try {
					currentTabPane.getTabs().get(1).setContent(stackPane);
					SingleSelectionModel<Tab> selectionModel = currentTabPane.getSelectionModel();
					selectionModel.select(1);

					// Legend legend = (Legend) findNode(lineChart, Legend.class.getName(),
					// "chart-legend");
					// for (final Node legendItem : legend.getChildren()) {
					//
					// final Label legendLabel = (Label) legendItem;
					//
					// if (0 == legendLabel.getText().compareToIgnoreCase("the name of the legend I
					// want hidden (or replaced with some other test)")) {
					// legend.getChildren().remove(legendItem);
					// break;
					// }
					// }

				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {

				forecaster.forecast(singleRegressor, regLoc, floatingInterval);

				NumberAxis xAxis = new NumberAxis();
				NumberAxis yAxis = new NumberAxis();
				xAxis.setLabel("Time-Index");
				yAxis.setLabel(forecaster.getHeader()[0]);
				// creating the chart
				LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
				lineChart.getStylesheets().add("LineChart.css");
				lineChart.setTitle("Forecast of: " + currentFile.getName());
				// defining a series
				XYChart.Series series = new XYChart.Series();
				series.setName("original series");
				XYChart.Series forecastedSeries = new XYChart.Series();
				forecastedSeries.setName("forcasted series");
				// populating the series with data

				LinkedList<String[]> data = forecaster.getCsvCells();
				LinkedList<String[]> newData = forecaster.getForecastedCsvCells();
				boolean connected = false;
				try {
					for (int i = 0; i < data.size(); i++) {
						if (data.get(i)[0].isEmpty()) {
							if (!connected) {
								forecastedSeries.getData()
										.add(new XYChart.Data(i - 1, Double.parseDouble(newData.get(i - 1)[0])));
								connected = true;
							}
							forecastedSeries.getData().add(new XYChart.Data(i, Double.parseDouble(newData.get(i)[0])));
						} else {
							series.getData().add(new XYChart.Data(i, Double.parseDouble(data.get(i)[0])));
						}
					}
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Data incomplete!");
					alert.setHeaderText("The dataset is incomplete!");
					alert.setContentText("Some factorcombination are missing!");

					alert.showAndWait();
					return;
				}
				Scene scene = new Scene(lineChart, 800, 600);
				lineChart.setCreateSymbols(false);
				lineChart.getData().addAll(series, forecastedSeries);
				ObservableList<Node> nodes = this.window.getScene().getRoot().getChildrenUnmodifiable();
				TabPane currentTabPane = null;
				for (Node child : nodes) {
					if (child instanceof TabPane) {
						currentTabPane = (TabPane) child;
					}
				}
				try {

					currentTabPane.getTabs().get(1).setContent(lineChart);
					SingleSelectionModel<Tab> selectionModel = currentTabPane.getSelectionModel();
					selectionModel.select(1);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			menuBar.getMenus().get(0).getItems().get(1).setDisable(false);
		}
	}

	public Scene initLayout() {
		currentFile = null;
		forecaster = null;
		preferences = null;
		menuBar = new MenuBar();
		BorderPane borderPane = new BorderPane();

		Menu fileMenu = new Menu("File");
		// create the menu items
		MenuItem open = new MenuItem("Open");
		// MenuItem openRecent = new MenuItem("Open Recent");
		open.setOnAction(this);
		MenuItem export = new MenuItem("Export");
		export.setDisable(true);
		export.setOnAction(this);

		MenuItem close = new MenuItem("Close");
		close.setOnAction(e -> this.window.setScene(initLayout()));
		close.setDisable(true);

		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(e -> Platform.exit());

		fileMenu.getItems().addAll(open,
				// openRecent,
				export, close, exit);

		Menu editMenu = new Menu("Edit");
		MenuItem preferences = new MenuItem("Preferences");
		preferences.setOnAction(this);

		editMenu.getItems().add(preferences);

		Menu runMenu = new Menu("Run");
		MenuItem startForecast = new MenuItem("Start Forecast");
		startForecast.setDisable(true);
		startForecast.setOnAction(e -> forecastButton.fire());
		runMenu.getItems().add(startForecast);

		Menu helpMenu = new Menu("Help");
		MenuItem about = new MenuItem("About");
		helpMenu.getItems().add(about);

		menuBar.getMenus().addAll(fileMenu, editMenu, runMenu, helpMenu);
		borderPane.setTop(menuBar);

		return (new Scene(borderPane, window.getWidth(), window.getHeight()));
	}

	public Scene fileInputLayout(Forecast data) {

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(menuBar);

		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab varTab = new Tab();
		varTab.setText("Independent Variables");
		Tab visTab = new Tab();
		visTab.setText("Visualization");

		ButtonBar buttonBar = new ButtonBar();
		forecastButton = new Button();
		forecastButton.setText("Start Forecast");
		ButtonBar.setButtonData(forecastButton, ButtonData.RIGHT);
		forecastButton.setOnAction(this);
		validateBox = new CheckBox("Validate");
		validateBox.setOnAction(e -> slider.setVisible(!slider.isVisible()));

		buttonBar.getButtons().add(forecastButton);
		buttonBar.setPadding(new Insets(10));

		slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(80);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(4);
		slider.setBlockIncrement(10);
		slider.setVisible(false);

		gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		Text firstText = new Text("Independent Variable Name");
		Text secondText = new Text("Use as");
		firstText.setFont(Font.font("Lucida Sans", FontWeight.BOLD, 13));
		secondText.setFont(Font.font("Lucida Sans", FontWeight.BOLD, 13));
		gridPane.add(firstText, 0, 0, 1, 1);
		gridPane.add(secondText, 1, 0, 1, 1);

		String[] header = data.getHeader();
		String[] colType = data.getColType();
		for (int i = 1; i < header.length; i++) {
			gridPane.add(new Text(header[i]), 0, i, 1, 1);
			ComboBox tmpComboBox = new ComboBox();
			tmpComboBox.getItems().addAll("Classifier", "Regressor", "Ignore");
			if (colType[i] == "Double")
				tmpComboBox.setValue("Regressor");
			else
				tmpComboBox.setValue("Classifier");
			gridPane.add(tmpComboBox, 1, i, 1, 1);

		}
		gridPane.add(validateBox, 0, header.length);
		gridPane.add(slider, 0, forecaster.getHeader().length + 1);

		Text sliderText = new Text(String.valueOf(String.valueOf((int) slider.getValue())));
		sliderText.setVisible(false);
		slider.valueProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				sliderText.textProperty().setValue(String.valueOf((int) slider.getValue()));
			}
		});
		slider.visibleProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				sliderText.setVisible(!sliderText.isVisible());
			}
		});
		gridPane.add(sliderText, 1, forecaster.getHeader().length + 1);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setPrefSize(300, 300);
		scrollPane.setContent(gridPane);
		VBox vbox = new VBox();
		vbox.getChildren().addAll(scrollPane, buttonBar);
		varTab.setContent(vbox);
		tabPane.getTabs().addAll(varTab, visTab);
		borderPane.setCenter(tabPane);

		menuBar.getMenus().get(2).getItems().get(0).setDisable(false);
		menuBar.getMenus().get(0).getItems().get(2).setDisable(false);

		window.setMaximized(window.isMaximized());
		Scene scene = new Scene(borderPane, window.getWidth(), window.getHeight(), Color.WHITE);
		return scene;

	}

	public void fileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV and Text Files", "*.txt", "*.csv"),
				new ExtensionFilter("All Files", "*.*"));
		currentFile = fileChooser.showOpenDialog(window);
	}

}
