package pl.poznan.igr.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.service.unzip.UnzipService;

public class UnzipServiceIntegrationTest extends AbstractIntegrationTest {

	public static final String OWNER = "owner";
	public static final String FILE_PATH = "src/test/resources/test.txt";
	public static final String ZIP_PATH = "src/test/resources/test.zip";

	@Autowired
	private ImportService importService;

	@Autowired
	private UnzipService unzipService;
	
	@Test
	public void testUnzipFile() {

		importService.importFile(OWNER, ZIP_PATH);

		Context ctx = Context.findAllContexts().get(0);
		assertEquals(Status.UPLOADED, ctx.getStatus());
		
		unzipService.unzipFile(ctx);
				
		//unzipService.unzipFile(ZIP_PATH);
		
	}

}
