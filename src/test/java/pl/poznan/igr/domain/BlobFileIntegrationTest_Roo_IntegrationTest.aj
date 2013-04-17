// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package pl.poznan.igr.domain;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.BlobFileDataOnDemand;
import pl.poznan.igr.domain.BlobFileIntegrationTest;

privileged aspect BlobFileIntegrationTest_Roo_IntegrationTest {
    
    declare @type: BlobFileIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: BlobFileIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml");
    
    declare @type: BlobFileIntegrationTest: @Transactional;
    
    @Autowired
    BlobFileDataOnDemand BlobFileIntegrationTest.dod;
    
    @Test
    public void BlobFileIntegrationTest.testCountBlobFiles() {
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", dod.getRandomBlobFile());
        long count = BlobFile.countBlobFiles();
        Assert.assertTrue("Counter for 'BlobFile' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void BlobFileIntegrationTest.testFindBlobFile() {
        BlobFile obj = dod.getRandomBlobFile();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to provide an identifier", id);
        obj = BlobFile.findBlobFile(id);
        Assert.assertNotNull("Find method for 'BlobFile' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'BlobFile' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void BlobFileIntegrationTest.testFindAllBlobFiles() {
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", dod.getRandomBlobFile());
        long count = BlobFile.countBlobFiles();
        Assert.assertTrue("Too expensive to perform a find all test for 'BlobFile', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<BlobFile> result = BlobFile.findAllBlobFiles();
        Assert.assertNotNull("Find all method for 'BlobFile' illegally returned null", result);
        Assert.assertTrue("Find all method for 'BlobFile' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void BlobFileIntegrationTest.testFindBlobFileEntries() {
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", dod.getRandomBlobFile());
        long count = BlobFile.countBlobFiles();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<BlobFile> result = BlobFile.findBlobFileEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'BlobFile' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'BlobFile' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void BlobFileIntegrationTest.testFlush() {
        BlobFile obj = dod.getRandomBlobFile();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to provide an identifier", id);
        obj = BlobFile.findBlobFile(id);
        Assert.assertNotNull("Find method for 'BlobFile' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyBlobFile(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'BlobFile' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void BlobFileIntegrationTest.testMergeUpdate() {
        BlobFile obj = dod.getRandomBlobFile();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to provide an identifier", id);
        obj = BlobFile.findBlobFile(id);
        boolean modified =  dod.modifyBlobFile(obj);
        Integer currentVersion = obj.getVersion();
        BlobFile merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'BlobFile' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void BlobFileIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", dod.getRandomBlobFile());
        BlobFile obj = dod.getNewTransientBlobFile(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'BlobFile' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        Assert.assertNotNull("Expected 'BlobFile' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void BlobFileIntegrationTest.testRemove() {
        BlobFile obj = dod.getRandomBlobFile();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'BlobFile' failed to provide an identifier", id);
        obj = BlobFile.findBlobFile(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'BlobFile' with identifier '" + id + "'", BlobFile.findBlobFile(id));
    }
    
}
