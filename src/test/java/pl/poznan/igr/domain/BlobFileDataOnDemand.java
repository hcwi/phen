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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Configurable
@Component
public class BlobFileDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<BlobFile> data;

	public BlobFile getNewTransientBlobFile(int index) {
        BlobFile obj = new BlobFile();
        setContent(obj, index);
        setContentType(obj, index);
        setCreated(obj, index);
        setFileName(obj, index);
        return obj;
    }

	public void setContent(BlobFile obj, int index) {
        byte[] content = String.valueOf(index).getBytes();
        obj.setContent(content);
    }

	public void setContentType(BlobFile obj, int index) {
        String contentType = "contentType_" + index;
        obj.setContentType(contentType);
    }

	public void setCreated(BlobFile obj, int index) {
        Date created = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreated(created);
    }

	public void setFileName(BlobFile obj, int index) {
        String fileName = "fileName_" + index;
        obj.setFileName(fileName);
    }

	public BlobFile getSpecificBlobFile(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        BlobFile obj = data.get(index);
        Long id = obj.getId();
        return BlobFile.findBlobFile(id);
    }

	public BlobFile getRandomBlobFile() {
        init();
        BlobFile obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return BlobFile.findBlobFile(id);
    }

	public boolean modifyBlobFile(BlobFile obj) {
        return false;
    }

	public void init() {
        int from = 0;
        int to = 10;
        data = BlobFile.findBlobFileEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'BlobFile' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<BlobFile>();
        for (int i = 0; i < 10; i++) {
            BlobFile obj = getNewTransientBlobFile(i);
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
