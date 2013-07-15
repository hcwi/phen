package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
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
		assertTrue("Zip file does not exist.", zip.exists());

		importService.importFile(OWNER, BIG_ZIP_PATH);

		System.err.println(Context.countContexts());
		Context ctx = Context.findAllContexts().get(0);
		assertEquals(Status.UPLOADED, ctx.getStatus());

		unzipService.unzipFile(ctx);

		UnzipSession us = UnzipSession.findUnzipSessionForContext(ctx);
		assertNotNull(us);
		File f = new File(us.getUnzipPath());
		assertTrue("Unzipped file does not exist.", f.exists());

		removeUnzipped(f);

	}

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
	public void testFromPath_SmallZip() {
		File zip = new File(SMALL_ZIP_PATH);
		assertTrue("Zip file does not exist.", zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(SMALL_ZIP_PATH);
			String fname = unzippedFile; // + "/" + SMALL_FILE_NAME;
			log.debug("Unzip path: " + fname);
			File f = new File(fname);
			assertTrue("Unzipped file does not exist.", f.exists());
			removeUnzipped(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFromPath_BigZip() {

		File zip = new File(BIG_ZIP_PATH);
		assertTrue("Zip file does not exist.", zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(BIG_ZIP_PATH);
			String fname = unzippedFile;// + "/" + BIG_FILE_NAME;
			log.debug("Unzip path: " + fname);
			File f = new File(fname);
			assertTrue("Unzipped file does not exist.", f.exists());
			removeUnzipped(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFromPath_DoubleZip() {

		File zip = new File(DOUBLE_ZIP_PATH);
		assertTrue("Zip file does not exist.", zip.exists());

		try {
			String unzippedFile = unzipService.unzipFile(DOUBLE_ZIP_PATH);
			File f = new File(unzippedFile);
			assertTrue("Unzipped file does not exist.", f.exists());
			removeUnzipped(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFromPath_TripleZip() {

		File zip = new File(TRIPLE_ZIP_PATH);
		assertTrue("Zip file does not exist.", zip.exists());

		String unzippedFile;
		try {
			unzippedFile = unzipService.unzipFile(TRIPLE_ZIP_PATH);
			String fname = unzippedFile;
			log.debug("Unzip path: " + fname);
			File f = new File(fname);
			assertTrue("Unzipped file does not exist.", f.exists());
			removeUnzipped(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test(expected = UnzipException.class)
	public void testFromPath_NotZip() {

		File zip = new File(SMALL_FILE_PATH);
		assertTrue("Zip file does not exist.", zip.exists());

		try {
			unzipService.unzipFile(SMALL_FILE_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
