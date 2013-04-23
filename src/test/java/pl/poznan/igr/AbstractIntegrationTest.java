package pl.poznan.igr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext*.xml")
@Transactional
public abstract class AbstractIntegrationTest {

	@PersistenceContext
	transient EntityManager entityManager;

	/*@Before
	public void setUpMocks() {
		MockitoAnnotations.initMocks(this);
	}*/

	/*public void cleanUpDatabase() {
		hqlDelete(ImportSession.class);
		hqlDelete(ZipSession.class);
		hqlDelete(BlobItem.class);
		hqlDelete(StatisticsSession.class);
		hqlDelete(Context.class);
	}

	public int hqlDelete(Class<?> entity) {
		String hql = String.format("delete from %s", entity.getName());
		Query query = entityManager.createQuery(hql);
		return query.executeUpdate();
	}
*/
}
