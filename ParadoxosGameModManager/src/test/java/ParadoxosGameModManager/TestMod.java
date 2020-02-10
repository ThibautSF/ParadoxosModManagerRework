package ParadoxosGameModManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pmm.ParadoxosGameModManager.ModManager;
import com.pmm.ParadoxosGameModManager.mod.Mod;

/**
 * Test the Mod class
 *
 * @see about Parameterized test :
 *      https://github.com/junit-team/junit4/wiki/parameterized-tests
 * @author GROSJEAN Nicolas (alias Mouchi)
 *
 */
public class TestMod {

	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "FakeMod.mod", "Direct mod" }, { "699235856", "Zipped mod" } });
	}

	public String modName;

	public String modType;

	@BeforeAll
	public static void setUp() {
		ModManager.PATH = System.getProperty("user.dir") + "/testRessources/";
	}

	@Test
	public void modifiedFilestest() {
		Mod mod = new Mod(modName, true);
		Set<String> modifiedFiles = mod.getModifiedFiles();
		Assertions.assertEquals(7, modifiedFiles.size());
		Assertions.assertTrue(modifiedFiles.contains("gfx\\interface\\player_counters_toggle.dds"));
		Assertions.assertTrue(modifiedFiles.contains("gfx\\interface\\radar_toggle.dds"));
		Assertions.assertTrue(modifiedFiles.contains("gfx\\texticons\\air_experience_20x20.dds"));
		Assertions.assertTrue(modifiedFiles.contains("gfx\\texticons\\army_experience_20x20.dds"));
		Assertions.assertTrue(modifiedFiles.contains("gfx\\texticons\\navy_experience_20x20.dds"));
		Assertions.assertTrue(modifiedFiles.contains("localisation\\additional.yml"));
		Assertions.assertTrue(modifiedFiles.contains("localisation\\additional_l_french.yml"));
	}
}
