package pl.poznan.igr.web;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.AnalysisASession;
import pl.poznan.igr.domain.ZipSession;

@RequestMapping("/download/**")
@Controller
public class DownloadController {

	private final static Logger log = LoggerFactory
			.getLogger(DownloadController.class);

	@RequestMapping(method = RequestMethod.GET, value = "stats/{contextId}")
	public void downloadStatSessionForContext(@PathVariable Long contextId,
			HttpServletResponse response) {

		Context ctx = Context.findContext(contextId);

		try {
			BlobFile blob = checkNotNull(ctx.getResultFile(), "No zip for context {0}", contextId);
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ blob.getFileName());
			response.getOutputStream().write(blob.getContent());
		} catch (IOException e) {
			log.error("Can't send the file", e);
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "data/{contextId}")
	public void downloadImportSessionForContext(@PathVariable Long contextId,
			HttpServletResponse response) {

		Context ctx = Context.findContext(contextId);
		final ImportSession is = checkNotNull(ImportSession.findImportSessionForContext(ctx),
				"No data for context {0}", contextId);

		try {
			BlobFile blob = is.getBlobFile();
			response.setContentType("plain/text");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ blob.getFileName());
			response.getOutputStream().write(blob.getContent());
		} catch (IOException e) {
			log.error("Can't send the file", e);
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "enriched/{contextId}")
	public void downloadEnrichedZipSessionForContext(@PathVariable Long contextId,
			HttpServletResponse response) {

		Context ctx = Context.findContext(contextId);
		final ZipSession zs = checkNotNull(ZipSession.findZipSessionForContext(ctx),
				"No data for context {0}", contextId);

		try {
			BlobFile blob = zs.getBlobFileEnriched();
			response.setContentType("plain/text");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ blob.getFileName());
			response.getOutputStream().write(blob.getContent());
		} catch (IOException e) {
			log.error("Can't send the file", e);
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "reduced/{contextId}")
	public void downloadReducedZipSessionForContext(@PathVariable Long contextId,
			HttpServletResponse response) {

		Context ctx = Context.findContext(contextId);
		final ZipSession zs = checkNotNull(ZipSession.findZipSessionForContext(ctx),
				"No data for context {0}", contextId);

		try {
			BlobFile blob = zs.getBlobFileReduced();
			response.setContentType("plain/text");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ blob.getFileName());
			response.getOutputStream().write(blob.getContent());
		} catch (IOException e) {
			log.error("Can't send the file", e);
			throw new RuntimeException(e);
		}
	}
}
