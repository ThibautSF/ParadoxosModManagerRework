/**
 *
 */
package com.pmm.ParadoxosGameModManager.window;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * @author tsimo
 *
 */
public class Utils {

	public static void setTooltip(Node node, Tooltip tl) {
		// Remove delay to show tooltip when mouse over button
		node.setOnMouseEntered(event -> {
			Point2D p = node.localToScreen(node.getLayoutBounds().getMaxX(), node.getLayoutBounds().getMaxY());
			if (tl != null) {
				tl.show(node, p.getX(), p.getY());
			}
		});
		// Hide the tooltip when mouse leave button
		node.setOnMouseExited(event -> {
			if (tl != null) {
				tl.hide();
			}
		});
	}
}
