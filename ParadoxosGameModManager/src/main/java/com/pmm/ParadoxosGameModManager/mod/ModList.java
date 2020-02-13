package com.pmm.ParadoxosGameModManager.mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class ModList {
	//
	// Fields and Constructors
	//
	private SimpleStringProperty name;
	private SimpleStringProperty description;
	private SimpleStringProperty launchargs;
	private Languages language;
	private boolean customOrder;
	private List<Mod> modlist;
	private List<ModConflict> modConflicts;

	private static int MOD_NOT_IN_LIST = -1;

	/**
	 * @param name
	 * @param description
	 * @param language
	 * @param modlist
	 * @param customOrder
	 * @param launchargs
	 */
	public ModList(String name, String description, Languages language, List<Mod> modlist, boolean customOrder,
			String launchargs) {
		this.name = new SimpleStringProperty(name);
		this.description = new SimpleStringProperty(description);
		this.language = language;
		this.modlist = modlist;
		this.customOrder = customOrder;
		this.launchargs = new SimpleStringProperty(launchargs);
		computeConflicts(modlist);
	}

	public ModList(String name, String description, Languages language, List<Mod> modlist) {
		this(name, description, language, modlist, false, "");
	}

	//
	// Getters and Setters
	//
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		if (name != null) {
			name = name.trim();
		}
		this.name = new SimpleStringProperty(name);
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		if (description != null) {
			description = description.trim();
		}
		this.description = new SimpleStringProperty(description);
	}

	public Languages getLanguage() {
		return this.language;
	}

	public String getLanguageName() {
		return this.language.getName();
	}

	public String getLanguageCode() {
		return this.language.getCode();
	}

	public void setLanguage(Languages language) {
		this.language = language;
	}

	public boolean isCustomOrder() {
		return this.customOrder;
	}

	public void setCustomOrder(boolean customOrder) {
		this.customOrder = customOrder;
	}

	public String getLaunchArgs() {
		return this.launchargs.get();
	}

	public void setLaunchArgs(String launchargs) {
		if (launchargs != null) {
			launchargs = launchargs.trim();
		}
		this.launchargs = new SimpleStringProperty(launchargs);
	}

	/**
	 * @return
	 */
	public List<Mod> getModlist() {
		return modlist;
	}

	/**
	 * @param modList
	 */
	public void setModlist(List<Mod> modList) {
		this.modlist = modList;
		computeConflicts(modList);
	}

	public List<ModConflict> getModConflicts() {
		return modConflicts;
	}

	//
	// Methods
	//
	/**
	 * @param mod
	 * @return
	 */
	public int isModInList(Mod mod) {
		for (int i = 0; i < modlist.size(); i++) {
			Mod one_mod = modlist.get(i);
			if (one_mod.equals(mod))
				return i;
		}
		return MOD_NOT_IN_LIST;
	}

	/**
	 * @param mod
	 * @return
	 */
	public boolean addMod(Mod mod) {
		if (isModInList(mod) == MOD_NOT_IN_LIST) {
			modlist.add(mod);
			addConflicts(mod);
			return true;
		}
		return false;
	}

	/**
	 * @param mods
	 */
	public void addAllMod(List<Mod> mods) {
		for (Mod one_mod : mods) {
			this.addMod(one_mod);
		}
	}

	/**
	 * @param mod
	 * @return
	 */
	public boolean removeMod(Mod mod) {
		int index = isModInList(mod);
		if (index != MOD_NOT_IN_LIST) {
			modlist.remove(index);
			removeConflicts(mod);
		}
		return false;
	}

	public boolean hasConflict() {
		return !modConflicts.isEmpty();
	}

	public boolean hasConflict(Mod mod) {
		if (isModInList(mod) == MOD_NOT_IN_LIST)
			// Conflicts only concern mods in the list
			return false;
		for (ModConflict conflict : modConflicts) {
			if (conflict.getMod1().equals(mod) || conflict.getMod2().equals(mod))
				return true;
		}
		return false;
	}

	public Map<Mod, List<String>> getMappedConflicts(Mod mod) {
		Map<Mod, List<String>> res = new HashMap<>();
		for (ModConflict conflict : modConflicts) {
			if (conflict.getMod1().equals(mod)) {
				mapConflicts(res, conflict.getMod2(), conflict.getConflictFiles());
			}
			if (conflict.getMod2().equals(mod)) {
				mapConflicts(res, conflict.getMod1(), conflict.getConflictFiles());
			}
		}
		return res;
	}

	private static void mapConflicts(Map<Mod, List<String>> map, Mod mod, List<String> conflicts) {
		List<String> mappedConflicts = map.get(mod);
		if (mappedConflicts == null) {
			mappedConflicts = new ArrayList<>();
			map.put(mod, mappedConflicts);
		}
		mappedConflicts.addAll(conflicts);
	}

	private void computeConflicts(List<Mod> modList) {
		this.modConflicts = new ArrayList<>();
		for (Mod mod1 : modList) {
			Iterator<Mod> it = modList.iterator();
			boolean mod1Found = false;
			while (it.hasNext()) {
				// We skip mod1 and all the previous mods because they have already been seen
				Mod mod2 = it.next();
				if (mod2.equals(mod1)) {
					mod1Found = true;
					continue;
				}
				if (!mod1Found) {
					continue;
				}

				// Search conflict
				addModsConflict(mod1, mod2);
			}
		}
	}

	private void addConflicts(Mod newMod) {
		for (Mod mod : modlist) {
			if (!mod.equals(newMod)) {
				addModsConflict(mod, newMod);
			}
		}
	}

	private void removeConflicts(Mod removedMod) {
		Iterator<ModConflict> it = modConflicts.iterator();
		while (it.hasNext()) {
			ModConflict conflict = it.next();
			if (conflict.getMod1().equals(removedMod) || conflict.getMod2().equals(removedMod)) {
				it.remove();
			}
		}
	}

	private void addModsConflict(Mod mod1, Mod mod2) {
		ModConflict modConflict = new ModConflict(mod1, mod2);
		for (String modifiedFile : mod1.getModifiedFiles()) {
			if (mod2.getModifiedFiles().contains(modifiedFile)) {
				modConflict.addConflictFile(modifiedFile);
			}
		}
		if (modConflict.getConflictFiles().size() > 0) {
			modConflicts.add(modConflict);
		}
	}
}
