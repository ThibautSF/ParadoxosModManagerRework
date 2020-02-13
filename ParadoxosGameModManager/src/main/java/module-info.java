module com.pmm.ParadoxosGameModManager {
	requires java.desktop;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires com.google.gson;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.junit.jupiter.api;

	exports com.pmm.ParadoxosGameModManager;
	exports com.pmm.ParadoxosGameModManager.debug;
	exports com.pmm.ParadoxosGameModManager.mod;
	exports com.pmm.ParadoxosGameModManager.settings;
	exports com.pmm.ParadoxosGameModManager.versioning;
	exports com.pmm.ParadoxosGameModManager.window;
}