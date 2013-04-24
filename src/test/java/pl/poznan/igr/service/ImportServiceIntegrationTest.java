package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;

public class ImportServiceIntegrationTest extends AbstractIntegrationTest {

	public static final String OWNER = "owner";
	public static final String FILE_PATH = "src/test/resources/test.txt";
	public static final String ZIP_PATH = "src/test/resources/testzip.zip";

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
		assertEquals(true, blobFile.getCreated().before(new Date()));

		Context ctx = Context.findAllContexts().get(0);
		assertEquals(OWNER, ctx.getOwner());
		assertEquals(true, ctx.getStarted().before(new Date()));
		assertEquals(null, ctx.getFinished());
		assertEquals(Status.UPLOADED, ctx.getStatus());
		
		ImportSession session = ImportSession.findAllImportSessions().iterator().next();
		assertEquals(true,  session.getCreationDate().before(new Date()));
		assertEquals(ctx, session.getContext());
		
		BlobFile uploadedFile = session.getBlobFile();
		assertEquals(uploadedFile, blobFile);
		
		ImportSession is = ImportSession.findImportSessionForContext(ctx);
		assertEquals(session, is);
				
	}
	
	@Test
	public void testImportZip() {

		assertEquals(0, BlobFile.countBlobFiles());
		assertEquals(0, Context.countContexts());

		importService.importFile(OWNER, ZIP_PATH);

		assertEquals(1, BlobFile.countBlobFiles());
		assertEquals(1, Context.countContexts());

		BlobFile blobFile = BlobFile.findAllBlobFiles().iterator().next();
		assertEquals("testzip.zip", new String(blobFile.getFileName()));
		//TODO uncomment and find out why ?!
		//assertEquals(true, blobFile.getCreated().before(new Date()));
		Context ctx = Context.findAllContexts().get(0);
		assertEquals(OWNER, ctx.getOwner());
		assertEquals(true, ctx.getStarted().before(new Date()));
		assertEquals(null, ctx.getFinished());
		assertEquals(Status.UPLOADED, ctx.getStatus());
		
		ImportSession session = ImportSession.findAllImportSessions().iterator().next();
		assertEquals(true,  session.getCreationDate().before(new Date()));
		assertEquals(ctx, session.getContext());
		
		BlobFile uploadedFile = session.getBlobFile();
		assertEquals(uploadedFile, blobFile);
		
		ImportSession is = ImportSession.findImportSessionForContext(ctx);
		
		assertEquals(true, null != is);
		assertEquals(session, is);
		
		System.out.println();
		
	}

}
