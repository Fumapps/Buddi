/*
 * Created on Jun 26, 2005 by wyatt
 */
package org.homeunix.thecave.buddi;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.homeunix.thecave.buddi.view.mvvm.main.MainView;
import org.homeunix.thecave.buddi.view.mvvm.main.MainViewModel;

import ca.digitalcave.moss.common.Version;

/**
 * The main class, containing the launch methods for Buddi.
 */
public class Buddi extends Application {

	private static Version version = null;

	@Override
	public void start(Stage primaryStage) throws Exception {
		MainViewModel viewModel = new MainViewModel();
		MainView view = new MainView();

		viewModel.initialize();
		view.bind(viewModel);

		Scene scene = new Scene(view.getRoot(), 800, 600);

		primaryStage.setScene(scene);
		primaryStage.setTitle(viewModel.getTitle());

		// Bind title
		viewModel.titleProperty().addListener((obs, oldVal, newVal) -> primaryStage.setTitle(newVal));

		primaryStage.show();
	}

	public static Version getVersion() {
		if (version == null)
			version = Version.getVersionResource("version.txt");
		return version;
	}

	/**
	 * Main method for Buddi.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Launch JavaFX Application
		launch(args);
	}

	// --- Legacy Static Methods (Restored for Compilation) ---

	public static void doUpdateCheck(ca.digitalcave.moss.swing.MossFrame frame) {
		// Stub for compilation
	}

	public static void startUpdateCheck(ca.digitalcave.moss.swing.MossFrame frame) {
		// Stub for compilation
	}

	private static String pluginsFolder;
	private static String languagesFolder;
	private static String reportsFolder;
	private static Boolean noAutoSave = false;
	private static Boolean debian = false;
	private static Boolean windowsInstaller = false;
	private static Boolean genericUnix = false;
	private static Boolean slackware = false;
	private static Boolean redhat = false;
	private static Boolean legacy = false;

	public static boolean isUnix() {
		if (genericUnix == null)
			genericUnix = false;
		return genericUnix;
	}

	public static boolean isLegacy() {
		if (legacy == null)
			legacy = false;
		return legacy;
	}

	public static boolean isSlackware() {
		if (slackware == null)
			slackware = false;
		return slackware;
	}

	public static boolean isWindowsInstaller() {
		if (windowsInstaller == null)
			windowsInstaller = false;
		return windowsInstaller;
	}

	public static boolean isRedhat() {
		if (redhat == null)
			redhat = false;
		return redhat;
	}

	public static boolean isDebian() {
		if (debian == null)
			debian = false;
		return debian;
	}

	private static boolean isAutoSave() {
		if (noAutoSave == null)
			noAutoSave = false;
		return !noAutoSave;
	}

	public static File getPluginsFolder() {
		if (pluginsFolder == null)
			pluginsFolder = ca.digitalcave.moss.common.OperatingSystemUtil.getUserFolder("Buddi") + File.separator
					+ Const.PLUGIN_FOLDER;
		return new File(pluginsFolder);
	}

	public static File getReportsFolder() {
		if (reportsFolder == null)
			reportsFolder = ca.digitalcave.moss.common.OperatingSystemUtil.getUserFolder("Buddi") + File.separator
					+ Const.REPORT_FOLDER;
		return new File(reportsFolder);
	}

	public static File getLanguagesFolder() {
		if (languagesFolder == null)
			languagesFolder = ca.digitalcave.moss.common.OperatingSystemUtil.getUserFile("Buddi", Const.LANGUAGE_FOLDER)
					.getAbsolutePath();
		return new File(languagesFolder);
	}
}
