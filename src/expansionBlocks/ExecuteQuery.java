/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package expansionBlocks;

import Parameters.Parameters;
import java.util.Map;
import java.util.Set;
import configurations.Configuration;
import java.io.FileNotFoundException;
import java.io.IOException;
import model.Query;
import utils.IndriModule;
import utils.TimeCalculator;

/**
 *
 * @author joan
 */
public class ExecuteQuery
{

static public Map<String, Double> execute(Configuration configuration, Query query) throws Exception
{

	Map<String, Double> videos;
	switch (configuration.getPipeline())
	{
		default:
			videos = IndriModule.getVideos(configuration, query);
			query.setResult(videos);
			break;
		case REWEIGHT_QUERIES:
			videos = IndriModule.getVideos(configuration, query, query.getRunIndriQuery());
	}

	return videos;
}

private static void println(Object string)
{
	System.out.println("	[ExecuteQuery.java]: " + string);
}

private static void println()
{
	System.out.println("	[ExecuteQuery.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[ExecuteQuery.java]: " + string);
}

private static void print()
{
	System.out.print("	[ExecuteQuery.java]: ");
}

public static void main(String[] args) throws FileNotFoundException, IOException, Exception
{
	Parameters parameters = new Parameters("input.cfg");
	Configuration c = new Configuration(parameters);

	Query q = Query.readQueryLine(c, "72;skeleton of dinosaur;a skeleton of a dinosaur of any species. The skeleton can be shown in its completeness or in part; ; ; ; ; ; ; ; ; ; ; ; ;");

	QueryPreProcess.execute(c, q);

	CreatePaths.execute(c, q);

	SelectPaths.execute(c, q);

	CreateCommunitiesFromPaths.execute(c, q);

//	ProcessCommunities.execute(c, q);
//	execute(c, q);
	Query.serialize(c, q);
	Query.deserialize(c);

}

public static TimeCalculator execute(Configuration configuration, Set<Query> queries) throws Exception
{
	TimeCalculator tc = new TimeCalculator();
	for (Query query : queries)
	{
		tc.insert(query.getId() + " " + query.getInput(), System.currentTimeMillis());
		execute(configuration, query);
		tc.insert(query.getId() + " " + query.getInput(), System.currentTimeMillis());
	}
	return tc;
}
}
