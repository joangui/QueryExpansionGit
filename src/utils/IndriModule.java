package utils;

import DEFS.Definitions.LANGUAGE;
import Sets.SetUtils;
import Parameters.Parameters;
import Strings.StringUtils;
import Strings.Tokenize.Tokenizer;
import configurations.Configuration;
import configurations.Configuration.TEST;
import static configurations.Configuration.TEST.WEBCLEF;
import edu.upc.dama.utils.objects.UtilsMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lemurproject.indri.*;
import model.Article;
import model.Entity;
import model.Query;
import model.Token;
import model.Video;
import org.apache.commons.codec.binary.Base64;

public class IndriModule
{

static
{
	//System.loadLibrary("libindri_jni");
}

private static Double getFinalWeight(Double entityWeight, Double oldEntityWeight)
{

	Double finalWeight = null;
	switch (QUERY_TERM_WEIGHT_AGREGATION.SUM_WEIGHT)
	{
		case SUM_WEIGHT:
			finalWeight = entityWeight + oldEntityWeight;
			break;
		case MAX_WEIGHT:
			finalWeight = entityWeight > oldEntityWeight ? entityWeight : oldEntityWeight;
			break;
		case AVG_WEIGHT:
			finalWeight = (entityWeight + oldEntityWeight) / 2;
			break;
		case NEW_WEIGHT:
			finalWeight = entityWeight;
			break;
		case OLD_WEIGHT:
			finalWeight = oldEntityWeight;
			break;
	}
	return finalWeight;
}

private static Map<String, Double> constructQueryExpansion(Map<String, Double> tmp, Map<String, Double> mapTokensInput)
{
	Map<String, Double> tmp2 = new HashMap<String, Double>();
	Double module = 0.0;
	for (Map.Entry<String, Double> e : tmp.entrySet())
	{
		module += ((e.getValue() * e.getValue()));
	}
	module = Math.sqrt(module);

	for (Map.Entry<String, Double> e : tmp.entrySet())
	{
		tmp2.put(e.getKey(), e.getValue() / module);
	}
	for (Map.Entry<String, Double> e : mapTokensInput.entrySet())
	{
		Double oldWeight = tmp2.put(e.getKey(), e.getValue());
		if (oldWeight != null)
		{
			tmp2.put(e.getKey(), getFinalWeight(e.getValue(), oldWeight));
		}
	}

	module = 0.0;
	for (Map.Entry<String, Double> e : tmp2.entrySet())
	{
		module += ((e.getValue() * e.getValue()));
	}
	module = Math.sqrt(module);

	Map<String, Double> tmp3 = new HashMap<String, Double>();
	for (Map.Entry<String, Double> e : tmp2.entrySet())
	{
		tmp3.put(e.getKey(), e.getValue() / module);
	}
	return tmp3;

}

public static boolean hasResults(Configuration configuration, String myQuery) throws Exception
{

	String myIndex = configuration.getResultCollectionPath();

	QueryEnvironment env = new QueryEnvironment();

	ScoredExtentResult[] results;

	env.addIndex(myIndex);

	results = env.runQuery(myQuery, 1);
	ParsedDocument[] documents = env.documents(results);
	//println("                                            #results:"+results.length);
	boolean result = (documents.length > 0);
	env.close();
	//println("\"" + myQuery + "\" has results: " + result);
	return result;
}

private static String getTermBasedBase64(String entity)
{
	//println("2: "+entity);

	StringBuilder base64Sequence = new StringBuilder();
	String[] entityTokens = entity.split(" ");
	/* // si es vol un  windowsSize dinàmic en funció de la mida
	 * // de la query cal desactivar la següent linea:
	 */

	for (String s : entityTokens)
	{
		if (!s.isEmpty())
		{
			base64Sequence.append("#base64(").append(base64_encode(s)).append(")");
		}
	}
	String entity64 = "(" + base64Sequence.toString() + ")";

	return entity64;
}

private static Map createFeedbackMap()
{
	Map p = new HashMap();
	p.put("fbDocs", "10");
	p.put("fbTerms", "10");
	p.put("fbMu", "0.5");
	p.put("fbOrigWeight", "0.7");
	return p;
}

private static String buildIndriQuery(Configuration configuration, Query query) throws FileNotFoundException, UnsupportedEncodingException, Exception
{
	String originalIndriQuery = IndriModule.getCombineBase64Query(query.getInput());
	String lexicalIndriQuery = query.getLexicalIndriQuery();
	println("lexicalIndriQuery:" + lexicalIndriQuery);
	String topologicalIndriQuery = null;// = generateTopologicalQueryForQueryExpansionPipeline(configuration, query);

	switch (configuration.getPipeline())
	{
		case QUERY_EXPANSION:
			topologicalIndriQuery = buildTopologicalQueryForQueryExpansionPipeline(configuration, query);
			break;
		case MULTIPLE_ENTITY_QUERY_EXPANSION:
			topologicalIndriQuery = buildTopologicalQueryForMultipleEntityQueryExpansionPipeline(configuration, query);
			break;
	}

	query.setOriginalIndriQuery(originalIndriQuery);
	query.setTopologicalIndriQuery(topologicalIndriQuery);

	String expansionIndriQuery;
	if (lexicalIndriQuery != null && !lexicalIndriQuery.isEmpty())
	{

		expansionIndriQuery = "#weight(ORIGINAL_WEIGHT" + originalIndriQuery + " GLOBAL_SIMPLE_EASY_EXTENSION#weight(" + lexicalIndriQuery + ") EXPANSION_WEIGHT" + topologicalIndriQuery + ")";
	} else
	{
		expansionIndriQuery = "#weight(ORIGINAL_WEIGHT" + originalIndriQuery + " EXPANSION_WEIGHT" + topologicalIndriQuery + ")";
	}

	PrintWriter writerQoQlQt_file = configuration.getWriterExpansionQuery();
	writerQoQlQt_file.write(expansionIndriQuery + "\n");
	println("ConfigurableLine: " + expansionIndriQuery);
	query.setConfigurableIndriQueryLine(expansionIndriQuery);

	return setWeightsExpansionIndriQuery(configuration, expansionIndriQuery);
}

public static String setWeightsExpansionIndriQuery(Configuration configuration, String query)
{
	Parameters parameters = configuration.getParameters();

	Double originalWeight = parameters.readDouble(0.0, "WEIGHT_ORIGINAL_QUERY");
	Double globalSimpleEasyExtension = parameters.readDouble(0.0, "GLOBAL_SIMPLE_EASY_EXTENSION");
	String ow = String.valueOf(originalWeight);
	Double expansionWeight;
	Double expansionWeightNoOsee;
	if (parameters.readDouble(-1.0, "TOPOLOGICAL_QUERY") != 0.0)
	{
		expansionWeight = 1 - originalWeight - globalSimpleEasyExtension;
		expansionWeightNoOsee = 1 - originalWeight;
	} else
	{
		expansionWeight = 0.0;
		expansionWeightNoOsee = 0.0;
	}
	String ew = String.valueOf(expansionWeight);
	String ewNoOsee = String.valueOf(expansionWeightNoOsee);
	String osee = String.valueOf(globalSimpleEasyExtension);

	query = query.replace("ORIGINAL_WEIGHT", ow);
	if (query.contains("GLOBAL_SIMPLE_EASY_EXTENSION"))
	{
		query = query.replace("EXPANSION_WEIGHT", ew);
		query = query.replace("GLOBAL_SIMPLE_EASY_EXTENSION", osee);
	} else
	{
		query = query.replace("EXPANSION_WEIGHT", ewNoOsee);
	}

	int countMatches = StringUtils.countMatches(query, "SIMPLE_EASY_EXTENSION");
	Double simpleEasyExtensionWeight = 1.0 / countMatches;
	NumberFormat f = new DecimalFormat("###.#####");

//            if(simpleEasyExtensionWeight<0.001)simpleEasyExtensionWeight=0.0;
	String seew = f.format(simpleEasyExtensionWeight);
	query = query.replace("SIMPLE_EASY_EXTENSION", seew);

	return query;
}

private static void println(Object string)
{
	System.out.println("	[IndriModule.java]: " + string);
}

private static void println()
{
	System.out.println("	[IndriModule.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[IndriModule.java]: " + string);
}

private static void print()
{
	System.out.print("	[IndriModule.java]: ");
}

private static String generateTopologicalQueryMultipleEntityQueryExpansion2(Configuration configuration, Query query) throws Exception
{

	println("Generating topological query for multiple entity query expansion.");

	Map<Entity, Map<Entity, Double>> entityCommunities = query.getEntityCommunities();
	LinkedHashSet<String> topologicalQueryBased64Components = new LinkedHashSet<>();
	LinkedHashSet<String> topologicalQueryPlainComponents = new LinkedHashSet<>();
	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{

		Entity entity = e.getKey();
		if (entity.isAmbiguous())
		{
			entity.desambiguation(e.getValue().keySet());
		}
		String entityName = entity.getName();
		Map<Entity, Double> community = e.getValue();
		query.setCommunity(community);
		Set<Token> inputTokens = query.getInputTokens();
		Token token = Token.Token(entityName, false);
		Set<Token> inputTokensTmp = new HashSet<>();
		inputTokensTmp.add(token);
		query.setInputTokens(inputTokensTmp);
		String topologicalQueryFromEntity = generateEntityBasedBase64TopologicalQuery(configuration, query, entity);
		String topologicalPlainQueryFromEntity = generateEntityBasedPlainTopologicalQuery(configuration, query, entity);

		query.setInputTokens(inputTokens);
		topologicalQueryBased64Components.add(topologicalQueryFromEntity);
		topologicalQueryPlainComponents.add(topologicalPlainQueryFromEntity);

	}
	//println("Plain Query: " + topologicalQueryPlainComponents);

	if (topologicalQueryBased64Components.size() == 1)
	{
		for (String s : topologicalQueryPlainComponents)
		{
			println("Indri Plain Query: " + s);
		}
		for (String s : topologicalQueryBased64Components)
		{
			return s;
		}
	}
	if (!topologicalQueryBased64Components.isEmpty())
	{
		StringBuilder topologialQueryBased64 = new StringBuilder();
		StringBuilder topologialQueryPlain = new StringBuilder();

		topologialQueryPlain.append("#combine(");
		for (String s : topologicalQueryPlainComponents)
		{
			topologialQueryPlain.append(" " + s + " ");
		}

		topologialQueryPlain.append(")");

		println("Indri Plain Query: " + topologialQueryPlain);

		topologialQueryBased64.append("#combine(");
		for (String s : topologicalQueryBased64Components)
		{
			topologialQueryBased64.append(" " + s + " ");
		}
		topologialQueryBased64.append(")");
		query.setPlainTopologicalQuery(topologialQueryPlain);
		return topologialQueryBased64.toString();
	} else
	{
		Set<Entity> inputEntities = query.getInputEntities();
		if (!inputEntities.isEmpty())
		{
			StringBuilder topologicalQueryBased64 = new StringBuilder();
			topologicalQueryBased64.append("#combine(");
			for (Entity entity : inputEntities)
			{
				topologicalQueryBased64.append(getEntityBased64Query(entity));
			}
			topologicalQueryBased64.append(")");

			return topologicalQueryBased64.toString();
		} else
		{
			Set<Token> inputTokens = query.getInputTokens();
			StringBuilder sb = new StringBuilder();
			sb.append("#combine(");
			for (Token token : inputTokens)
			{
				sb.append("#1(").append("#base64(").append(base64_encode(token.getName())).append("))");
			}
			sb.append(")");
			return sb.toString();
		}
	}
}

public static String getEntityBased64Query(Entity entity)
{
	Set<String> otherNames = entity.getEntityNames();

	String userEntityName = entity.getUserName();
	userEntityName = StringUtils.removeInBrackets(userEntityName).trim();
	otherNames.remove(entity.getUserName());

	StringBuilder external = new StringBuilder();

	StringBuilder internal = new StringBuilder();
	if (!"".equals(userEntityName))
		internal.append("1.0 #1" + "").append(getTermBasedBase64(userEntityName));

	//sb = !"".equals(userEntityName) ? new StringBuilder("#wsyn(1.0 #1" + "" + getTermBasedBase64(userEntityName) + "") : new StringBuilder("#wsyn(");
//	sb = !"".equals(userEntityName) ? new StringBuilder("") : new StringBuilder("#wsyn(");
	Set<String> entities = new HashSet<>();
	entities.add(userEntityName);

	String entityName = entity.getName();
	if (!entityName.equals(userEntityName))
	{
		entityName = StringUtils.removeInBrackets(entityName).trim();
		if (!entityName.equals(""))
		{
			internal.append("1.0 #uw1").append(getTermBasedBase64(entityName)).append(" ");
		}
		entities.add(entityName);
		otherNames.remove(entity.getName());
	}

	for (String entitySynonym : otherNames)
	{
		entitySynonym = StringUtils.removeInBrackets(entitySynonym).trim();
		if (!entities.contains(entitySynonym) && !"".equals(entitySynonym))
		{
			internal.append("0.1 #uw1").append(getTermBasedBase64(entitySynonym)).append(" ");
		}
	}
	if (!"".equals(internal.toString()))
	{
		external = new StringBuilder("#wsyn(" + internal + ")");
	}

	otherNames.add(entity.getUserName());
	otherNames.add(entity.getName());
	return external.toString();
}

public static String getEntityPlainQuery(Entity entity)
{
	Set<String> otherNames = entity.getEntityNames();
	String userEntityName = entity.getUserName();
	otherNames.remove(userEntityName);
	userEntityName = StringUtils.removeInBrackets(entity.getUserName()).trim();
	StringBuilder internal = new StringBuilder();
	StringBuilder external = new StringBuilder();

	if (!"".equals(userEntityName))
		internal.append("1.0 #1" + "").append("(").append(userEntityName).append(")");

	Set<String> entities = new HashSet<>();
	entities.add(userEntityName);

	String entityName = entity.getName();
	if (!entityName.equals(userEntityName))
	{
		entityName = StringUtils.removeInBrackets(entityName).trim();
		if (!entityName.equals(""))
			internal.append("1.0 #uw1(").append(entityName).append(") ");
		entities.add(entityName);
		otherNames.remove(entity.getName());
	}

	for (String entitySynonym : otherNames)
	{
		entitySynonym = StringUtils.removeInBrackets(entitySynonym).trim();
		if (!entities.contains(entitySynonym) && !"".equals(entitySynonym))
		{
			internal.append("0.1 #uw(").append((entitySynonym)).append(") ");
		}
	}

	if (!"".equals(internal.toString()))
	{
		external = new StringBuilder("#wsyn(" + internal + ")");
	}
	return external.toString();
}

private static String buildTopologicalQueryForMultipleEntityQueryExpansionPipeline(Configuration configuration, Query query) throws Exception
{
	switch (configuration.getTopologicalQueryForm())
	{
		case PLAIN:
			println("Building Plain Topological Query");
			return plainForm(configuration, query);
		case INTERSECTION:
			println("Building Intersection Topological Query");
			return intersectionForm(configuration, query);
	}
	return null;
}

private static String intersectionForm(Configuration configuration, Query query)
{
	StringBuilder sb = new StringBuilder();
	StringBuilder sb_plain = new StringBuilder();

	if (configuration.removeEntitiesBasedOnCategories())
		println("Using category filtered communities to create the topologica query");

	Map<Entity, Map<Entity, Double>> entityCommunities = configuration.removeEntitiesBasedOnCategories() ? query.getEntityCommunitiesCategoryFiltered() : query.getEntityCommunities();
	Set<Entity> queryEntities = entityCommunities.keySet();

	if (entityCommunities.isEmpty())
		return getCombineBase64Query(query.getInput());

	sb.append("#weight(");
	sb_plain.append("#weight(");
	println("Query Entities: " + queryEntities);
	for (Entity entity : queryEntities)
	{
		String entityQuery = getEntityBased64Query(entity);
		String plainEntityQuery = getEntityPlainQuery(entity);
		sb.append(1.0).append(entityQuery);
		sb_plain.append(1.0).append(plainEntityQuery);
		if (entityCommunities.get(entity).isEmpty())
			entityCommunities.remove(entity);
	}

	Set<Entity> intersectedExpansionFeatures = new HashSet<>();
	Map<Entity, Map<Entity, Double>> tmp = new HashMap<>(entityCommunities);
	for (Map.Entry<Entity, Map<Entity, Double>> e : tmp.entrySet())
	{
		Entity key = e.getKey();
		Map<Entity, Double> value = e.getValue();
		value.put(key, 1D);
		entityCommunities.put(key, value);
	}
	tmp = null;

	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Map<Entity, Double> community = e.getValue();
		Set<Entity> keySet = community.keySet();
		intersectedExpansionFeatures.addAll(keySet);
	}
	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Map<Entity, Double> community = e.getValue();
		removeDisambiguationEntities(community);
		intersectedExpansionFeatures = SetUtils.intersection(intersectedExpansionFeatures, community.keySet());
	}
	Map<Entity, Double> intersectedExpansionFeaturesMap = new HashMap<>();
	for (Entity entity : intersectedExpansionFeatures)
	{
		Double w = 0.0;
		for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
		{
			w += e.getValue().get(entity);
		}
		intersectedExpansionFeaturesMap.put(entity, w / entityCommunities.size());
	}

	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Map<Entity, Double> community = e.getValue();
		for (Entity entity : intersectedExpansionFeatures)
		{
			community.remove(entity);
		}
	}

	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Entity entity = e.getKey();
		Map<Entity, Double> community = e.getValue();
		community.remove(entity);
		if (!community.isEmpty())
		{
			String communityQuery = getCommunityBased64Query(community);
			String communityPlainQuery = getCommunityPlainQuery(community);
			sb.append(community.size() / 10.0).append(communityQuery);
			sb_plain.append(community.size() / 10.0).append(communityPlainQuery);
		}
	}

	String communityQuery = getCommunityBased64Query(intersectedExpansionFeaturesMap);
	String communityPlainQuery = getCommunityPlainQuery(intersectedExpansionFeaturesMap);
	sb.append(intersectedExpansionFeaturesMap.size()).append(communityQuery);
	sb_plain.append(intersectedExpansionFeaturesMap.size()).append(communityPlainQuery);

	sb.append(")");
	sb_plain.append(")");

	println("Plain Indri Query for \"" + query.getInput() + "\":" + sb_plain.toString());
	query.setPlainTopologicalQuery(sb_plain);
	query.setTopologicalIndriQuery(sb.toString());
	return sb.toString();
}

private static String plainForm(Configuration configuration, Query query)
{
	StringBuilder sb = new StringBuilder();
	StringBuilder sb_plain = new StringBuilder();

	if (configuration.removeEntitiesBasedOnCategories())
		println("Using category filtered communities to create the topologica query");

	Map<Entity, Map<Entity, Double>> entityCommunities = configuration.removeEntitiesBasedOnCategories() ? query.getEntityCommunitiesCategoryFiltered() : query.getEntityCommunities();
	Set<Entity> queryEntities = entityCommunities.keySet();

	if (entityCommunities.isEmpty())
		return getCombineBase64Query(query.getInput());

	sb.append("#weight(");
	sb_plain.append("#weight(");
	println("Query Entities: " + queryEntities);
	for (Entity entity : queryEntities)
	{
		String entityQuery = getEntityBased64Query(entity);
		String plainEntityQuery = getEntityPlainQuery(entity);
		sb.append(1.0).append(entityQuery);
		sb_plain.append(1.0).append(plainEntityQuery);
	}
	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Entity entity = e.getKey();
		Map<Entity, Double> community = e.getValue();
		community.remove(entity);
		removeDisambiguationEntities(community);
		if (!community.isEmpty())
		{
			String communityQuery = getCommunityBased64Query(community);
			String communityPlainQuery = getCommunityPlainQuery(community);
			sb.append(community.size() / 10.0).append(communityQuery);
			sb_plain.append(community.size() / 10.0).append(communityPlainQuery);
		}
	}

	sb.append(")");
	sb_plain.append(")");

	println("Plain Indri Query for \"" + query.getInput() + "\":" + sb_plain.toString());
	query.setPlainTopologicalQuery(sb_plain);
	query.setTopologicalIndriQuery(sb.toString());
	return sb.toString();
}

private static String getCommunityBased64Query(Map<Entity, Double> community)
{
	StringBuilder sb = new StringBuilder();
	sb.append("#weight(");
	for (Map.Entry<Entity, Double> entityWeightPair : community.entrySet())
	{
		Entity communityEntity = entityWeightPair.getKey();
		Double weight = entityWeightPair.getValue();
		String entityBased64Query = getEntityBased64Query(communityEntity);
		if (!entityBased64Query.equals(""))
			sb.append(weight).append(entityBased64Query);
	}
	sb.append(")");
	return sb.toString();
}

private static String getCommunityPlainQuery(Map<Entity, Double> community)
{
	StringBuilder sb = new StringBuilder();
	sb.append("#weight(");
	for (Map.Entry<Entity, Double> entityWeightPair : community.entrySet())
	{
		Entity communityEntity = entityWeightPair.getKey();
		Double weight = entityWeightPair.getValue();
		String entityPlainQuery = getEntityPlainQuery(communityEntity);
		if (!entityPlainQuery.equals(""))
			sb.append(StringUtils.decimalFormat(weight, 2)).append(entityPlainQuery);
	}
	sb.append(")");
	return sb.toString();
}

private static void removeDisambiguationEntities(Map<Entity, Double> community)
{
	Set<Entity> disambiguationEntities = new HashSet<>();
	for (Map.Entry<Entity, Double> e : community.entrySet())
	{
		if (Entity.isDisambiguationEntity(e.getKey()))
			disambiguationEntities.add(e.getKey());

	}
	for (Entity entity : disambiguationEntities)
	{
		community.remove(entity);
	}
}

public static enum QUERY_TERM_WEIGHT_AGREGATION
{

SUM_WEIGHT, MAX_WEIGHT, AVG_WEIGHT, NEW_WEIGHT, OLD_WEIGHT
}

public static void main2(String[] args) throws Exception
{
	//	System.load("/home/joan/DAMA/Recercaixa/recercaixa-2.0/lib/indri/indriInstallation/lib/libindri_jni.so");
	//	System.loadLibrary("libindri_jni");
	//String indexPath = "/home/joan/DAMA/Recercaixa/CLEF/DATASET IMAGES WIKIPEDIA/all_text/indexTest";
	String indexPath = "/home/joan/DAMA/Recercaixa/recercaixa-2.0/lib/indri/indexcaixa-2.0/lib/indri/index/index";
	//createIndex(indexPath);

	QueryEnvironment env = new QueryEnvironment();
	String myIndex = indexPath;

	String myQuery;

	myQuery = "#weight(0.007776573733553539#uw4(canal)  0.006239737274220033#uw4(death in venice)  0.07051640435361364#uw4(halkett boat)  0.26824786324786326#uw4(gondola)  0.2106863835634365#uw4(paddle)  0.04054154995331466#uw4(bridge of sighs)  0.275554435483871#uw4(canoe)  0.03184180619366692#uw4(submarine)  0.20994623655913974#uw4(kayak)  0.16917378917378917#uw4(steering wheel)  0.05678524374176548#uw4(buoyancy)  0.07787207120729525#uw4(list of water sports)  0.04797193544562747#uw4(rowing)  0.18713347407928688#uw4(hull)  0.011869910100475941#uw4(waterworld)  0.08243490499648135#uw4(one inch boy)  0.008780487804878048#uw4(doge s palace  venice)  0.021726062056250738#uw4(brentwood college school)  0.03868656362340088#uw4(ship)  0.1589938795523646#uw4(boat)  0.26679802052785917#uw4(rudder)  0.05821930304688925#uw4(propulsion tools)  0.3185063085063085#uw4(oar)  0.0076049903862349475#uw4(venice)  0.19649014778325125#uw4(bow)  0.2287186905413506#uw4(canoe racing)  0.20145773979107312#uw4(stern)  0.1749944382647386#uw4(vessel)  100#uw(gondola) 1#uw(venice) )";

	myQuery = "volskwagen beetle";

	PrintWriter writer;

	/*
	 Map<Video, Double> videos = getVideos(myQuery, LANGUAGE.ENGLISH);
	 writer = Writer.getWriter("/home/joan/Desktop/noExpansion.txt");
	 for (Map.Entry<Video, Double> e : videos.entrySet()) {
	 writer.println(e.getKey().getTitle());
	 }
	 writer.close();
	 */
	Map<String, Double> videosPonte = getVideosPonte(myQuery, LANGUAGE.ENGLISH, null);
	writer = FileManagement.Writer.getWriter("~/Desktop/PonteExpansion.txt");
	for (Map.Entry<String, Double> e : videosPonte.entrySet())
	{
		writer.println(e.getKey());
	}
	writer.close();
	Map<String, Double> videosRM = getVideosRM(myQuery, LANGUAGE.ENGLISH, null);
	writer = FileManagement.Writer.getWriter("~/Desktop/RMExpansion.txt");
	for (Map.Entry<String, Double> e : videosRM.entrySet())
	{
		writer.println(e.getKey());
	}
	writer.close();

	/*
	 ScoredExtentResult[] results;
	 env.addIndex(myIndex);
	 println("Documents: " + env.documentCount() + ", camps: " + env.fieldList().toString());
	 results = env.runQuery(myQuery, 100);
	 ParsedDocument[] documents = env.documents(results);
	 String[] names = env.documentMetadata(results, "name");
	 for (int i = 0; i < results.length; i++) {
	 {
	 println(names[i]);
	 }
	 }
	 for (int i = 0; i < documents.length; i++) {
	 int passageBegin = results[i].begin;
	 int passageEnd = results[i].end;
	 int byteBegin = documents[i].positions[ passageBegin].begin;
	 int byteEnd = documents[i].positions[ passageEnd - 1].end;
	 String startText = documents[i].text + byteBegin;
	 int byteLength = byteEnd - byteBegin;
	 }
	 env.close();
	 */
}

private static void createIndex(String indexPath) throws Exception
{
	String[] stopWordList =
	{
		"a", "an", "and", "are", "as", "at", "be", "by", "for",
		"from", "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
		"to", "was", "were", "will", "with"
	};

	String myIndex = indexPath;

	IndexEnvironment envI = new IndexEnvironment();
	envI.setStoreDocs(true);

	// create an Indri repository
	envI.setMemory(256000000);

	//envI.setStemmer("krovetz");
	envI.setStopwords(stopWordList);

	envI.setIndexedFields(new String[]
	{
		"name", "description", "comment", "caption",
	});
	envI.setMetadataIndexedFields(new String[]
	{
		"name", "description", "comment", "caption",
	}, new String[]
	{
		"name", "description", "comment", "caption",
	});
	envI.create(myIndex);

	// add xml files to the just created index i.e myIndex
	// xml_data is a folder which contains the list of xml files to be added 
	File filesDir = new File("/home/joan/DAMA/Recercaixa/CLEF/DATASET IMAGES WIKIPEDIA/all_text/metadata");
	File[] files = filesDir.listFiles();
	int noOffiles = files.length;
	for (int i = 0; i < noOffiles; i++)
	{
		File files2Dir = files[i];
		File[] files2 = files2Dir.listFiles();
		for (int j = 0; j < files2.length; j++)
		{
			println(files2[i].getCanonicalPath() + "\t" + files2[i].getCanonicalFile());
			envI.addFile(files2[i].getCanonicalPath(), "xml");
		}
	}
}

/*public static Map<Video, Double> getVideos(String myQuery, LANGUAGE language) throws Exception {
 return getVideos(myQuery, language, PSEUDO_RELEVANCE_FEEDBACK.NONE);
 }*/
public static Map<String, Double> getVideos(Configuration configuration, Query query, String indriQuery) throws FileNotFoundException, Exception
{
	SearchEngineParameters sep = new SearchEngineParameters(configuration);
	QueryEnvironment env = new QueryEnvironment();
	String myIndex = sep.indexPath;

	ScoredExtentResult[] results;

	env.addIndex(myIndex);

	println("Indri Query: " + indriQuery);
	results = env.runQuery(indriQuery, 100);

	if (sep.prf == PSEUDO_RELEVANCE_FEEDBACK.PONTE)
	{
		println("        using ponte pseudo-relevance feedback");
		PonteExpander qe = new PonteExpander(env, new HashMap());
		String queryExpanded = qe.expand(indriQuery, results);
		println("PRF_QUERY: " + queryExpanded);
		results = env.runQuery(queryExpanded, 1000);

	} else if (sep.prf == PSEUDO_RELEVANCE_FEEDBACK.RM)
	{
		println("        using rm pseudo-relevance feedback");
		RMExpander rm = new RMExpander(env, new HashMap());
		String queryExpanded = rm.expand(indriQuery, results);
		println("PRF_QUERY: " + queryExpanded);
		results = env.runQuery(queryExpanded, 1000);
	}
	Map<String, Double> constructVideosMap = constructVideosMap(results, env, sep.trecTest);
	String printTrecFormat = printTrecFormat(configuration, results, env, query.getId());
	query.setTrecOutput(printTrecFormat);
	return constructVideosMap;// (results, env, trecTest);
}

public static Map<String, Double> getVideos(Configuration configuration, Query query) throws Exception
{

	String indriQuery = buildIndriQuery(configuration, query);

	return getVideos(configuration, query, indriQuery);

}

/*public static Map<String, Double> getVideos(Configuration configuration, Query query) throws Exception
 {

 String myTopologicalQuery = generateQuery(configuration, query);

 query.setTopologicalExpansionFeatures(myTopologicalQuery);

 return getVideos(configuration, query);

 }*/
public static Map<String, Integer> getWikipediaPages(Configuration configuration, List<String> inputList, List<String> filters) throws Exception
{

	Set<String> possibilities = new HashSet<String>();

	for (String query : inputList)
	{
		possibilities.addAll(obtainPossibilitiesFromQuery(query, configuration.getLanguage()));
	}

	String indexPath = configuration.getWikipediaIndexPath();
	LANGUAGE language = null;
	language = configuration.getLanguage();
	if (indexPath == null)
		switch (language)
		{
			case ENGLISH:
				indexPath = "/scratch/joan/WikipediaIndex/index";
				//indexPath = "/scratch/joan/englishWikipediaIndex2";
				break;
			case GERMAN:
				indexPath = "/scratch/joan/GermanWikipediaIndex3";
				break;
			default:
				println("There is no index for " + language + " Wikipedia.");
		}
	println("Wikipedia Index: " + indexPath);
	String myIndex = indexPath;

	ScoredExtentResult[] results;
	QueryEnvironment env = new QueryEnvironment();
	Set<String> searched = new HashSet<String>();

	env.addIndex(myIndex);

	Map<String, Integer> resultsToReturn = new HashMap<String, Integer>();
	for (String t : possibilities)
	{
//            println("1: "+t);
		String originalP = t.replaceAll(" -", " ");
		//t = t.replaceAll("[^A-Za-z0-9 ]", " ");
		List<String> tokenizedList = Tokenizer.getTokenizedList(t, language, true);
		tokenizedList = new ArrayList<String>(TokenFilter.remove(filters, tokenizedList));

		//println("tolenized List = " + tokenizedList);
		String myQuery = null;
		int windowsSize;

		if (tokenizedList.size() > 1)
		{
			String[] split = t.split(" ");
			windowsSize = split.length;
			myQuery = "#uw" + 5 + "" + getTermBasedBase64(originalP) + "";
		} else
		{
			windowsSize = 1;
			if (!tokenizedList.isEmpty())
			{
				if (!TokenFilter.isFiltered(filters, tokenizedList.get(0)))
				{
					if (tokenizedList.get(0).length() > 1)
					{
						myQuery = "#uw" + getTermBasedBase64(tokenizedList.get(0)) + ".title";
					}
				}
			}
		}

		if (myQuery != null && !tokenizedList.isEmpty() && !searched.contains(myQuery))
		{
			searched.add(myQuery);
			results = env.runQuery(myQuery, 100);

			ParsedDocument[] documents = env.documents(results);
			if (documents.length > 0)
			{
				println("        myQuery: \"" + t + "(" + myQuery + ")\" " + documents.length + " results.");
			}
			//println("Query (IndriModule.java): " + t + ". results: " + documents.length);

			int conservativeWindowsSize = windowsSize;
			for (int j = 0; j < documents.length; j++)
			{
				windowsSize = conservativeWindowsSize;
				try
				{
					String title = documents[j].text.substring(documents[j].text.indexOf("<title>") + "<title>".length(), documents[j].text.indexOf("</title>"));
					if (!title.contains(":") || title.contains("Category:"))
					{
						if (windowsSize > 1)
						{
							//Si té més d'una paraula, però aquestes apareixen al títol (part més important del text) s'ha de  veure bonificat.
							Set<Token> tokensForTitle = Token.tokenize(title, language, true);
							Set<Token> tokensForInputList = Token.tokenize(inputList, language, true);
							tokensForTitle.retainAll(tokensForInputList);

							int count = tokensForTitle.size();
							//windowsSize = (windowsSize - count) + (count * 2);
							windowsSize = windowsSize;
						} else
						{
							//windowsSize *= 2;
							windowsSize *= 1;
						}
						// println("                        "+title);
						Integer oldSize = resultsToReturn.put(title.toLowerCase(), windowsSize);
						if (oldSize != null && oldSize > windowsSize)
						{
							resultsToReturn.put(title.toLowerCase(), oldSize);
						}
					}
				} catch (StringIndexOutOfBoundsException e)
				{
					println("Problems with: " + documents[j]);
				}
			}
		}
	}
	env.close();
	return resultsToReturn;
}

public static Map<String, Integer> getWikipediaPagesFromEntities(Configuration configuration, Set<Entity> entities) throws Exception
{

	List<String> filters = configuration.getFilterFilePaths();
	LANGUAGE language = configuration.getLanguage();
	Set<String> possibilities = new HashSet<>();
	for (Entity entity : entities)
	{
		if (!entity.isStopWord(language, filters))
			possibilities.addAll(entity.getEntityNames());
		else
			println("\"" + entity + "\" is not used to retrieve relevant documents for being a filtered word.");
	}

	String indexPath = configuration.getWikipediaIndexPath();
	language = configuration.getLanguage();
	if (indexPath == null)
		switch (language)
		{
			case ENGLISH:
				indexPath = "/scratch/joan/WikipediaIndex/index";
				//indexPath = "/scratch/joan/englishWikipediaIndex2";
				break;
			case GERMAN:
				indexPath = "/scratch/joan/GermanWikipediaIndex3";
				break;
			default:
				println("There is no index for " + language + " Wikipedia.");
		}
	println("Wikipedia Index: " + indexPath);
	String myIndex = indexPath;

	ScoredExtentResult[] results;
	QueryEnvironment env = new QueryEnvironment();
	Set<String> searched = new HashSet<>();

	env.addIndex(myIndex);

	Map<String, Integer> resultsToReturn = new HashMap<>();
	for (String t : possibilities)
	{
//            println("1: "+t);
		String originalP = t.replaceAll(" -", " ");
		//t = t.replaceAll("[^A-Za-z0-9 ]", " ");
		List<String> tokenizedList = Tokenizer.getTokenizedList(t, language, true);
		tokenizedList = new ArrayList<>(TokenFilter.remove(filters, tokenizedList));

		//println("tolenized List = " + tokenizedList);
		String myQuery = null;
		int windowsSize;

		if (tokenizedList.size() > 1)
		{
			String[] split = t.split(" ");
			windowsSize = split.length;
			myQuery = "#" + windowsSize + "" + getTermBasedBase64(originalP) + "";
		} else
		{
			windowsSize = 1;
			if (!tokenizedList.isEmpty())
			{
				if (!TokenFilter.isFiltered(filters, tokenizedList.get(0)))
				{
					if (tokenizedList.get(0).length() > 1)
					{
						myQuery = "#1" + getTermBasedBase64(tokenizedList.get(0)) + ".title";
					}
				}
			}
		}

		if (myQuery != null && !tokenizedList.isEmpty() && !searched.contains(myQuery))
		{
			searched.add(myQuery);
			results = env.runQuery(myQuery, 100);

			ParsedDocument[] documents = env.documents(results);
			if (documents.length > 0)
			{
				println("        myQuery: \"" + t + "(" + myQuery + ")\" " + documents.length + " results.");
			}
			//println("Query (IndriModule.java): " + t + ". results: " + documents.length);

			int conservativeWindowsSize = windowsSize;
			for (int j = 0; j < documents.length; j++)
			{
				windowsSize = conservativeWindowsSize;
				try
				{
					String title = documents[j].text.substring(documents[j].text.indexOf("<title>") + "<title>".length(), documents[j].text.indexOf("</title>"));
					if (!title.contains(":") || title.contains("Category:"))
					{
						/*
						 if (windowsSize > 1)
						 {
						 //Si té més d'una paraula, però aquestes apareixen al títol (part més important del text) s'ha de  veure bonificat.
						 Set<Token> tokensForTitle = Token.tokenize(title, language, true);
						 Set<Token> tokensForInputList = Token.tokenize(inputList, language, true);
						 tokensForTitle.retainAll(tokensForInputList);

						 int count = tokensForTitle.size();
						 //windowsSize = (windowsSize - count) + (count * 2);
						 windowsSize = windowsSize;
						 } else
						 {
						 windowsSize *= 1;
						 }
						 */
						Integer oldSize = resultsToReturn.put(title.toLowerCase(), windowsSize);
						if (oldSize != null && oldSize > windowsSize)
						{
							resultsToReturn.put(title.toLowerCase(), oldSize);
						}
					}
				} catch (StringIndexOutOfBoundsException e)
				{
					println("Problems with: " + documents[j]);
				}
			}
		}
	}
	env.close();
	return resultsToReturn;
}

public static void main1(String[] args) throws Exception
{
	println(obtainPossibilitiesFromQuery("A B C D E", LANGUAGE.ENGLISH));
	println(obtainPossibilitiesFromQuery2("A B C D E", LANGUAGE.ENGLISH));
}

private static List<String> obtainPossibilitiesFromQuery(String query, LANGUAGE language) throws Exception
{

	query = query.replaceAll("'s", "");
	String[] tokenString = query.split(" ");
	List<String> tokenNames = new ArrayList<>();

	for (String t : tokenString)
	{
		tokenNames.add(t.trim());
	}

	List<String> possibilities = new ArrayList<>();
	if (Article.isEntity(query))
	{
		possibilities.add(query);
		return possibilities;
	}

	StringBuilder tmp = new StringBuilder();
	for (String tokenName : tokenNames)
	{
		tmp.append(tokenName).append(" ");
	}

	possibilities.add(tmp.toString().trim());
	int i = 1;

	String[] cleanQuery = possibilities.get(0).split(" ");

	for (int k = 0; k < cleanQuery.length; k++)
	{
		String shingle = cleanQuery[k];
		List<String> tokenizedList = Tokenizer.getTokenizedList(shingle, language, true);
		if (!tokenizedList.toString().trim().isEmpty())
		{
			possibilities.add(shingle);
		}
	}

	for (int k = 0; k < cleanQuery.length - 1; k++)
	{
		String shingle = (cleanQuery[k] + " " + cleanQuery[k + 1]);

		List<String> tokenizedList = Tokenizer.getTokenizedList(shingle, language, true);
		if (!tokenizedList.toString().trim().isEmpty())
		{
			possibilities.add(shingle);
		}
	}

	Set<String> possibilitiesSet = new HashSet<String>(possibilities);

	possibilities = new ArrayList<String>(possibilitiesSet);

//        println(possibilities);
	return possibilities;

}

private static List<String> obtainPossibilitiesFromQuery2(String query, LANGUAGE language) throws Exception
{
	//query="colored Volkswagen beetle";
	query = query.replaceAll("'s", "");
	String[] tokenString = query.split(" ");
	List<String> tokenNames = new ArrayList<>();

	for (String t : tokenString)
	{
		//tokenNames.add("#base64("+base64_encode(t.getName())+")");
		tokenNames.add(t.trim());
	}

	List<String> possibilities = new ArrayList<>();
	StringBuilder tmp = new StringBuilder();
	for (String tokenName : tokenNames)
	{
		tmp.append(tokenName).append(" ");
	}

	String lastString = tmp.toString();
	possibilities.add(tmp.toString().trim());
	int i = 1;
	String finalString = "";
	while ("".equals(finalString))
	{
		finalString = tokenNames.get(tokenNames.size() - i);
		i++;
	}
//        println("P: " + possibilities);
	i = 0;
//        while (!finalString.equalsIgnoreCase(lastTheoricalString))

	while (i < possibilities.size())
	{
		//            println(i  + " " + finalString + "=?=" + lastTheoricalString);
		String base = possibilities.get((i));
		//println("base=" + base);
		String[] split = base.split(" ");
		//println(i + ":split size= " + split.length + " " + finalString + "=?=" + lastTheoricalString);
		StringBuilder left = new StringBuilder();
		for (int j = 0; j < split.length - 1; j++)
		{
			left.append(split[j]).append(" ");
		}
		StringBuilder right = new StringBuilder();
		for (int j = 1; j < split.length; j++)
		{
			right.append(split[j]).append(" ");
		}
		List<String> leftList = Tokenizer.getTokenizedList(left.toString(), language, true);

		List<String> rightList = Tokenizer.getTokenizedList(right.toString(), language, true);
		if (!(lastString.equals(left.toString().trim())) && !leftList.isEmpty())
		{
			possibilities.add(left.toString().trim());
		}

		if (!rightList.isEmpty())
		{
			lastString = right.toString().trim();
			possibilities.add(lastString);
		}

		i++;
	}

	//println(possibilities);
	Set<String> possibilitiesSet = new HashSet<>(possibilities);

	possibilities = new ArrayList<>(possibilitiesSet);

	return possibilities;

}

private enum QUERY_EXTENSION_NORMALIZATION_METHOD
{

E, E1, E2
}

public enum PSEUDO_RELEVANCE_FEEDBACK
{

NONE, PONTE, RM
}

private static String generateEntityBasedBase64TopologicalQuery(Configuration configuration, Query query, Entity entity)
{
	Map<Entity, Double> community = query.getCommunity();
	community.remove(entity);

	StringBuilder sb = new StringBuilder();

	sb.append("#combine(").append(getEntityBased64Query(entity));

	if (!community.isEmpty())
	{
		sb.append("#weight(");
		for (Map.Entry<Entity, Double> entityWeightPair : community.entrySet())
		{
			Entity communityEntity = entityWeightPair.getKey();
			Double weight = entityWeightPair.getValue();
			sb.append(weight).append(getEntityBased64Query(communityEntity));
		}
		sb.append(")");
	}
	sb.append(")");
	community.put(entity, 1.0);

	return sb.toString();
}

private static String generateEntityBasedPlainTopologicalQuery(Configuration configuration, Query query, Entity entity)
{
	Map<Entity, Double> community = query.getCommunity();
	community.remove(entity);

	StringBuilder sb = new StringBuilder();
	sb.append("#combine(").append(getEntityPlainQuery(entity));

	if (!community.isEmpty())
	{
		sb.append("#weight(");
		for (Map.Entry<Entity, Double> entityWeightPair : community.entrySet())
		{
			Entity communityEntity = entityWeightPair.getKey();
			Double weight = entityWeightPair.getValue();
			sb.append(weight).append(getEntityPlainQuery(communityEntity));
		}
		sb.append(")");
	}
	sb.append(")");
	community.put(entity, 1.0);

	return sb.toString();
}

private static String buildTopologicalQueryForQueryExpansionPipeline(Configuration configuration, Query query) throws Exception
{
	println("Generating Topological Query for Query " + query.getId());

	SearchEngineParameters sep = new SearchEngineParameters(configuration);

	QUERY_TERM_WEIGHT_AGREGATION qtwa = sep.getQTWA();
	String windowSize = sep.getWindowSize();
	Double weightOriginalQuery = sep.getWeightOriginalQuery();
	String indriOperation = sep.getIndriOperation();

	StringBuilder plainQuery = new StringBuilder();

	NumberFormat f = new DecimalFormat("###.#####");
	StringBuilder internalQuery = new StringBuilder();

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Construir mapa amb tots els string (initial query terms & title articles) i el seu valor. Out: finalQueryEntities //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	Map<String, Double> queryUnities;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Mapa amb els articles que s'han obtingut a partir del mètode concret que s'ha fet servir. Out: queryUnitiesArtciles// 
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	Map<String, Double> queryUnitiesArticles = new HashMap<>();
	List<String> articlesQuery = new ArrayList<>();
	Map<Entity, Double> entities = query.getCommunity();
	println("Articles from community:" + entities + "|IndriModule.java 396");
	Double maxWeight = 0.00000000000000000000000000000000000000000000000000001;
	for (Entry<Entity, Double> e : entities.entrySet())
	{

		Entity entity = e.getKey();
		Double entityWeight = e.getValue();
		Set<String> entityNames = entity.getEntityNames();
		for (String entityName : entityNames)
		{
			Double oldEntityWeight = queryUnitiesArticles.put(entityName, entityWeight);
			Double finalWeight = null;
			if (oldEntityWeight != null)
			{
				println("COLLISION WHEN GENERATING QUERYUNITIESARTICLES INSERTING " + entityName + " |IndriModule.java 444");
				switch (qtwa)
				{
					case SUM_WEIGHT:
						finalWeight = entityWeight + oldEntityWeight;
						break;
					case MAX_WEIGHT:
						finalWeight = entityWeight > oldEntityWeight ? entityWeight : oldEntityWeight;
						break;
					case AVG_WEIGHT:
						finalWeight = (entityWeight + oldEntityWeight) / 2;
						break;
					case NEW_WEIGHT:
						finalWeight = entityWeight;
						break;
					case OLD_WEIGHT:
						finalWeight = oldEntityWeight;
						break;
				}
				queryUnitiesArticles.put(entityName, finalWeight);
				maxWeight = maxWeight < finalWeight ? finalWeight : maxWeight;
			}
			maxWeight = maxWeight < entityWeight ? entityWeight : maxWeight;
		}
	}

	Map<String, Double> mapTokensInput = new HashMap<>();
	Set<Token> tokens = query.getInputTokens();
	println("[WARNING] Tokens module is equal to 1. In the original version was equal to 0.71.Why?" + tokens);
	for (Token t : tokens)
	{
		String entity = t.getName();
		if (entity.contains("'"))
		{
			entity = t.getName().substring(0, t.getName().indexOf("'"));
		}
		println("Term " + entity + " is added in Level 0. With weight equal to  " + maxWeight);
		Double oldEntityWeight = queryUnitiesArticles.put(entity, maxWeight);
		if (oldEntityWeight != null)
		{
			println("COLLISION WHEN GENERATING QUERYUNITIESARTICLES INSERTING " + entity + " |IndriModule.java 444");
			Double finalWeight = oldEntityWeight + (1 - oldEntityWeight) * maxWeight;
			queryUnitiesArticles.put(entity, finalWeight);
		}
	}

	//Mòdul del mapa del articles
	double module = 0.0;
	for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
	{
		module += (e.getValue() * e.getValue());
	}
	module = Math.sqrt(module);

	Map<String, Double> tmp = new HashMap<String, Double>();
	for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
	{
		String articleEntity = e.getKey();
		Double articleEntityWeight = e.getValue() / module;
		tmp.put(articleEntity, articleEntityWeight);
	}
	queryUnitiesArticles = UtilsMap.sortByValue(tmp);
	module = 0.0;
	for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
	{
		module += (e.getValue() * e.getValue());
	}
	module = Math.sqrt(module);

	println("Ranked articles: " + queryUnitiesArticles + " MOD=" + module);

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                              Mapa amb els original query terms. Out:mapTokensInput                                //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	println("Extension Form: " + configuration.getExtensionNormalizationMethod());
	QUERY_EXTENSION_NORMALIZATION_METHOD queryForm = QUERY_EXTENSION_NORMALIZATION_METHOD.valueOf(configuration.getExtensionNormalizationMethod());

	tmp = new HashMap<>();
	Map<String, Double> tmp2 = new HashMap<>();
	switch (queryForm)
	{
		case E:
			for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
			{
				tmp.put(e.getKey(), e.getValue());
			}
			for (Map.Entry<String, Double> e : mapTokensInput.entrySet())
			{
				Double oldWeight = tmp.put(e.getKey(), e.getValue());
				if (oldWeight != null)
				{
					tmp.put(e.getKey(), getFinalWeight(e.getValue(), oldWeight));
				}
			}

			break;
		case E1:
			module = 0.0;
			for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
			{

				module += ((e.getValue() * e.getValue()));

			}
			module = Math.sqrt(module);
			for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
			{
				tmp.put(e.getKey(), e.getValue() / module);
			}

			for (Map.Entry<String, Double> e : mapTokensInput.entrySet())
			{
				Double oldWeight = tmp.put(e.getKey(), e.getValue());
				if (oldWeight != null)
				{
					tmp.put(e.getKey(), getFinalWeight(e.getValue(), oldWeight));
				}
			}

			tmp = constructQueryExpansion(tmp, mapTokensInput);

			break;

		case E2:
			Iterator<Entry<String, Double>> it = mapTokensInput.entrySet().iterator();
			boolean stop = false;
			Double factor = null;
			while (it.hasNext() && !stop)
			{
				Entry<String, Double> e = it.next();
				factor = e.getValue();
				stop = true;
			}
			for (Map.Entry<String, Double> e : queryUnitiesArticles.entrySet())
			{
				tmp.put(e.getKey(), e.getValue() / factor);
			}
			println("Mapa de termes després d'haver sigut divit pel factor: " + tmp);
			tmp = constructQueryExpansion(tmp, mapTokensInput);

			break;

	}
	queryUnities = UtilsMap.sortByValue(tmp);
	println("Topological query terms sorted by weight: " + queryUnities);

	Map<String, String> finalQueryEntities = new HashMap<>();
	for (Map.Entry<String, Double> e : queryUnities.entrySet())
	{
		String entityWeight = f.format(e.getValue());
		String entity = e.getKey();
		if (!entityWeight.equals("0"))
		{// && entity.matches("[\\w-_\\s]+$")) {
			Article a = new Article(entity);
			if (a.getId() != null)
			{
				articlesQuery.add(a.getId() + "");
			}

			entity = removeInBrackets(entity);
			entity.replaceAll(".", "");
			//if (!entity.isEmpty())
			while (!entity.isEmpty() && (entity.charAt(entity.length() - 1) == '-' || entity.charAt(entity.length() - 1) == '_'))
			{
				//println("\""+entity+"\"");
				entity = entity.substring(0, entity.length() - 1);
			}
			if (!entity.isEmpty())
			{
				finalQueryEntities.put(entity, entityWeight);
			}
		}
	}
	finalQueryEntities = UtilsMap.sortByValue(finalQueryEntities);
	for (Map.Entry<String, String> e : finalQueryEntities.entrySet())
	{
		String entityWeight = e.getValue();
		String entity = e.getKey();
		windowSize = String.valueOf(entity.split(" ").length);
		String entity64;
		entity64 = getTermBasedBase64(entity);
		internalQuery.append(entityWeight).append("#uw").append(windowSize).append(entity64);
		String entityPlain = "(" + entity + ")";
		plainQuery.append(entityWeight).append("#uw").append(windowSize).append(entityPlain);
	}

	plainQuery = new StringBuilder("#" + indriOperation + "(" + plainQuery.toString().trim() + ")");
	println("Plain topological query of Query " + query.getId() + ": " + plainQuery);
	query.setPlainTopologicalQuery(plainQuery);

	String searchEngineQuery = "#" + indriOperation + "(" + internalQuery.toString().trim() + ")";

	return searchEngineQuery;
}

private static String removeInBrackets(String title)
{
	return StringUtils.removeInBrackets(title);
}

private static Map<String, Double> constructVideosMap(ScoredExtentResult[] results, QueryEnvironment env, Configuration.TEST test) throws Exception
{
	ParsedDocument[] documents = env.documents(results);
	//Map<Video, Double> videos = new HashMap<Video, Double>();
	Map<String, Double> videos = new HashMap<>();

	final Pattern pattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>", Pattern.CASE_INSENSITIVE);
	for (int i = 0; i < documents.length; i++)
	{
		int passageBegin = results[i].begin;
		int passageEnd = results[i].end;

		int byteBegin = documents[i].positions[ passageBegin].begin;
		int byteEnd = documents[i].positions[ passageEnd - 1].end;

		String startText = documents[i].text + byteBegin;
		int byteLength = byteEnd - byteBegin;

		try
		{
//if (i < 2) System.out.println(startText)			;
			if (test == Configuration.TEST.WEBCLEF)
			{
				String id = startText.substring(startText.indexOf("id="), startText.indexOf("file"));
				String videoId = id.substring(id.indexOf("\"") + 1, id.lastIndexOf("\""));
				videos.put((new Video(videoId)).getTitle(), results[i].score);
//				System.out.println("Video: " + new Video(videoId) + "number: " + results[i].number + ", ordinal: " + results[i].ordinal + ", parent ordinal: " + results[i].parentO/rdinal + " score: " + results[i].score);
//                System.out.println(id +" number: "+results[i].number+", ordinal: "+results[i].ordinal+", parent ordinal: "+results[i].parentOrdinal+" score: "+results[i].score );

			} else
			{
				//String id = startText.substring(startText.indexOf("<DOCNO>") + 8, startText.indexOf("</DOCNO>"));
				Matcher matcher = pattern.matcher(startText);
				matcher.find();
				String videoId = matcher.group(1).trim();
				if (test == TEST.WEBCLEF_ENTITIFIED)
					videos.put(new Video(videoId).getTitle(), results[i].score);
				else
					videos.put(videoId, results[i].score);
			}
		} catch (java.lang.StringIndexOutOfBoundsException s)
		{
			println("[[ERROR]: Problem with result document:\n                     +" + startText);

		}
	}
	videos = UtilsMap.sortByValue(videos);
	return videos;
}

private static String printTrecFormat(Configuration configuration, ScoredExtentResult[] results, QueryEnvironment env, String queryId) throws Exception
{
	int start = 0;
	String[] documentNames = env.documentMetadata(results, "docno");
	ParsedDocument[] documents = env.documents(results);
	StringBuilder result = new StringBuilder();

	for (int i = 0; i < documents.length; i++)
	{
		String resultName = "";
		if (configuration.getTrecTest() == WEBCLEF)
		{
			int passageBegin = results[i].begin;
			int byteBegin = documents[i].positions[ passageBegin].begin;
			String startText = documents[i].text + byteBegin;
			resultName = startText.substring(startText.indexOf("id=") + 3, startText.indexOf("file")).replaceAll("\"", "");
		} else
		{
			resultName = documentNames[i];
		}
		int rank = start + i + 1;
		String line = (queryId + " 0 " + resultName + " " + rank + " " + results[i].score + " indri");
		result.append(line).append(";");
		//writer.println(line);
	}
	//writer.close();
	env.close();

	return result.toString();
}

public static void treceval(String trecevalFile, String qrelsFile) throws IOException
{
	//run(new File("/home/joan/trec_eval.9.0/trec_eval"), new File("/home/joan/trec_eval.9.0/qrles12/qrels.51-100.disk1.disk2"), new File(trecevalFile), new PrintWriter("dataOutFile.txt"), new PrintWriter("evalOutFile.txt"));
	run(new File("/home/joan/trec_eval.9.0/trec_eval"), new File(qrelsFile), new File(trecevalFile), new PrintWriter("dataOutFile.txt"), new PrintWriter("evalOutFile.txt"));
}

public static String getQueryFromTitleArticles(List<String> query)
{
	StringBuilder internalQuery = new StringBuilder();
	NumberFormat f = new DecimalFormat("###.#####");
	for (String title : query)
	{
		title = "#base64(" + base64_encode(title) + ")";
		title = "(" + title + ") ";
		title = f.format(0.15) + "#uw" + 1 + title + " ";
		internalQuery.append(title);

	}
	String finalQuery = "#" + "weight" + "(" + internalQuery.toString().trim() + ")";

	return finalQuery;

}

/// Base64 encode an input block of memory into a string
/// @param input the input to encode
/// @param length the length of the input
/// @return the encoded string
public static String base64_encode(String input)
{
	/*
	 input = input.replaceAll("\\r|\\n", "");
	 char[] lookup = {
	 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
	 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
	 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
	 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	 };
	 char[] in = input.toCharArray();
	 StringBuilder result;
	 int value;
	 int mainLength;

	 int length = input.length();
	 if (length % 3 != 0) {
	 mainLength = length - length % 3;
	 } else {
	 mainLength = length;
	 }
	 result = new StringBuilder((length / 2 + 1) * 3);
	 for (int i = 0; i < mainLength; i += 3) {
	 value = (in[i + 0] & 0xff) << 16 | (in[i + 1] & 0xff) << 8 | (in[i + 2] & 0xff);
	 char fourth = lookup[value & 0x3f];
	 value >>= 6;
	 char third = lookup[value & 0x3f];
	 value >>= 6;
	 char second = lookup[value & 0x3f];
	 value >>= 6;
	 char first = lookup[value & 0x3f];

	 result.append(first);
	 result.append(second);
	 result.append(third);
	 result.append(fourth);
	 }
	 if (mainLength != length) {
	 value = 0;
	 int remaining = length - mainLength;
	 {
	 // build a value based on the characters we 
	 // have left
	 char first = 0;
	 char second = 0;
	 char third = 0;

	 if (remaining >= 1) {
	 first = in[mainLength + 0];
	 }
	 if (remaining >= 2) {
	 second = in[mainLength + 1];
	 }
	 if (remaining >= 3) {
	 third = in[mainLength + 2];
	 }

	 value = first << 16 | second << 8 | third;
	 }
	 {
	 // encode them
	 char fourth = '=';
	 char third = '=';
	 char second = '=';
	 char first = '=';

	 if (remaining >= 3) {
	 fourth = lookup[value & 0x3f];
	 }
	 value >>= 6;
	 if (remaining >= 2) {
	 third = lookup[value & 0x3f];
	 }
	 value >>= 6;
	 if (remaining >= 1) {
	 second = lookup[value & 0x3f];
	 }
	 value >>= 6;
	 first = lookup[value & 0x3f];

	 result.append(first);
	 result.append(second);
	 result.append(third);
	 result.append(fourth);
	 }


	 }
	 */
	byte[] encoded = Base64.encodeBase64(input.getBytes());
	return new String(encoded);
	// return result.toString();

}

public static String getCombineBase64Query(String text)
{
	return "#combine" + getTermBasedBase64(text) + "";
}

public static String getCombinePlainQuery(String text)
{
	return "#combine(" + text + ")";
}

public static void main3(String[] args)
{
	try
	{
		QueryEnvironment env = new QueryEnvironment();
		String myIndex = "/home/joan/DAMA/Recercaixa/recercaixa-2.0/lib/indri/index/";
		String myQuery = "#combine(an elephant)";
		ScoredExtentResult[] results = null;
		String[] names = null;
		// open an Indri repository
		env.addIndex(myIndex);
		PonteExpander ponte = new PonteExpander(env, new HashMap());
		// run an Indri query, returning 10 results
		results = env.runQuery(myQuery, 10);
		String newQuery = ponte.expand(myQuery, results);
		println("New query: " + newQuery);
		results = env.runQuery(newQuery, 10);
		// fetch the names of the retrieved documents
		names = env.documentMetadata(results, "docno");
		for (int i = 0; i < results.length; i++)
		{
			println(names[i] + " " + results[i].score + " "
					+ results[i].begin + " " + results[i].end);
		}
		env.close();
	} catch (Exception e)
	{
		e.printStackTrace();
	}
}

public static void main(String[] args) throws Exception
{

	QueryEnvironment env = new QueryEnvironment();
	String myIndex = "/scratch/joan/DATASET IMAGES WIKIPEDIA/index";
	myIndex = "/home/joan/Desktop/WebclefEngloshIndex";
	myIndex = "/home/joan/DAMA/Recercaixa/recercaixa-2.0/lib/indri/indri-5.3/buildindex/tmpIndex";

	String indriQuery;
	indriQuery = "#weight(0.08#combine(#base64(c2tlbGV0b24=)#base64(b2Y=)#base64(ZGlub3NhdXI=))0.92#weight(0.66378#uw1(#base64(ZGlub3NhdXI=))0.66378#uw1(#base64(c2tlbGV0b24=))))";
	indriQuery = "#weight(0.08#combine(skeleton of dinosaur) 0.92#weight(0.66378#uw1(dinosaur)0.66378#uw1(skeleton)))";
	//indriQuery="#weight(0.08#combine(#base64(c2tlbGV0b24=)#base64(b2Y=)#base64(ZGlub3NhdXI=))0.92#weight(0.66378#uw1(#base64(ZGlub3NhdXI=))0.66378#uw1(#base64(c2tlbGV0b24=))))";
	//indriQuery="#combine(#base64(c2tlbGV0b24=)#base64(b2Y=)#base64(ZGlub3NhdXI=))";
	ScoredExtentResult[] results;

	env.addIndex(myIndex);

	results = env.runQuery(indriQuery, 100);

	Map<String, Double> constructVideosMap = constructVideosMap(results, env, Configuration.TEST.WEBCLEF);

	println(constructVideosMap);// (results
	//main1(null);
}

public static Map<String, Double> getVideosPonte(String myQuery, LANGUAGE language, Configuration.TEST trecTest) throws Exception
{
	String indexPath = null;
	switch (language)
	{
		case ENGLISH:
			indexPath = "/scratch/joan/DATASET IMAGES WIKIPEDIA/index";
			break;
		case GERMAN:
			indexPath = "/scratch/joan/DATASET IMAGES WIKIPEDIA/GermanIndex";
			break;
		default:
			println("No index for " + language + ".");
	}
	String myIndex = indexPath;

	ScoredExtentResult[] results;

	QueryEnvironment qenv = new QueryEnvironment();
	qenv.addIndex(myIndex);

	Map map = new HashMap();
	System.load("/home/joan/IndriTests/libindri_jni.so");
	PonteExpander qe = new PonteExpander(qenv, map);

	ScoredExtentResult[] results2 = qenv.runQuery(myQuery, 100);
	String queryExpanded = qe.expand(myQuery, results2);

	println("Running: " + myQuery);
	results = qe.runExpandedQuery(myQuery, 100);
	results = qenv.runQuery(queryExpanded, 100);

	return constructVideosMap(results, qenv, trecTest);

}

public static Map<String, Double> getVideosRM(String myQuery, LANGUAGE language, Configuration.TEST trecTest) throws Exception
{
	String indexPath = null;
	switch (language)
	{
		case ENGLISH:
			indexPath = "/scratch/joan/DATASET IMAGES WIKIPEDIA/index";
			break;
		case GERMAN:
			indexPath = "/scratch/joan/DATASET IMAGES WIKIPEDIA/GermanIndex";
			break;
		default:
			println("No index for " + language + ".");
	}
	QueryEnvironment qenv = new QueryEnvironment();
	String myIndex = indexPath;

	ScoredExtentResult[] results;

	qenv.addIndex(myIndex);

	RMExpander rm = new RMExpander(qenv, IndriModule.createFeedbackMap());

	ScoredExtentResult[] results2 = qenv.runQuery(myQuery, 100);
	String queryExpanded = rm.expand(myQuery, results2);

	println("Running: " + myQuery);
	results = rm.runExpandedQuery(myQuery, 100);
	results = qenv.runQuery(queryExpanded, 100);

	return constructVideosMap(results, qenv, trecTest);

}

static public double[] run(File trecEvalFile, File qrelFile, File resultFile, Writer dataOutWriter, Writer evalOutWriter) throws IOException
{
	double[] array = new double[6];
	Process p = Runtime.getRuntime().exec(trecEvalFile + " -q -c " + qrelFile + " " + resultFile);
	println("Run treceval:" + trecEvalFile + " -q -c " + qrelFile + " " + resultFile);
	BufferedReader in = null;
	PrintWriter dataOut = null;
	PrintWriter evalOut = null;

	try
	{
		in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		if (dataOutWriter != null)
		{
			dataOut = new PrintWriter(dataOutWriter);
		}
		if (evalOutWriter != null)
		{
			evalOut = new PrintWriter(evalOutWriter);
		}
		final String format1 = "    at ";
		final String format2 = "  At   ";
		final String format3 = "                  ";
		int count = 0;
		for (String line; (line = in.readLine()) != null;)
		{
			if (evalOut != null)
			{
				evalOut.println(line);
			}
			if (line.startsWith(format1))
			{
				line = line.substring(format1.length());
				if (dataOut != null)
				{
					dataOut.println(line);
				}
			}
			if (line.startsWith(format2))
			{
				line = line.substring(format2.length());
				int h = line.indexOf(":");
				line = line.substring(h + 1).trim();
				array[count++] = Double.parseDouble(line);
			}
			if (line.startsWith(format3))
			{
				line = line.substring(format3.length());
				array[5] = Double.parseDouble(line);
			}
		}
	} catch (NumberFormatException e)
	{
		println("Exception: " + e.getMessage());
	} catch (IOException e)
	{
		println("Exception: " + e.getMessage());
		throw e;
	} finally
	{
		if (in != null)
		{
			in.close();
		}
		if (dataOut != null)
		{
			dataOut.close();
		}
		if (evalOut != null)
		{
			evalOut.close();
		}
	}
	return array;
}

public static class SearchEngineParameters
{

//String myQuery;
LANGUAGE language;
PSEUDO_RELEVANCE_FEEDBACK prf;
String indexPath;
Configuration.TEST trecTest;
Query query;
String indriOperation;//= param.readString("combine", "INDRI_OPERATION");
String windowsSize;// = param.readString("", "WINDOWS_SIZE");
Boolean base64;// = param.readBoolean(true, "INDRI_BASE64");
Double weightOriginalQuery;// = param.readDouble(0.0, "WEIGHT_ORIGINAL_QUERY");
QUERY_TERM_WEIGHT_AGREGATION qtwa;// = IndriModule.QUERY_TERM_WEIGHT_AGREGATION.valueOf(param.readString("SUM_WEIGHT", "QUERY_TERM_WEIGHT_AGREGATION"));
//PrintWriter trecevalFile;

public SearchEngineParameters(Configuration configuration) throws FileNotFoundException, UnsupportedEncodingException
{
	Parameters parameters = configuration.getParameters();

	this.qtwa = QUERY_TERM_WEIGHT_AGREGATION.valueOf(parameters.readString("SUM_WEIGHT", "QUERY_TERM_WEIGHT_AGREGATION"));
	this.windowsSize = parameters.readString("", "WINDOWS_SIZE");
	this.weightOriginalQuery = parameters.readDouble(0.0, "WEIGHT_ORIGINAL_QUERY");
	this.indriOperation = parameters.readString("combine", "INDRI_OPERATION");
	this.prf = PSEUDO_RELEVANCE_FEEDBACK.valueOf(parameters.readString("NONE", "PSEUDO_RELEVANCE_FEEDBACK"));
	this.trecTest = TEST.valueOf(parameters.readString("WEBCLEF", "TEST"));
	//this.trecevalFile = FileManagement.Writer.getWriter(parameters.readString("QueryResultsTRECEVAL.txt", "TREC_EVAL_FILE"));
	this.indexPath = configuration.getResultCollectionPath();
}

/*public void setQuery(Query query)
 {
 this.query = query;
 }
 */
/*public void setMyQuery(String myQuery)
 {
 this.myQuery = myQuery;
 }
 */
private QUERY_TERM_WEIGHT_AGREGATION getQTWA()
{
	return qtwa;
}

private String getWindowSize()
{
	return windowsSize;
}

private Double getWeightOriginalQuery()
{
	return weightOriginalQuery;
}

private String getIndriOperation()
{
	return indriOperation;
}

public void setWindowsSize(String windowsSize)
{
	this.windowsSize = windowsSize;
}

public void setWeightOriginalQuery(Double weightOriginalQuery)
{
	this.weightOriginalQuery = weightOriginalQuery;
}

public void setIndriOperation(String indriOperation)
{
	this.indriOperation = indriOperation;
}

private void setTest(TEST trecTest)
{
	this.trecTest = trecTest;
}

//private void setTrecEvalFile(PrintWriter trecevalFile)
//{
//	this.trecevalFile = trecevalFile;
//}
private void setIndexPath(String resultCollectionPath)
{
	this.indexPath = resultCollectionPath;
}
}
}
