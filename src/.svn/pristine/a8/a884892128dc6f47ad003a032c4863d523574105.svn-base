/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import configurations.Configuration;
import expansionBlocks.IdentifyEntities;
import expansionBlocks.QueryPreProcess;
import java.util.SortedSet;
import java.util.TreeSet;
import model.Query;

/**
 *
 * @author joan
 */
class AdintonPipeline
{

static void execute(Configuration configuration) throws Exception
{
	

	SortedSet<Query> queries = new TreeSet<>();

	queries = new TreeSet(Query.readQueries(configuration));
	
	QueryPreProcess.execute(configuration, queries);
	
	IdentifyEntities.execute(configuration, queries);

	for(Query query : queries)
	{
		println(query.getId()+" - "+query.getInput()+" - "+query.getContext());
		println("	Input Entities: "+query.getInputEntities());
		println("	Context Entities: "+ query.getContextEntities());
		println("");
	}
	/*

	
	MultipleEntityQueryExpansion.execute(configuration, queries);

	CreatePaths.execute(configuration, queries);
	SelectPaths.execute(configuration, queries);
	CreateCommunitiesFromPaths.execute(configuration, queries);
	ProcessCommunities.execute(configuration, queries);

	
	println("===================================================SUMMARY==================================================");
	for (Query q : queries)
	{
		println("============================================================================================================");
		println("Query \"" + q.getInput() + "\"");
		StringBuilder sb = new StringBuilder();
		println("	Method: Entity and synonyms");
		Set<Entity> inputEntities = q.getInputEntities();
		for(Entity entity : inputEntities)
		{
			Set<String> value = entity.getEntityNames();
			Set<String> entitySynonymsSet = new HashSet<>();
			for (String synonyms : value)
			{
				entitySynonymsSet.add(StringUtils.removeInBrackets(synonyms).trim());
			}

			boolean firstEntity = true;
			sb.append("			").append(entity).append("= ");
			for (String synonyms : entitySynonymsSet)
			{
				if (!firstEntity)
					sb.append(", ");
				sb.append("\"").append(synonyms).append("\"");
				firstEntity = false;
			}
			println(sb);
			sb = new StringBuilder();
		}
		Map<Entity, Map<Entity, Double>> entityCommunities = q.getEntityCommunities();
		println("	Method: Entity and communities");
		for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
		{
			Entity entity = e.getKey();
			Map<Entity, Double> articlesFromSet = e.getValue();
			articlesFromSet = MapUtils.sortByValue(articlesFromSet);

			sb = new StringBuilder("			" + entity + "= ");
			boolean firstTitle = true;
			Set<String> entities = new LinkedHashSet<>();
			for (Map.Entry<Entity, Double> e2 : articlesFromSet.entrySet())
			{
				entities.add(StringUtils.removeInBrackets(e2.getKey().getName()).trim());
			}
			for (String entityName : entities)
			{
				if (!firstTitle)
					sb.append(", ");
				firstTitle = false;
				sb.append("\"").append(entityName).append("\"");
			}
			sb.append("");
			println(sb);
		}

		Map<Entity, Double> community = q.getCommunity();
		Map<Entity, Double> articlesFromSet =community;
		println("	Method: Traditional Query Expansion");
		sb = new StringBuilder("			" + q.getInput() + "= ");
		boolean firstTitle = true;
		
		Set<String> entities = new LinkedHashSet<>();
		for (Map.Entry<Entity, Double> e2 : articlesFromSet.entrySet())
		{
			entities.add(StringUtils.removeInBrackets(e2.getKey().getName()));
		}
		for (String entityName : entities)
		{
			if (!firstTitle)
				sb.append(", ");
			firstTitle = false;
			sb.append("\"").append(entityName).append("\"");
		}
		println(sb);

		String lexicalIndriQuery = q.getLexicalIndriQuery();
		List<String> extractInBrackets = StringUtils.extractInBrackets(lexicalIndriQuery);
		sb = new StringBuilder("			" + q.getInput() + "= ");
		firstTitle = true;
		if (!extractInBrackets.isEmpty())
		{
			for (String entityName : entities)
			{
				if (!firstTitle)
					sb.append(", ");
				firstTitle = false;
				sb.append("\"").append(entityName).append("\"");
			}
			println(sb);
		}
	}*/
}

private static void println(Object string)
{
	System.out.println("	[AdintonPipeline.java]: " + string);
}

}
