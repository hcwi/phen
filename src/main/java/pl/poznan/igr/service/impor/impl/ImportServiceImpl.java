package pl.poznan.igr.service.impor.impl;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

	//TODO add content type
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Autowired
	private RouterServiceImpl routerService;

	@Override
	@Transactional
	public void importFile(String owner, String path) {

		try {

			final Context ctx = createContext(owner);

			final File f = new File(path);
			byte[] content = Files.toByteArray(f);
			String fileName = f.getName();
			final BlobFile blobFile = createBlobFile(fileName, content);

			createImportSessionForContext(blobFile, ctx);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createImportSessionForContext(BlobFile blobFile, Context ctx) {
		ImportSession is = new ImportSession();
		is.setBlobFile(blobFile);
		is.setCreationDate(new Date());
		is.setContext(ctx);
		is.persist(); // no need to persist() because of
						// cascade=CascadeType.PERSIST set in Context

		ctx.setStatus(Status.UPLOADED);
		// ctx.setImportSession(is);
		ctx = ctx.merge();
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
