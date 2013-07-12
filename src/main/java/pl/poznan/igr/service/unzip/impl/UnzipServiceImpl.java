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
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.ServiceImpl;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.impl.StatsServiceImpl;
import pl.poznan.igr.service.unzip.UnzipException;
import pl.poznan.igr.service.unzip.UnzipService;

import com.google.common.io.Files;

@Service
public class UnzipServiceImpl extends ServiceImpl implements UnzipService {

	public static final String OUT_PATH = "target/output";

	@Autowired
	RouterService routerService;

	private final static Logger log = LoggerFactory
			.getLogger(StatsServiceImpl.class);

	@Override
	@Transactional
	public void process(Context ctx) {
		unzipFile(ctx);
		routerService.runNext(ctx);
	}

	@Override
	@Transactional
	public void unzipFile(Context context) {

		BlobFile blob = context.getImportSession().getBlobFile();
		byte[] content = blob.getContent();

		try {

			checkZip(content);

			String unzippedPath = extractFiles(new ByteArrayInputStream(content));

			UnzipSession uz = new UnzipSession();
			uz.setUnzipPath(unzippedPath);
			uz.setContext(context);

			context.setUnzipSession(uz);
			context.setStatus(Status.UNZIPPED);

		} catch (UnzipException e) {
			e.printStackTrace();
			// TODO pass on to the user
			context.setStatus(Status.UNZIP_FAILED);
		} catch (IOException e) {
			e.printStackTrace();
			context.setStatus(Status.UNZIP_FAILED);
		}
	}

	// extracts only ZIP
	// TODO think of rar archives - error (on IN filter) or unrar
	private String extractFiles(InputStream from) throws IOException,
			FileNotFoundException {

		File wd = Files.createTempDir();
		String inDir = "";

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
				/*if (inDir == null) {
					inDir = name.substring(0, name.length() - 1);
				}*/
				inDir = name.substring(0, name.length()-1);
				continue;
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
		//TODO which path? general or deepest
		if (inDir != "") {
			unzippedPath += "/" + inDir;
		}
		return unzippedPath;
	}

	@Override
	// for test purposes only
	public String unzipFile(String path) throws IOException {

		checkZip(path);
		String unzippedPath = extractFiles(new FileInputStream(path));
		log.debug("Unzipped path: " + unzippedPath);
		return unzippedPath;

	}

	// for test purposes only
	private void checkZip(String path) throws IOException {

		try {
			ZipFile zf = new ZipFile(new File(path));
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				System.out.println(zipEntry);
			}
			zf.close();
		} catch (ZipException e) {
			throw new UnzipException(
					"Unzip failed. Probably your file in not in a proper zip format");
		}
	}

	private void checkZip(byte[] content) throws IOException {

		File f = File.createTempFile("igr", "tmpFile.zip");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(content);
		fos.close();

		try {
			ZipFile zf = new ZipFile(f);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				System.out.println(zipEntry);
			}
			zf.close();
		} catch (ZipException e) {
			throw new UnzipException(
					"Unzip failed. Probably your file in not in a proper zip format");
		}
	}
}
