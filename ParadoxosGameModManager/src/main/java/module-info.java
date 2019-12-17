module ParadoxosGameModManager {
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires com.google.gson;
	requires java.desktop;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.junit.jupiter.api;
	requires java.xml;

	exports com.pmm.ParadoxosGameModManager;
	exports com.pmm.ParadoxosGameModManager.mod;
	exports com.pmm.ParadoxosGameModManager.settings;
}