package pl.poznan.igr.domain;

import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ImportSessionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    ImportSessionDataOnDemand dod;

	@Test
    public void testCountImportSessions() {
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", dod.getRandomImportSession());
        long count = ImportSession.countImportSessions();
        Assert.assertTrue("Counter for 'ImportSession' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindImportSession() {
        ImportSession obj = dod.getRandomImportSession();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to provide an identifier", id);
        obj = ImportSession.findImportSession(id);
        Assert.assertNotNull("Find method for 'ImportSession' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'ImportSession' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllImportSessions() {
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", dod.getRandomImportSession());
        long count = ImportSession.countImportSessions();
        Assert.assertTrue("Too expensive to perform a find all test for 'ImportSession', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<ImportSession> result = ImportSession.findAllImportSessions();
        Assert.assertNotNull("Find all method for 'ImportSession' illegally returned null", result);
        Assert.assertTrue("Find all method for 'ImportSession' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindImportSessionEntries() {
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", dod.getRandomImportSession());
        long count = ImportSession.countImportSessions();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ImportSession> result = ImportSession.findImportSessionEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'ImportSession' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'ImportSession' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testFlush() {
        ImportSession obj = dod.getRandomImportSession();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to provide an identifier", id);
        obj = ImportSession.findImportSession(id);
        Assert.assertNotNull("Find method for 'ImportSession' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyImportSession(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'ImportSession' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testMergeUpdate() {
        ImportSession obj = dod.getRandomImportSession();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to provide an identifier", id);
        obj = ImportSession.findImportSession(id);
        boolean modified =  dod.modifyImportSession(obj);
        Integer currentVersion = obj.getVersion();
        ImportSession merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'ImportSession' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testPersist() {
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", dod.getRandomImportSession());
        ImportSession obj = dod.getNewTransientImportSession(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'ImportSession' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'ImportSession' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        ImportSession obj = dod.getRandomImportSession();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ImportSession' failed to provide an identifier", id);
        obj = ImportSession.findImportSession(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'ImportSession' with identifier '" + id + "'", ImportSession.findImportSession(id));
    }
}
