/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import Graph.GraphCommunities;
import Parameters.Parameters;
import configurations.Configuration;
import edu.upc.dama.dex.core.DbGraph;
import edu.upc.dama.dex.utils.DexUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Entity;

/**
 *
 * @author joan
 */
public class WCC_Circles extends AbstractCommunityMethod
{

private final GraphCommunities graphCommunities;

public WCC_Circles(Configuration config) throws FileNotFoundException, IOException
{
	super(config);

	Parameters param = config.getParameters();

	String[] emptyParameters = new String[1];
	emptyParameters[0] = "";
	Parameters p = new Parameters(emptyParameters, true);
	p.setString("nodeType", config.getNODE_NAME());
	p.setString("edgeType", param.readString("links", "SIMILARITY_EDGE_NAME"));
	p.setInteger("sample", 250);
	p.setBoolean("AVOID_PRECOMPUTE", false);
	p.setBoolean("inMemory", param.readBoolean(true, "IN_MEMORY_COMMUNITIES"));
	p.setBoolean("forcePrecompute", param.readBoolean(false, "FORCE_PRECOMPUTE"));
	p.setEnum("improveMethod", param.readEnum(GraphCommunities.IMPROVE_COMMUNITIES_METHOD.THREE_HOP, "IMPROVE_METHOD"));
	p.setBoolean("printDetail", param.readBoolean(false, "PRINT_COMMUNITIES_DETAILS"));
	p.setString("weightingAttribute", param.readString("", "WEIGHTING_ATTRIBUTE"));

	println("Llegint parameters Similarity edge type: " + param.readString("links", "SIMILARITY_EDGE_NAME"));

	graphCommunities = new GraphCommunities(DexUtil.getDexImage(), "Article", param.readString("links", "SIMILARITY_EDGE_NAME"), p);
	graphCommunities.setSeed(7);

}

@Override
Map<Entity, Double> executeMethod(Set<Long> path)
{
	String s = "";
	List<Long> targetList = new ArrayList<Long>();
	boolean coma = false;
	for (Long id : path)
	{
		if (coma)
		{
			s += "," + id.toString();
		} else
		{
			s += id.toString();
		}
		coma = true;
		targetList.add(id);
	}
	println("Calculing community for " + targetList);
	long start = System.nanoTime();
	Map<Long, Double> calculateCommunity = graphCommunities.calculateCommunity(targetList);
	Map<Entity, Double> entityCommunity = new HashMap<>();//graphCommunities.calculateCommunity(targetList);
	for (Map.Entry<Long, Double> e : calculateCommunity.entrySet())
	{
		Entity entity = new Entity(e.getKey());
		entityCommunity.put(entity, e.getValue());
	}

	return entityCommunity;

}

@Override
protected double calculateMetric(Collection<Long> community)
{
	throw new UnsupportedOperationException("Not supported yet.");
}

private static void println(Object string)
{
	System.out.println("	[WCC_Circles.java]: " + string);
}

private static void println()
{
	System.out.println("	[WCC_Circles.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[WCC_Circles.java]: " + string);
}

private static void print()
{
	System.out.print("	[WCC_Circles.java]: ");
}
}
