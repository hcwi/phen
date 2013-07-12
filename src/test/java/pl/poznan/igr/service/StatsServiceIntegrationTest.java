package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.service.stats.StatsService;
import pl.poznan.igr.service.unzip.UnzipService;

public class StatsServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private StatsService statsService;

	@Autowired
	private ImportService importService;

	@Autowired
	private UnzipService unzipService;

	public static final String ZIP_PATH = "src/test/resources/Phenotyping.zip";
	public static final String OWNER = "owner";
	public static final String FILE_NAME = "Phenotyping";
	public static final String OUT_PATH = "target/output";

	@Test
	public void testCalculateStats() {

		File zip = new File(ZIP_PATH);
		assertEquals("Zip file does not exist.", true, zip.exists());

		importService.importFile(OWNER, ZIP_PATH);

		Context ctx = Context.findAllContexts().get(
				(int) Context.countContexts() - 1);
		assertEquals(Status.UPLOADED, ctx.getStatus());

		unzipService.unzipFile(ctx);

		assertEquals(Status.UNZIPPED, ctx.getStatus());

		UnzipSession us = UnzipSession.findUnzipSessionForContext(ctx);
		assertFalse("Unzip session is null.", us == null);
		assertEquals(us, ctx.getUnzipSession());

		// TODO obsluga struktury katalogu
		// obecnie jako wd podajemy katalog na najnizszym poziomie, z plikami

		File f = new File(us.getUnzipPath());
		assertEquals("Unzipped file does not exist.", true, f.exists());
		assertEquals(Status.UNZIPPED, ctx.getStatus());

		statsService.calculateStats(ctx);

		// TODO uncomment and remove second stage to other test
		assertEquals(Status.ANALYSED_SAVED, ctx.getStatus());
		File stats = new File(us.getUnzipPath() + "/output/stats.txt");
		assertEquals("Stats file does not exist.", true, stats.exists());

		List<BlobFile> blobs = BlobFile.findAllBlobFiles();
		for (BlobFile blob : blobs) {
			System.out.println(blob.getId() + "\t" + blob.getFileName() + "\t"
					+ blob.getCreated());
		}

		BlobFile blobFile = BlobFile.findAllBlobFiles().get(
				(int) (BlobFile.countBlobFiles() - 1));
		assertEquals("stats.txt", new String(blobFile.getFileName()));
		assertFalse(blobFile.getCreated().after(new Date()));
		assertEquals(Status.ANALYSED_SAVED, ctx.getStatus());

	}

}
