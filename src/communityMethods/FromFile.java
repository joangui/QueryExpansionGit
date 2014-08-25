/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import configurations.Configuration;
import edu.upc.dama.dex.core.DbGraph;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Entity;
import model.Paths;
import model.Query;

/**
 *
 * @author joan
 */
public class FromFile extends AbstractCommunityMethod
{

private OfflineCommunities ofc;

public FromFile(Configuration config) throws IOException
{
	super(config);
	ofc = loadCommunities(file);
}

@Override
public Map<Set<Long>, Map<Entity, Double>> execute(Query query)
{
	Paths paths = query.getSelectedPaths();
	try
	{
		Map<Set<Long>, Map<Entity, Double>> communities = new HashMap<>();

		for (int i = 0; i < paths.size(); i++)
		{

			Set<Long> path = paths.get(i);
			Map<Entity, Double> community = executeMethod(path);
			communities.put(path, community);

		}

		return communities;
	} catch (Exception ex)
	{
		Logger.getLogger(FromFile.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
}

@Override
Map<Entity, Double> executeMethod(Set<Long> path)
{
	Set<Long> communityIds = new HashSet<>();

	for (Long id : path)
	{
		Set<Set<Long>> community1 = ofc.getCommunity(id);
		for (Set<Long> c : community1)
		{
			communityIds.addAll(c);
		}

	}
	Double weight = 1.0 / communityIds.size();
	Map<Entity, Double> community = new HashMap<>();
	for (Long id : communityIds)
	{
		community.put(new Entity(id), weight);
	}
	return community;

}

static private OfflineCommunities loadCommunities(String file) throws FileNotFoundException, IOException
{
	OfflineCommunities ofc = new OfflineCommunities();
	BufferedReader reader = FileManagement.ReadFiles.getReader(file);
	String line;
	int communityID = 0;
	Map<Long, List<Integer>> idToCommunity = ofc.getIdToCommunity();

	while ((line = reader.readLine()) != null)
	{
		if (line.charAt(0) != '#')
		{
			Scanner s = new Scanner(line);

			/*
			 String[] communityIDs_strings = line.split("\\ ");
			 Set<Long> communityIDs = new HashSet<Long>();
			 for (String s : communityIDs_strings)
			 {
			 long id = Long.valueOf(s);
			 List<Integer> idToCommunities = idToCommunity.get(id);
			 if (idToCommunities == null)
			 {
			 idToCommunities = new ArrayList<>();
			 }
			 idToCommunities.add(communityID);
			 idToCommunity.put(id, idToCommunities);
			 communityIDs.add(id);
			 }*/
			Long id;
			Set<Long> communityIDs = new HashSet<Long>();
			while (s.hasNextLong())
			{
				id = s.nextLong();
				List<Integer> idToCommunities = idToCommunity.get(id);
				if (idToCommunities == null)
				{
					idToCommunities = new ArrayList<>();
				}
				idToCommunities.add(communityID);
				idToCommunity.put(id, idToCommunities);
				communityIDs.add(id);

			}

			ofc.setCommunity(communityID, communityIDs);

			communityID++;
		}
	}

	return ofc;
}

@Override
protected double calculateMetric(Collection<Long> community)
{
	throw new UnsupportedOperationException("Not supported yet.");
}

private static class OfflineCommunities
{

Map<Long, List<Integer>> idToCommunity;
Map<Integer, Set<Long>> communities;

public OfflineCommunities()
{
	idToCommunity = new HashMap<>();
	communities = new HashMap<>();
}

public void setCommunity(Integer idCommunity, Set<Long> community)
{
	communities.put(idCommunity, community);
}

public Map<Long, List<Integer>> getIdToCommunity()
{
	return idToCommunity;
}

private Set<Set<Long>> getCommunity(Long i)
{
	Set<Set<Long>> result = new HashSet<>();

	List<Integer> communitiesID = idToCommunity.get(i);
	if (communitiesID != null)
	{
		for (int communityID : communitiesID)
		{
			result.add(communities.get(communityID));
		}
	}
	return result;
}
}

public static void main(String[] args) throws FileNotFoundException, IOException
{
	String files = "/home/joan/DAMA/Recercaixa/communities.txt";

	OfflineCommunities ofc = loadCommunities(files);

	System.out.println(ofc.getCommunity(17L));
	System.out.println(ofc.getCommunity(22L));
	System.out.println(ofc.getCommunity(111L));

	System.out.println("Done.");

}
}
