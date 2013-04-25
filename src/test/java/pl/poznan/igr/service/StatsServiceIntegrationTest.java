package pl.poznan.igr.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import pl.poznan.igr.AbstractIntegrationTest;
import pl.poznan.igr.service.stats.StatsService;

public class StatsServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private StatsService statsService;

	@Test
	public void testCalculateStats() {
		
		statsService.calculateStats(null);
	}

}
