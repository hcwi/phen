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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.io.Files;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.impl.StatsServiceImpl;
import pl.poznan.igr.service.unzip.UnzipService;

@Service
public class UnzipServiceImpl implements UnzipService {

	public static final String OUT_PATH = "target/output";

	@Autowired
	RouterService routerService;

	private final static Logger log = LoggerFactory
			.getLogger(StatsServiceImpl.class);

	@Override
	@Transactional
	public void unzipFile(Context context) {

		ImportSession imp = ImportSession.findImportSessionForContext(context);
		BlobFile blob = imp.getBlobFile();
		byte[] content = blob.getContent();

		try {

			String unzippedPath = extractFiles(new ByteArrayInputStream(content));

			UnzipSession uz = new UnzipSession();
			uz.setUnzipPath(unzippedPath);
			uz.setContext(context);
			uz.persist();

			context.setStatus(Status.UNZIPPED);
			context.merge();

		} catch (IOException e) {
			e.printStackTrace();
			context.setStatus(Status.UNZIP_FAILED);
		}

	}

	@Override
	@Transactional
	public void process(Context ctx) {
		unzipFile(ctx);
		routerService.runNext(ctx);
	}

	private String extractFiles(InputStream from) throws IOException,
			FileNotFoundException {

		// File wd = new File(OUT_PATH, id);
		// wd.mkdirs();
		File wd = Files.createTempDir();
		String inDir = null;

		ZipInputStream zis = new ZipInputStream(from);

		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {

			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			log.debug("Unzipped file: " + name + "\n size " + compressedSize
					+ " -> " + size);

			File file = new File(wd, name);
			if (name.endsWith("/")) {
				file.mkdirs();
				// TODO think what to do with stats.txt file
				// in this version it gets saved in wd/inDir
				if (inDir == null) {
					inDir = name.substring(0, name.length() - 1);
				}
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

		String unzippedPath = wd.getAbsolutePath();
		if (inDir != null) {
			unzippedPath += "/" + inDir;
		}
		return unzippedPath;
	}

	@Override
	public String unzipFile(String path) {

		try {
			String unzippedPath = extractFiles(new FileInputStream(path));
			log.debug("Unzipped path: " + unzippedPath);
			return unzippedPath;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
