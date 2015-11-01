package pl.poznan.igr.domain;

import org.hibernate.cfg.Configuration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import java.io.IOException;
import java.util.Properties;

public class HibernateExporter {

    public static void main(String[] args) throws IOException {
        execute("persistenceUnit", "createTable.sql", true);
        execute("persistenceUnit", "dropTable.sql", false);
    }

    public static void execute(String persistenceUnitName, String destination, boolean create) {
        Ejb3Configuration cfg = new Ejb3Configuration().configure(persistenceUnitName, new Properties());
        Configuration hbmcfg = cfg.getHibernateConfiguration();
        SchemaExport schemaExport = new SchemaExport(hbmcfg);
        schemaExport.setOutputFile(destination);
        schemaExport.setFormat(true);
        schemaExport.execute(true, false, !create, create);
    }
}

