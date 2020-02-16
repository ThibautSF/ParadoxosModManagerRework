package com.pmm.ParadoxosGameModManager;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pmm.ParadoxosGameModManager.debug.ErrorPrint;
import com.pmm.ParadoxosGameModManager.mod.Mod;
import com.pmm.ParadoxosGameModManager.mod.ModList;
import com.pmm.ParadoxosGameModManager.window.BasicDialog;
import com.pmm.ParadoxosGameModManager.window.WorkIndicatorDialog;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class ListManager extends Stage {
	// Window Var
	private static int WINDOW_WIDTH = 800;
	private static int WINDOW_HEIGHT = 600;
	private GridPane window = new GridPane();

	private VBox menu = new VBox();
	private String pthModstr = "Path : %s";
	private Label pthModLbl = new Label(pthModstr);
	private String nbModstr = "Mod(s) found : %d";
	private Label nbModLbl = new Label(nbModstr);

	private HBox actionsBox = new HBox(8);
	private Button buttonRefresh = new Button();
	private Button buttonBack = new Button();

	private VBox yrListsBox = new VBox();
	private String lblYrLists = "Your lists (%d found)";
	private Label yourLists = new Label(lblYrLists);

	private VBox content = new VBox();
	private TableView<ModList> lists = new TableView<>();
	private TableColumn<ModList, String> listNameCol = new TableColumn<>("List Name");
//	private TableColumn<ModList,String> listDescrCol = new TableColumn<ModList,String>("Description");
	private TableColumn<ModList, String> languageCol = new TableColumn<>("Language");
	private TableColumn<ModList, Integer> nbModCol = new TableColumn<>("NB");
	private TableColumn<ModList, String> modOrderCol = new TableColumn<>("Order");

	private ObservableList<ModList> listOfLists = FXCollections.observableArrayList();
//	private ObservableList<ModList> selectedListsList = FXCollections.observableArrayList();

	private HBox buttons = new HBox(8);
	private Button newList = new Button("New");
	private Button modifyList = new Button("Modify");
	private Button delList = new Button("Delete");
	private Button applyList = new Button("Apply");
	private HBox buttons2 = new HBox(8);
	private Button exportList = new Button("Export");
	private Button importList = new Button("Import");

	// Local Var
	private File gameDir;
	private String absolutePath;
	private Map<String, Mod> availableMods = new HashMap<>();
	private List<ModList> userListArray = new ArrayList<>();
	private WorkIndicatorDialog<String> wd = null;

	/**
	 * @throws FileNotFoundException
	 * @throws Exception
	 *
	 */
	public ListManager(String path) throws FileNotFoundException {
		gameDir = new File(path);
		absolutePath = gameDir.getAbsolutePath();

		setTitle(ModManager.APP_NAME + " : " + ModManager.GAME);

		window.setHgap(8);
		window.setVgap(8);
		window.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		window.setPadding(new Insets(0, 0, 5, 0));

		// Uncomment when editing window to see cells
//		window.setGridLinesVisible(true);

		RowConstraints row1 = new RowConstraints(40, 40, 40);
		RowConstraints row2 = new RowConstraints(25, 25, 25);
		RowConstraints row3 = new RowConstraints();
		row3.setMaxHeight(Double.MAX_VALUE);
		row3.setVgrow(Priority.ALWAYS);
		content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		VBox.setVgrow(lists, Priority.ALWAYS);
		RowConstraints row4 = new RowConstraints(30, 30, 30);
		RowConstraints row5 = new RowConstraints(30, 30, 30);
		window.getRowConstraints().addAll(row1, row2, row3, row4, row5);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(0);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(25);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(25);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setPercentWidth(25);
		ColumnConstraints col5 = new ColumnConstraints();
		col5.setPercentWidth(25);
		ColumnConstraints col6 = new ColumnConstraints();
		col6.setPercentWidth(0);
		window.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

		// ListManager Top
		window.add(menu, 0, 0, 6, 1);
		menu.setStyle("-fx-background-color: #EAE795;");
		menu.getChildren().addAll(pthModLbl, nbModLbl);
		pthModLbl.setText(String.format(pthModstr, absolutePath));

		refreshTexts();

		window.add(actionsBox, 4, 0);
		buttonRefresh.setGraphic(new FontIcon(FontAwesomeSolid.REDO_ALT));
		buttonBack.setGraphic(new FontIcon(FontAwesomeSolid.LONG_ARROW_ALT_LEFT));
		actionsBox.setAlignment(Pos.CENTER_RIGHT);
		actionsBox.getChildren().addAll(buttonRefresh, buttonBack);

		buttonRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					loadModFilesArray();
				} catch (Exception e) {
					ErrorPrint.printError(e, "Refresh");
				}
			}// end action
		});

		buttonBack.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				Node source = (Node) t.getSource();
				Stage stage = (Stage) source.getScene().getWindow();
				stage.close();
				try {
					new ModManager(true);
				} catch (Exception e) {
					ErrorPrint.printError(e, "Reload Game");
				}
			}// end action
		});

		// ModList "Your mods" field
		window.add(yrListsBox, 1, 1, 4, 1);
		yrListsBox.getChildren().add(yourLists);
		yrListsBox.setStyle("-fx-alignment: center;");
		yourLists.setStyle("-fx-font: bold 20 serif;");

		// Center
		window.add(content, 1, 2, 4, 1);
		content.getChildren().add(lists);
		content.setStyle("-fx-alignment: center;");

		listNameCol.setSortable(true);
		languageCol.setSortable(true);
		nbModCol.setSortable(true);
		modOrderCol.setSortable(true);
		lists.getColumns().add(listNameCol);
		lists.getColumns().add(languageCol);
		lists.getColumns().add(nbModCol);
		lists.getColumns().add(modOrderCol);

//		listOfLists.addListener(new ListChangeListener<ModList>() {
//			@Override
//			public void onChanged(Change<? extends ModList> c) {
//				// TODO Improve sorting ? → maybe remake all the base of PMM...
//				System.out.println("La liste a changé");
//				System.out.println(c.toString());
//			}
//		});

		listNameCol.setCellValueFactory(new PropertyValueFactory<ModList, String>("name"));
		listNameCol.setMinWidth(300);

		languageCol.setCellValueFactory(
				cell -> new SimpleStringProperty(cell.getValue().getLanguageName().toUpperCase(Locale.ENGLISH)));

		nbModCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getModlist().size()).asObject());

		modOrderCol.setCellValueFactory(cell -> {
			boolean customOrder = cell.getValue().isCustomOrder();
			String orderStr = "Default";
			if (customOrder) {
				orderStr = "Custom";
			}
			return new ReadOnlyStringWrapper(orderStr);
		});

		lists.setRowFactory(tv -> {
			TableRow<ModList> row = new TableRow<>() {
//				@Override
//				protected void updateItem(ModList item, boolean empty) {
//					super.updateItem(item, empty);
//					if (item == null) {
//						setStyle("");
//					} else if (selectedListsList.contains(item)) {
//						setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50;");
//					} else {
//						setStyle("");
//					}
//				}
			};

			row.setOnMouseClicked(event -> {
				int pos = row.getIndex();

				// Enable/Disable buttons which need a selected list
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
					if (pos >= 0) {
						modifyList.setDisable(false);
						delList.setDisable(false);
						applyList.setDisable(false);
						exportList.setDisable(false);
					} else {
						modifyList.setDisable(true);
						delList.setDisable(true);
						applyList.setDisable(true);
						exportList.setDisable(true);
					}
				}
			});

			return row;
		});

		// fixed width for buttons
		newList.setPrefWidth(75);
		modifyList.setPrefWidth(75);
		delList.setPrefWidth(75);
		applyList.setPrefWidth(75);
		importList.setPrefWidth(75);
		exportList.setPrefWidth(75);

		// Buttons line 1
		window.add(buttons, 1, 3, 4, 1);
		buttons.setStyle("-fx-alignment: bottom-center;");
		buttons.getChildren().addAll(newList, modifyList, delList, applyList);
		modifyList.setDisable(true);
		delList.setDisable(true);
		applyList.setDisable(true);

		// Buttons line 2
		window.add(buttons2, 1, 4, 4, 1);
		buttons2.setStyle("-fx-alignment: top-center;");
		buttons2.getChildren().addAll(importList, exportList);
		exportList.setDisable(true);

		Scene sc = new Scene(window, WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setScene(sc);
		this.setMinHeight(WINDOW_HEIGHT);
		this.setMinWidth(WINDOW_WIDTH);
		this.show();

		// Load the list of mod files
		loadModFilesArray();

		Stage stage = (Stage) window.getScene().getWindow();
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldB, Boolean newB) {
				if (newB.booleanValue()) {
					// Window focus
					try {
						updateList();
						refreshTexts();
					} catch (Exception e) {
						ErrorPrint.printError(e, "When update ListView of ModLists on window focus");
						e.printStackTrace();
					}
				} else {
					// Window unfocus
				}
			}
		});

		newList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				new ListCreator(path, availableMods);
			}// end action
		});

		modifyList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				int pos = lists.getSelectionModel().getSelectedIndex();
				ModList toModify = lists.getSelectionModel().getSelectedItem();
				try {
					new ListCreator(path, availableMods, toModify);
				} catch (Exception e) {
					if (pos == -1) {
						ErrorPrint.printError(e, "User try to enter in list modification without selecting a list");
					} else {
						ErrorPrint.printError(e, "When enter in modification of a list");
					}
					e.printStackTrace();
				}
			}// end action
		});

		Alert alertConfirm = new Alert(AlertType.CONFIRMATION);
		alertConfirm.setTitle("Confirmation");
		alertConfirm.setHeaderText("Confirm !");

		delList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				int pos = lists.getSelectionModel().getSelectedIndex();
				ModList toDelete = lists.getSelectionModel().getSelectedItem();
				alertConfirm.setContentText("Are you ok to delete '" + toDelete.getName() + "' ?");

				Optional<ButtonType> result = alertConfirm.showAndWait();
				if (result.get() == ButtonType.OK) {
					try {
						ModManager.userlistsJSON.readFile(ModManager.GAME_LIST_STORAGE_FILE.getAbsolutePath());
						ModManager.userlistsJSON.removeList(toDelete.getName());
						updateList();
						refreshTexts();
					} catch (Exception e) {
						if (pos == -1) {
							ErrorPrint.printError(e, "User try to delete a list without selecting a list");
						} else {
							ErrorPrint.printError(e, "When trying to delete a list");
						}
						e.printStackTrace();
					}
				}
			}// end action
		});

		applyList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				ModList toApply = lists.getSelectionModel().getSelectedItem();
				String strInfo = "\n\nWARNING:\nThe new Paradox Launcher could show another order than the one you just apply. Don’t worry, it should be purely cosmetic.\n"
						+ "Don’t change anything in the launcher after it opens, just click the play button. That should preserve the load order generated by Paradoxos Mod Manager.";
				alertConfirm.setContentText("Are you ok to apply '" + toApply.getName() + "' ?" + strInfo);

				Optional<ButtonType> result = alertConfirm.showAndWait();
				if (result.get() == ButtonType.OK) {
					try {
						if (applyModList(toApply)) {
							Alert alertInfo = new Alert(AlertType.CONFIRMATION);
							alertInfo.setTitle("Success");
							alertInfo.setHeaderText(null);
							alertInfo.setContentText("The list was successfully applied !");

							ButtonType buttonTypeLaunchGame = new ButtonType("Launch Game");
							ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

							alertInfo.getButtonTypes().setAll(buttonTypeLaunchGame, buttonTypeCancel);

							Optional<ButtonType> resultInfo = alertInfo.showAndWait();
							if (resultInfo.get() == buttonTypeLaunchGame) {
								if (Desktop.isDesktopSupported()) {
									new Thread(() -> {
										try {
											String uristr = "steam://run/" + ModManager.STEAM_ID;
											if (toApply.getLaunchArgs().length() > 0) {
												uristr += "//" + toApply.getLaunchArgs();
											}
											URI uri = new URI(uristr);
											Desktop.getDesktop().browse(uri);
										} catch (IOException | URISyntaxException e) {
											ErrorPrint.printError(e);
											e.printStackTrace();
										}
									}).start();
								}
							}
						} else {
							Alert alertError = new Alert(AlertType.ERROR);
							alertError.setTitle("Error");
							alertError.setHeaderText("Ooops, there was an error !");
							alertError.setContentText(
									"Sorry but the list apply failed :(\nA debug file should be generated :)");

							alertError.showAndWait();
						}
					} catch (IOException e) {
						ErrorPrint.printError(e, "When list application");
						e.printStackTrace();
					}
				}
			}// end action
		});

		importList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				FileChooser importChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("*JSON files (*.json)",
						"*.json");
				importChooser.setTitle("Choose an exported list json file for " + ModManager.GAME);
				importChooser.setInitialDirectory(new File(File.separator));
				importChooser.getExtensionFilters().add(extFilter);
				File file = importChooser.showOpenDialog(stage.getOwner());
				if (file != null && !file.isDirectory()) {
					try {
						String strResult = ModManager.userlistsJSON.importList(file.getAbsolutePath(), availableMods);
						updateList();
						refreshTexts();

						BasicDialog.showGenericDialog("Import result", null, strResult, AlertType.INFORMATION);
					} catch (Exception e) {
						BasicDialog.showGenericDialog("Error", e.getMessage(), AlertType.ERROR);
						ErrorPrint.printError(e, "When import list");
						e.printStackTrace();
					}
				}
			}// end action
		});

		exportList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				int pos = lists.getSelectionModel().getSelectedIndex();
				ModList toExport = lists.getSelectionModel().getSelectedItem();
				try {
					String strResult = ModManager.userlistsJSON.exportList(ModManager.GAME, toExport.getName());

					BasicDialog.showGenericDialog("Export result", "List '" + toExport.getName() + "' exported in :",
							strResult, AlertType.INFORMATION, true);
				} catch (Exception e) {
					if (pos == -1) {
						ErrorPrint.printError(e, "User try to export a list without selecting a list");
						BasicDialog.showGenericDialog("Error", "No list selected", AlertType.ERROR);
					} else {
						ErrorPrint.printError(e, "When export a list");
						BasicDialog.showGenericDialog("Error", e.getMessage(), AlertType.ERROR);
					}
					e.printStackTrace();
				}
			}// end action
		});
	}

	/**
	 * @throws Exception
	 */
	private void updateList() throws Exception {
		ModManager.userlistsJSON.readFile(ModManager.GAME_LIST_STORAGE_FILE.getAbsolutePath());
		userListArray = ModManager.userlistsJSON.getSavedList(availableMods);

		ObservableList<TableColumn<ModList, ?>> sortOrder = lists.getSortOrder();
		List<TableColumn<ModList, ?>> sortColumns = new ArrayList<>();
		List<SortType> sortTypes = new ArrayList<>();

		for (TableColumn<ModList, ?> tableColumn : sortOrder) {
			sortColumns.add(tableColumn);
			sortTypes.add(tableColumn.getSortType());
		}

		listOfLists.clear();
		listOfLists.addAll(userListArray);

		lists.setItems(listOfLists);
		lists.refresh();

		for (int i = 0; i < sortColumns.size(); i++) {
			lists.getSortOrder().add(sortColumns.get(i));
			sortColumns.get(i).setSortType(sortTypes.get(i));
		}

		lists.sort();

		// Loose selection after refresh
		modifyList.setDisable(true);
		delList.setDisable(true);
		applyList.setDisable(true);
		exportList.setDisable(true);
	}

	private void refreshTexts() {
		nbModLbl.setText(String.format(nbModstr, getModNumber()));
		yourLists.setText(String.format(lblYrLists, getListNumber()));
	}

	private boolean applyModList(ModList applyList) throws IOException {
		boolean status = false;

		switch (ModManager.ACTMOD_TYPE) {
		case "json":
			// Version 2 → Imperator (like)
			status = applyOneModListV2(applyList);
			status = applyLanguageV2(applyList.getLanguageCode());
			break;

		default:
			// Version 1 → Crusader Kings II to Stellaris (like)
			status = applyOneModListV1(applyList);
			break;
		}

		return status;
	}

	/**
	 * @param selected
	 * @return
	 * @throws IOException
	 */
	private boolean applyOneModListV1(ModList applyList) throws IOException {
		List<Mod> applyMods = applyList.getModlist();

		String sep = File.separator;

		// Clean customModFiles
		deleteCustomModFiles();

		if (applyList.isCustomOrder()) {
			// Generate .mod files with custom name
			generateCustomModFiles(applyMods);
		} else {
			// Sort list to ASCII order before apply
			Collections.sort(applyMods, new Comparator<Mod>() {
				@Override
				public int compare(Mod m1, Mod m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});
		}

		File inputFile = new File(ModManager.PATH + sep + ModManager.SETTING_FILE);
		File tempFile = new File(ModManager.PATH + sep + ModManager.SETTING_FILE + ".tmp");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String startLineRemove = "gui";
		String aloneLineRemove = "language";
		String currentLine;
		boolean startEdit = false, startCopy = true, noLast_Mods = true, hasEqual = false, waitEqual = false;

		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			if (hasEqual && trimmedLine.contains("{")) {
				hasEqual = false;
				startEdit = true;
				writer.write(
						currentLine.substring(0, currentLine.indexOf("{") + 1) + System.getProperty("line.separator"));
			}
			if (waitEqual && trimmedLine.contains("=")) {
				waitEqual = false;
				if (trimmedLine.contains("{")) {
					startEdit = true;
					writer.write(currentLine.substring(0, currentLine.indexOf("{") + 1)
							+ System.getProperty("line.separator"));
				} else {
					hasEqual = true;
				}
			}
			if (trimmedLine.contains(startLineRemove)) {
				String toWrite;
				if (trimmedLine.contains(startLineRemove + "={")) {
					startEdit = true;
					toWrite = currentLine.substring(0, currentLine.indexOf("{") + 1);
				} else if (trimmedLine.contains(startLineRemove + "=")) {
					hasEqual = true;
					toWrite = currentLine.substring(0, currentLine.indexOf("=") + 1);
				} else {
					waitEqual = true;
					toWrite = currentLine.substring(0,
							currentLine.indexOf(startLineRemove.charAt(startLineRemove.length() - 1)));
				}
				if (startLineRemove.equals("last_mods")) {
					noLast_Mods = false;
				}
				startCopy = false;
				writer.write(toWrite + System.getProperty("line.separator"));
			}
			if (startEdit) {
				if (startLineRemove.equals("gui")) {
					printLanguageBloc(applyList.getLanguageCode(), writer);
					startLineRemove = "last_mods";
				} else {
					if (applyList.isCustomOrder()) {
						modPrint(applyMods, writer, "pmm_");
					} else {
						modPrint(applyMods, writer);
					}
				}
				startEdit = false;
			} else {
				if (startCopy) {
					if (trimmedLine.contains(aloneLineRemove)) {
						writer.write(aloneLineRemove + "=\"" + applyList.getLanguageCode() + "\""
								+ System.getProperty("line.separator"));
						startLineRemove = "last_mods";
					} else {
						writer.write(currentLine + System.getProperty("line.separator"));
					}
				}
				if (!startCopy && !hasEqual && !waitEqual) {
					if (trimmedLine.contains("}")) {
						startCopy = true;
						writer.write(currentLine.substring(currentLine.indexOf("}"), currentLine.length())
								+ System.getProperty("line.separator"));
					}
				}
			}
		}
		if (noLast_Mods) {
			writer.write("last_mods={" + System.getProperty("line.separator"));
			if (applyList.isCustomOrder()) {
				modPrint(applyMods, writer, "pmm_");
			} else {
				modPrint(applyMods, writer);
			}
			writer.write("}" + System.getProperty("line.separator"));
		}
		writer.close();
		reader.close();
		inputFile.delete();
		boolean successful = tempFile.renameTo(inputFile);
		return successful;
	}

	/**
	 * @param applyList
	 * @return
	 * @throws IOException
	 */
	private boolean applyOneModListV2(ModList applyList) throws IOException {
		List<Mod> applyMods = applyList.getModlist();

		String sep = File.separator;

		// Clean customModFiles
		deleteCustomModFiles();

		if (applyList.isCustomOrder()) {
			// Generate .mod files with custom name
			generateCustomModFiles(applyMods);
		} else {
			// Sort list to ASCII order before apply
			Collections.sort(applyMods, new Comparator<Mod>() {
				@Override
				public int compare(Mod m1, Mod m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});
		}

		Collections.reverse(applyMods);

		File inputFile = new File(ModManager.PATH + sep + ModManager.ACTMOD_FILE);
		File tempFile = new File(ModManager.PATH + sep + ModManager.ACTMOD_FILE + ".tmp");

		FileReader fileReader = new FileReader(inputFile);
		FileWriter fileWriter = new FileWriter(tempFile);

		Gson gson = new Gson();
		JsonObject json = new JsonObject();

		json = gson.fromJson(fileReader, JsonObject.class);

//		if (!json.containsKey("disabled_dlcs")) {
//			json.put("disabled_dlcs", new String[0]);
//		}

		JsonArray enabled_mods = new JsonArray();

		String modfolder = "mod/";
//		if (!(modfolder.lastIndexOf("/") == modfolder.length() - 1)) {
//			modfolder += "/";
//		}
		String prefix = "";
		if (applyList.isCustomOrder()) {
			prefix = "pmm_";
		}

		for (Mod mod : applyMods) {
			String modpath = modfolder + prefix + mod.getFileName();
			enabled_mods.add(modpath);
		}

		json.add("enabled_mods", enabled_mods);

		try {
			fileWriter.write(json.toString());
		} catch (IOException e) {
			ErrorPrint.printError(e, "When writing json");
			e.printStackTrace();
		}

		fileReader.close();
		fileWriter.close();
		inputFile.delete();
		boolean successful = tempFile.renameTo(inputFile);
		return successful;
	}

	/**
	 * @param languageCode
	 * @return
	 * @throws IOException
	 */
	private boolean applyLanguageV2(String languageCode) throws IOException {
		String sep = File.separator;

		File inputFile = new File(ModManager.PATH + sep + ModManager.SETTING_FILE);
		File tempFile = new File(ModManager.PATH + sep + ModManager.SETTING_FILE + ".tmp");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String startLineRemove = "\"language\"";
		// String aloneLineRemove = "language";
		String currentLine;
		boolean startEdit = false, startCopy = true, hasEqual = false, waitEqual = false;

		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			if (hasEqual && trimmedLine.contains("{")) {
				hasEqual = false;
				startEdit = true;
				writer.write(
						currentLine.substring(0, currentLine.indexOf("{") + 1) + System.getProperty("line.separator"));
			}
			if (waitEqual && trimmedLine.contains("=")) {
				waitEqual = false;
				if (trimmedLine.contains("{")) {
					startEdit = true;
					writer.write(currentLine.substring(0, currentLine.indexOf("{") + 1)
							+ System.getProperty("line.separator"));
				} else {
					hasEqual = true;
				}
			}
			if (trimmedLine.contains(startLineRemove)) {
				String toWrite;
				if (trimmedLine.contains(startLineRemove + "={")) {
					startEdit = true;
					toWrite = currentLine.substring(0, currentLine.indexOf("{") + 1);
				} else if (trimmedLine.contains(startLineRemove + "=")) {
					hasEqual = true;
					toWrite = currentLine.substring(0, currentLine.indexOf("=") + 1);
				} else {
					waitEqual = true;
					toWrite = currentLine.substring(0,
							currentLine.indexOf(startLineRemove.charAt(startLineRemove.length() - 1)));
				}
				startCopy = false;
				writer.write(toWrite + System.getProperty("line.separator"));
			}
			if (startEdit) {
				if (startLineRemove.equals("\"language\"")) {
					writer.write("\t\tversion=0" + System.getProperty("line.separator") + "\t\tvalue=\"" + languageCode
							+ "\"" + System.getProperty("line.separator"));
					startLineRemove = "aaaaa";
				}
				startEdit = false;
			} else {
				if (startCopy) {
					writer.write(currentLine + System.getProperty("line.separator"));
				}
				if (!startCopy && !hasEqual && !waitEqual) {
					if (trimmedLine.contains("}")) {
						startCopy = true;
						writer.write(currentLine + System.getProperty("line.separator"));
					}
				}
			}
		}

		writer.close();
		reader.close();
		inputFile.delete();
		boolean successful = tempFile.renameTo(inputFile);
		return successful;
	}

	/**
	 * @param applyMods
	 */
	private void generateCustomModFiles(List<Mod> applyMods) {
		String sep = File.separator;
		File modDir = new File(ModManager.PATH + sep + "mod");

		File[] content = modDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith("pmm_") && name.toLowerCase().endsWith(".mod");
			}
		});
		for (File file : content) {
			file.delete();
		}

		// Create the custom .mod file for each mod (XXX_idorname.mod)
		int n = applyMods.size();
		int digits = 0;
		do {
			n = n / 10;
			digits++;
		} while (n != 0);
		String numberFormat = "%0" + digits + "d";
		String.format("%03d", 1);

		for (int i = 0; i < applyMods.size(); i++) {
			Mod mod = applyMods.get(i);

			String customModName = String.format(numberFormat, i + 1) + "_" + mod.getName();

			File modFile = new File(ModManager.PATH + sep + "mod" + sep + mod.getFileName());
			File customModFile = new File(ModManager.PATH + sep + "mod" + sep + "pmm_" + mod.getFileName());
			try {
				Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(customModFile.getAbsolutePath()),
						StandardCopyOption.REPLACE_EXISTING);

				File inputFile = customModFile;
				File tempFile = new File(ModManager.PATH + sep + "mod" + sep + String.format(numberFormat, i)
						+ mod.getFileName() + ".tmp");

				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

				String aloneLineRemove = "name";
				String currentLine;
				boolean startEdit = false, startCopy = true, hasEqual = false, waitEqual = false;

				while ((currentLine = reader.readLine()) != null) {
					// trim newline when comparing with lineToRemove
					String trimmedLine = currentLine.trim();
					if (hasEqual && trimmedLine.contains("{")) {
						hasEqual = false;
						startEdit = true;
						writer.write(currentLine.substring(0, currentLine.indexOf("{") + 1)
								+ System.getProperty("line.separator"));
					}
					if (waitEqual && trimmedLine.contains("=")) {
						waitEqual = false;
						if (trimmedLine.contains("{")) {
							startEdit = true;
							writer.write(currentLine.substring(0, currentLine.indexOf("{") + 1)
									+ System.getProperty("line.separator"));
						} else {
							hasEqual = true;
						}
					}
					if (startEdit) {
						startEdit = false;
					} else {
						if (startCopy) {
							if (trimmedLine.contains(aloneLineRemove)) {
								writer.write(aloneLineRemove + "=\"" + customModName + "\""
										+ System.getProperty("line.separator"));
							} else {
								writer.write(currentLine + System.getProperty("line.separator"));
							}
						}
						if (!startCopy && !hasEqual && !waitEqual) {
							if (trimmedLine.contains("}")) {
								startCopy = true;
								writer.write(currentLine.substring(currentLine.indexOf("}"), currentLine.length())
										+ System.getProperty("line.separator"));
							}
						}
					}
				}

				writer.close();
				reader.close();
				inputFile.delete();

				tempFile.renameTo(inputFile);

			} catch (IOException e) {
				ErrorPrint.printError(e, "Try to create a custom mod file");
			}
		}
	}

	/**
	 * Clean customModFiles : "mod/pmm_xxxxx.mod" files are deleted
	 */
	private void deleteCustomModFiles() {
		String sep = File.separator;

		File modDir = new File(ModManager.PATH + sep + "mod");
		File[] content = modDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith("pmm_") && name.toLowerCase().endsWith(".mod");
			}
		});
		if (content != null) {
			for (File file : content) {
				file.delete();
			}
		}
	}

	/**
	 * @param applyMods
	 * @param writer
	 * @throws IOException
	 */
	private void modPrint(List<Mod> applyMods, BufferedWriter writer) throws IOException {
		modPrint(applyMods, writer, "", "mod/");
	}

	/**
	 * @param applyMods
	 * @param writer
	 * @param prefix
	 * @throws IOException
	 */
	private void modPrint(List<Mod> applyMods, BufferedWriter writer, String prefix) throws IOException {
		modPrint(applyMods, writer, prefix, "mod/");
	}

	/**
	 * @param applyMods
	 * @param writer
	 * @param prefix
	 * @param modfolder
	 * @throws IOException
	 */
	private void modPrint(List<Mod> applyMods, BufferedWriter writer, String prefix, String modfolder)
			throws IOException {
		if (!(modfolder.lastIndexOf("/") == modfolder.length() - 1)) {
			modfolder += "/";
		}
		for (Mod mod : applyMods) {
			String addLine = "\t\"" + modfolder + prefix + mod.getFileName() + "\"";
			writer.write(addLine + System.getProperty("line.separator"));
		}
	}

	/**
	 * @param languageCode
	 * @param writer
	 * @throws IOException
	 */
	private void printLanguageBloc(String languageCode, BufferedWriter writer) throws IOException {
		writer.write("\tlanguage=" + languageCode + System.getProperty("line.separator") + "\thas_set_language=yes"
				+ System.getProperty("line.separator"));
	}

	/**
	 * @return
	 */
	private int getModNumber() {
		return availableMods.size();
	}

	/**
	 * @return
	 */
	private int getListNumber() {
		return userListArray.size();
	}

	/**
	 *
	 */
	private void loadModFilesArray() {
		String workLabel = ModManager.isConflictComputed() ? "Generate mods and conflicts..." : "Generate mods...";

		wd = new WorkIndicatorDialog<>(window.getScene().getWindow(), workLabel);

		wd.addTaskEndNotification(result -> {
			try {
				updateList();
			} catch (Exception eCreate) {
				ErrorPrint.printError(eCreate, "When update ListView of ModLists on window creation");
				eCreate.printStackTrace();
			}

			refreshTexts();

			wd = null; // don't keep the object, cleanup
		});

		wd.exec("LoadMods", inputParam -> {
			// String sep = File.separator;
			File userDir = new File(absolutePath);

			File[] childs = userDir.listFiles();

			for (int i = 0; i < childs.length; i++) {
				File modDir = childs[i];

//				if (modDir.isDirectory() && ListManager.modFileNames.contains(modDir.getName().toLowerCase())) {
				if (modDir.isDirectory() && modDir.getName().toLowerCase().equals("mod")) {
					// Clean customModFiles
					deleteCustomModFiles();

					String[] modFiles = modDir.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".mod");
						}
					});
					wd.maxProgress = modFiles.length;
					int j = 0;
					for (String modFile : modFiles) {
						availableMods.put(modFile, new Mod(modFile, ModManager.isConflictComputed()));
						j++;
						wd.currentProgress = j;
					}
				} else {
//					throw new FileNotFoundException("The folder '" + modFile.getAbsolutePath()
//							+ "'is missing, please check the path.\nBe sure to have started the game launcher once !");
				}
			}

			return 1;
		});
	}
}
