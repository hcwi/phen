package pl.poznan.igr.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.test.RooIntegrationTest;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.imp.ImportService;
import pl.poznan.igr.service.imp.impl.ImportServiceImpl;

@RooIntegrationTest(entity = ImportServiceImpl.class)
public class ImportServiceIntegrationTest {

	public static final String OWNER = "owner";
	public static final String FILE_PATH = "src/test/resources/test.txt";

	@Autowired
	private ImportService importService;

	@Test
	public void testImportFile() {

		assertEquals(0, BlobFile.countBlobFiles());
		assertEquals(0, Context.countContexts());

		importService.importFile(OWNER, FILE_PATH);

		assertEquals(1, BlobFile.countBlobFiles());
		assertEquals(1, Context.countContexts());

		BlobFile blobFile = BlobFile.findAllBlobFiles().iterator().next();
		assertEquals("This is a test.", new String(blobFile.getContent()));
		assertEquals("test.txt", new String(blobFile.getFileName()));
		Date now = new Date();
		assertEquals(now.after(blobFile.getCreated()), true);

		Context ctx = Context.findAllContexts().get(0);
		assertEquals(OWNER, ctx.getOwner());
		assertEquals(true, ctx.getStarted().before(new Date()));
		assertEquals(null, ctx.getFinished());
		assertEquals(Status.UPLOADED, ctx.getStatus());
		BlobFile uploadedFile = ctx.getUploadedFile();
		assertEquals(uploadedFile, blobFile);
	}

}
