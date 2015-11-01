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
public class UnzipSessionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    UnzipSessionDataOnDemand dod;

	@Test
    public void testCountUnzipSessions() {
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", dod.getRandomUnzipSession());
        long count = UnzipSession.countUnzipSessions();
        Assert.assertTrue("Counter for 'UnzipSession' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindUnzipSession() {
        UnzipSession obj = dod.getRandomUnzipSession();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to provide an identifier", id);
        obj = UnzipSession.findUnzipSession(id);
        Assert.assertNotNull("Find method for 'UnzipSession' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'UnzipSession' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllUnzipSessions() {
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", dod.getRandomUnzipSession());
        long count = UnzipSession.countUnzipSessions();
        Assert.assertTrue("Too expensive to perform a find all test for 'UnzipSession', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<UnzipSession> result = UnzipSession.findAllUnzipSessions();
        Assert.assertNotNull("Find all method for 'UnzipSession' illegally returned null", result);
        Assert.assertTrue("Find all method for 'UnzipSession' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindUnzipSessionEntries() {
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", dod.getRandomUnzipSession());
        long count = UnzipSession.countUnzipSessions();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<UnzipSession> result = UnzipSession.findUnzipSessionEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'UnzipSession' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'UnzipSession' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testFlush() {
        UnzipSession obj = dod.getRandomUnzipSession();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to provide an identifier", id);
        obj = UnzipSession.findUnzipSession(id);
        Assert.assertNotNull("Find method for 'UnzipSession' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyUnzipSession(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'UnzipSession' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testMergeUpdate() {
        UnzipSession obj = dod.getRandomUnzipSession();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to provide an identifier", id);
        obj = UnzipSession.findUnzipSession(id);
        boolean modified =  dod.modifyUnzipSession(obj);
        Integer currentVersion = obj.getVersion();
        UnzipSession merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'UnzipSession' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testPersist() {
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", dod.getRandomUnzipSession());
        UnzipSession obj = dod.getNewTransientUnzipSession(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'UnzipSession' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'UnzipSession' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        UnzipSession obj = dod.getRandomUnzipSession();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'UnzipSession' failed to provide an identifier", id);
        obj = UnzipSession.findUnzipSession(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'UnzipSession' with identifier '" + id + "'", UnzipSession.findUnzipSession(id));
    }
}
