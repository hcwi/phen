// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package pl.poznan.igr.domain;

import java.util.Date;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.StatsSession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.domain.type.Status;

privileged aspect Context_Roo_JavaBean {
    
    public String Context.getOwner() {
        return this.owner;
    }
    
    public void Context.setOwner(String owner) {
        this.owner = owner;
    }
    
    public Date Context.getStarted() {
        return this.started;
    }
    
    public void Context.setStarted(Date started) {
        this.started = started;
    }
    
    public Date Context.getFinished() {
        return this.finished;
    }
    
    public void Context.setFinished(Date finished) {
        this.finished = finished;
    }
    
    public Status Context.getStatus() {
        return this.status;
    }
    
    public void Context.setStatus(Status status) {
        this.status = status;
    }
    
    public ImportSession Context.getImportSession() {
        return this.importSession;
    }
    
    public void Context.setImportSession(ImportSession importSession) {
        this.importSession = importSession;
    }
    
    public UnzipSession Context.getUnzipSession() {
        return this.unzipSession;
    }
    
    public void Context.setUnzipSession(UnzipSession unzipSession) {
        this.unzipSession = unzipSession;
    }
    
    public StatsSession Context.getStatsSession() {
        return this.statsSession;
    }
    
    public void Context.setStatsSession(StatsSession statsSession) {
        this.statsSession = statsSession;
    }
    
}
