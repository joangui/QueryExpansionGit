/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import edu.upc.dama.dex.core.DbGraph;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;
import java.util.Collection;
import configurations.Configuration;

/**
 *
 * @author joan
 */
public class Conductance extends AbstractCommunityMethod
{

    public Conductance(Configuration config)
    {
        super(config);
    }

    @Override
	protected double calculateMetric(Collection<Long> community)
    {
        double conductance = 0.0;
        Objects communityObjects = new Objects(graph.getSession());
        for (Long id : community)
        {
            communityObjects.add(id);
        }
        Objects neighbors = graph.neighbors(communityObjects, edgeType, Graph.EDGES_BOTH);

        long externalEdges = neighbors.difference(communityObjects);
        
        
        neighbors.close();
        communityObjects.close();

        conductance = (double) externalEdges / totalNumberOfNodes;

        return 1 / conductance;
    }

	
}
