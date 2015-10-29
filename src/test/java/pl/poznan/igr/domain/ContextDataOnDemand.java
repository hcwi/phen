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
import pl.poznan.igr.domain.type.Status;

@Configurable
@Component
public class ContextDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Context> data;

	@Autowired
    ImportSessionDataOnDemand importSessionDataOnDemand;

	@Autowired
    StatsSessionDataOnDemand statsSessionDataOnDemand;

	@Autowired
    UnzipSessionDataOnDemand unzipSessionDataOnDemand;

	public Context getNewTransientContext(int index) {
        Context obj = new Context();
        setFinished(obj, index);
        setOwner(obj, index);
        setStarted(obj, index);
        setStatus(obj, index);
        setZipSession(obj, index);
        return obj;
    }

	public void setFinished(Context obj, int index) {
        Date finished = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setFinished(finished);
    }

	public void setOwner(Context obj, int index) {
        String owner = "owner_" + index;
        if (owner.length() > 64) {
            owner = owner.substring(0, 64);
        }
        obj.setOwner(owner);
    }

	public void setStarted(Context obj, int index) {
        Date started = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setStarted(started);
    }

	public void setStatus(Context obj, int index) {
        Status status = Status.class.getEnumConstants()[0];
        obj.setStatus(status);
    }

	public void setZipSession(Context obj, int index) {
        ZipSession zipSession = null;
        obj.setZipSession(zipSession);
    }

	public Context getSpecificContext(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Context obj = data.get(index);
        Long id = obj.getId();
        return Context.findContext(id);
    }

	public Context getRandomContext() {
        init();
        Context obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return Context.findContext(id);
    }

	public boolean modifyContext(Context obj) {
        return false;
    }

	public void init() {
        int from = 0;
        int to = 10;
        data = Context.findContextEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Context' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Context>();
        for (int i = 0; i < 10; i++) {
            Context obj = getNewTransientContext(i);
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
