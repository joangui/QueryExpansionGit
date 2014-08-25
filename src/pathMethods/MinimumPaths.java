/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathMethods;

import Structures.Pair;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;
import edu.upc.dama.dex.core.Value;
import edu.upc.dama.dex.utils.DexUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import model.Article;
import model.Category;

/**
 *
 * @author joan
 */
public class MinimumPaths //extends MultiDEXMinimumPathsTraversal 
{


protected Objects startNodes;
protected Objects endNodes;
protected int numNodes;
protected Objects univers;
protected List<Integer> nodeTypes;
protected List<Integer> edgeTypes;
protected final String MINIMUM_PATHS_TRAVERSAL_ATTRIBUTE_NAME = "PT";
protected Long start;
protected Objects touched;
protected int numberOfPaths;
protected int nodePou = 0;
HashSet<LinkedList<Long>> paths = new HashSet<LinkedList<Long>>();
Set<Set<Long>> pathsList = new HashSet<>();
private int freeEdge = Graph.INVALID_TYPE;

public MinimumPaths(List<Long> startNodesList, List<Long> endNodesList, List<Long> universeNodes, List<Integer> nodesType, List<Integer> edgesType)
{
	Graph g = DexUtil.getDBGraph();

	startNodes = new Objects(g.getSession());
	for (long node_id : startNodesList)
	{
		this.startNodes.add(node_id);
	}

	endNodes = new Objects(g.getSession());
	for (long node_id : endNodesList)
	{
		this.endNodes.add(node_id);
	}

	univers = new Objects(g.getSession());
	for (long node_id : universeNodes)
	{
		this.univers.add(node_id);
	}

	//univers = new Objects(g.getSession(), startNodes, endNodes, Objects.COMBINE_UNION);
	this.numNodes = startNodes.size();
	this.edgeTypes = edgesType;
	this.nodeTypes = nodesType;

	initialize();
}

public Map<Long, Double> doTraversal()
{
	println("Minimum paths...");
	println("	#start nodes: " + startNodes.size());
	println("	#end nodes: " + endNodes.size());
	println("	#univers nodes: " + univers.size());
	Objects.Iterator start_it = startNodes.iterator();
	while (start_it.hasNext())
	{
		numberOfPaths = 0;
		start = start_it.next();
		Queue<Long> q = new LinkedList<>();
		q.add(start);
		shortestPaths();
		if (numberOfPaths == 0)
		{
			nodePou++;
		}
		//println("Number of paths from "+getDocumentName(start)+": "+numberOfPaths);
	}
	println("Nombre de pous: " + nodePou + " sobre " + startNodes.size());
	endNodes.close();

	start_it.close();

	Map<Long, Double> result = new HashMap<>();
	Objects.Iterator it = touched.iterator();
	while (it.hasNext())
	{
		long oid = it.next();
		double PT = getPT(oid);
		result.put(oid, PT);
	}
	it.close();
	return result;

}

public void setFreeEdge(int freeEdge)
{
	this.freeEdge = freeEdge;
}

public void close()
{
	if (startNodes != null && startNodes.isOpen())
	{
		startNodes.close();
	}
	if (endNodes != null && endNodes.isOpen())
	{
		endNodes.close();
	}

	if (univers != null && univers.isOpen())
	{
		univers.close();
	}
	if (touched != null && touched.isOpen())
	{
		resetCounters();
		touched.close();
	}
	paths = null;
}

protected void initialize()
{
	Graph g = DexUtil.getDBGraph();
	for (int nodeType_id : nodeTypes)
	{

		long attributeID = g.findAttribute(nodeType_id, MINIMUM_PATHS_TRAVERSAL_ATTRIBUTE_NAME);
		if (attributeID == Graph.INVALID_ATTRIBUTE)
		{
			g.newAttribute(nodeType_id, MINIMUM_PATHS_TRAVERSAL_ATTRIBUTE_NAME, Value.DOUBLE);
		}
	}

	touched = new Objects(g.getSession());
}

protected Objects getNeighbors(long oid)
{
	return getNeighbors(oid, null);
}

protected Objects getNeighbors(long oid, Objects exclude)
{
	Graph g = DexUtil.getDBGraph();
	Objects neighbors = new Objects(g.getSession());
	for (int edge_id : edgeTypes)
	{
		Objects tmp = g.neighbors(oid, edge_id, Graph.EDGES_OUT);
		neighbors.union(tmp);
		tmp.close();
	}
	neighbors.intersection(univers);
	if (exclude != null)
	{
		neighbors.difference(exclude);
	}
	return neighbors;
}

private void setPT(long oid, Value v)
{
	Graph g = DexUtil.getDBGraph();
	g.setAttribute(oid, g.findAttribute(g.getType(oid), MINIMUM_PATHS_TRAVERSAL_ATTRIBUTE_NAME), v);
}

protected double getPT(long oid)
{
	Graph g = DexUtil.getDBGraph();
	Value v = g.getAttribute(oid, g.findAttribute(g.getType(oid), MINIMUM_PATHS_TRAVERSAL_ATTRIBUTE_NAME));
	if (v.isNull())
	{
		return 0.0;
	} else
	{
		return v.getDouble();
	}
}

private Double increasePT(long oid)
{
	Double newRP = getPT(oid) + 1;
	setPT(oid, new Value(newRP));
	return newRP;
}

protected String getDocumentName(Long oid)
{
	Graph g = DexUtil.getDBGraph();
	int CAT_TYPE = g.findType(Category.class.getSimpleName());
	int ART_TYPE = g.findType(Article.class.getSimpleName());

	String NAME_ATTR_NAME = "name";
	long NAME_ATTR_ID = g.findAttribute(CAT_TYPE, NAME_ATTR_NAME);

	String TITLE_ATTR_NAME = "title";
	long TITLE_ATTR_ID = g.findAttribute(ART_TYPE, TITLE_ATTR_NAME);
	String ret;
	if (g.getType(oid) == ART_TYPE)
	{
		ret = (g.getAttribute(oid, TITLE_ATTR_ID)).getString();
	} else
	{
		ret = ("||" + (g.getAttribute(oid, NAME_ATTR_ID)).getString() + "||");
	}

	return ret;
}

static public void printPath(Queue<Long> q)
{
	//println("Q="+q);
	Graph g = DexUtil.getDBGraph();
	Iterator it = q.iterator();
	int CAT_TYPE = g.findType(Category.class.getSimpleName());
	int ART_TYPE = g.findType(Article.class.getSimpleName());

	String NAME_ATTR_NAME = "name";
	long NAME_ATTR_ID = g.findAttribute(CAT_TYPE, NAME_ATTR_NAME);

	String TITLE_ATTR_NAME = "title";
	long TITLE_ATTR_ID = g.findAttribute(ART_TYPE, TITLE_ATTR_NAME);
	boolean printComma = false;
	StringBuilder bf = new StringBuilder();
	while (it.hasNext())
	{
		Long oid = (Long) it.next();
		if (printComma)
		{
			//		print(",");
		}
		printComma = true;
		if (g.getType(oid) == CAT_TYPE)
		{
			bf.append("||").append(g.getAttribute(oid, NAME_ATTR_ID)).append("[").append(oid).append("]||");
			//print(oid);
		} else
		{
			if (g.getType(oid) == ART_TYPE)
			{
				bf.append(g.getAttribute(oid, TITLE_ATTR_ID)).append("[").append(oid).append("]");
				//				print(oid);
			}
		}
		bf.append("<--");
	}
	//	println(":("+q.size()+")");
	println(bf);
}

public static String pathToString(Set<Long> path)
{
	Graph g = DexUtil.getDBGraph();
	Iterator it = path.iterator();
	int CAT_TYPE = g.findType(Category.class.getSimpleName());
	int ART_TYPE = g.findType(Article.class.getSimpleName());

	String NAME_ATTR_NAME = "name";
	long NAME_ATTR_ID = g.findAttribute(CAT_TYPE, NAME_ATTR_NAME);

	String TITLE_ATTR_NAME = "title";
	long TITLE_ATTR_ID = g.findAttribute(ART_TYPE, TITLE_ATTR_NAME);
	boolean printComma = false;
	StringBuilder bf = new StringBuilder();
	while (it.hasNext())
	{
		Long oid = (Long) it.next();
		if (printComma)
		{
			//		print(",");
		}
		printComma = true;
		if (g.getType(oid) == CAT_TYPE)
		{
			bf.append("||").append(g.getAttribute(oid, NAME_ATTR_ID)).append("[").append(oid).append("]||");
			//print(oid);
		} else
		{
			if (g.getType(oid) == ART_TYPE)
			{
				bf.append(g.getAttribute(oid, TITLE_ATTR_ID)).append("[").append(oid).append("]");
				//				print(oid);
			}
		}
		bf.append("<--");
	}
	//	println(":("+q.size()+")");
	return bf.toString();
}

public void shortestPaths()
{
	Graph g = DexUtil.getDBGraph();
	Queue<Pair<Long, Integer>> q = new LinkedList<>();
	Objects visited = new Objects(g.getSession());
	Objects finalNodes = new Objects(g.getSession());
	Map<Long, LinkedList<Long>> achievableFrom = new HashMap();
	q.add(new Pair<Long, Integer>(start, 0));
	achievableFrom.put(start, null);
	boolean stop = false;
	int maxLevel = -1;

	//println("Start node: \"" + (new Article(start)).getTitle() + "\": " + start + " is able to achieve:");
	while (!q.isEmpty() && !stop)
	{
		Pair<Long, Integer> node = q.poll();
		if (maxLevel > 0 && node.getSecond() > maxLevel)
		{
			stop = true;
		}
		if (!stop)
		{
			long nodeId = node.getFirst();
			int nodeLevel = node.getSecond();
			if (endNodes.contains(nodeId) && nodeId != start && g.getType(nodeId) != g.findType("Category") && !isFreeEdge(start, nodeId))
			{
				//println("		"+node.id+": \""+(new Article(node.id)).getTitle()+" is a final node");
				maxLevel = nodeLevel;
				finalNodes.add(nodeId);

			} else
			{
				Objects neighbors = getNeighbors(nodeId);
				Objects.Iterator it = neighbors.iterator();
				while (it.hasNext())
				{
					long futureNode = it.next();
					if (!visited.contains(futureNode))
					{
						if (isFreeEdge(nodeId, futureNode))
						{
							q.add(new Pair<Long, Integer>(futureNode, nodeLevel));
						} else
						{
							q.add(new Pair<Long, Integer>(futureNode, nodeLevel + 1));
						}
						LinkedList<Long> tmp = achievableFrom.get(futureNode);
						if (tmp != null)
						{
							tmp.add(nodeId);
						} else
						{
							tmp = new LinkedList<Long>();
							tmp.add(nodeId);
						}
						achievableFrom.put(futureNode, tmp);
						//cal marcar-se des d'on he arribat.
					}
				}
				visited.add(nodeId);
				neighbors.close();
			}
		}
	}
	//println();
	if (maxLevel != -1)
	{
		for (long oid : finalNodes)
		{
			Objects v = new Objects(g.getSession());
			constructPath(oid, achievableFrom, new LinkedList<Long>(), v);
			v.close();

		}
	}
	finalNodes.close();
	visited.close();
}

private void constructPath(long oid, Map<Long, LinkedList<Long>> achievableFrom, LinkedList<Long> res, Objects visited)
{
	res.add(oid);
	visited.add(oid);
	LinkedList<Long> tmp = achievableFrom.get(oid);
	if (tmp != null)
	{
		for (long next : tmp)
		{
			if (!visited.contains(next))
			{
				constructPath(next, achievableFrom, res, visited);
				if (res.getLast().longValue() == start.longValue())
				{
					increasePT(res);
				}
			}
		}
	}

}

protected void increasePT(LinkedList<Long> res)
{
	if (!contains(paths, new LinkedList<Long>(res)))
	{
		pathsList.add(new HashSet<Long>(res));
		//println("SUM:"+pathsList);
		//println("R:"+res);
//				printPath(res);
		numberOfPaths++;
		for (long oid : res)
		{
			touched.add(oid);
			increasePT(oid);
		}
		paths.add(res);
	}
}

private void resetCounters()
{
	for (long oid : touched)
	{
		setPT(oid, new Value(0.0));
	}
}

protected boolean contains(HashSet<LinkedList<Long>> paths, LinkedList<Long> res)
{
	for (LinkedList<Long> l : paths)
	{
		if (l.size() == res.size())
		{
			if (equals(l, res))
			{
				return true;
			}
		}
	}
	return false;
}

private boolean equals(LinkedList<Long> l, LinkedList<Long> l2)
{
	boolean ret = true;
	for (int i = 0; i < l.size(); i++)
	{
		if (l.get(i) != l2.get(i))
		{
			return false;
		}
	}
	return ret;
}

public Set<Set<Long>> getPaths()
{
	println("doing paths");
	doTraversal();
	//println("paths done: " + pathsList.size() + " found.");

	return pathsList;
}

private boolean isFreeEdge(long id, long futureNode)
{
	Graph g = DexUtil.getDBGraph();
	Boolean res = null;
	if (freeEdge == Graph.INVALID_TYPE)
	{
		return false;
	}
	Objects neighbors = g.neighbors(id, freeEdge, Graph.EDGES_BOTH);
	if (neighbors.contains(futureNode))
	{
		res = true;
	} else
	{
		res = false;
	}
	neighbors.close();
	return res;
}

private static void println(Object string)
{
	System.out.println("	[MimumPaths.java]: " + string);
}

private static void println()
{
	System.out.println("	[MimumPaths.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[MimumPaths.java]: " + string);
}

private static void print()
{
	System.out.print("	[MimumPaths.java]: ");
}

}
