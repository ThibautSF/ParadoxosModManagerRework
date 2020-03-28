package com.pmm.ParadoxosGameModManager.versioning;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.pmm.ParadoxosGameModManager.ModManager;
import com.pmm.ParadoxosGameModManager.debug.ErrorPrint;
import com.pmm.ParadoxosGameModManager.window.BasicDialog;
import com.pmm.ParadoxosGameModManager.window.WorkIndicatorDialog;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * An online checker implementation
 *
 * @author SIMON-FINE Thibaut (alias Bisougai), and GROSJEAN Nicolas (alias
 *         Mouchi)
 *
 */
public class OnlineVersionChecker {
	private static String URL_APP_RELEASES = "https://github.com/ThibautSF/ParadoxosModManagerRework/releases";
	private static String URL_APP_INFO_TXT = "https://raw.githubusercontent.com/ThibautSF/ParadoxosModManagerRework/master/AppInfo.txt";

	private static String VERSION = "0.8.2";

	private String lastestOnlineVersionNumber;

	private Stage dialog;
	private WorkIndicatorDialog<String> wd = null;

	public OnlineVersionChecker() {
		String changelogOrNothing = newVersionOnline();
		if (changelogOrNothing.length() > 0) {
			showUpdateWindow(changelogOrNothing);
		}
	}

	private String newVersionOnline() {
		StringBuilder changelog = new StringBuilder();
		boolean updateExist = true;
		boolean versionChangelog = false;
		boolean firstRead = true;

		String[] aLocalV = VERSION.split("\\.");
		try {
			URL appInfoTxt = new URL(URL_APP_INFO_TXT);
			BufferedReader in = new BufferedReader(new InputStreamReader(appInfoTxt.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("AppVersion=")) {
					String onlineVersion = inputLine.substring(inputLine.indexOf("=") + 1, inputLine.length());
					String[] aOnlineV = onlineVersion.split("\\.");

					if (firstRead) {
						lastestOnlineVersionNumber = onlineVersion;
					}

					updateExist = checkIsNewerVersion(aLocalV, aOnlineV);

					if (!updateExist) {
						// We reach an inferior or equal app version number → End
						if (!versionChangelog && changelog.length() > 0) {
							changelog.append("No changelog available");
						}
						return changelog.toString();
					} else {
						if (!firstRead) {
							if (!versionChangelog) {
								changelog.append("No changelog available\n");
							}

							changelog.append("\n");
						}

						changelog.append(onlineVersion + ":\n");
						versionChangelog = false;
					}

					firstRead = false;
				} else {
					if (inputLine.length() > 0) {
						versionChangelog = true;
						changelog.append(inputLine + "\n");
					}
				}
			}
			in.close();
		} catch (Exception e) {
			ErrorPrint.printError(e, "Check Online Version");
			changelog = new StringBuilder();
			BasicDialog.showGenericDialog("Version checking error", "Unable to check online version", AlertType.ERROR);
		}

		// We reach end of file (case final line was an AppVersion)
		if (!versionChangelog && changelog.length() > 0) {
			changelog.append("No changelog available");
		}

		return changelog.toString();
	}

	/**
	 * Compare local version to online. Both arrays must have the same number of
	 * values (raise IllegalArguementException if local.length!=online.length)
	 *
	 * @param local  an array of string which contains integer and represent the
	 *               local version of the app (ex; for "1.0" input should be
	 *               ["1","0"])
	 * @param online an array of string which contains integer and represent the
	 *               online version of the app
	 * @return
	 */
	private boolean checkIsNewerVersion(String[] local, String[] online) {
		if (local.length != online.length)
			throw new IllegalArgumentException("The local and online array must have the same length");

		int i = 0;
		while (i < local.length) {
			if (Integer.parseInt(online[i]) > Integer.parseInt(local[i]))
				return true;
			else if (Integer.parseInt(online[i]) == Integer.parseInt(local[i])) {
				i++;
			} else {
				break;
			}
		}

		return false;
	}

	/**
	 * Get the download GitHub URL. EX :
	 * https://github.com/NicolasGrosjean/Translate_helper/releases/download/v2.1/TranslateHelper_v2-1.rar
	 *
	 * @return string of the url to download the last version of the software
	 */
	private String getGithHubDownloadUrl() {
		// Paradoxos Example :
		// https://github.com/ThibautSF/ParadoxosModManagerRework/releases/download/0.5.2/ParadoxosModManager0.5.2.zip

		StringBuilder builder = new StringBuilder();

		builder.append("https://github.com/ThibautSF/ParadoxosModManagerRework/releases/download/");
		builder.append(lastestOnlineVersionNumber);
		builder.append("/ParadoxosModManager");
		builder.append(lastestOnlineVersionNumber);
		builder.append(".zip");
		return builder.toString();
	}

	/**
	 * Get the download GitHub release tag page URL. EX :
	 * https://github.com/ThibautSF/ParadoxosModManagerRework/releases/tag/0.5.2
	 *
	 * @return string of the url to see infos about the last version of the software
	 *         (on GitHub)
	 */
	private String getGithHubReleaseUrl() {
		StringBuilder builder = new StringBuilder();

		builder.append("https://github.com/ThibautSF/ParadoxosModManagerRework/releases/tag/");
		builder.append(lastestOnlineVersionNumber);
		return builder.toString();
	}

	/**
	 * Generate a javafx alert and confirmation window to inform about a new version
	 * (and ask what he want to do)
	 *
	 * @param changelog the content of the scrollable textarea
	 */
	private void showUpdateWindow(String changelog) {
		dialog = new Stage();
		dialog.setTitle("Paradoxos Mod Manager - Update Available");
		dialog.setResizable(false);

		GridPane expContent = new GridPane();
		Scene scene = new Scene(expContent, 575, 420);

		Text contentText = new Text(
				String.format("A new version of %s is available online !\nLocal : %s\nOnline : %s\n",
						ModManager.APP_NAME, VERSION, lastestOnlineVersionNumber));

		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(0);
		RowConstraints row2 = new RowConstraints();
		row2.setPrefHeight(scene.getHeight() - 80);
		RowConstraints row3 = new RowConstraints();
		row3.setMinHeight(50);
		row3.setPrefHeight(50);
		row3.setMaxHeight(50);
		RowConstraints row4 = new RowConstraints();
		row4.setPercentHeight(0);
		expContent.getRowConstraints().addAll(row1, row2, row3, row4);
		expContent.setVgap(10);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(0);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(100);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(0);
		expContent.getColumnConstraints().addAll(col1, col2, col3);
		expContent.setHgap(10);

		// expContent.setGridLinesVisible(true);

		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.autosize();

		VBox content = new VBox();

		expContent.add(content, 1, 1);
		content.getChildren().add(contentText);

		if (changelog.length() > 0 && !changelog.equals("No changelog available")) {
			Label label = new Label("CHANGELOG:");

			TextArea textArea = new TextArea(changelog);
			textArea.setEditable(false);
			textArea.setWrapText(true);

			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(textArea, Priority.ALWAYS);

			content.getChildren().addAll(label, textArea);
		}

		HBox buttons = new HBox(10);
		Button buttonWebAll = new Button("All versions\n(with source)");
		Button buttonWebDownload = new Button("Get Update\n(autoinstall)");
		Button buttonCancel = new Button("Continue\n(Stay " + VERSION + ")");

		buttonWebAll.setOnAction((e) -> {
			goURL(URL_APP_RELEASES);
		});
		buttonWebDownload.setOnAction((e) -> {
			startDownload(dialog);
		});
		buttonCancel.setOnAction((e) -> {
			dialog.close();
		});

		buttons.getChildren().add(buttonWebAll);
//		buttons.getChildren().add(buttonWebDownload);
		buttons.getChildren().add(buttonCancel);
		buttons.setAlignment(Pos.BOTTOM_RIGHT);

		expContent.add(buttons, 1, 2);
		dialog.setScene(scene);
		dialog.showAndWait();
	}

	private void startDownload(Stage stage) {
		wd = new WorkIndicatorDialog<>(stage.getScene().getWindow(), "Paradoxos is downloading...");

		String urlStr = getGithHubDownloadUrl();

		wd.addTaskEndNotification(result -> {
			if (result == 1) {
				// OK
				String[] run = { "java", "-jar", ModManager.UPDATER_NAME };
				try {
					Runtime.getRuntime().exec(run);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.exit(0);
			} else {
				// KO
				BasicDialog.showGenericDialog("Error during download",
						"Download page will be opened in your navigator.", AlertType.ERROR);
				goURL(getGithHubReleaseUrl());
			}

			wd = null; // don't keep the object, cleanup
		});

		wd.exec("Download", inputParam -> {
			try {
				URL url = new URL(urlStr);

				Download d = new Download(url);

				wd.maxProgress = 100;
				while (d.getStatus() == Download.DOWNLOADING) {
					wd.currentProgress = d.getProgress();
				}

				if (d.getStatus() == Download.COMPLETE) {
					File oldName = new File(d.getLocalFileName());
					File newName = new File(ModManager.UPDATE_ZIP_NAME);
					oldName.renameTo(newName);

					return 1;
				}

			} catch (IOException e) {
				return 0;
			}

			return 0;
		});
	}

	/**
	 * Open the web browser with the target url If not available copy url to
	 * clipboard
	 *
	 * @param url
	 */
	private void goURL(String url) {
		if (Desktop.isDesktopSupported()) {
			new Thread(() -> {
				try {
					URI uri = new URI(url);
					Desktop.getDesktop().browse(uri);
					System.exit(0);
				} catch (IOException | URISyntaxException e) {
					ErrorPrint.printError(e, "Open URL ( " + url + " )");
					e.printStackTrace();
				}
			}).start();
		} else {
			StringSelection selection = new StringSelection(getGithHubDownloadUrl());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);

			BasicDialog.showGenericDialog("Unable to open Web Browser", "Url was copied in your clipboard.",
					AlertType.ERROR);
			dialog.close();
		}
	}
}
