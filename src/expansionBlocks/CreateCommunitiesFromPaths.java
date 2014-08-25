/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package expansionBlocks;

import Parameters.Parameters;
import Strings.StringUtils;
import communityMethods.AbstractCommunityMethod;
import configurations.Configuration;
import configurations.Configuration.METHOD;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Article;
import model.Entity;
import model.Paths;
import model.Query;

/**
 *
 * @author joan
 */
public class CreateCommunitiesFromPaths
{

static protected String QUERY_FILE;

static public Map<Set<Long>, Map<Entity, Double>> execute(Configuration config, Query query) throws IOException, Exception
{

	AbstractCommunityMethod gm = config.getAbstractCommunityMethod();
	Map<Set<Long>, Map<Entity, Double>> mapPathCommunitties = gm.execute(query);
	int i = 1;
	for (Map.Entry<Set<Long>, Map<Entity, Double>> e : mapPathCommunitties.entrySet())
	{
		StringBuilder sb = new StringBuilder();
		double wcc = 0.0;
		Set<Long> targetList = e.getKey();
		Paths paths = query.getSelectedPaths();
		Map<Entity, Double> calculateCommunity = e.getValue();
		println("Calculated community  (" + (targetList) + (") ") + (i) + ("/") + (paths.size()));
		sb.append("This community contains ").append(calculateCommunity.size()).append(" nodes: {");
		for (Map.Entry<Entity, Double> e2 : calculateCommunity.entrySet())
		{

			sb.append("[").append(e2.getKey()).append(",").append(StringUtils.decimalFormat(e2.getValue(), 2)).append("],");

			wcc += e2.getValue();
		}
		sb.append(":}. WCC=").append(StringUtils.decimalFormat(wcc / calculateCommunity.size(), 2));
		i++;
		println(sb);
	}
	query.setCommunities(mapPathCommunitties);
	return mapPathCommunitties;

}

public static void main(String[] args) throws FileNotFoundException, IOException, Exception
{
	Parameters parameters = new Parameters("input.cfg");
	Configuration c = new Configuration(parameters);

	Query q = Query.readQueryLine(c, "71;colored Volkswagen beetles;Volkswagen beetles in any other color, for example, red, blue, green or yellow; ; ; ; ; ; ; ; ; ; ; ; ;");

	QueryPreProcess.execute(c, q);

	CreatePaths.execute(c, q);

	SelectPaths.execute(c, q);

	execute(c, q);

}

private static void printCommunity(METHOD method, Set<Query> queries) throws FileNotFoundException, UnsupportedEncodingException
{
	String QueryCommunityFile = QUERY_FILE.replace("paths", "communities_" + method);
	PrintWriter writer = FileManagement.Writer.getWriter(QueryCommunityFile);

	for (Query query : queries)
	{
		writer.print(query.getId() + ";");
		Collection<Map<Entity, Double>> communities = query.getCommunities().values();
		boolean first = true;
		if (!first)
		{
			writer.print(",");
		}
		for (Map<Entity, Double> community : communities)
		{
			boolean first2 = true;
			for (Map.Entry<Entity, Double> e3 : community.entrySet())
			{
				long id = e3.getKey().getId();
				if (!first2)
				{
					writer.print("-");
				}
				writer.print(id);
				first2 = false;

			}
		}
		writer.println();
	}
	writer.close();

}

private static List<List<Long>> readPaths(Parameters param)
{
	List<List<Long>> paths = new ArrayList<>();
	List<String> stringPaths = param.readStringList(null, "PATHS", ",");
	for (String s : stringPaths)
	{
		List<Long> path = new ArrayList<>();
		String[] split = s.split("-");
		for (String s2 : split)
		{
			path.add(Long.valueOf(s2));
		}
		paths.add(path);
	}
	return paths;

}

private static void println(Object string)
{
	System.out.println("	[CreateCommunitiesFromPaths.java]: " + string);
}

private static void println()
{
	System.out.println("	[CreateCommunitiesFromPaths.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[CreateCommunitiesFromPaths.java]: " + string);
}

private static void print()
{
	System.out.print("	[CreateCommunitiesFromPaths.java]: ");
}

public static void execute(Configuration configuration, Set<Query> queries) throws Exception
{
	for (Query q : queries)
	{
		execute(configuration, q);
	}
}
}
