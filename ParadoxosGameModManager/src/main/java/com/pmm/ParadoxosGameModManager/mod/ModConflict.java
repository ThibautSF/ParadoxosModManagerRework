package com.pmm.ParadoxosGameModManager.mod;

import java.util.ArrayList;
import java.util.List;

public class ModConflict {
	private Mod mod1;
	private Mod mod2;
	private List<String> conflictFiles;

	public ModConflict(Mod mod1, Mod mod2) {
		this.mod1 = mod1;
		this.mod2 = mod2;
		this.conflictFiles = new ArrayList<>();
	}

	public Mod getMod1() {
		return mod1;
	}

	public Mod getMod2() {
		return mod2;
	}

	public List<String> getConflictFiles() {
		return conflictFiles;
	}

	public void addConflictFile(String conflictFile) {
		conflictFiles.add(conflictFile);
	}
}
