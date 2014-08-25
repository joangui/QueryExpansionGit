/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;
import java.util.Collection;
import configurations.Configuration;

/**
 *
 * @author joan
 */
public class Modularity extends AbstractCommunityMethod
{

    public Modularity(Configuration config)
    {
        super(config);
    }

    @Override
    protected double calculateMetric(Collection<Long> community)
    {

        
        double modularity = 0.0;
        Objects communityObjects = new Objects(graph.getSession());
        for (Long id : community)
        {
            communityObjects.add(id);
        }


        //Calc number of edges of type edgeType
        Objects tmp = graph.select(edgeType);
        int numEdges = tmp.size();
        tmp.close();




        //Calc total degree of the community
        int totalDegree = 0;
        int internalDegree = 0;
        for (long id : community)
        {
            tmp = graph.neighbors(id, edgeType, Graph.EDGES_BOTH);
            totalDegree += tmp.size();

            tmp.intersection(communityObjects);
            internalDegree += tmp.size();

            tmp.close();
        }

        double edgesFactor = 4.0 * numEdges * numEdges;
        for (long id : community)
        {
            long idDegree = calculateDegree(id, edgeType, Graph.EDGES_BOTH);
            modularity -= idDegree * (totalDegree - idDegree) / edgesFactor;
        }
        modularity += internalDegree / (2.0 * numEdges);
        communityObjects.close();


        return modularity;
    }
}
