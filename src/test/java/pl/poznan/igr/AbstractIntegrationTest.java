package pl.poznan.igr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import pl.poznan.igr.domain.BlobFile;
import pl.poznan.igr.domain.Context;
import pl.poznan.igr.domain.ImportSession;
import pl.poznan.igr.domain.AnalysisASession;
import pl.poznan.igr.domain.UnzipSession;
import pl.poznan.igr.service.UnzipServiceIntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@Transactional
public abstract class AbstractIntegrationTest {

	@PersistenceContext
	transient EntityManager entityManager;

	@Before
	public void setUpMocks() {
		MockitoAnnotations.initMocks(this);
	}

	protected final static Logger log = LoggerFactory
			.getLogger(UnzipServiceIntegrationTest.class);

	public void cleanUpDatabase() {
		hqlDelete(ImportSession.class);
		hqlDelete(AnalysisASession.class);
		hqlDelete(UnzipSession.class);
		hqlDelete(BlobFile.class);
		hqlDelete(Context.class);
	}

	public int hqlDelete(Class<?> entity) {
		String hql = String.format("delete from %s", entity.getName());
		Query query = entityManager.createQuery(hql);
		return query.executeUpdate();
	}

}
