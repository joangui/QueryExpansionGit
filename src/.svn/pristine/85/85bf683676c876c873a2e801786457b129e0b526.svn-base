/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import Parameters.Parameters;
import Strings.StringUtils;
import configurations.Configuration;
import configurations.Configuration.METHOD;
import edu.upc.dama.dex.core.DbGraph;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;
import edu.upc.dama.dex.utils.DexUtil;
import static edu.upc.dama.expressions.MethodExpressionHelper.METHOD;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import model.Article;
import model.Entity;
import model.Paths;
import model.Query;

/**
 *
 * @author joan
 */
public abstract class AbstractCommunityMethod
{

protected int nodeType;
protected int edgeType;
protected Graph graph = DexUtil.getDBGraph();
protected Map<Integer, Map<Long, Long>> totalDegrees = new HashMap<>();
protected String file;
protected Objects allNodes;
private final METHOD method;
private final Random rnd;
private final int MAX_CANDIDATES_SIZE = 100;
protected final int totalNumberOfNodes;
protected final Configuration configuration;
//private Query query;

public AbstractCommunityMethod(Configuration config)
{
	this.nodeType = config.getNodeType();
	this.edgeType = config.getEdgeType();

	method = config.getCommunityMethod();
	Parameters parameters = config.getParameters();
	switch (method)
	{
		case WCC:
			this.file = parameters.getString("WCC_FILE");
			break;
		case OSLOM:
			this.file = parameters.getString("OSLOM_FILE");
			break;
		case LOUVAIN:
			this.file = parameters.getString("LOUVAIN_FILE");
			break;
		case INFOMAP:
			this.file = parameters.getString("INFOMAP_FILE");
			break;
	}

	this.allNodes = config.getAllNodes();
	this.totalNumberOfNodes = config.getTotalNumberOfNodes();
	rnd = new Random(7);
	this.configuration = config;
}

/**
 *
 * @param graph This algorithms search communities in graph.
 * @param query It uses paths in query as seed. The algorithm builds a community
 * around each path.
 * @return The community that better represents the seeds.
 * @throws java.lang.Exception
 */
public Map<Set<Long>, Map<Entity, Double>> execute(Query query) throws Exception
{
	Paths paths = query.getSelectedPaths();

	//this.query = query;
	Map<Set<Long>, Map<Entity, Double>> communities = new HashMap<>();

	for(int i = 0; i < paths.size();i++)
	{
		Set<Long> path = paths.get(i);
		//System.out.println("path "+ i +": "+path);
		println(i + ": Calculating community for path: " + path);

		Map<Entity, Double> community = executeMethod(path);

		/*if (configuration.addRedirectsToCommunities())
		{
			community = addRedirectionsForDocs(community);
		}*/
		communities.put(path, community);
		//i++;

	}

	return communities;
}

/*public Query getQuery()
 {
 return query;
 }*/
Map<Entity, Double> executeMethod(Set<Long> path)
{
	System.out.println("Path: " + path);

	Set<Long> communityNodes = new HashSet<>();
	communityNodes.addAll(path);
	List<Long> candidates = new ArrayList<>();
	for (Long id : path)
	{
		Objects neighbors = graph.neighbors(id, edgeType, Graph.EDGES_BOTH);
		for (Long id2 : neighbors)
		{
			candidates.add(id2);
		}
		neighbors.close();
	}
	System.out.println("    List of candidates size: " + candidates.size());

	double initialModularity = calculateMetric(new ArrayList<Long>(communityNodes));

	double bestModularity = initialModularity;
	long candidateNode = -1;

	boolean metricIncreases = true;
	while (metricIncreases && candidates.size() > 0)
	{
		metricIncreases = false;
		for (long id : candidates)
		{
			double tmpModularity = calculateMetric(new ArrayList<Long>(communityNodes), id);
			if (tmpModularity > bestModularity)
			{
				bestModularity = tmpModularity;
				candidateNode = id;
				metricIncreases = true;
			}
		}
		System.out.println("    " + method + ": " + bestModularity);

		//Update community and candidates list
		if (metricIncreases)
		{
			System.out.println("    Improving the community");
			communityNodes.add(candidateNode);
			List<Long> tmpCandidates = new ArrayList<Long>();
			Objects neighbors = graph.neighbors(candidateNode, edgeType, Graph.EDGES_BOTH);
			for (Long id2 : neighbors)
			{
				tmpCandidates.add(id2);
			}
			Collections.shuffle(tmpCandidates, rnd);
			int SUBLIST_SIZE = tmpCandidates.size() < MAX_CANDIDATES_SIZE ? tmpCandidates.size() : MAX_CANDIDATES_SIZE;
			candidates.addAll(tmpCandidates.subList(0, SUBLIST_SIZE));
			neighbors.close();
			System.out.println("            List of candidates size: " + candidates.size());
		}
	}
	System.out.println("    Community: " + communityNodes + "\n");

	Double weightPerNode = 1.0 / communityNodes.size();
	Map<Entity, Double> community = new HashMap<>();
	for (Long id : communityNodes)
	{
		community.put(new Entity(id), weightPerNode);

	}

	return community;
}

protected long calculateDegree(long id, int edgeType, short EDGES_BOTH)
{

	Map<Long, Long> typeTotalDegrees = totalDegrees.get(edgeType);
	if (typeTotalDegrees == null)
	{
		typeTotalDegrees = new HashMap<>();
	}

	Long idTypeTotalDegrees = typeTotalDegrees.get(id);
	if (idTypeTotalDegrees == null)
	{
		/*            Objects explode = graph.explode(id, edgeType, EDGES_BOTH);
		 idTypeTotalDegrees = explode.size();
		 explode.close();
		 */
		idTypeTotalDegrees = graph.degree(id, edgeType, EDGES_BOTH);
		typeTotalDegrees.put(id, idTypeTotalDegrees);
		totalDegrees.put(edgeType, typeTotalDegrees);
	}

	return idTypeTotalDegrees;
}

abstract protected double calculateMetric(Collection<Long> community);

protected double calculateMetric(Collection<Long> community, long id)
{
	community.add(id);
	double modularity = calculateMetric(community);
	community.remove(id);
	return modularity;
}

public void close()
{
	allNodes.close();
}

private Map<Long, Double> addRedirectionsForDocs(Map<Long, Double> community)
{
	Map<Long, Double> finalCollection = new HashMap<>();
	for (Map.Entry<Long, Double> e : community.entrySet())
	{
		Long id = e.getKey();
		Double weight = e.getValue();
		Objects redirects = graph.neighbors(id, Article.getRedirectionEdgeType(), Graph.EDGES_BOTH);
		redirects.add(id);
		Objects redirects2 = graph.neighbors(redirects, Article.getRedirectionEdgeType(), Graph.EDGES_BOTH);
		redirects.close();

		for (Long id_redirect : redirects2)
		{
			finalCollection.put(id_redirect, weight);
		}
		finalCollection.put(id, weight);
		redirects2.close();
	}
	community = null;
	return finalCollection;
}

private void println(Object string)
{
	System.out.println("	[AbstractCommunityMethod.java]: " + string);
}

}
