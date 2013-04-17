package pl.poznan.igr.service.imp.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.imp.ImportService;

import com.google.common.io.Files;

@Service
public class ImportServiceImpl implements ImportService {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Transactional
	public void importFile(String owner, String path) {

		try {
			final File f = new File(path);
			byte[] content = Files.toByteArray(f);
			String fileName = f.getName();

			BlobFile blobFile = createBlobFile(fileName, content,
					DEFAULT_CONTENT_TYPE);
			createContext(owner, blobFile);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createContext(String owner, BlobFile blobFile) {
		Context ctx = new Context();
		ctx.setOwner(owner);
		ctx.setStarted(new Date());
		ctx.setStatus(Status.UPLOADED);
		ctx.setUploadedFile(blobFile);
		ctx.persist();
	}

	private BlobFile createBlobFile(String fileName, byte[] content,
			String string) {
		final BlobFile blobFile = new BlobFile();
		blobFile.setContent(content);
		blobFile.setFileName(fileName);
		blobFile.setCreated(new Date());
		blobFile.persist();
		return blobFile;
	}

	@Override
	public void importFile(String owner, MultipartFile file) {
		try {

			BlobFile blobFile = createBlobFile(file.getName(), file.getBytes(),
					file.getContentType());
			createContext(owner, blobFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
