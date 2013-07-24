package pl.poznan.igr.service.zip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ZipSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.ServiceImpl;
import pl.poznan.igr.service.router.RouterService;
import pl.poznan.igr.service.stats.impl.StatsServiceImpl;
import pl.poznan.igr.service.zip.ZipService;

@Service
public class ZipServiceImpl extends ServiceImpl implements ZipService {

	public static final String OUT_PATH = "target/output";

	@Autowired
	RouterService routerService;

	private final static Logger log = LoggerFactory
			.getLogger(StatsServiceImpl.class);

	@Override
	@Transactional
	public void process(Context ctx) {
		rezipFiles(ctx);
		routerService.runNext(ctx);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private void rezipFiles(Context ctx) {

		try {
			String path = ctx.getUnzipSession().getUnzipPath();
			String rezipped = rezipFiles(path);

			ZipSession zs = new ZipSession();
			zs.setCreationDate(new Date());
			zs.setContext(ctx);

			byte[] content = new byte[100000];
			FileInputStream fis = new FileInputStream(new File(rezipped));
			fis.read(content);
			fis.close();

			BlobFile blob = createBlobFile(rezipped, content);
			zs.setBlobFile(blob);

			System.err.println(zs);

			ctx.setStatus(Status.REZIPPED);
			ctx.setZipSession(zs);
					
			//TODO delete zip files
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BlobFile createBlobFile(String fileName, byte[] content) {

		final BlobFile blobFile = new BlobFile();
		blobFile.setCreated(new Date());
		blobFile.setContent(content);
		blobFile.setFileName(fileName);
		return blobFile;
	}

	@Override
	public String rezipFiles(String path) {

		String foutPath = "";
		try {

			File fout = File.createTempFile("rezipped", ".zip");
			FileOutputStream fos = new FileOutputStream(fout);
			ZipOutputStream zos = new ZipOutputStream(fos);

			File fin = new File(path);
			rezipFile(zos, "", fin);

			zos.close();
			fos.close();

			foutPath = fout.getAbsolutePath();
			checkZip(foutPath);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return foutPath;
	}

	private void rezipFile(ZipOutputStream zos, String path, File file)
			throws IOException {

		if (path != "") {
			path += "/";
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				rezipFile(zos, path + file.getName(), f);
			}
		} else {
			zos.putNextEntry(new ZipEntry(path + file.getName()));
			addContentToZip(zos, file);
			zos.closeEntry();
		}
	}

	private void addContentToZip(ZipOutputStream zos, File fin)
			throws IOException {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fin);
			byte[] buffer = new byte[102400];
			int len;
			while ((len = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
		} finally {
			fis.close();
		}
	}

	// for test purposes only
	private void checkZip(String path) throws IOException {

		try {
			ZipFile zf = new ZipFile(new File(path));
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				log.debug(zipEntry.toString());
			}
			zf.close();
		} catch (ZipException e) {
			throw new ZipException(
					"Zip failed. Some internal problem with zipping processed files. Contact service admin.");
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
				log.debug(zipEntry.toString());
			}
			zf.close();
		} catch (ZipException e) {
			throw new ZipException(
					"Unzip failed. Some internal problem with zipping processed files. Contact service admin.");
		}
	}

}
