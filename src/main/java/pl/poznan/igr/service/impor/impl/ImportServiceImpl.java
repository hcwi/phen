package pl.poznan.igr.service.impor.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.impor.ImportService;

import com.google.common.io.Files;

@Service
public class ImportServiceImpl implements ImportService {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Override
	@Transactional
	public void importFile(String owner, String path) {

		try {
			
			final Context ctx = createContext(owner);

			final File f = new File(path);
			byte[] content = Files.toByteArray(f);
			String fileName = f.getName();
			final BlobFile blobFile = createBlobFile(fileName, content,
					DEFAULT_CONTENT_TYPE);

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
		is.persist(); //no need to persist() because of cascade=CascadeType.PERSIST set in Context
		
		ctx.setStatus(Status.UPLOADED);
		//ctx.setImportSession(is);
		ctx = ctx.merge();
	}

	private Context createContext(String owner) {
		final Context ctx = new Context(owner);
		ctx.persist();
		return ctx;
	}

	private BlobFile createBlobFile(String fileName, byte[] content,
			String string) {
		final BlobFile blobFile = new BlobFile();
		blobFile.setCreated(new Date());
		blobFile.setContent(content);
		blobFile.setFileName(fileName);
		//blobFile.persist();
		return blobFile;
	}

	@Override
	public void importFile(String owner, MultipartFile file) {
		try {

			final Context ctx = createContext(owner);
			
			BlobFile blobFile = createBlobFile(file.getName(), file.getBytes(),
					file.getContentType());
			
			createImportSessionForContext(blobFile, ctx);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
