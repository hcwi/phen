package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.service.unzip.UnzipService;

public class UnzipServiceIntegrationTest extends AbstractIntegrationTest {

	public static final String OWNER = "owner";
	public static final String SMALL_FILE_NAME = "test.txt";
	public static final String BIG_FILE_NAME = "Phenotyping";
	public static final String SMALL_ZIP_PATH = "src/test/resources/testzip.zip";
	public static final String BIG_ZIP_PATH = "src/test/resources/Phenotyping.zip";

	@Autowired
	private ImportService importService;

	@Autowired
	private UnzipService unzipService;

	@Test
	public void testUnzipFromContext() {

		testFromContext(OWNER, SMALL_ZIP_PATH, SMALL_FILE_NAME);
		testFromContext(OWNER, BIG_ZIP_PATH, BIG_FILE_NAME);

	}

	private void testFromContext(String owner, String zipPath, String fileName) {

		importService.importFile(owner, zipPath);

		Context ctx = Context.findAllContexts().get(
				(int) Context.countContexts() - 1);
		assertEquals(Status.UPLOADED, ctx.getStatus());

		unzipService.unzipFile(ctx);

		File f = new File(fileName);
		assert (f.exists());
		//deleteFile(f);
		try {
			if (f.isFile())
				f.delete();
			else
				FileUtils.deleteDirectory(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert (!f.exists());
	}

	@SuppressWarnings("unused")
	private void deleteFile(File f) {
		if (!f.exists()) {
			System.out.println(f.getName() + " not exist");
		}
		if (f.isFile()) {
			f.delete();
		} else {
			String[] fnames = f.list();
			if (fnames != null) {
				for (String fname : fnames) {
					File f2 = new File(f, fname);
					deleteFile(f2);
				}
			}
			f.delete();
		}
	}

	@Test
	public void testUnzipFromPath() {

		unzipService.unzipFile(SMALL_ZIP_PATH);

		File f = new File(SMALL_FILE_NAME);
		assert (f.exists());
		f.delete();
		assert (!f.exists());
	}

}
