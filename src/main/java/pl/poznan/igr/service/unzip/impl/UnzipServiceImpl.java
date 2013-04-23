package pl.poznan.igr.service.unzip.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.service.unzip.UnzipService;

import com.google.common.io.Files;

@Service
public class UnzipServiceImpl implements UnzipService {

	@Autowired
	@Override
	@Transactional
	public void unzipFile(Context context) {

		ImportSession imp = context.getImportSession();
		BlobFile blob = imp.getBlobFile();

		try {
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(
					blob.getContent()));
			File f = new File(blob.getFileName());
			byte[] from = blob.getContent();
			Files.write(from, f);

			String filename = blob.getFileName();
			ZipFile zipFile = new ZipFile(filename);
			Enumeration<?> enu = zipFile.entries();

			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = zipEntry.getName();
				long size = zipEntry.getSize();
				long compressedSize = zipEntry.getCompressedSize();
				System.out.printf(
						"name: %-20s | size: %6d | compressed size: %6d\n",
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

				byte[] buffer = new byte[1024];

				while (zis.available() > 0) {
					zis.read(buffer);
					fos.write(buffer);
				}

				zis.close();
				fos.close();

			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unzipFile(String path) {
		// TODO Auto-generated method stub

	}

}
