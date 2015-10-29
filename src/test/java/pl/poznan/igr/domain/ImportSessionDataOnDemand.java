package pl.poznan.igr.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Configurable
@Component
public class ImportSessionDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<ImportSession> data;

	@Autowired
    BlobFileDataOnDemand blobFileDataOnDemand;

	@Autowired
    ContextDataOnDemand contextDataOnDemand;

	public ImportSession getNewTransientImportSession(int index) {
        ImportSession obj = new ImportSession();
        setCreationDate(obj, index);
        return obj;
    }

	public void setCreationDate(ImportSession obj, int index) {
        Date creationDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreationDate(creationDate);
    }

	public ImportSession getSpecificImportSession(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        ImportSession obj = data.get(index);
        Long id = obj.getId();
        return ImportSession.findImportSession(id);
    }

	public ImportSession getRandomImportSession() {
        init();
        ImportSession obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return ImportSession.findImportSession(id);
    }

	public boolean modifyImportSession(ImportSession obj) {
        return false;
    }

	public void init() {
        int from = 0;
        int to = 10;
        data = ImportSession.findImportSessionEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'ImportSession' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<ImportSession>();
        for (int i = 0; i < 10; i++) {
            ImportSession obj = getNewTransientImportSession(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
}
