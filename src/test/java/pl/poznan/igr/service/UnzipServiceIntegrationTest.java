package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.service.unzip.UnzipException;
import pl.poznan.igr.service.unzip.UnzipService;

public class UnzipServiceIntegrationTest extends AbstractIntegrationTest {

	public static final String OWNER = "owner";
	public static final String SMALL_FILE_NAME = "test.txt";
	public static final String BIG_FILE_NAME = "Phenotyping";
	public static final String SMALL_ZIP_PATH = "src/test/resources/testzip.zip";
	public static final String BIG_ZIP_PATH = "src/test/resources/Phenotyping.zip";
	public static final String SMALL_FILE_PATH = "src/test/resources/test.txt";
	public static final String DOUBLE_ZIP_PATH = "src/test/resources/PhenotypingDouble.zip";
	public static final String TRIPLE_ZIP_PATH = "src/test/resources/PhenotypingTriple.zip";
	
	
	@Autowired
	private ImportService importService;

	@Autowired
	private UnzipService unzipService;

	private final static Logger log = LoggerFactory
			.getLogger(UnzipServiceIntegrationTest.class);

	/*
	 * public void testUnzipFromContext() {
	 * 
	 * //testFromContext(OWNER, SMALL_ZIP_PATH, SMALL_FILE_NAME);
	 * testFromContext(OWNER, BIG_ZIP_PATH, BIG_FILE_NAME);
	 * 
	 * }
	 */
	@Test
	public void testFromContext() {

		File zip = new File(BIG_ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		importService.importFile(OWNER, BIG_ZIP_PATH);

		Context ctx = Context.findAllContexts().get(
				(int) Context.countContexts() - 1);
		assertEquals(Status.UPLOADED, ctx.getStatus());

		unzipService.unzipFile(ctx);

		UnzipSession us = UnzipSession.findUnzipSessionForContext(ctx);
		assertEquals(true, us != null);
		File f = new File(us.getUnzipPath());
		assertEquals("Unzipped file does not exist.", true, f.exists());

		// deleteFile(f);
		removeUnzipped(f);

	}

	private void removeUnzipped(File f) {

		// CLEAN find out why top directories are not removed
		// (target/output/ctxId)
		try {
			if (f.isFile())
				f.delete();
			else {
				FileUtils.deleteDirectory(f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("Unzipped file has not been deleted.", false, f.exists());
	}

	private void deleteFile(File f) {
		if (!f.exists()) {
			log.debug(f.getName() + " not exist");
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
	public void testFromPath_SmallZip() {
		File zip = new File(SMALL_ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(SMALL_ZIP_PATH);
			String fname = unzippedFile; // + "/" + SMALL_FILE_NAME;
			System.err.println(fname);
			File f = new File(fname);
			assertEquals("Unzipped file does not exist.", true, f.exists());
			deleteFile(f);
			assertEquals("Unzipped file has not been deleted.", false,
					f.exists());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFromPath_BigZip() {
		
		File zip = new File(BIG_ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(BIG_ZIP_PATH);
			String fname = unzippedFile ;//+ "/" + BIG_FILE_NAME;
			System.err.println(fname);
			File f = new File(fname);
			assertEquals("Unzipped file does not exist.", true, f.exists());
			deleteFile(f);
			assertEquals("Unzipped file has not been deleted.", false,
					f.exists());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testFromPath_DoubleZip() {
		
		File zip = new File(DOUBLE_ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(DOUBLE_ZIP_PATH);
			String fname = unzippedFile ;
			System.err.println(fname);
			File f = new File(fname);
			assertEquals("Unzipped file does not exist.", true, f.exists());
			deleteFile(f);
			assertEquals("Unzipped file has not been deleted.", false,
					f.exists());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFromPath_TripleZip() {
		
		File zip = new File(TRIPLE_ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(TRIPLE_ZIP_PATH);
			String fname = unzippedFile ;
			System.err.println(fname);
			File f = new File(fname);
			assertEquals("Unzipped file does not exist.", true, f.exists());
			deleteFile(f);
			assertEquals("Unzipped file has not been deleted.", false,
					f.exists());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test(expected = UnzipException.class)
	public void testFromPath_NotZip() {
		
		File zip = new File(SMALL_FILE_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		try {
			unzipService.unzipFile(SMALL_FILE_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
