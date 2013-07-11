package pl.poznan.igr.service.impor.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;
import pl.poznan.igr.service.router.impl.RouterServiceImpl;

import com.google.common.io.Files;

@Service
public class ImportServiceImpl implements ImportService {

	// TODO add content type
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Autowired
	private RouterServiceImpl routerService;
	
	private static final Logger log = LoggerFactory.getLogger(ImportServiceImpl.class); 

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void importFile(String owner, String path) {

		try {

			if (log.isDebugEnabled()) {
				checkDBState();
			}

			final Context ctx = createContext(owner);

			final File f = new File(path);
			byte[] content = Files.toByteArray(f);
			String fileName = f.getName();
			final BlobFile blobFile = createBlobFile(fileName, content);

			createImportSessionForContext(blobFile, ctx);
			
			if (log.isDebugEnabled()) {
				checkDBState();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void checkDBState() {

		log.debug("-------- CHECK DB ---------");
		
		long size = Context.countContexts();
		log.debug(size + " contexts");
		for (int i = 0; i < size; i++) {
			log.debug(Context.findAllContexts().get(i).toString());
		}

		size = ImportSession.countImportSessions();
		log.debug(size + " imports");
		for (int i = 0; i < size; i++) {
			log.debug(ImportSession.findAllImportSessions().get(i).toString());
		}

		size = BlobFile.countBlobFiles();
		log.debug(size + " blobs");
		for (int i = 0; i < size; i++) {
			log.debug(BlobFile.findAllBlobFiles().get(i).toString());
		}
		
		log.debug("---------------------------");

	}

	private void createImportSessionForContext(BlobFile blobFile, Context ctx) {

		//ctx = ctx.merge();
		
		ImportSession is = new ImportSession();
		is.setBlobFile(blobFile);
		is.setCreationDate(new Date());
		is.setContext(ctx);

		//is.persist(); // no need to persist because of
		// cascade=CascadeType.PERSIST set in Context

		ctx.setImportSession(is);
		ctx.setStatus(Status.UPLOADED);
		
		System.out.println(is);
		System.out.println(blobFile);
	}

	private Context createContext(String owner) {

		final Context ctx = new Context(owner);
		ctx.persist();
		return ctx;
	}

	private BlobFile createBlobFile(String fileName, byte[] content) {

		final BlobFile blobFile = new BlobFile();
		blobFile.setCreated(new Date());
		blobFile.setContent(content);
		blobFile.setFileName(fileName);
		// blobFile.persist();
		return blobFile;
	}

	@Override
	public void importFile(String owner, String fileName, byte[] content) {

		final Context ctx = createContext(owner);
		BlobFile blobFile = createBlobFile(fileName, content);
		createImportSessionForContext(blobFile, ctx);

		routerService.runNext(ctx);
	}

}
