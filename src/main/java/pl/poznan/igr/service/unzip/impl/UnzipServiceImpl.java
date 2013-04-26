package pl.poznan.igr.service.unzip.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.unzip.UnzipService;

@Service
public class UnzipServiceImpl implements UnzipService {

	public static final String OUT_PATH = "target/output";

	@Override
	@Transactional
	public void unzipFile(Context context) {

		ImportSession imp = ImportSession.findImportSessionForContext(context);
		BlobFile blob = imp.getBlobFile();
		byte[] content = blob.getContent();
		String id = context.getId().toString();

		try {

			extractFiles(new ByteArrayInputStream(content), id);

			UnzipSession uz = new UnzipSession();
			String fname = imp.getBlobFile().getFileName();
			uz.setUnzipPath(OUT_PATH + "/" + context.getId() + "/"
					+ fname.substring(0, fname.length()-4));
			uz.setContext(context);
			uz.persist();

			context.setStatus(Status.UNZIPPED);

		} catch (IOException e) {
			e.printStackTrace();
			context.setStatus(Status.UNZIP_FAILED);
		}

	}

	private void extractFiles(InputStream from, String id) throws IOException,
			FileNotFoundException {

		File wd = new File(OUT_PATH, id);
		wd.mkdirs();

		ZipInputStream zis = new ZipInputStream(from);

		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {

			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			System.out.printf("name: %s \n size: %d \n compressed size: %d\n",
					name, size, compressedSize);

			File file = new File(wd, name);
			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			}

			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			FileOutputStream fos = new FileOutputStream(file);

			byte[] bytes = new byte[1024];
			int length;

			while ((length = zis.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			fos.close();
		}
		zis.close();
	}

	@Override
	public void unzipFile(String path) {

		try {
			extractFiles(new FileInputStream(path), "tmp");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
