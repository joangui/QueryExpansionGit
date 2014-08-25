/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expansionBlocks;

import DEFS.Definitions;
import Structures.Pair;
import configurations.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import model.Article;
import model.Entity;
import model.Query;
import model.Token;
import utils.TimeCalculator;

/**
 *
 * @author joan
 */
public class IdentifyEntities
{

static public Pair<Set<Entity>, Set<Entity>> execute(Configuration configuration, Query query) throws Exception
{
	List<String> easyExpandedInput = query.getEasyExpandedInput();
	Set<Entity> inputEntities = findEntities(configuration, query.getInput(), easyExpandedInput);

	Set<Entity> contextEntities = findEntities(configuration, query.getContext(), new ArrayList<String>());
	println("Entities of query " + query.getId() + ": " + inputEntities + "||" + contextEntities);

	query.setInputEntities(inputEntities);
	query.setContextEntities(contextEntities);
	return new Pair(inputEntities, contextEntities);
}

public static TimeCalculator execute(Configuration configuration, SortedSet<Query> queries) throws Exception
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

@Deprecated
static private Set<Entity> findEntities(Configuration configuration, Query query) throws Exception
{

	List<String> easyExpandedInput = query.getEasyExpandedInput();
	return findEntities(configuration, query.getInput(), easyExpandedInput);
}

static private Set<Entity> findEntities(Configuration configuration, String input, Map<Integer, Pair<Integer, Set<Entity>>> entitiesMap, int minimumWordsForEntity) throws Exception
{
	//println("Finding entities in : \"" + input + "\". minimum lenghts threshold = "+minimumWordsForEntity);
	Set<Entity> entities = new HashSet<>();
	//println("Find entities in: \""+input+"\"");
	String[] inputSeparated = input.split("\\ ");
	for (int i = 0; i < inputSeparated.length; i++)
	{
		StringBuilder base = new StringBuilder();
		String possibleEntity = "";
		for (int j = i; j < inputSeparated.length && possibleEntity.length()<2048; j++)
		{
			possibleEntity = base.append(inputSeparated[j]).append(" ").toString().trim();
			while (possibleEntity != null && !"".equals(possibleEntity) && (possibleEntity.charAt(possibleEntity.length() - 1) == '.' || possibleEntity.charAt(possibleEntity.length() - 1) == ','))
			{
				possibleEntity = possibleEntity.substring(0, possibleEntity.length() - 1);
			}

			//println("possibleEntity 1: \"" + possibleEntity+"\"");
			
			if (!Token.isStopWord(configuration, possibleEntity) && possibleEntity.split("\\ ").length >= minimumWordsForEntity && Article.isEntity(possibleEntity))
			{
				Entity entity = new Entity(possibleEntity);
			//	println("	Entity : " + entity);

				//for (int k = i; k < i + possibleEntity.split("\\ ").length && !entities.contains(entity); k++)
				for (int k = i; k < i + possibleEntity.split("\\ ").length; k++)
				{
					//entities.add(entity);
//					println("entity= "+ entity+ ", i = "+i +". Position "+k);
					Pair<Integer, Set<Entity>> positionUsage = entitiesMap.get(k);
					Integer length = positionUsage.getFirst();

					Set<Entity> newList = positionUsage.getSecond();
					if (newList == null || possibleEntity.split("\\ ").length > length)
					{
						length = possibleEntity.split("\\ ").length;
						newList = new HashSet<>();
					} else if (possibleEntity.split("\\ ").length == length)
					{
						newList = positionUsage.getSecond();
					}

					if (possibleEntity.split("\\ ").length >= length)
					{
//						println("Entity idenfier for \"" + possibleEntity + "\"=" + entity);
						newList.add(entity);
						Pair<Integer, Set<Entity>> newPositionUsage = new Pair<>(possibleEntity.split("\\ ").length, newList);
						int r = k;
						//for (int r = k; r < k + possibleEntity.split("\\ ").length; r++)
						{
							entitiesMap.put(r, newPositionUsage);
						}
					}
				}
			}
		}

	}
	return entities;

}

@Deprecated
static private Set<String> findEntitiesOld(Configuration configuration, String input, Map<Integer, Pair<Integer, List<String>>> entitiesMap) throws Exception
{
	LinkedList<String> tokensList = calculateTokensList(input);
	Set<String> entities = new HashSet<>();
	//println("Find entities in: \""+input+"\"");
	String[] inputSeparated = input.split("\\ ");
	for (int i = 0; i < inputSeparated.length; i++)
	{
		String entity = "";
		StringBuilder base = new StringBuilder();
		String possibleEntity = "";
		for (int j = i; j < inputSeparated.length; j++)
		{

			possibleEntity = base.append(inputSeparated[j]).append(" ").toString().trim();
			if (!Token.isStopWord(configuration, possibleEntity) && Article.isEntity(possibleEntity))
			{
				entity = Article.getEntity(possibleEntity);
				int offset = 0;
				entities.add(entity);
				for (int k = i; k < i + possibleEntity.split("\\ ").length; k++)
				{
					println("entity= " + entity + ", i = " + i + "Position " + k);
					Pair<Integer, List<String>> positionUsage = entitiesMap.get(k);
					Integer length = positionUsage.getFirst();

					List<String> newList = positionUsage.getSecond();
					if (newList == null || possibleEntity.split("\\ ").length > length)
					{
						length = entity.split("\\ ").length;
						newList = new ArrayList<>();
					} else if (entity.split("\\ ").length == length)
					{
						newList = positionUsage.getSecond();
					}

					if (entity.split("\\ ").length >= length)
					{
						newList.add(entity);
						Pair<Integer, List<String>> newPositionUsage = new Pair<>(entity.split("\\ ").length, newList);
						entitiesMap.put(k, newPositionUsage);
					}

				}
			}
			entity = "";
		}

	}
	return entities;

}

private static void println(Object string)
{
	System.out.println("	[IdentifyEntities.java]: " + string);
}

static private LinkedList<String> calculateTokensList(String input) throws Exception
{
	//Set<Token> tokens = Token.tokenize(input, configuration.getLanguage(), true);
	LinkedList tokensList = new LinkedList();
	String[] split = input.split("\\ ");
	for (String s : split)
	{
		Token t = new Token(s);
		if (t.getId() != null)
			tokensList.addLast(t.getName());
	}

//	println("Tokens List: " + tokensList);
	return tokensList;
}

static private Map<Integer, Pair<Integer, Set<Entity>>> inicializeWordsMap(String input) throws Exception
{

	LinkedList<String> calculateTokensList = new LinkedList<>(Arrays.asList(input.split("\\ ")));
	int i = 0;

	Map<Integer, Pair<Integer, Set<Entity>>> entitiesMap = new HashMap<>();

	for (String s : calculateTokensList)
	{
		entitiesMap.put(i, new Pair<Integer, Set<Entity>>());
		i++;
	}
	return entitiesMap;
}

static private Set<Entity> findEntities(Configuration configuration, String input, List<String> easyExpandedInput) throws Exception
{
	Set<Entity> entities = new HashSet<>();
	Map<Integer, Pair<Integer, Set<Entity>>> wordsMap = inicializeWordsMap(input);

	easyExpandedInput.remove(input.toLowerCase());

	Set<Entity> findEntities = findEntities(configuration, input.toLowerCase(), wordsMap, 1);
	entities.addAll(findEntities);
	for (String inputSynonym : easyExpandedInput)
	{
		entities.addAll(findEntities(configuration, inputSynonym, wordsMap, 2));
	}
	entities = new HashSet<>();
	Collection<Pair<Integer, Set<Entity>>> values = wordsMap.values();
	for (Pair<Integer, Set<Entity>> p : values)
	{
		Set<Entity> entitiesList = p.getSecond();
		if (entitiesList != null)
			entities.addAll(entitiesList);
	}
	return entities;

}

static public Set<Entity> findEntities(Configuration configuration, String input) throws Exception
{
	return findEntities(configuration, input,   new ArrayList<String>());
}

public static void getSynonyms(Configuration configuration, Query query)
{
	Set<Entity> inputEntities = query.getInputEntities();
	Map<String, Set<String>> entitySynonyms = new HashMap<>();
	for (Entity entity : inputEntities)
	{
		println("Synonyms for entity: \"" + entity + "\" in query " + query.getId() + ":" + entity.getEntityNames());
	}
}

public static void getSynonyms(Configuration configuration, SortedSet<Query> queries)
{
	for (Query query : queries)
	{
		getSynonyms(configuration, query);
	}
}
}
