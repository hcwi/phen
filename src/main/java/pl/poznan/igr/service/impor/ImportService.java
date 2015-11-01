package pl.poznan.igr.service.impor;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.type.Status;
import pl.poznan.igr.service.unzip.UnzipService;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class ImportService {

    private final static Logger log = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    UnzipService unzipService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importFile(String owner, String path) {

        try {

            final Context ctx = createContext(owner);

            final File f = new File(path);
            byte[] content = Files.toByteArray(f);
            String fileName = f.getName();
            final BlobFile blobFile = createBlobFile(fileName, content);

            createImportSessionForContext(blobFile, ctx);

        } catch (IOException e) {
            new RuntimeException(e);
        }

    }

    public void importFile(String owner, String fileName, byte[] content) {

        final Context ctx = createContext(owner);
        BlobFile blobFile = createBlobFile(fileName, content);
        createImportSessionForContext(blobFile, ctx);

        unzipService.process(ctx);
    }

    private Context createContext(String owner) {

        final Context ctx = new Context(owner);
        ctx.persist();
        return ctx;
    }

    private BlobFile createBlobFile(String fileName, byte[] content) {

        final BlobFile blobFile = new BlobFile();
        blobFile.setCreated(new Date());
        blobFile.setContent(content);
        blobFile.setFileName(fileName);
        return blobFile;
    }

    private void createImportSessionForContext(BlobFile blobFile, Context ctx) {

        ImportSession is = new ImportSession();
        is.setBlobFile(blobFile);
        is.setCreationDate(new Date());
        is.setContext(ctx);

        ctx.setImportSession(is);
        ctx.setStatus(Status.UPLOADED);

        log.debug(is.toString());
        log.debug(blobFile.toString());
    }
}
