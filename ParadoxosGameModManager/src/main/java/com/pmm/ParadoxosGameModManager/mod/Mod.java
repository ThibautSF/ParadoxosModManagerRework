package com.pmm.ParadoxosGameModManager.mod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.pmm.ParadoxosGameModManager.ModManager;
import com.pmm.ParadoxosGameModManager.debug.ErrorPrint;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class Mod {
	private SimpleStringProperty fileName;
	private SimpleStringProperty name;
	private SimpleStringProperty versionCompatible;
	private SimpleStringProperty remoteFileID;
	private SimpleStringProperty steamPath;
	private SimpleStringProperty dirPath;
	private SimpleStringProperty archivePath;
	private SimpleStringProperty realModDirectoryPath;
	private boolean missing;
	private Set<String> modifiedFiles = new HashSet<>();

	private final List<String> filterDir = Arrays.asList(".git");
	private final List<String> filterFile = Arrays.asList();

	/**
	 *
	 * @param filename
	 * @param computeConflicts
	 */
	public Mod(String filename, boolean computeConflicts) {
		this(filename, null, computeConflicts);
	}

	/**
	 *
	 * @param filename
	 * @param remoteFileID
	 * @param computeModifiedFiles
	 */
	public Mod(String filename, String remoteFileID, boolean computeModifiedFiles) {
		try {
			Integer.parseInt(filename);
			this.fileName = new SimpleStringProperty("ugc_" + filename + ".mod");
		} catch (Exception e) {
			this.fileName = new SimpleStringProperty(filename);
		}

		if (remoteFileID != null) {
			this.remoteFileID = new SimpleStringProperty(remoteFileID);
			this.steamPath = new SimpleStringProperty(
					"https://steamcommunity.com/sharedfiles/filedetails/?id=" + this.remoteFileID.get());
		} else {
			this.remoteFileID = new SimpleStringProperty("");
			this.steamPath = new SimpleStringProperty("No remote ID found");
		}

		this.versionCompatible = new SimpleStringProperty("?");
		this.name = this.fileName;

		try {
			readFileMod();
			this.missing = false;
			if (computeModifiedFiles) {
				setModifiedFiles();
			}
		} catch (IOException e) {
			this.missing = true;
			this.name = new SimpleStringProperty("MOD MISSING");
			this.versionCompatible = new SimpleStringProperty("");
			ErrorPrint.printError("Unable to open " + ModManager.PATH + "mod" + File.separator + filename
					+ " ! File is missing or corrupted !");
			// e.printStackTrace();
		}
	}

	public Mod(String filename) {
		this.name = new SimpleStringProperty("MOD MISSING");
		this.fileName = new SimpleStringProperty(filename);
		this.remoteFileID = new SimpleStringProperty("");
		this.steamPath = new SimpleStringProperty("No remote ID found");
		this.versionCompatible = new SimpleStringProperty("?");
		this.missing = true;
	}

	public Mod(String modName, String filename, String remoteFileID) {
		this.name = new SimpleStringProperty("MOD MISSING : " + modName);
		this.fileName = new SimpleStringProperty(filename);
		if ((remoteFileID != null) && !"".equals(remoteFileID)) {
			this.remoteFileID = new SimpleStringProperty(remoteFileID);
			this.steamPath = new SimpleStringProperty(
					"https://steamcommunity.com/sharedfiles/filedetails/?id=" + this.remoteFileID.get());
		} else {
			this.remoteFileID = new SimpleStringProperty("");
			this.steamPath = new SimpleStringProperty("No remote ID found");
		}
		this.versionCompatible = new SimpleStringProperty("?");
		this.missing = true;
	}

	/**
	 * @throws IOException
	 */
	private void readFileMod() throws IOException {
		String sep = File.separator;
		Pattern p = Pattern.compile("\\\".*?\\\"");
		Matcher m;

		Path pth = Paths.get(ModManager.PATH + "mod" + sep + fileName.get());
		List<String> lines = Files.readAllLines(pth);
		for (String line : lines) {
			String lineWFirstChar = (line.length() > 0) ? line.substring(1, line.length()) : "";
			if (line.matches("\\s*name\\s*=.*") || lineWFirstChar.matches("\\s*name\\s*=.*")) {
				m = p.matcher(line);
				if (m.find()) {
					name = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length() - 1));
				}
			} else if (line.matches("\\s*path\\s*=.*") || lineWFirstChar.matches("\\s*path\\s*=.*")) {
				m = p.matcher(line);
				if (m.find()) {
					String s = (String) m.group().subSequence(1, m.group().length() - 1);
					File dir = new File(s);
					if (dir.exists()) {
						dirPath = new SimpleStringProperty(dir.getAbsolutePath());
						realModDirectoryPath = new SimpleStringProperty(dir.getAbsolutePath());
					} else {
						// maybe path was relative
						dir = new File(ModManager.PATH + s);
						if (dir.exists()) {
							dirPath = new SimpleStringProperty(dir.getAbsolutePath());
							realModDirectoryPath = new SimpleStringProperty(dir.getAbsolutePath());
						}
					}
				}
			} else if (line.matches("\\s*archive\\s*=.*") || lineWFirstChar.matches("\\s*archive\\s*=.*")) {
				m = p.matcher(line);
				if (m.find()) {
					String s = (String) m.group().subSequence(1, m.group().length() - 1);
					File archive = new File(s);
					if (archive.exists()) {
						archivePath = new SimpleStringProperty(archive.getAbsolutePath());
						realModDirectoryPath = new SimpleStringProperty(archive.getParentFile().getAbsolutePath());
					} else {

						// maybe path was relative
						archive = new File(ModManager.PATH + s);
						if (archive.exists()) {
							archivePath = new SimpleStringProperty(archive.getAbsolutePath());
							realModDirectoryPath = new SimpleStringProperty(archive.getParentFile().getAbsolutePath());
						}
					}
				}
			} else if (line.matches("\\s*supported_version\\s*=.*")
					|| lineWFirstChar.matches("\\s*supported_version\\s*=.*")) {
				m = p.matcher(line);
				if (m.find()) {
					versionCompatible = new SimpleStringProperty(
							(String) m.group().subSequence(1, m.group().length() - 1));
				}
			} else if (line.matches("\\s*remote_file_id\\s*=.*")
					|| lineWFirstChar.matches("\\s*remote_file_id\\s*=.*")) {
				m = p.matcher(line);
				if (m.find()) {
					remoteFileID = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length() - 1));
					this.steamPath = new SimpleStringProperty(
							"https://steamcommunity.com/sharedfiles/filedetails/?id=" + this.remoteFileID.get());
				}
			}
		}

	}

	private void setModifiedFiles() {
		String dirOrArchivePath = (dirPath != null) ? dirPath.get()
				: ((archivePath != null) ? archivePath.get() : null);
		if ((dirOrArchivePath == null) || dirOrArchivePath.length() < 2) {
			ErrorPrint.printError("Unable to find mod files");
			return;
		}

		// Should be useless now (relative path is detected on .mod reading + ':' is
		// only windows
//		if (dirOrArchivePath.charAt(1) != ':')
//		{
//			// The path was relative
//			dirOrArchivePath = ModManager.PATH + dirOrArchivePath;
//		}

		// TODO maybe use File methods (isFile OR isDirectory)
		if (dirOrArchivePath.endsWith(".zip")) {
			addModifiedFiles(dirOrArchivePath);
		} else {
			addModifiedFiles(new File(dirOrArchivePath), "");
		}
	}

	private void addModifiedFiles(File directory, String relativeDirPath) {
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (filterFile.contains(file.getName()))
					return false;

				File parent = file.getParentFile();
				while (parent != null) {
					if (filterDir.contains(parent.getName()))
						return false;
					parent = parent.getParentFile();
				}

				return true;
			}
		});
		if (files == null) {
			ErrorPrint.printError("Unable to find mod files from the directory");
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				String newRelativeDirPath = "".equals(relativeDirPath) ? file.getName()
						: relativeDirPath + File.separator + file.getName();
				addModifiedFiles(file, newRelativeDirPath);
			} else if (!"".equals(relativeDirPath)) {
				// Don't consider files in the root mod directory
				modifiedFiles.add(relativeDirPath + File.separator + file.getName());
			}
		}
	}

	private void addModifiedFiles(String dirOrArchivePath) {
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		try {
			fis = new FileInputStream(dirOrArchivePath);
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while (true) {
				ZipEntry zEntry = null;
				try {
					zEntry = zipIs.getNextEntry();
				} catch (ZipException e) {
					ErrorPrint.printError("Unable to unzip some files of " + dirOrArchivePath);
					break;
				}
				if (zEntry == null) {
					break;
				}
				if (zEntry.isDirectory()) {
					continue;
				}
				String fileRelativePath = zEntry.getName();

				File file = new File(fileRelativePath);
				if (filterFile.contains(file.getName())) {
					continue;
				}

				File parent = file.getParentFile();
				boolean hasFilteredDir = false;
				while (parent != null) {
					if (filterDir.contains(parent.getName())) {
						hasFilteredDir = true;
						break;
					}
					parent = parent.getParentFile();
				}
				if (hasFilteredDir) {
					continue;
				}

				fileRelativePath = fileRelativePath.replace('/', '\\');
				if (fileRelativePath.contains("\\")) {
					// Don't consider files in the root mod directory
					modifiedFiles.add(fileRelativePath);
				}
			}
		} catch (IOException e) {
			ErrorPrint.printError("Unable to unzip " + dirOrArchivePath);
		} finally {
			try {
				if (zipIs != null) {
					zipIs.close();
				}
			} catch (IOException e1) {
			}
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e1) {
			}
		}
	}

	//
	// Getters and Setters
	//

	/**
	 * @return
	 */
	public String getFileName() {
		return fileName.get();
	}

	/**
	 * @return
	 */
	public String getName() {
		return name.get();
	}

	/**
	 * @return
	 */
	public String getVersionCompatible() {
		return versionCompatible.get();
	}

	/**
	 * @return
	 */
	public String getRemoteFileID() {
		return remoteFileID.get();
	}

	/**
	 * @return
	 */
	public String getSteamPath() {
		return steamPath.get();
	}

	/**
	 * @return
	 */
	public String getSteamInAppPath() {
		return "steam://url/CommunityFilePage/" + remoteFileID.get();
	}

	/**
	 * @return
	 */
	public String getModDirPath() {
		if (realModDirectoryPath != null)
			return realModDirectoryPath.get();

		return "";
	}

	//
	// Methods
	//
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mod mod = (Mod) obj;
		return (fileName.get().equals(mod.getFileName()));
	}

	/**
	 * @return
	 */
	public boolean isMissing() {
		return missing;
	}

	public Set<String> getModifiedFiles() {
		return modifiedFiles;
	}
}
