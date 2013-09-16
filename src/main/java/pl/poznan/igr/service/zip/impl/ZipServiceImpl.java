package pl.poznan.igr.service.zip.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
		rezipFiles2(ctx);
		routerService.runNext(ctx);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private void rezipFiles2(Context ctx) {

		ZipSession zs = new ZipSession();
		zs.setContext(ctx);
		ctx.setZipSession(zs);

		try {
			String path = ctx.getUnzipSession().getUnzipPath();
			System.out.println(path);
			Pattern p = Pattern.compile("(?<=.*)[^/]+$");
			Matcher m = p.matcher(path);

			if (m.find()) {
				
				String dir = m.group(0);
				String enriched = dir + "_enriched.zip";

				File f = new File(path + "/" + enriched);
				if (f.exists()) {
					
					byte[] content = new byte[100000];
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					
					FileInputStream fis = new FileInputStream(f);
					int len = fis.read(content);
					while (len > -1) {
						os.write(content, 0, len);
						len = fis.read(content);
					}
					fis.close();

					os.close();

					BlobFile blob = new BlobFile(enriched, os.toByteArray());
					zs.setBlobFileEnriched(blob);
				}
				

				String reduced = dir + "_reduced.zip";
				f = new File(path + "/" + reduced);
				if (f.exists()) {
					
					byte[] content = new byte[100000];
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					
					FileInputStream fis = new FileInputStream(f);
					int len = fis.read(content);
					while (len > -1) {
						os.write(content, 0, len);
						len = fis.read(content);
					}
					fis.close();

					os.close();

					BlobFile blob = new BlobFile(reduced, os.toByteArray());
					zs.setBlobFileReduced(blob);
				}
				
				log.debug(zs.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			ctx.setStatus(Status.REZIP_FAILED);
		}

		ctx.setStatus(Status.REZIPPED);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private void rezipFiles(Context ctx) {

		try {
			String path = ctx.getUnzipSession().getUnzipPath();
			String rezipped = rezipFiles(path);

			byte[] content = new byte[100000];
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			FileInputStream fis = new FileInputStream(new File(rezipped));
			int len = fis.read(content);
			while (len > -1) {
				os.write(content, 0, len);
				len = fis.read(content);
			}
			fis.close();

			os.close();

			BlobFile blob = new BlobFile(rezipped, os.toByteArray());
			ZipSession zs = new ZipSession();
			zs.setBlobFileEnriched(blob);
			zs.setContext(ctx);

			log.debug(zs.toString());

			ctx.setStatus(Status.REZIPPED);
			ctx.setZipSession(zs);

			File f = new File(rezipped);
			f.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
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
