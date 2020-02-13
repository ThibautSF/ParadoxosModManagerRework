/**
 *
 */
package com.pmm.ParadoxosGameModManager.window;

import java.util.List;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 *         Source : http://code.makery.ch/blog/javafx-dialogs-official/
 *
 */
public class BasicDialog {

	/**
	 * @param header
	 * @param message
	 */
	public static void showGenericDialog(String header, String message, AlertType alert_type) {
		showGenericDialog("Paradoxos", header, message, alert_type);
	}

	/**
	 * @param title
	 * @param header
	 * @param message
	 */
	public static void showGenericDialog(String title, String header, String message, AlertType alert_type) {
		Alert alert = new Alert(alert_type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		// alert.setContentText(message);

		Text contentText = new Text(message);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.autosize();
		expContent.add(contentText, 0, 0);

		alert.getDialogPane().setContent(expContent);

		alert.showAndWait();
	}

	/**
	 * @param title
	 * @param header
	 * @param message
	 * @param buttons
	 * @return
	 */
	public static Optional<ButtonType> showGenericConfirm(String title, String header, String message,
			List<ButtonType> buttons) {
		return showGenericConfirm(title, header, message, buttons, false);
	}

	/**
	 * @param title
	 * @param header
	 * @param message
	 * @param buttons
	 * @param cancel
	 * @return
	 */
	public static Optional<ButtonType> showGenericConfirm(String title, String header, String message,
			List<ButtonType> buttons, boolean cancel) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);

		Text contentText = new Text(message);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.autosize();
		expContent.add(contentText, 0, 0);

		alert.getDialogPane().setContent(expContent);

		if (cancel) {
			buttons.add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
		}

		alert.getButtonTypes().setAll(buttons);

		return alert.showAndWait();
	}
}
