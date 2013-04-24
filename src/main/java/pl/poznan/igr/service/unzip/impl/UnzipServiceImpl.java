package pl.poznan.igr.service.unzip.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.service.unzip.UnzipService;

import com.google.common.io.Files;

@Service
public class UnzipServiceImpl implements UnzipService {

	@Override
	@Transactional
	public void unzipFile(Context context) {

		ImportSession imp = ImportSession.findImportSessionForContext(context);
		BlobFile blob = imp.getBlobFile();
		byte[] content = blob.getContent();

		try {
			extractFiles(new ByteArrayInputStream(content));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void extractFiles(InputStream from) throws IOException,
			FileNotFoundException {

		ZipInputStream zis = new ZipInputStream(from);

		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {

			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			System.out.printf("name: %s \n size: %d \n compressed size: %d\n",
					name, size, compressedSize);

			File file = new File(name);
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
			extractFiles(new FileInputStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
