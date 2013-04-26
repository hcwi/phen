package pl.poznan.igr.service.stats;

import java.io.IOException;

import pl.poznan.igr.domain.Context;

public interface StatsService {

	void calculateStats(Context ctx);

	void calculateStats(String fileName) throws IOException, InterruptedException;
}
