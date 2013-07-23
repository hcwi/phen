// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

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
import org.springframework.stereotype.Component;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ContextDataOnDemand;
import pl.poznan.igr.domain.ImportSessionDataOnDemand;
import pl.poznan.igr.domain.StatsSessionDataOnDemand;
import pl.poznan.igr.domain.UnzipSessionDataOnDemand;
import pl.poznan.igr.domain.type.Status;

privileged aspect ContextDataOnDemand_Roo_DataOnDemand {
    
    declare @type: ContextDataOnDemand: @Component;
    
    private Random ContextDataOnDemand.rnd = new SecureRandom();
    
    private List<Context> ContextDataOnDemand.data;
    
    @Autowired
    ImportSessionDataOnDemand ContextDataOnDemand.importSessionDataOnDemand;
    
    @Autowired
    StatsSessionDataOnDemand ContextDataOnDemand.statsSessionDataOnDemand;
    
    @Autowired
    UnzipSessionDataOnDemand ContextDataOnDemand.unzipSessionDataOnDemand;
    
    public Context ContextDataOnDemand.getNewTransientContext(int index) {
        Context obj = new Context();
        setFinished(obj, index);
        setOwner(obj, index);
        setStarted(obj, index);
        setStatus(obj, index);
        setStatusMessage(obj, index);
        return obj;
    }
    
    public void ContextDataOnDemand.setFinished(Context obj, int index) {
        Date finished = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setFinished(finished);
    }
    
    public void ContextDataOnDemand.setOwner(Context obj, int index) {
        String owner = "owner_" + index;
        if (owner.length() > 64) {
            owner = owner.substring(0, 64);
        }
        obj.setOwner(owner);
    }
    
    public void ContextDataOnDemand.setStarted(Context obj, int index) {
        Date started = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setStarted(started);
    }
    
    public void ContextDataOnDemand.setStatus(Context obj, int index) {
        Status status = Status.class.getEnumConstants()[0];
        obj.setStatus(status);
    }
    
    public void ContextDataOnDemand.setStatusMessage(Context obj, int index) {
        String statusMessage = "statusMessage_" + index;
        obj.setStatusMessage(statusMessage);
    }
    
    public Context ContextDataOnDemand.getSpecificContext(int index) {
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
    
    public Context ContextDataOnDemand.getRandomContext() {
        init();
        Context obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return Context.findContext(id);
    }
    
    public boolean ContextDataOnDemand.modifyContext(Context obj) {
        return false;
    }
    
    public void ContextDataOnDemand.init() {
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
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
    
}
