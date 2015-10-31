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
import pl.poznan.igr.domain.analysis.AnalysisASession;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class AnalysisASessionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    StatsSessionDataOnDemand dod;

	@Test
    public void testCountStatsSessions() {
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", dod.getRandomStatsSession());
        long count = AnalysisASession.countStatsSessions();
        Assert.assertTrue("Counter for 'StatsSession' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindStatsSession() {
        AnalysisASession obj = dod.getRandomStatsSession();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to provide an identifier", id);
        obj = AnalysisASession.findStatsSession(id);
        Assert.assertNotNull("Find method for 'StatsSession' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'StatsSession' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllStatsSessions() {
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", dod.getRandomStatsSession());
        long count = AnalysisASession.countStatsSessions();
        Assert.assertTrue("Too expensive to perform a find all test for 'StatsSession', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<AnalysisASession> result = AnalysisASession.findAllStatsSessions();
        Assert.assertNotNull("Find all method for 'StatsSession' illegally returned null", result);
        Assert.assertTrue("Find all method for 'StatsSession' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindStatsSessionEntries() {
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", dod.getRandomStatsSession());
        long count = AnalysisASession.countStatsSessions();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<AnalysisASession> result = AnalysisASession.findStatsSessionEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'StatsSession' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'StatsSession' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testFlush() {
        AnalysisASession obj = dod.getRandomStatsSession();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to provide an identifier", id);
        obj = AnalysisASession.findStatsSession(id);
        Assert.assertNotNull("Find method for 'StatsSession' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyStatsSession(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'StatsSession' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testMergeUpdate() {
        AnalysisASession obj = dod.getRandomStatsSession();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to provide an identifier", id);
        obj = AnalysisASession.findStatsSession(id);
        boolean modified =  dod.modifyStatsSession(obj);
        Integer currentVersion = obj.getVersion();
        AnalysisASession merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'StatsSession' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }

	@Test
    public void testPersist() {
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", dod.getRandomStatsSession());
        AnalysisASession obj = dod.getNewTransientStatsSession(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'StatsSession' identifier to be null", obj.getId());
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
        Assert.assertNotNull("Expected 'StatsSession' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        AnalysisASession obj = dod.getRandomStatsSession();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'StatsSession' failed to provide an identifier", id);
        obj = AnalysisASession.findStatsSession(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'StatsSession' with identifier '" + id + "'", AnalysisASession.findStatsSession(id));
    }
}
