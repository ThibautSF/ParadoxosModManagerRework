package com.pmm.ParadoxosGameModManager;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pmm.ParadoxosGameModManager.debug.ErrorPrint;
import com.pmm.ParadoxosGameModManager.mod.Languages;
import com.pmm.ParadoxosGameModManager.mod.Mod;
import com.pmm.ParadoxosGameModManager.mod.ModList;
import com.pmm.ParadoxosGameModManager.window.BasicDialog;
import com.pmm.ParadoxosGameModManager.window.Utils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class ListCreator extends Stage {
	// Window Var
	private static int WINDOW_WIDTH = 1200;
	private static int WINDOW_HEIGHT = 750;
	private GridPane window = new GridPane();

	private VBox titleBox = new VBox();
	private Label lblListName = new Label("List Name : ");
	private TextField fieldListName = new TextField();

	private VBox helpBox = new VBox();
	private Button buttonHelp = new Button();
	private Tooltip tooltipHelp = new Tooltip(
			"Primary Click on mod to select it (shift and ctrl multiple selection works too)\nSecondary Click to clear the selection");

	private VBox descrBox = new VBox();
	private Label lblListDesc = new Label("Description : ");
	private TextField fieldListDesc = new TextField();

	private VBox langBox = new VBox();
	private Label lblListLang = new Label("Game language : ");
	private ComboBox<Languages> cbListLang = new ComboBox<>(FXCollections.observableArrayList(Languages.values()));

	private VBox yrModsBox = new VBox();
	private String strYrMods = "Your mods (%d founds)";
	private Label lblYrMods = new Label(strYrMods);

	private VBox listModsBox = new VBox();
	private TableView<Mod> mods = new TableView<>();
	private TableColumn<Mod, Mod> actionsCol = new TableColumn<>("Actions");
	private TableColumn<Mod, Boolean> conflictCol = new TableColumn<>("Conflict");
	private TableColumn<Mod, String> modNameCol = new TableColumn<>("Mod Name");
	private TableColumn<Mod, String> fileNameCol = new TableColumn<>("File");
	private TableColumn<Mod, String> versionCol = new TableColumn<>("Version");
	private TableColumn<Mod, String> steamPath = new TableColumn<>("Workshop");
	private HBox listModsActionBox = new HBox();
	private Button activateSelectedRows = new Button();
	private Button disableSelectedRows = new Button();

	private VBox customOrderBox = new VBox();
	private CheckBox cbCustomOrder = new CheckBox("Use custom order (ASCII order otherwise)");
	private String strOrderInfo = "";
	private Label lblOrderInfo = new Label(strOrderInfo);
	private HBox resetOrderBox = new HBox();
	private Button btnResetOrder = new Button("Reset ASCII order");
	private Button btnResetInvOrder = new Button("Reset reverse ASCII order");

	private VBox listOrderBox = new VBox();
	private TableView<Mod> modsOrdering = new TableView<>();
	private TableColumn<Mod, String> orderModNameCol = new TableColumn<>("Mod Name");
	private TableColumn<Mod, Mod> orderPosCol = new TableColumn<>("#");
	private TableColumn<Mod, Mod> orderActionCol = new TableColumn<>("Actions");

	private ObservableList<Mod> listOfMods = FXCollections.observableArrayList();
	private ObservableList<Mod> selectedModsList = FXCollections.observableArrayList();
	private ObservableList<Mod> missingMods = FXCollections.observableArrayList();

	private HBox clearListBox = new HBox();
	private Button clearList = new Button("Clear List");
	private Button selectAllList = new Button("Add/Remove All");
	private HBox cancelListBox = new HBox();
	private Button cancelList = new Button("Cancel");
	private HBox saveListBox = new HBox();
	private Button saveList = new Button("Save");
	private Button saveListExit = new Button("Save & Close");
	private HBox importCurrentListBox = new HBox();
	private Button importCurrentList = new Button("Import from current");
	private String lblSaveifMissings = "Missings mods will be cleared !";
	private Label saveifMissings = new Label(lblSaveifMissings);

	// Local Var
	private ModList list;
	private Map<String, Mod> availableMods;
	private List<Mod> userMods;

	private List<Mod> modListBckp;

	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	private ArrayList<Mod> modSelections = new ArrayList<>();
	private ArrayList<Mod> orderingSelections = new ArrayList<>();

	/**
	 * @param path
	 * @param modFiles
	 */
	public ListCreator(String path, Map<String, Mod> availableMods) {
		this(path, availableMods, new ModList("", "", Languages.ENGLISH, new ArrayList<Mod>()));
	}

	/**
	 * @param path
	 * @param modFiles
	 * @param list
	 */
	public ListCreator(String path, Map<String, Mod> availableMods, ModList list) {
		this.list = list;

		this.availableMods = availableMods;
		this.userMods = new ArrayList<>(availableMods.values());
		Collections.sort(this.userMods, new Comparator<Mod>() {
			@Override
			public int compare(Mod m1, Mod m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});
		this.modListBckp = list.getModlist();

		setTitle(ModManager.APP_NAME + " : " + ModManager.GAME);

		window.setHgap(8);
		window.setVgap(8);
		window.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		window.setPadding(new Insets(0, 0, 5, 0));

		// Uncomment when editing window to see cells
//		window.setGridLinesVisible(true);

		RowConstraints row1 = new RowConstraints(50, 50, 50);
		RowConstraints row2 = new RowConstraints(50, 50, 50);
		RowConstraints row3 = new RowConstraints(25, 25, 25);
		RowConstraints row4 = new RowConstraints();
		row4.setMaxHeight(Double.MAX_VALUE);
		row4.setVgrow(Priority.ALWAYS);
		listModsBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		VBox.setVgrow(mods, Priority.ALWAYS);
		listOrderBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		VBox.setVgrow(modsOrdering, Priority.ALWAYS);
		RowConstraints row5 = new RowConstraints(15, 15, 15);
		RowConstraints row6 = new RowConstraints(25, 25, 25);
		window.getRowConstraints().addAll(row1, row2, row3, row4, row5, row6);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(0);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(20);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(10);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setPercentWidth(15);
		ColumnConstraints col5 = new ColumnConstraints();
		col5.setPercentWidth(20);
		ColumnConstraints col6 = new ColumnConstraints();
		col6.setPercentWidth(35);
		ColumnConstraints col7 = new ColumnConstraints();
		col7.setPercentWidth(0);
		window.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6, col7);

		// ModList title fields
		window.add(titleBox, 1, 0, 2, 1);
		titleBox.getChildren().add(lblListName);
		titleBox.getChildren().add(fieldListName);
		titleBox.setStyle("-fx-alignment: center-left;");
		fieldListName.setText(list.getName());

		// ModList Lang fields
		window.add(langBox, 3, 0, 1, 1);
		langBox.getChildren().add(lblListLang);
		langBox.getChildren().add(cbListLang);
		langBox.setStyle("-fx-alignment: center-left;");
		cbListLang.setValue(list.getLanguage());

		// ModList help/info fields
		window.add(helpBox, 4, 0, 1, 1);
		buttonHelp.setGraphic(new FontIcon(FontAwesomeSolid.INFO));
		buttonHelp.setTooltip(tooltipHelp);
		helpBox.getChildren().add(buttonHelp);
		helpBox.setAlignment(Pos.TOP_RIGHT);

		Utils.setTooltip(buttonHelp, tooltipHelp);

		// ModList Descr fields
		window.add(descrBox, 1, 1, 4, 1);
		descrBox.getChildren().add(lblListDesc);
		descrBox.getChildren().add(fieldListDesc);
		descrBox.setStyle("-fx-alignment: center-left;");
		fieldListDesc.setText(list.getDescription());

		// ModList "Your mods" field
		window.add(yrModsBox, 2, 2, 2, 1);
		yrModsBox.getChildren().add(lblYrMods);
		yrModsBox.setStyle("-fx-alignment: center;");
		lblYrMods.setText(String.format(strYrMods, userMods.size()));
		lblYrMods.setStyle("-fx-font: bold 20 serif;");

		// ModList list of mods (start)
		window.add(listModsActionBox, 1, 2, 1, 1);
		listModsActionBox.getChildren().addAll(activateSelectedRows, disableSelectedRows);
		listModsActionBox.setStyle("-fx-alignment: center-left;");
		listModsActionBox.setSpacing(5);
		activateSelectedRows.setGraphic(new FontIcon(FontAwesomeSolid.CHECK_SQUARE));
		Utils.setTooltip(activateSelectedRows, new Tooltip("Add selected mods to the list"));
		disableSelectedRows.setGraphic(new FontIcon(FontAwesomeRegular.SQUARE));
		Utils.setTooltip(disableSelectedRows, new Tooltip("Remove selected mods to the list"));

		activateSelectedRows.setOnAction(event -> {
			for (Mod sI : modSelections) {
				if (!sI.isMissing()) {
					if (!selectedModsList.contains(sI)) {
						selectedModsList.add(sI);
						list.addMod(sI);
					}
					mods.refresh();
					modsOrdering.refresh();
				}
			}
		});

		disableSelectedRows.setOnAction(event -> {
			for (Mod sI : modSelections) {
				if (!sI.isMissing()) {
					if (selectedModsList.contains(sI)) {
						selectedModsList.remove(sI);
						list.removeMod(sI);
					}
					mods.refresh();
					modsOrdering.refresh();
				}
			}
		});

		window.add(listModsBox, 1, 3, 4, 1);
		listModsBox.getChildren().add(mods);
		actionsCol.setSortable(false);
		actionsCol.setMinWidth(100);
		actionsCol.setMaxWidth(100);
		conflictCol.setSortable(false);
		conflictCol.setMinWidth(65);
		conflictCol.setMaxWidth(65);
		modNameCol.setSortable(false);
		fileNameCol.setSortable(false);
		versionCol.setSortable(false);
		mods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		mods.getColumns().add(actionsCol);
		if (ModManager.isConflictComputed()) {
			mods.getColumns().add(conflictCol);
		}
		mods.getColumns().add(modNameCol);
		mods.getColumns().add(fileNameCol);
		mods.getColumns().add(versionCol);
		mods.getColumns().add(steamPath);

		Utils.setTooltip(mods, new Tooltip("Tip: Use shift and ctrl for multiple selection"));

		actionsCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Mod, Mod>, ObservableValue<Mod>>() {
			@Override
			public ObservableValue<Mod> call(CellDataFeatures<Mod, Mod> p) {
				return new SimpleObjectProperty<>(p.getValue());
			}
		});
		conflictCol.setCellValueFactory(new Callback<CellDataFeatures<Mod, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<Mod, Boolean> m) {
				Map<Mod, List<String>> conflicts = list.getMappedConflicts(m.getValue());
				return new SimpleBooleanProperty(!conflicts.isEmpty());
			}
		});
		modNameCol.setCellValueFactory(new PropertyValueFactory<Mod, String>("name"));
		fileNameCol.setCellValueFactory(new PropertyValueFactory<Mod, String>("fileName"));
		versionCol.setCellValueFactory(new PropertyValueFactory<Mod, String>("versionCompatible"));
		steamPath.setCellValueFactory(new PropertyValueFactory<Mod, String>("steamPath"));

		actionsCol.setCellFactory(new Callback<TableColumn<Mod, Mod>, TableCell<Mod, Mod>>() {
			@Override
			public TableCell<Mod, Mod> call(TableColumn<Mod, Mod> modTableColumn) {
				return new MultipleButtonCell();
			}
		});

		conflictCol.setCellFactory(new Callback<TableColumn<Mod, Boolean>, TableCell<Mod, Boolean>>() {
			@Override
			public TableCell<Mod, Boolean> call(TableColumn<Mod, Boolean> booleanTableColumn) {
				return new ButtonCell();
			}
		});

		mods.setRowFactory(tv -> {
			TableRow<Mod> row = new TableRow<>() {
				@Override
				protected void updateItem(Mod item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
					} else if (list.hasConflict(item)) {
						setStyle("-fx-text-fill: white; -fx-background-color: #D28201;");
					} else if (selectedModsList.contains(item)) {
						setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50;");
					} else if (missingMods.contains(item)) {
						setStyle("-fx-background-color: red; -fx-font-weight: bold;");
					} else {
						setStyle("");
					}
				}
			};

			// Old selection
//			row.setOnMouseClicked(event -> {
//				Mod mod = row.getItem();
//				mods.getSelectionModel().clearSelection();
//				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
//					if (!mod.isMissing()) {
//						if (selectedModsList.contains(mod)) {
//							selectedModsList.remove(mod);
//							list.removeMod(mod);
//						} else {
//							selectedModsList.add(mod);
//							list.addMod(mod);
//						}
//						mods.refresh();
//						modsOrdering.refresh();
//					}
//				} else if (event.getButton() == MouseButton.SECONDARY) {
//					if (Desktop.isDesktopSupported()) {
//						new Thread(() -> {
//							try {
//								URI uri = new URI(mod.getSteamPath());
//								Desktop.getDesktop().browse(uri);
//							} catch (IOException | URISyntaxException e) {
//								ErrorPrint.printError(e);
//								e.printStackTrace();
//							}
//						}).start();
//					}
//				}
//			});

			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
					modSelections.clear();// important...

					ObservableList<Mod> items = mods.getSelectionModel().getSelectedItems();

					for (Mod iI : items) {
						modSelections.add(iI);
					}

				} else if (event.getButton() == MouseButton.SECONDARY) {
					mods.getSelectionModel().clearSelection();
					modSelections.clear();
				}
			});

			return row;
		});

		printModList();
		// ModList list of mods (end)

		// Use custom mod order
		window.add(customOrderBox, 5, 0, 1, 2);
		customOrderBox.setStyle("-fx-alignment: bottom-left;");
		customOrderBox.getChildren().addAll(cbCustomOrder, lblOrderInfo);
		cbCustomOrder.setSelected(list.isCustomOrder());

		lblOrderInfo.setStyle("-fx-text-fill: red;");
		lblOrderInfo.setText("Info: mod #1 have the highest priority (like legacy launcher)\n"
				+ "Thus mean that, if #1 and #2 have similar modifications, \n"
				+ "PMM will ensure that #1 overwrite #2 in the launcher order.");

//		switch (ModManager.ACTMOD_TYPE) {
//		case "json":
//			// Version 2 → Imperator, new launcher Stellaris/EU4 (like)
//			lblOrderInfo.setText(
//					"New order detected: #1 is loaded firstly by the game.\nThus mods will be applied bottom to top.");
//			break;
//
//		default:
//			// Version 1 → Crusader Kings II to Stellaris legacy (like)
//			lblOrderInfo.setText(
//					"Legacy order detected: #1 is loaded lastly by the game.\nThus mods will be applied top to bottom.");
//			break;
//		}

		// TableView order mods (start)
		window.add(listOrderBox, 5, 2, 1, 3);
		listOrderBox.getChildren().add(modsOrdering);
		orderModNameCol.setSortable(false);
		orderPosCol.setSortable(false);
		orderPosCol.setMinWidth(40);
		orderPosCol.setMaxWidth(40);
		orderActionCol.setSortable(false);
		orderActionCol.setMinWidth(100);
		orderActionCol.setMaxWidth(100);
		modsOrdering.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		modsOrdering.getColumns().add(orderModNameCol);
		modsOrdering.getColumns().add(orderPosCol);
		modsOrdering.getColumns().add(orderActionCol);
		modsOrdering.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		Utils.setTooltip(modsOrdering,
				new Tooltip("Tips: Use shift and ctrl for multiple selection.\nDrag&Drop to order."));

		orderModNameCol.setCellValueFactory(new PropertyValueFactory<Mod, String>("name"));

		orderPosCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Mod, Mod>, ObservableValue<Mod>>() {
			@Override
			public ObservableValue<Mod> call(CellDataFeatures<Mod, Mod> p) {
				return new SimpleObjectProperty<>(p.getValue());
			}
		});

		orderActionCol
				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Mod, Mod>, ObservableValue<Mod>>() {
					@Override
					public ObservableValue<Mod> call(CellDataFeatures<Mod, Mod> p) {
						return new SimpleObjectProperty<>(p.getValue());
					}
				});

		orderPosCol.setCellFactory(new Callback<TableColumn<Mod, Mod>, TableCell<Mod, Mod>>() {
			@Override
			public TableCell<Mod, Mod> call(TableColumn<Mod, Mod> param) {
				return new TableCell<>() {
					@Override
					protected void updateItem(Mod item, boolean empty) {
						super.updateItem(item, empty);

						if (this.getTableRow() != null && item != null) {
							Mod m = this.getTableRow().getItem();
							int pos = selectedModsList.indexOf(m) + 1;
							// int pos = this.getTableRow().getIndex()+1;
							setText(pos + "");
						} else {
							setText("");
						}
					}
				};
			}
		});

		orderActionCol.setCellFactory(new Callback<TableColumn<Mod, Mod>, TableCell<Mod, Mod>>() {
			@Override
			public TableCell<Mod, Mod> call(TableColumn<Mod, Mod> personBooleanTableColumn) {
				return new MultipleOrderButtonCell();
			}
		});

		// Source : https://stackoverflow.com/a/52437193
		modsOrdering.setRowFactory(tv -> {
			TableRow<Mod> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.SECONDARY) {
					modsOrdering.getSelectionModel().clearSelection();
					orderingSelections.clear();
				}
			});

			row.setOnDragDetected(event -> {
				if (!row.isEmpty()) {
					Integer index = row.getIndex();

					orderingSelections.clear();// important...

					ObservableList<Mod> items = modsOrdering.getSelectionModel().getSelectedItems();

					for (Mod iI : items) {
						orderingSelections.add(iI);
					}

					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				}
			});

			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();

				if (db.hasContent(SERIALIZED_MIME_TYPE)) {

					int dropIndex;
					Mod dI = null;

					if (row.isEmpty()) {
						dropIndex = modsOrdering.getItems().size();
					} else {
						dropIndex = row.getIndex();
						dI = modsOrdering.getItems().get(dropIndex);
					}
					int delta = 0;
					if (dI != null) {
						while (orderingSelections.contains(dI)) {
							delta = 1;
							--dropIndex;
							if (dropIndex < 0) {
								dI = null;
								dropIndex = 0;
								break;
							}
							dI = modsOrdering.getItems().get(dropIndex);
						}
					}

					for (Mod sI : orderingSelections) {
						modsOrdering.getItems().remove(sI);
					}

					if (dI != null) {
						dropIndex = modsOrdering.getItems().indexOf(dI) + delta;
					} else if (dropIndex != 0) {
						dropIndex = modsOrdering.getItems().size();
					}

					modsOrdering.getSelectionModel().clearSelection();

					for (Mod sI : orderingSelections) {
						// draggedIndex = selections.get(i);
						modsOrdering.getItems().add(dropIndex, sI);
						modsOrdering.getSelectionModel().select(dropIndex);
						dropIndex++;
					}

					event.setDropCompleted(true);
					orderingSelections.clear();
					event.consume();
				}
			});

			return row;
		});

		modsOrdering.setItems(selectedModsList);
		// TableView order mods (end)

		// Clear list and select all button (start)
		window.add(clearListBox, 1, 5, 1, 1);
		clearListBox.setStyle("-fx-alignment: center-left;");
		clearListBox.setSpacing(5);
		clearListBox.getChildren().addAll(clearList, selectAllList);

		clearList.setOnAction(action -> {
			selectedModsList.clear();
			listOfMods.removeAll(missingMods);
			missingMods.clear();
			list.setModlist(new ArrayList<Mod>());
			mods.refresh();
			modsOrdering.refresh();
			saveifMissings.setVisible(false);
		});

		selectAllList.setOnAction(action -> {
			if (selectedModsList.size() >= listOfMods.size() - missingMods.size()) {
				selectedModsList.clear();
				list.setModlist(new ArrayList<Mod>());
			} else {
				selectedModsList.clear();
				selectedModsList.addAll(listOfMods);
				selectedModsList.removeAll(missingMods);
				list.setModlist(new ArrayList<>(selectedModsList));
			}

			mods.refresh();
			modsOrdering.refresh();
		});
		// Clear list button (end)

		// Buttons Cancel & Save (start)
		window.add(cancelListBox, 2, 5, 1, 1);
		cancelListBox.setStyle("-fx-alignment: center-right;");
		cancelListBox.getChildren().add(cancelList);

		window.add(saveListBox, 3, 5, 1, 1);
		saveListBox.setStyle("-fx-alignment: center-left;");
		saveListBox.setSpacing(5);
		saveListBox.getChildren().addAll(saveList, saveListExit);

		saveifMissings.setStyle("-fx-text-fill: red;");
		window.add(saveifMissings, 3, 4, 2, 1);
		saveifMissings.setVisible(missingMods.size() > 0);

		cancelList.setOnAction(event -> {
			list.setModlist(modListBckp);
			close();
		});

		saveList.setOnAction(event -> save());

		saveListExit.setOnAction(event -> {
			if (save()) {
				close();
			}
		});
		// Buttons Cancel & Save (end)

		// Import current config button (start)
		window.add(importCurrentListBox, 4, 5, 1, 1);
		importCurrentListBox.setStyle("-fx-alignment: center-right;");
		importCurrentListBox.getChildren().add(importCurrentList);

		importCurrentList.setOnAction(event -> {
			String title = "Import current mod config";
			String header = "What do you want to do ?";
			String message = "- Append the mod from the game configuration to the list\n- Replace all the mods selected with the game config";

			List<ButtonType> buttons = new ArrayList<>();

			ButtonType buttonAppend = new ButtonType("Append");
			ButtonType buttonReplace = new ButtonType("Replace");

			buttons.add(buttonAppend);
			buttons.add(buttonReplace);

			Optional<ButtonType> choice = BasicDialog.showGenericConfirm(title, header, message, buttons, true);

			if (choice.get().getButtonData() != ButtonData.CANCEL_CLOSE) {
				try {
					if (choice.get() == buttonReplace) {
						list.setModlist(new ArrayList<Mod>());
						selectedModsList.clear();
						listOfMods.removeAll(missingMods);
						missingMods.clear();
					}
					getModList();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		// Import current config button (end)

		// Reset mod ordering button (start)
		window.add(resetOrderBox, 5, 5, 1, 1);
		resetOrderBox.setStyle("-fx-alignment: center;");
		resetOrderBox.setSpacing(5);
		resetOrderBox.getChildren().addAll(btnResetOrder, btnResetInvOrder);

		btnResetOrder.setOnAction(action -> {
			String title = "Reset to default order";
			String header = "What do you want to do ?";
			String message = "";

			List<ButtonType> buttons = new ArrayList<>();

			ButtonType buttonOk = new ButtonType("Continue");

			buttons.add(buttonOk);

			Optional<ButtonType> choice = BasicDialog.showGenericConfirm(title, header, message, buttons, true);

			if (choice.get().getButtonData() != ButtonData.CANCEL_CLOSE) {
				if (choice.get() == buttonOk) {
					Collections.sort(selectedModsList, new Comparator<Mod>() {
						@Override
						public int compare(Mod m1, Mod m2) {
							return m1.getName().compareTo(m2.getName());
						}
					});
				}
			}
		});

		btnResetInvOrder.setOnAction(action -> {
			String title = "Reset to default reverse order";
			String header = "What do you want to do ?";
			String message = "";

			List<ButtonType> buttons = new ArrayList<>();

			ButtonType buttonOk = new ButtonType("Continue");

			buttons.add(buttonOk);

			Optional<ButtonType> choice = BasicDialog.showGenericConfirm(title, header, message, buttons, true);

			if (choice.get().getButtonData() != ButtonData.CANCEL_CLOSE) {
				if (choice.get() == buttonOk) {
					Collections.sort(selectedModsList, new Comparator<Mod>() {
						@Override
						public int compare(Mod m1, Mod m2) {
							return -m1.getName().compareTo(m2.getName());
						}
					});
				}
			}
		});

		// Reset mod ordering button (end)

		// Print the scene
		Scene sc = new Scene(window, WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setScene(sc);
		this.setMinHeight(WINDOW_HEIGHT);
		this.setMinWidth(WINDOW_WIDTH);
		this.show();
	}

	/**
	 *
	 */
	private void printModList() {
		List<Mod> modsFromList = list.getModlist();

//		for (Mod oneMod : userMods) {
//			if(modsFromList.contains(oneMod)) {
//				selectedModsList.add(oneMod);
//			}
//		}

		for (Mod oneMod : modsFromList) {
			if (oneMod.isMissing()) {
				missingMods.add(oneMod);
				continue;
			}

			if (userMods.contains(oneMod)) {
				selectedModsList.add(oneMod);
			}

		}

		listOfMods.addAll(missingMods);
		listOfMods.addAll(userMods);

		mods.setItems(listOfMods);
	}

	/**
	 * @param version
	 * @throws IOException
	 */
	private void getModList() throws IOException {
		switch (ModManager.ACTMOD_TYPE) {
		case "json":
			getModListV2();
			getLanguageV2();
			break;

		default:
			getModListV1();
			break;
		}
	}

	/**
	 * @throws IOException
	 */
	private void getModListV1() throws IOException {
		String sep = File.separator;
		Languages language = Languages.getLanguage(null);
		File inputFile = new File(ModManager.PATH + sep + ModManager.ACTMOD_FILE);

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		String startLineRead = "last_mods";
		String aloneLineRead = "language";
		String currentLine;
		boolean startEdit = false, startRead = false, hasEqual = false, waitEqual = false, languageFound = false;

		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			if (hasEqual && trimmedLine.contains("{")) {
				hasEqual = false;
				startEdit = true;
			}
			if (waitEqual && trimmedLine.contains("=")) {
				waitEqual = false;
				if (trimmedLine.contains("{")) {
					startEdit = true;
				} else {
					hasEqual = true;
				}
			}
			if (trimmedLine.contains(startLineRead)) {
				if (trimmedLine.contains(startLineRead + "={")) {
					startEdit = true;
				} else if (trimmedLine.contains(startLineRead + "=")) {
					hasEqual = true;
				} else {
					waitEqual = true;
				}
				startRead = true;
			}
			if (startEdit) {
				System.out.println(trimmedLine);
				readMods(trimmedLine);

				startEdit = false;
			} else {
				if (!startRead) {
					if (!languageFound && trimmedLine.contains(aloneLineRead)) {
						String languageStr = trimmedLine.substring(trimmedLine.indexOf("l_") + 2);
						language = Languages.getLanguage(languageStr.replace("\"", ""));
						languageFound = true;
						startLineRead = "last_mods";
					}

				}
				if (startRead && !hasEqual && !waitEqual) {
					if (trimmedLine.contains("}")) {
						startRead = false;
					}

					readMods(trimmedLine);
				}
			}
		}
		reader.close();

		refresh(language);
	}

	/**
	 * @throws IOException
	 */
	private void getModListV2() throws IOException {
		String sep = File.separator;

		File inputFile = new File(ModManager.PATH + sep + ModManager.ACTMOD_FILE);

		FileReader fileReader = new FileReader(inputFile);

		Gson gson = new Gson();
		JsonObject json = new JsonObject();

		json = gson.fromJson(fileReader, JsonObject.class);

		if (json.keySet().contains("enabled_mods")) {
			String[] mods = gson.fromJson(json.get("enabled_mods"), String[].class);

			for (int i = mods.length - 1; i >= 0; i--) {
				String mod = mods[i];
				readMods(mod);
			}
		}

		fileReader.close();
	}

	private void getLanguageV2() throws IOException {
		String sep = File.separator;
		Languages language = Languages.getLanguage(null);

		File inputFile = new File(ModManager.PATH + sep + ModManager.SETTING_FILE);

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		String startLineRead = "\"language\"";
		// String aloneLineRead = "language";
		String currentLine;
		boolean startEdit = false, startRead = false, hasEqual = false, waitEqual = false, languageFound = false;

		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			if (hasEqual && trimmedLine.contains("{")) {
				hasEqual = false;
				startEdit = true;
			}
			if (waitEqual && trimmedLine.contains("=")) {
				waitEqual = false;
				if (trimmedLine.contains("{")) {
					startEdit = true;
				} else {
					hasEqual = true;
				}
			}
			if (trimmedLine.contains(startLineRead)) {
				if (trimmedLine.contains(startLineRead + "={")) {
					startEdit = true;
				} else if (trimmedLine.contains(startLineRead + "=")) {
					hasEqual = true;
				} else {
					waitEqual = true;
				}
				startRead = true;
			}
			if (startEdit) {
				if (trimmedLine.indexOf("value=") >= 0) {
					String languageStr = trimmedLine.substring(trimmedLine.indexOf("\"") + 1,
							trimmedLine.indexOf("\"") + 1);

					language = Languages.getLanguage(languageStr.replace("\"", ""));
				}

				startEdit = false;
			} else {
				if (!startRead) {
					//
				}

				if (startRead && !hasEqual && !waitEqual) {
					if (trimmedLine.contains("}")) {
						startRead = false;
					}

					if (!languageFound && trimmedLine.contains("value=")) {
						String languageStr = trimmedLine.substring(trimmedLine.indexOf("l_") + 2);
						language = Languages.getLanguage(languageStr.replace("\"", ""));
						languageFound = true;
						startLineRead = "aaaaa";
					}
				}
			}
		}
		reader.close();

		refresh(language);
	}

	private void readMods(String trimmedLine) {
		while (trimmedLine.indexOf("/") >= 0) {
			String oneModStr = trimmedLine.substring(trimmedLine.indexOf("/") + 1, trimmedLine.indexOf(".mod") + 4);

			Mod oneMod = availableMods.get(oneModStr);

			if (oneMod == null) {
				oneMod = new Mod(oneModStr);
				if (!missingMods.contains(oneMod)) {
					missingMods.add(oneMod);
					list.addMod(oneMod);
				}
			} else {
				if (!selectedModsList.contains(oneMod)) {
					selectedModsList.add(oneMod);
					list.addMod(oneMod);
				}
			}

			trimmedLine = trimmedLine.substring(trimmedLine.indexOf(".mod") + 4, trimmedLine.length());
		}
	}

	private void refresh(Languages language) {
		cbListLang.setValue(language);

		for (Mod mod : selectedModsList) {
			if (!listOfMods.contains(mod)) {
				listOfMods.add(mod);
			}
		}

		for (Mod mod : missingMods) {
			if (!listOfMods.contains(mod)) {
				listOfMods.add(mod);
			}
		}

		if (missingMods.size() > 0) {
			saveifMissings.setVisible(true);
		} else {
			saveifMissings.setVisible(false);
		}

		mods.refresh();
		modsOrdering.refresh();
	}

	private boolean save() {
		String listOldName = list.getName();

		try {
			ModManager.userlistsJSON.readFile(ModManager.GAME_LIST_STORAGE_FILE.getAbsolutePath());
		} catch (Exception e) {
			BasicDialog.showGenericDialog("Read/Write Error", "Error when reading or writing the file.",
					AlertType.ERROR);
			ErrorPrint.printError(e, "When save list in mod");
			e.printStackTrace();
			return false;
		}

		if (fieldListName.getText() == null || fieldListName.getText().equals("")) {
			BasicDialog.showGenericDialog("No list name !", "You need to give a name to the list.", AlertType.WARNING);
			return false;
		}

		if (!fieldListName.getText().equals(listOldName)
				&& ModManager.userlistsJSON.getList(availableMods, fieldListName.getText()) != null) {
			BasicDialog.showGenericDialog("List already exist !", "You already have a list with this name.",
					AlertType.WARNING);
			return false;
		}

		String title = "Confirm saving";
		List<ButtonType> buttons = new ArrayList<>();
		ButtonType buttonYes = new ButtonType("Yes");
		ButtonType buttonNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
		buttons.add(buttonYes);
		buttons.add(buttonNo);
		if (!ModManager.isConflictComputed() && selectedModsList.size() > 1) {
			String header = "The conflict manager is not activated";
			String message = "Do you want to save this mod list even it can have conflicts between mods ?";
			Optional<ButtonType> choice = BasicDialog.showGenericConfirm(title, header, message, buttons, false);
			if (choice.get().getButtonData() == ButtonData.CANCEL_CLOSE)
				return false;
		} else if (list.hasConflict()) {
			String header = "Some conflicts have been detected between mods";
			String message = "Do you want to save this mod list even if there is potential conflicts ?";
			Optional<ButtonType> choice = BasicDialog.showGenericConfirm(title, header, message, buttons, false);
			if (choice.get().getButtonData() == ButtonData.CANCEL_CLOSE)
				return false;
		}

		list.setModlist(selectedModsList);
		list.setName(fieldListName.getText());
		list.setDescription(fieldListDesc.getText());
		list.setLanguage(cbListLang.getValue());
		list.setCustomOrder(cbCustomOrder.isSelected());
		try {
			ModManager.userlistsJSON.modifyList(list, listOldName);

			return true;
		} catch (Exception e) {
			BasicDialog.showGenericDialog("Read/Write Error", "Error when reading or writing the file.",
					AlertType.ERROR);
			ErrorPrint.printError(e, "When save list in mod");
			e.printStackTrace();
		}

		return false;
	}

	// Define the button cell
	private class ButtonCell extends TableCell<Mod, Boolean> {
		final Button cellButton = new Button("...");
		final StackPane paddedButton = new StackPane();

		ButtonCell() {
			paddedButton.setPadding(new Insets(-5, 5, -5, 0));
			paddedButton.getChildren().add(cellButton);
			cellButton.setScaleY(0.5);
			cellButton.setOnAction(action -> {
				Mod mod = getTableRow().getItem();
				Map<Mod, List<String>> conflicts = list.getMappedConflicts(mod);
				if (conflicts.isEmpty()) {
					BasicDialog.showGenericDialog("No conflicts", "Only highlighted items in orange have conflicts",
							AlertType.ERROR);
				} else {
					displayConflicts(mod, conflicts, ModManager.isShowFileConflict());
				}
			});
		}

		// Display button if the row is not empty
		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				cellButton.setDisable(!t);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(paddedButton);
			}
		}

		// Inspired from : http://code.makery.ch/blog/javafx-dialogs-official/
		private void displayConflicts(Mod mod, Map<Mod, List<String>> conflicts, boolean showFiles) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Conflicts");
			alert.setHeaderText("Conflicts of the mod " + mod.getName());
			alert.setContentText("Conflicts with other selected mods");

			StringBuilder conflictText = new StringBuilder();
			for (Entry<Mod, List<String>> entry : conflicts.entrySet()) {
				conflictText.append(entry.getKey().getName());
				if (!showFiles) {
					conflictText.append('\n');
					continue;
				}
				conflictText.append(" :\n");
				entry.getValue().sort(null);
				for (String conflictFile : entry.getValue()) {
					conflictText.append('\t');
					conflictText.append(conflictFile);
					conflictText.append('\n');
				}
				conflictText.append('\n');
			}

			TextArea textArea = new TextArea(conflictText.toString());
			textArea.setEditable(false);
			textArea.setWrapText(false);

			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(textArea, 0, 0);

			// Set content into the dialog pane.
			alert.getDialogPane().setContent(expContent);

			alert.showAndWait();
		}
	}

	// Define the multi button cell
	private class MultipleButtonCell extends TableCell<Mod, Mod> {
		final Button selectButton = new Button();
		final Button steamButton = new Button();
		final Button dirButton = new Button();
		final HBox paddedButtons = new HBox();

		MultipleButtonCell() {
			paddedButtons.setPadding(new Insets(-2, 0, -2, 0));
			paddedButtons.setAlignment(Pos.CENTER);
			paddedButtons.getChildren().addAll(selectButton, steamButton, dirButton);

			selectButton.setScaleX(0.9);
			selectButton.setScaleY(0.9);
			selectButton.setOnAction(action -> {
				Mod mod = getTableRow().getItem();
				if (!mod.isMissing()) {
					if (!selectedModsList.contains(mod)) {
						selectedModsList.add(mod);
						list.addMod(mod);
					} else {
						selectedModsList.remove(mod);
						list.removeMod(mod);
					}
					mods.refresh();
					modsOrdering.refresh();
				}
			});

			FontIcon iconSteamButton = new FontIcon(FontAwesomeBrands.STEAM_SQUARE);
			steamButton.setScaleX(0.9);
			steamButton.setScaleY(0.9);
			steamButton.setGraphic(iconSteamButton);
			steamButton.setOnAction(action -> {
				Mod mod = getTableRow().getItem();
				if (Desktop.isDesktopSupported()) {
					new Thread(() -> {
						try {
							// URI uri = new URI(mod.getSteamPath());
							URI uri = new URI(mod.getSteamInAppPath());
							Desktop.getDesktop().browse(uri);
						} catch (IOException | URISyntaxException e) {
							ErrorPrint.printError(e);
							e.printStackTrace();
						}
					}).start();
				}
			});

			FontIcon iconDirButton = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
			dirButton.setScaleX(0.9);
			dirButton.setScaleY(0.9);
			dirButton.setGraphic(iconDirButton);
			dirButton.setOnAction(action -> {
				Mod mod = getTableRow().getItem();
				if (Desktop.isDesktopSupported()) {
					new Thread(() -> {
						try {
							File folder = new File(mod.getModDirPath());
							Desktop.getDesktop().open(folder);
						} catch (IOException e) {
							ErrorPrint.printError(e);
							e.printStackTrace();
						}
					}).start();
				}
			});
		}

		// Display button if the row is not empty
		@Override
		protected void updateItem(Mod m, boolean empty) {
			super.updateItem(m, empty);
			if (!empty) {
				enableOrDisableButtons(m);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(paddedButtons);
			}
		}

		private void enableOrDisableButtons(Mod mod) {
			if (mod.isMissing()) {
				selectButton.setVisible(false);
				selectButton.setGraphic(null);
				Utils.setTooltip(selectButton, null);
			} else {
				selectButton.setVisible(true);

				if (!selectedModsList.contains(mod)) {
					selectButton.setGraphic(new FontIcon(FontAwesomeRegular.SQUARE));
					Utils.setTooltip(selectButton, new Tooltip("Enable " + mod.getName()));
				} else {
					selectButton.setGraphic(new FontIcon(FontAwesomeSolid.CHECK_SQUARE));
					Utils.setTooltip(selectButton, new Tooltip("Disable " + mod.getName()));
				}
			}

			if (mod.getRemoteFileID() != "") {
				steamButton.setDisable(false);
				Utils.setTooltip(steamButton, new Tooltip("Open workshop page of " + mod.getName()));
			} else {
				steamButton.setDisable(true);
				Utils.setTooltip(steamButton, null);
			}

			File f = new File(mod.getModDirPath());
			if (f.exists()) {
				dirButton.setDisable(false);
				Utils.setTooltip(dirButton, new Tooltip("Open directory of " + mod.getName()));
			} else {
				dirButton.setDisable(true);
				Utils.setTooltip(dirButton, null);
			}
		}
	}

	// Define the multi button cell
	private class MultipleOrderButtonCell extends TableCell<Mod, Mod> {
		final Button upButton = new Button();
		final Button downButton = new Button();
		final Button removeButton = new Button();
		final HBox paddedButtons = new HBox();

		MultipleOrderButtonCell() {
			paddedButtons.setPadding(new Insets(-2, 0, -2, 0));
			paddedButtons.setAlignment(Pos.CENTER);
			paddedButtons.getChildren().addAll(upButton, downButton, removeButton);

			FontIcon iconUpButton = new FontIcon(FontAwesomeSolid.ARROW_UP);
			upButton.setScaleX(0.8);
			upButton.setScaleY(0.8);
			upButton.setGraphic(iconUpButton);
			upButton.setOnAction(action -> {
				int pos = getTableRow().getIndex();

				if (pos - 1 >= 0) {
					Mod mod = selectedModsList.remove(pos);
					selectedModsList.add(pos - 1, mod);
				}
			});

			FontIcon iconDownButton = new FontIcon(FontAwesomeSolid.ARROW_DOWN);
			downButton.setScaleX(0.8);
			downButton.setScaleY(0.8);
			downButton.setGraphic(iconDownButton);
			downButton.setOnAction(action -> {
				int pos = getTableRow().getIndex();

				if (pos + 1 <= selectedModsList.size()) {
					Mod mod = selectedModsList.remove(pos + 1);
					selectedModsList.add(pos, mod);
				}
			});

			FontIcon iconRemoveButton = new FontIcon(FontAwesomeSolid.TIMES);
			removeButton.setScaleX(0.8);
			removeButton.setScaleY(0.8);
			removeButton.setGraphic(iconRemoveButton);
			removeButton.setOnAction(action -> {
				int pos = getTableRow().getIndex();

				selectedModsList.remove(pos);
				mods.refresh();
				modsOrdering.refresh();
			});
		}

		// Display button if the row is not empty
		@Override
		protected void updateItem(Mod m, boolean empty) {
			super.updateItem(m, empty);
			if (!empty) {
				enableOrDisableButtons(m);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(paddedButtons);
			}
		}

		private void enableOrDisableButtons(Mod mod) {
			int pos = selectedModsList.indexOf(mod);

			if (pos > 0) {
				upButton.setDisable(false);
			} else {
				upButton.setDisable(true);
			}

			if (pos < selectedModsList.size() - 1) {
				downButton.setDisable(false);
			} else {
				downButton.setDisable(true);
			}
		}
	}
}
