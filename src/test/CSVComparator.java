package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

/**
 * Compares two csv files for differences
 * 
 * @author saubaer
 *
 */
public class CSVComparator {

	private String referenceFilePath;
	private String originalFilePath;

	public CSVComparator(String originalFilePath, String referenceFilePath) {
		this.originalFilePath = originalFilePath;
		this.referenceFilePath = referenceFilePath;
	}

	/**
	 * compares two given csv files with DiffUtils and prints out the differences
	 */
	public void test() {

		List<String> original = null;
		List<String> revised = null;
		try {
			// read the files
			original = Files.readAllLines(
					new File(referenceFilePath).toPath(), StandardCharsets.UTF_8);
			revised = Files.readAllLines(new File(originalFilePath).toPath(), StandardCharsets.UTF_8);
			Patch<String> patch = DiffUtils.diff(original, revised);
			if (patch.getDeltas().isEmpty()) {
				System.out.println("no differences between files");
			}
			for (AbstractDelta<String> delta : patch.getDeltas()) {
				System.out.println(delta);
			}
		} catch (IOException | DiffException e) {
			e.printStackTrace();
		}
	}
}
