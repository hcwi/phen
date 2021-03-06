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
import pl.poznan.igr.domain.analysis.FDAnalysisSession;

@Component
@Configurable
public class StatsSessionDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<FDAnalysisSession> data;

	@Autowired
    BlobFileDataOnDemand blobFileDataOnDemand;

	@Autowired
    ContextDataOnDemand contextDataOnDemand;

	public FDAnalysisSession getNewTransientStatsSession(int index) {
        FDAnalysisSession obj = new FDAnalysisSession();
        setContext(obj, index);
        setCreationDate(obj, index);
        return obj;
    }

	public void setContext(FDAnalysisSession obj, int index) {
        Context context = contextDataOnDemand.getSpecificContext(index);
        obj.setContext(context);
    }

	public void setCreationDate(FDAnalysisSession obj, int index) {
        Date creationDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreationDate(creationDate);
    }

	public FDAnalysisSession getSpecificStatsSession(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        FDAnalysisSession obj = data.get(index);
        Long id = obj.getId();
        return FDAnalysisSession.findStatsSession(id);
    }

	public FDAnalysisSession getRandomStatsSession() {
        init();
        FDAnalysisSession obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return FDAnalysisSession.findStatsSession(id);
    }

	public boolean modifyStatsSession(FDAnalysisSession obj) {
        return false;
    }

	public void init() {
        int from = 0;
        int to = 10;
        data = FDAnalysisSession.findStatsSessionEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'StatsSession' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<FDAnalysisSession>();
        for (int i = 0; i < 10; i++) {
            FDAnalysisSession obj = getNewTransientStatsSession(i);
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
