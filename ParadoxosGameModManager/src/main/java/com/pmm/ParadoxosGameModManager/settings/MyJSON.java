package com.pmm.ParadoxosGameModManager.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pmm.ParadoxosGameModManager.ModManager;
import com.pmm.ParadoxosGameModManager.mod.Languages;
import com.pmm.ParadoxosGameModManager.mod.Mod;
import com.pmm.ParadoxosGameModManager.mod.ModList;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class MyJSON {
	private static final String USER_LISTS = "userlists";
	private static final String EXPORTED_LIST = "exportedlist";
	private static final String GAME_ID = "gameID";
	private static final String LIST = "list";
	private static final String NAME = "name";
	private static final String DESCR = "descr";
	private static final String CUSTOM_ORDER = "customOrder";
	private static final String LAUNCHARGS = "launchargs";
	private static final String LANG = "lang";
	private static final String MOD = "mod";
	private static final String ID = "id";
	private static final String FILE_NAME = "fileName";
	private static final String REMOTE_ID = "remoteID";
	private static final String MOD_NAME = "modName";
	private static final String MOD_ORDER = "order";

	private static final String APP_SETTINGS = "appsettings";

	private JsonObject root;
	private File file;

	/**
	 * @param file
	 * @throws IOException
	 */
	public void readFile(String file) throws IOException {
		Gson gson = new Gson();
		System.out.println(file);
		File json = new File(file);

		this.file = json;

		if (json.exists()) {
			FileReader fileReader = new FileReader(json);

			root = gson.fromJson(fileReader, JsonObject.class);

			fileReader.close();
		} else {
			root = new JsonObject();
			root.add(USER_LISTS, new JsonArray());

			if (json.getParentFile() != null) {
				json.getParentFile().mkdirs();
			}
			json.createNewFile();
			saveFile();
		}
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void readSettingFile(String file) throws IOException {
		Gson gson = new Gson();
		File json = new File(file);

		this.file = json;

		if (json.exists()) {
			FileReader fileReader = new FileReader(json);

			root = gson.fromJson(fileReader, JsonObject.class);

			fileReader.close();
		} else {
			root = new JsonObject();
			root.add(APP_SETTINGS, new JsonObject());

			if (json.getParentFile() != null) {
				json.getParentFile().mkdirs();
			}
			json.createNewFile();
			saveFile();
		}
	}

	/**
	 * @throws IOException
	 */
	public void saveFile() throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(root.toString());
		fileWriter.close();
	}

	/**
	 * @param availableMods
	 * @param oneListElement
	 * @return
	 */
	private ModList getModList(Map<String, Mod> availableMods, JsonObject oneListElement) {
		List<Mod> listMods = new ArrayList<>();
		Map<Integer, Mod> sortedMods = new TreeMap<>();
		List<Mod> unsortedMods = new ArrayList<>();

		String listName = oneListElement.get(NAME).getAsString();

		boolean listCustomOrder = oneListElement.get(CUSTOM_ORDER).getAsBoolean();

		String listDescr = oneListElement.get(DESCR).getAsString();

		String launchArgs = oneListElement.get(LAUNCHARGS).getAsString();

		String listLang = oneListElement.get(LANG).getAsString();

		JsonArray mods = oneListElement.getAsJsonArray(MOD);
		Iterator<JsonElement> j = mods.iterator();

		while (j.hasNext()) {
			JsonObject mod_json = j.next().getAsJsonObject();

			String fileName = "", modName = "", remoteFileId = null;
			int modOrder = -1;

			for (String attribute : mod_json.keySet()) {
				switch (attribute) {
				case ID:
				case FILE_NAME:
					fileName = mod_json.get(attribute).getAsString();
					break;
				case MOD_NAME:
					modName = mod_json.get(attribute).getAsString();
					break;
				case REMOTE_ID:
					remoteFileId = mod_json.get(attribute).getAsString();
					break;
				case MOD_ORDER:
					modOrder = mod_json.get(attribute).getAsInt();
					break;

				default:
					break;
				}
			}

			Mod oneMod = availableMods.get(fileName);
			if (oneMod == null) {
				oneMod = new Mod(modName, fileName, remoteFileId);
				if (remoteFileId != null && !"".equals(remoteFileId)) {
					for (Mod mod : availableMods.values()) {
						if (mod.getRemoteFileID().equals(remoteFileId)) {
							oneMod = mod;
						}
					}
				}
			}
			if (!unsortedMods.contains(oneMod) && !sortedMods.values().contains(oneMod)) {
				if (modOrder >= 0) {
					sortedMods.put(modOrder, oneMod);
				} else {
					unsortedMods.add(oneMod);
				}
			}
		}

		// Append mods with order value
		listMods.addAll(sortedMods.values());

		// Append mods without order value at the end of the list
		Collections.sort(unsortedMods, new Comparator<Mod>() {
			@Override
			public int compare(Mod m1, Mod m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});

		listMods.addAll(unsortedMods);

		return new ModList(listName, listDescr, Languages.getLanguage(listLang), listMods, listCustomOrder, launchArgs);
	}

	/**
	 * @return
	 */
	public List<ModList> getSavedList(Map<String, Mod> availableMods) {
		List<ModList> userLists = new ArrayList<>();

		JsonArray modLists = root.get(USER_LISTS).getAsJsonArray();
		Iterator<JsonElement> i = modLists.iterator();

		while (i.hasNext()) {
			JsonObject oneListElement = i.next().getAsJsonObject();

			ModList oneList = getModList(availableMods, oneListElement);

			userLists.add(oneList);
		}

		return userLists;
	}

	/**
	 * @param listName
	 * @throws IOException
	 */
	public void removeList(String listName) throws IOException {
		JsonArray modLists = root.get(USER_LISTS).getAsJsonArray();
		Iterator<JsonElement> i = modLists.iterator();

		while (i.hasNext()) {
			JsonObject oneListElement = i.next().getAsJsonObject();

			String listElementName = oneListElement.get(NAME).getAsString();
			if (listElementName.equals(listName)) {
				modLists.remove(oneListElement);

				break;
			}
		}

		this.saveFile();
	}

	/**
	 * @param availableMods
	 * @param listName
	 */
	public ModList getList(Map<String, Mod> availableMods, String listName) {
		ModList list = null;

		JsonArray modLists = root.get(USER_LISTS).getAsJsonArray();
		Iterator<JsonElement> i = modLists.iterator();

		while (i.hasNext()) {
			JsonObject oneListElement = i.next().getAsJsonObject();

			String listElementName = oneListElement.get(NAME).getAsString();
			if (listElementName.equals(listName)) {
				list = getModList(availableMods, oneListElement);
			}
		}

		return list;
	}

	/**
	 * @param list
	 * @throws IOException
	 */
	public void modifyList(ModList list) throws IOException {
		modifyList(list, null);
	}

	/**
	 * @param list
	 * @param listName
	 * @throws IOException
	 */
	public void modifyList(ModList list, String listName) throws IOException {
		JsonObject oneListElement = null, listModElement;
		JsonArray listModArray = new JsonArray();
		// JsonElement listDescrElement, listArgsElement, listLangElement;
		JsonArray modLists = root.get(USER_LISTS).getAsJsonArray();

		List<Mod> listMods;

		boolean isNew = (listName != null) ? false : true;

		if (!isNew) {

			Iterator<JsonElement> i = modLists.iterator();

			while (i.hasNext()) {
				JsonObject oneListElementIterated = i.next().getAsJsonObject();

				String listElementName = oneListElementIterated.get(NAME).getAsString();
				if (listElementName.equals(listName)) {
					oneListElement = oneListElementIterated;

					break;
				}
			}
		}

		if (oneListElement != null) {
			oneListElement.remove(MOD);
		} else {
			oneListElement = new JsonObject();
			modLists.add(oneListElement);
		}

		oneListElement.add(NAME, new JsonPrimitive(list.getName()));
		oneListElement.add(DESCR, new JsonPrimitive(list.getDescription()));

		oneListElement.add(CUSTOM_ORDER, new JsonPrimitive(list.isCustomOrder()));

		oneListElement.add(LAUNCHARGS, new JsonPrimitive(list.getLaunchArgs()));
		oneListElement.add(LANG, new JsonPrimitive(list.getLanguageName()));

		listMods = list.getModlist();
		oneListElement.add(MOD, listModArray);
		for (int i = 0; i < listMods.size(); i++) {
			Mod mod = listMods.get(i);

			listModElement = new JsonObject();
			listModElement.add(MOD_NAME, new JsonPrimitive(mod.getName()));
			listModElement.add(FILE_NAME, new JsonPrimitive(mod.getFileName()));
			listModElement.add(REMOTE_ID, new JsonPrimitive(mod.getRemoteFileID()));
			listModElement.add(MOD_ORDER, new JsonPrimitive(i));

			listModArray.add(listModElement);
		}

		this.saveFile();
	}

	/**
	 * @param listName
	 * @throws IOException
	 */
	public void exportList(String game, String listName) throws IOException {
		JsonArray modLists = root.get(USER_LISTS).getAsJsonArray();
		Iterator<JsonElement> i = modLists.iterator();

		while (i.hasNext()) {
			JsonObject oneListElement = i.next().getAsJsonObject();
			String listElementName = oneListElement.get(NAME).getAsString();

			if (listElementName.equals(listName)) {
				JsonObject newroot = new JsonObject();
				newroot.add(GAME_ID, new JsonPrimitive(ModManager.STEAM_ID));
				newroot.add(EXPORTED_LIST, oneListElement.deepCopy());

				String exportFileName = "Export_" + game + "_" + listName + ".xml";

				FileWriter fileWriter = new FileWriter(this.file.getParentFile() + File.separator + exportFileName);
				fileWriter.write(newroot.toString());
				fileWriter.close();

				break;
			}
		}
	}

	/**
	 * @param file
	 * @param availableMods
	 * @return
	 * @throws IOException
	 */
	public String importList(String file, Map<String, Mod> availableMods) throws IOException {
		Gson gson = new Gson();
		File json = new File(file);

		if (json.exists()) {
			FileReader fileReader = new FileReader(json);

			JsonObject importRoot = gson.fromJson(fileReader, JsonObject.class);

			fileReader.close();

			if (importRoot.get(GAME_ID).getAsInt() == ModManager.STEAM_ID) {
				JsonArray modLists = importRoot.get(LIST).getAsJsonArray();
				Iterator<JsonElement> i = modLists.iterator();

				while (i.hasNext()) {

					JsonObject oneListElement = i.next().getAsJsonObject();

					ModList oneList = getModList(availableMods, oneListElement);
					oneList.setName("[Imported]" + oneList.getName());

					modifyList(oneList);
				}

				return "Import done.";
			}

			return "Import procedure aborted, this list is not for the current game !";
		}

		return "Error, file '" + file + "' not found.";
	}

	/**
	 * @param gameLabel
	 * @return
	 */
	public HashMap<String, String> getGameSettings(String gameLabel) {
		HashMap<String, String> params = new HashMap<>();

		JsonObject appsettings = root.get(APP_SETTINGS).getAsJsonObject();
		JsonObject gamesettings = appsettings.get(gameLabel).getAsJsonObject();

		for (String key : gamesettings.keySet()) {
			params.put(key, gamesettings.get(key).getAsString());
		}

		return params;
	}

	/**
	 * @param gameLabel
	 * @param attrName
	 * @return
	 */
	public String getOneGameSetting(String gameLabel, String attrName) {
		JsonObject appsettings = root.get(APP_SETTINGS).getAsJsonObject();

		if (appsettings.has(gameLabel)) {
			JsonObject gamesettings = appsettings.get(gameLabel).getAsJsonObject();

			for (String key : gamesettings.keySet()) {
				if (key.equals(attrName))
					return gamesettings.get(key).getAsString();
			}
		}

		return null;
	}

	/**
	 * @param gameLabel
	 * @param attrName
	 * @param value
	 * @throws IOException
	 */
	public void modifyGameSettings(String gameLabel, String attrName, String value) throws IOException {
		JsonObject appsettings = root.get(APP_SETTINGS).getAsJsonObject();

		JsonObject gamesettings;
		if (appsettings.has(gameLabel)) {
			gamesettings = appsettings.get(gameLabel).getAsJsonObject();

		} else {
			gamesettings = new JsonObject();
			appsettings.add(gameLabel, gamesettings);
		}

		gamesettings.add(attrName, new JsonPrimitive(value));

		saveFile();
	}
}
