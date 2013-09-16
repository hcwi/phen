package pl.poznan.igr.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.service.zip.ZipService;

public class ZipServiceIntegrationTest extends AbstractIntegrationTest {

	public static final String OWNER = "owner";
	public static final String SMALL_FILE_NAME = "test.txt";
	public static final String BIG_FILE_NAME = "Phenotyping";
	public static final String SMALL_ZIP_PATH = "src/test/resources/testzip.zip";
	public static final String BIG_ZIP_PATH = "src/test/resources/Phenotyping.zip";
	public static final String SMALL_FILE_PATH = "src/test/resources/test.txt";
	public static final String DOUBLE_ZIP_PATH = "src/test/resources/PhenotypingDouble.zip";
	public static final String TRIPLE_ZIP_PATH = "src/test/resources/PhenotypingTriple.zip";

	@Autowired
	private ZipService zipService;

	private void removeUnzipped(File f) {

		try {
			if (f.isFile())
				f.delete();
			else {
				FileUtils.deleteDirectory(f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse("Unzipped file has not been deleted.", f.exists());
	}

	@Test
	public void testUnzipAndZipBack() {
		
		String path = "src/test/resources/Phenotyping";
		String packedAgain = zipService.rezipFiles(path);
		log.info("Packed again here: " + packedAgain);
		
		File f = new File(packedAgain);
		assertTrue("Zip file does not exist.", f.exists());
		
		removeUnzipped(f);
		assertFalse("Didn't remove zip file.", f.exists());
		log.info("Packed file removed");
	}

	@Test
	public void testRZip() {
		
		String path = "src/test/resources/Keygene2";

		Pattern p = Pattern.compile("(?<=.*)[^/]+$");
		Matcher m = p.matcher(path);
		if (m.find()) {
			
			String dir = m.group();
			String packedAgain = dir + "_enriched.zip"; 
			log.info(packedAgain);
						
			File f = new File(path + "/" + packedAgain);
			assertTrue("Zip file does not exist.", f.exists());
		}
		
	}

}
