package com.pmm.ParadoxosGameModManager.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.pmm.ParadoxosGameModManager.ModManager;
import com.pmm.ParadoxosGameModManager.debug.ErrorPrint;
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
	private static final String CUSTOM_ORDER = "customOrder";
	private static final String DESCR = "descr";
	private static final String LAUNCHARGS = "launchargs";
	private static final String LANG = "lang";
	private static final String MOD = "mod";
	private static final String ID = "id";
	private static final String FILE_NAME = "fileName";
	private static final String REMOTE_ID = "remoteID";
	private static final String MOD_NAME = "modName";
	private static final String MOD_ORDER = "order";

	private static final String APP_SETTINGS = "appsettings";
	private static final String GAME = "game";
	private static final String ATTR_GAMELABEL = "gamelabel";
	private static final String ATTR_VALUE = "value";

	private static Element root;
	private static org.jdom2.Document document;
	private static Element root_exported;
	private static org.jdom2.Document document_exported;
	private String file;

	/**
	 * @param file
	 * @throws IOException
	 * @throws JDOMException
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public void readFile(String file) throws JDOMException, IOException, ParserConfigurationException {
		SAXBuilder sxb = new SAXBuilder();
		File xml = new File(file);
		if (xml.exists()) {
			document = sxb.build(xml);
			root = document.getRootElement();
		} else {
			root = new Element(USER_LISTS);
			document = new Document(root);
		}

		// Init for export lists
		root_exported = new Element(EXPORTED_LIST);
		root_exported.setAttribute(GAME_ID, ModManager.STEAM_ID.toString());
		document_exported = new Document(root_exported);

		this.file = file;
	}

	/**
	 * @param file
	 * @throws Exception
	 */
	public void readSettingFile(String file) throws Exception {
		SAXBuilder sxb = new SAXBuilder();
		File xml = new File(file);
		if (xml.exists()) {
			document = sxb.build(xml);
			root = document.getRootElement();
		} else {
			root = new Element(APP_SETTINGS);
			document = new Document(root);
		}

		this.file = file;
	}

	/**
	 * @throws Exception
	 */
	public void saveFile() throws Exception {
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		sortie.output(document, new FileOutputStream(file));
	}

	/**
	 * @return
	 */
	public List<ModList> getSavedList(Map<String, Mod> availableMods) {
		List<ModList> userLists = new ArrayList<>();
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> i = modLists.iterator();
		while (i.hasNext()) {
			List<Mod> listMods = new ArrayList<>();
			Map<Integer, Mod> sortedMods = new TreeMap<>();
			List<Mod> unsortedMods = new ArrayList<>();

			Element oneListElement = i.next();
			String listName = oneListElement.getAttribute(NAME).getValue();

			boolean listCustomOrder = false;
			try {
				Attribute customOrderAttribute = oneListElement.getAttribute(CUSTOM_ORDER);
				if (customOrderAttribute != null) {
					listCustomOrder = customOrderAttribute.getBooleanValue();
				}
			} catch (DataConversionException e) {
				// Bad value
			}

			String listDescr = "";
			Element listDescrElement = oneListElement.getChild(DESCR);
			if (listDescrElement != null)
				listDescr = listDescrElement.getText();

			String launchArgs = "";
			Element listArgsElement = oneListElement.getChild(LAUNCHARGS);
			if (listArgsElement != null)
				launchArgs = listArgsElement.getText();

			String listLang = null;
			Element listLangElement = oneListElement.getChild(LANG);
			if (listLangElement != null)
				listLang = listLangElement.getText();

			List<Element> modsElements = oneListElement.getChildren(MOD);
			for (Element modElement : modsElements) {
				List<Attribute> modElementAttr = modElement.getAttributes();
				String fileName = "", modName = "", remoteFileId = null;
				int modOrder = -1;
				for (Attribute attribute : modElementAttr) {
					switch (attribute.getName()) {
					case ID:
					case FILE_NAME:
						fileName = attribute.getValue();
						break;
					case MOD_NAME:
						modName = attribute.getValue();
						break;
					case REMOTE_ID:
						remoteFileId = attribute.getValue();
						break;
					case MOD_ORDER:
						try {
							modOrder = attribute.getIntValue();
						} catch (DataConversionException e) {
							ErrorPrint.printError(e, "When reading mod order attribute (import)");
							e.printStackTrace();
						}
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

			ModList oneList = new ModList(listName, listDescr, Languages.getLanguage(listLang), listMods,
					listCustomOrder, launchArgs);
			userLists.add(oneList);
		}
		return userLists;
	}

	/**
	 * @param listName
	 * @throws Exception
	 */
	public void removeList(String listName) throws Exception {
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> iE = modLists.iterator();
		while (iE.hasNext()) {
			Element oneListElement = iE.next();
			String listElementName = oneListElement.getAttribute(NAME).getValue();
			if (listElementName.equals(listName)) {
				root.removeContent(oneListElement);
				break;
			}
		}
		this.saveFile();
	}

	/**
	 * @param list
	 * @throws Exception
	 */
	public void modifyList(ModList list) throws Exception {
		modifyList(list, null);
	}

	/**
	 * @param list
	 * @param listName
	 * @throws Exception
	 */
	public void modifyList(ModList list, String listName) throws Exception {
		Element oneListElement = null, listDescrElement, listArgsElement, listLangElement, listModElement;
		List<Mod> listMods;

		boolean isNew = (listName != null) ? false : true;

		if (!isNew) {
			List<Element> modLists = root.getChildren(LIST);
			Iterator<Element> i = modLists.iterator();
			while (i.hasNext()) {
				Element oneListElementIterated = i.next();
				String listElementName = oneListElementIterated.getAttribute(NAME).getValue();
				if (listElementName.equals(listName)) {
					oneListElement = oneListElementIterated;
					break;
				}
			}
		}

		if (oneListElement != null) {
			oneListElement.removeChildren(MOD);
		} else {
			oneListElement = new Element(LIST);
			root.addContent(oneListElement);
		}

		listDescrElement = oneListElement.getChild(DESCR);
		if (listDescrElement == null) {
			listDescrElement = new Element(DESCR);
			oneListElement.addContent(listDescrElement);
		}

		listLangElement = oneListElement.getChild(LANG);
		if (listLangElement == null) {
			listLangElement = new Element(LANG);
			oneListElement.addContent(listLangElement);
		}

		listArgsElement = oneListElement.getChild(LAUNCHARGS);
		if (listArgsElement == null) {
			listArgsElement = new Element(LAUNCHARGS);
			oneListElement.addContent(listArgsElement);
		}

		oneListElement.setAttribute(NAME, list.getName());
		oneListElement.setAttribute(CUSTOM_ORDER, list.isCustomOrder() + "");

		listDescrElement.setText(list.getDescription());
		listArgsElement.setText(list.getLaunchArgs());
		listLangElement.setText(list.getLanguageName());

		listMods = list.getModlist();
		for (int i = 0; i < listMods.size(); i++) {
			Mod mod = listMods.get(i);
			listModElement = new Element(MOD);
			listModElement.setAttribute(MOD_NAME, mod.getName());
			listModElement.setAttribute(FILE_NAME, mod.getFileName());
			listModElement.setAttribute(REMOTE_ID, mod.getRemoteFileID());
			listModElement.setAttribute(MOD_ORDER, i + "");
			oneListElement.addContent(listModElement);
		}

		this.saveFile();
	}

	/**
	 * @param listName
	 * @throws Exception
	 */
	public void exportList(String listName) throws Exception {
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> iE_export = modLists.iterator();
		while (iE_export.hasNext()) {
			Element oneListElement = iE_export.next();
			String listElementName = oneListElement.getAttribute(NAME).getValue();
			if (listElementName.equals(listName)) {
				root_exported.addContent(oneListElement.detach());
				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
				String exportFileName = "Export_" + ModManager.GAME + "_" + listName + ".xml";
				sortie.output(document_exported,
						new FileOutputStream(ModManager.xmlDir + File.separator + exportFileName));
				break;
			}
		}
	}

	public String importList(String xml, Map<String, Mod> availableMods) throws Exception {
		SAXBuilder sxb = new SAXBuilder();
		Document importDocument = sxb.build(xml);
		Element importRoot = importDocument.getRootElement();
		if (importRoot.getAttribute(GAME_ID).getValue().equals(ModManager.STEAM_ID.toString())) {
			List<Element> modLists = importRoot.getChildren(LIST);
			Iterator<Element> i = modLists.iterator();
			while (i.hasNext()) {
				List<Mod> listMods = new ArrayList<>();
				Map<Integer, Mod> sortedMods = new TreeMap<>();
				List<Mod> unsortedMods = new ArrayList<>();

				Element oneListElement = i.next();
				String listName = oneListElement.getAttribute(NAME).getValue();

				boolean listCustomOrder = false;
				try {
					Attribute customOrderAttribute = oneListElement.getAttribute(CUSTOM_ORDER);
					if (customOrderAttribute != null) {
						listCustomOrder = customOrderAttribute.getBooleanValue();
					}
				} catch (DataConversionException e) {
					// Bad value
				}

				String listDescr = "";
				Element listDescrElement = oneListElement.getChild(DESCR);
				if (listDescrElement != null)
					listDescr = listDescrElement.getText();

				String launchArgs = "";
				Element listArgsElement = oneListElement.getChild(LAUNCHARGS);
				if (listArgsElement != null)
					launchArgs = listArgsElement.getText();

				String listLang = null;
				Element listLangElement = oneListElement.getChild(LANG);
				if (listLangElement != null)
					listLang = listLangElement.getText();

				List<Element> modsElements = oneListElement.getChildren(MOD);
				for (Element modElement : modsElements) {
					List<Attribute> modElementAttr = modElement.getAttributes();
					String fileName = "", modName = "", remoteFileId = null;
					int modOrder = -1;
					for (Attribute attribute : modElementAttr) {
						switch (attribute.getName()) {
						case ID:
						case FILE_NAME:
							fileName = attribute.getValue();
							break;
						case MOD_NAME:
							modName = attribute.getValue();
							break;
						case REMOTE_ID:
							remoteFileId = attribute.getValue();
							break;
						case MOD_ORDER:
							try {
								modOrder = attribute.getIntValue();
							} catch (DataConversionException e) {
								ErrorPrint.printError(e, "When reading mod order attribute (import)");
								e.printStackTrace();
							}
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

				ModList oneList = new ModList("[Imported]" + listName + "_" + System.currentTimeMillis(), listDescr,
						Languages.getLanguage(listLang), listMods, listCustomOrder, launchArgs);
				modifyList(oneList);
			}
			return "Import done.";
		}
		return "Import procedure aborted, this list is not for the current game !";
	}

	/**
	 * @throws DataConversionException
	 *
	 */
	public HashMap<String, String> getGameSettings(String gameLabel) throws DataConversionException {
		HashMap<String, String> params = new HashMap<>();
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		while (i.hasNext()) {
			Element oneListElement = i.next();

			if (gameLabel.equals(oneListElement.getAttributeValue(ATTR_GAMELABEL))) {
				List<Element> gameParamsElements = oneListElement.getChildren();
				for (Element element : gameParamsElements) {
					params.put(element.getName(), element.getAttributeValue(ATTR_VALUE));
				}
				break;
			}
		}
		return params;
	}

	/**
	 * @throws DataConversionException
	 *
	 */
	public String getOneGameSetting(String gameLabel, String attrName) throws DataConversionException {
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		while (i.hasNext()) {
			Element oneListElement = i.next();

			if (gameLabel.equals(oneListElement.getAttributeValue(ATTR_GAMELABEL))) {
				List<Element> gameParamsElements = oneListElement.getChildren();
				for (Element element : gameParamsElements) {
					if (element.getName().equals(attrName)) {
						String value = element.getAttributeValue(ATTR_VALUE);
						return value;
					}
				}
			}
		}
		return null;
	}

	public void modifyGameSettings(String gameLabel, String attrName, String value) throws Exception {
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		Boolean flag_nogame = true, flag_noattrparam = true;
		while (i.hasNext()) {
			Element oneListElement = i.next();

			if (gameLabel.equals(oneListElement.getAttributeValue(ATTR_GAMELABEL))) {
				flag_nogame = false;
				List<Element> gameParamsElements = oneListElement.getChildren();
				Iterator<Element> j = gameParamsElements.iterator();

				while (j.hasNext()) {
					Element oneParamElement = j.next();

					if (attrName == oneParamElement.getName()) {
						flag_noattrparam = false;
						oneParamElement.setAttribute(ATTR_VALUE, value);

						break;
					}
				}

				if (flag_noattrparam) {
					Element newParamElement = new Element(attrName);
					newParamElement.setAttribute(ATTR_VALUE, value);
					oneListElement.addContent(newParamElement);
				}

				break;
			}
		}

		if (flag_nogame) {
			Element newGameElement = new Element(GAME);
			newGameElement.setAttribute(ATTR_GAMELABEL, gameLabel.toString());
			root.addContent(newGameElement);
			Element newParamElement = new Element(attrName);
			newParamElement.setAttribute(ATTR_VALUE, value);
			newGameElement.addContent(newParamElement);
		}

		saveFile();
	}
}
