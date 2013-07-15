package pl.poznan.igr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;

public abstract class ServiceImpl {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected void checkDBState() {

		if (log.isDebugEnabled()) {

			log.debug("-------- CHECK DB ---------");

			long size = Context.countContexts();
			log.debug(size + " contexts");
			for (int i = 0; i < size; i++) {
				log.debug(Context.findAllContexts().get(i).toString());
			}

			size = ImportSession.countImportSessions();
			log.debug(size + " imports");
			for (int i = 0; i < size; i++) {
				log.debug(ImportSession.findAllImportSessions().get(i)
						.toString());
			}

			size = BlobFile.countBlobFiles();
			log.debug(size + " blobs");
			for (int i = 0; i < size; i++) {
				log.debug(BlobFile.findAllBlobFiles().get(i).toString());
			}

			log.debug("---------------------------");
		}
	}
}
