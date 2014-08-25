/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import DEFS.Definitions;
import Strings.StringUtils;
import Strings.Tokenize.Tokenizer;
import Structures.MapUtils;
import configurations.Configuration;
import edu.upc.dama.utils.objects.UtilsMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.IndriModule;

/**
 *
 * @author joan
 */
public class Query implements Comparable<Query>

{

private String id;
private String input;
private String context;

private Set<Token> inputTokens;
private Set<Token> contextTokens;

private List<String> easyExpandedInput;
private List<String> easyExpandedContext;

private Set<Token> easyExpandedInputTokens;

private Paths paths;
private Paths selectedPaths;

//private Map<Set<Long>, Map<Long, Double>> communities;
//private Map<Long, Double> community;
private Map<Set<Long>, Map<Entity, Double>> communities;
private Map<Entity, Double> community;

private String originalIndriQuery;
private Set<String> lexicalIndriQuery;
private String topologicalIndriQuery;
private String configurableIndriQueryLine;

private Map<String, Double> result;
private String trecOutput;
private StringBuilder plainTopologicalQuery;
private Map<String, Set<String>> entitySynonyms;

//private Set<String> inputEntities;
//private Map<String, Map<Long, Double>> entityCommunities;
private Set<Entity> inputEntities;
private Map<Entity, Map<Entity, Double>> entityCommunities;
private final HashSet<Query> subqueries;
private Set<Entity> contextEntities;
private Map<Entity, Double> communityAfterRemoval;
private Set<String> partialResultsFiles;
private Map<Entity, Map<Entity, Double>> entityCommunitiesCategoryFiltered;
	private String runIndriQuery;

public Query(String id, String input, String context, Definitions.LANGUAGE language) throws Exception
{
	this.id = id;
	this.input = input;
	this.context = context;

	this.setInputTokens(Token.getTokens(Tokenizer.getTokenizedList(input, language, true)));
	this.setContextTokens(Token.getTokens(Tokenizer.getTokenizedList(context, language, true)));

	this.easyExpandedInput = new ArrayList<>();
	this.easyExpandedInput.add(input);
	this.easyExpandedContext = new ArrayList<>();
	this.easyExpandedContext.add(context);
	this.lexicalIndriQuery = new HashSet<>();

	this.subqueries = new HashSet<>();
}

/*public Set<String> getTokens()
 {
 return tokens;
 }*/
public void addSubquery(Query query)
{
	subqueries.add(query);
}

static public void serialize(Configuration configuration, Set<Query> queries) throws ParserConfigurationException, TransformerException
{

	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	// root elements
	Document doc = docBuilder.newDocument();
	Element rootElement = doc.createElement("queries");
	doc.appendChild(rootElement);

	ArrayList<Query> sortedQueries = new ArrayList<>(queries);
	java.util.Collections.sort(sortedQueries);

	StringBuilder trecEvalResults = new StringBuilder();

	for (int i = 0; i < sortedQueries.size(); i++)
	{
		Query query = sortedQueries.get(i);
		// staff elements
		Element queryXML = doc.createElement("query");
		rootElement.appendChild(queryXML);

		Element id = doc.createElement("id");
		id.appendChild(doc.createTextNode(query.id));
		queryXML.appendChild(id);

		Element input = doc.createElement("input");
		input.appendChild(doc.createTextNode(query.input));
		queryXML.appendChild(input);

		Element context = doc.createElement("context");
		context.appendChild(doc.createTextNode(query.context));
		queryXML.appendChild(context);

		Element inputTokens = doc.createElement("inputTokens");
		try
		{
			inputTokens.appendChild(doc.createTextNode(tokensToString(query.inputTokens)));
		} catch (NullPointerException e)
		{
			inputTokens.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(inputTokens);

		Element contextTokens = doc.createElement("contextTokens");
		try
		{
			contextTokens.appendChild(doc.createTextNode(tokensToString(query.contextTokens)));
		} catch (NullPointerException e)
		{
			contextTokens.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(contextTokens);

		Element easyExpandedInput = doc.createElement("easyExpandedInput");
		try
		{
			easyExpandedInput.appendChild(doc.createTextNode(query.easyExpandedInput.toString()));
		} catch (NullPointerException e)
		{
			easyExpandedInput.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(easyExpandedInput);

		Element easyExpandedContext = doc.createElement("easyExpandedContext");
		try
		{
			easyExpandedContext.appendChild(doc.createTextNode(query.easyExpandedContext.toString()));
		} catch (NullPointerException e)
		{
			easyExpandedContext.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(easyExpandedContext);

		Element easyExpandedInputTokens = doc.createElement("easyExpandedInputTokens");
		try
		{
			easyExpandedInputTokens.appendChild(doc.createTextNode(tokensToString(query.easyExpandedInputTokens)));
		} catch (NullPointerException e)
		{
			easyExpandedInputTokens.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(easyExpandedInputTokens);

		Element paths = doc.createElement("paths");
		try
		{
			paths.appendChild(doc.createTextNode(pathsToString(query.paths)));
		} catch (NullPointerException e)
		{
			paths.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(paths);

		Element plainEntities = doc.createElement("plainEntities");
		try
		{
			plainEntities.appendChild(doc.createTextNode(query.inputEntities.toString()));
		} catch (NullPointerException e)
		{
			plainEntities.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(plainEntities);

		Element entities = doc.createElement("entityCommunities");
		try
		{
			entities.appendChild(doc.createTextNode(entityCommunitiesToString(query.entityCommunities)));
		} catch (NullPointerException e)
		{
			entities.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(entities);

		Element entitiesCategoryFiltered = doc.createElement("entityCommunitiesCategoryFiltered");
		try
		{
			entitiesCategoryFiltered.appendChild(doc.createTextNode(entityCommunitiesToString(query.entityCommunitiesCategoryFiltered)));
		} catch (NullPointerException e)
		{
			entitiesCategoryFiltered.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(entitiesCategoryFiltered);

		Element plainEntityCommunities = doc.createElement("plainEntityCommunities");
		try
		{
			plainEntityCommunities.appendChild(doc.createTextNode(entityCommunitiesToStringPlain(query.entityCommunities)));
		} catch (NullPointerException e)
		{
			plainEntityCommunities.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(plainEntityCommunities);

		Element plainEntityCommunitiesCategoryFiltered = doc.createElement("plainEntityCommunitiesCategoryFiltered");
		try
		{
			plainEntityCommunitiesCategoryFiltered.appendChild(doc.createTextNode(entityCommunitiesToStringPlain(query.entityCommunitiesCategoryFiltered)));
		} catch (NullPointerException e)
		{
			plainEntityCommunitiesCategoryFiltered.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(plainEntityCommunitiesCategoryFiltered);

		Element partialResultsFiles = doc.createElement("partialResultsFiles");
		try
		{
			partialResultsFiles.appendChild(doc.createTextNode(partialResultsFileToString(query.partialResultsFiles)));
		} catch (NullPointerException e)
		{
			partialResultsFiles.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(partialResultsFiles);

		Element plainTopologicalQuery = doc.createElement("plainTopologicalQuery");
		try
		{
			plainTopologicalQuery.appendChild(doc.createTextNode(query.plainTopologicalQuery.toString()));
		} catch (NullPointerException e)
		{
			plainTopologicalQuery.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(plainTopologicalQuery);

		Element selectedPaths = doc.createElement("selectedPaths");
		try
		{
			selectedPaths.appendChild(doc.createTextNode(pathsToString(query.selectedPaths)));
		} catch (NullPointerException e)
		{
			selectedPaths.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(selectedPaths);

		Element communities = doc.createElement("communities");
		try
		{
			if (!query.communities.isEmpty())
			{
				communities.appendChild(doc.createTextNode(pathAndCommunitiesToString(query.communities)));
			} else
			{
				communities.appendChild(doc.createTextNode("null"));
			}
		} catch (NullPointerException e)
		{
			communities.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(communities);

		Element community = doc.createElement("community");
		try
		{
			community.appendChild(doc.createTextNode(communityToString(query.community).toString()));
		} catch (NullPointerException e)
		{
			community.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(community);

		Element communityPlain = doc.createElement("communityPlain");
		try
		{
			communityPlain.appendChild(doc.createTextNode(communityToStringPlain(query.community).toString()));
		} catch (NullPointerException e)
		{
			communityPlain.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(communityPlain);

		Element communityAfterRemoval = doc.createElement("communityAfterRemoval");
		try
		{
			communityAfterRemoval.appendChild(doc.createTextNode(communityToString(query.communityAfterRemoval).toString()));
		} catch (NullPointerException e)
		{
			communityAfterRemoval.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(communityAfterRemoval);

		Element communityAfterRemovalPlain = doc.createElement("communityAfterRemovalPlain");
		try
		{
			communityAfterRemovalPlain.appendChild(doc.createTextNode(communityToStringPlain(query.communityAfterRemoval).toString()));
		} catch (NullPointerException e)
		{
			communityAfterRemovalPlain.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(communityAfterRemovalPlain);

		Element originalIndriQuery = doc.createElement("originalIndriQuery");
		try
		{
			originalIndriQuery.appendChild(doc.createTextNode(query.getOriginalIndriQuery()));
		} catch (NullPointerException e)
		{
			originalIndriQuery.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(originalIndriQuery);

		Element lexicalIndriQuery = doc.createElement("lexicalIndriQuery");
		try
		{
			lexicalIndriQuery.appendChild(doc.createTextNode(query.lexicalIndriQuery.toString().trim()));
		} catch (NullPointerException e)
		{
			lexicalIndriQuery.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(lexicalIndriQuery);

		Element topologicalIndriQuery = doc.createElement("topologicalIndriQuery");
		try
		{
			if (!query.topologicalIndriQuery.isEmpty())
			{
				topologicalIndriQuery.appendChild(doc.createTextNode(query.topologicalIndriQuery));
			} else
			{
				topologicalIndriQuery.appendChild(doc.createTextNode("null"));
			}
		} catch (NullPointerException e)
		{
			topologicalIndriQuery.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(topologicalIndriQuery);

		Element configurableIndriQueryLine = doc.createElement("configurableIndriQueryLine");
		try
		{

			if (!query.configurableIndriQueryLine.isEmpty())
			{
				configurableIndriQueryLine.appendChild(doc.createTextNode(query.configurableIndriQueryLine));
			} else
			{
				configurableIndriQueryLine.appendChild(doc.createTextNode("null"));
			}

		} catch (NullPointerException e)
		{
			configurableIndriQueryLine.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(configurableIndriQueryLine);

		Element result = doc.createElement("result");
		try
		{
			result.appendChild(doc.createTextNode(query.result.toString()));
		} catch (NullPointerException e)
		{
			result.appendChild(doc.createTextNode("null"));
		}
		queryXML.appendChild(result);

		/*
		 Element trecOutput = doc.createElement("trecOutput");
		
		 try
		 {
		 if (!query.trecOutput.isEmpty())
		 {

		 trecOutput.appendChild(doc.createTextNode(query.trecOutput));
		 } else
		 {
		 trecOutput.appendChild(doc.createTextNode("null"));

		 }
		 } catch (NullPointerException e)
		 {
		 trecOutput.appendChild(doc.createTextNode("null"));
		 }
		 queryXML.appendChild(trecOutput);
		 */
		trecEvalResults.append(query.trecOutput);

	}

	Element trecOutput = doc.createElement("trecOutput");
	trecOutput.appendChild(doc.createTextNode(trecEvalResults.toString()));
	rootElement.appendChild(trecOutput);

	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	DOMSource source = new DOMSource(doc);
	StreamResult result = new StreamResult(new File(configuration.getResultQueryFile()));

	// Output to console for testing
	// StreamResult result = new StreamResult(System.out);
	transformer.transform(source, result);

}

public static List<String> deserializeTrecOutput(Configuration configuration) throws ParserConfigurationException, SAXException, IOException
{
	File fXmlFile = new File(configuration.getReadableResultQueryFile());
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	NodeList nList = doc.getElementsByTagName("queries");

	Set<Query> queries = new HashSet<>();
	for (int temp = 0; temp < nList.getLength(); temp++)
	{

		Node nNode = nList.item(temp);

		if (nNode.getNodeType() == Node.ELEMENT_NODE)
		{

			Element eElement = (Element) nNode;

			String id = eElement.getElementsByTagName("trecOutput").item(0).getTextContent();
			String[] split = id.split(";");
			List<String> asList = Arrays.asList(split);
			if (configuration.getQueryId() == null)
				return asList;
			else
				return removeUnnecessaryResults(configuration.getQueryId(), asList);
		}
	}
	return new ArrayList<String>();

}

static public Set<Query> deserialize(Configuration configuration) throws ParserConfigurationException, TransformerException, SAXException, IOException, Exception
{
	File fXmlFile = new File(configuration.getReadableResultQueryFile());
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	NodeList nList = doc.getElementsByTagName("query");

	Set<Query> queries = new HashSet<>();
	for (int temp = 0; temp < nList.getLength(); temp++)
	{

		Node nNode = nList.item(temp);

		if (nNode.getNodeType() == Node.ELEMENT_NODE)
		{

			Element eElement = (Element) nNode;

			String id = eElement.getElementsByTagName("id").item(0).getTextContent();
			String input = eElement.getElementsByTagName("input").item(0).getTextContent();
			String context = eElement.getElementsByTagName("context").item(0).getTextContent();

			Query query = new Query(id, input, context, configuration.getLanguage());
			println("Deserializing query " + query.id);
			println("	reading input tokens...");
			query.inputTokens = readTokensLine(eElement.getElementsByTagName("inputTokens").item(0).getTextContent());
			println("	reading context tokens...");
			query.contextTokens = readTokensLine(eElement.getElementsByTagName("contextTokens").item(0).getTextContent());

			query.easyExpandedInput = readStringList(eElement.getElementsByTagName("easyExpandedInput").item(0).getTextContent());
			query.easyExpandedContext = readStringList(eElement.getElementsByTagName("easyExpandedContext").item(0).getTextContent());

			println("	reading easy expanded input tokens...");
			query.easyExpandedInputTokens = readTokensLine(eElement.getElementsByTagName("easyExpandedInputTokens").item(0).getTextContent());

			query.paths = readPathsLine(eElement.getElementsByTagName("paths").item(0).getTextContent());
			query.selectedPaths = readPathsLine(eElement.getElementsByTagName("selectedPaths").item(0).getTextContent());

			query.entityCommunities = readEntityCommunities(eElement.getElementsByTagName("entityCommunities").item(0).getTextContent());
			query.entityCommunitiesCategoryFiltered = readEntityCommunities(eElement.getElementsByTagName("entityCommunitiesCategoryFiltered").item(0).getTextContent());
			query.communities = readCommunities(eElement.getElementsByTagName("communities").item(0).getTextContent());
			query.community = readCommunity(eElement.getElementsByTagName("community").item(0).getTextContent());
			query.originalIndriQuery = readStringLine(eElement.getElementsByTagName("originalIndriQuery").item(0).getTextContent());

			query.lexicalIndriQuery = readHashSet(eElement.getElementsByTagName("lexicalIndriQuery").item(0).getTextContent());
			query.topologicalIndriQuery = readStringLine(eElement.getElementsByTagName("topologicalIndriQuery").item(0).getTextContent());
			query.configurableIndriQueryLine = readStringLine(eElement.getElementsByTagName("configurableIndriQueryLine").item(0).getTextContent());
			query.result = readResults(eElement.getElementsByTagName("result").item(0).getTextContent());

			if (configuration.getQueryId() == null || query.id.equals(configuration.getQueryId()))
				queries.add(query);
		}
	}
	return queries;
}

private static StringBuilder communityToString(Map<Entity, Double> community)
{
	boolean firstElementCommunity = true;
	StringBuilder sb = new StringBuilder();
	for (Map.Entry<Entity, Double> e2 : community.entrySet())
	{
		Long id = e2.getKey().getId();
		Double w = e2.getValue();
		if (!firstElementCommunity)
		{
			sb.append(",");
		}
		sb.append(id).append("=").append(w);
		firstElementCommunity = false;
	}
	return sb;
}

private static StringBuilder communityToStringPlain(Map<Entity, Double> community)
{
	boolean firstElementCommunity = true;
	StringBuilder sb = new StringBuilder();
	for (Map.Entry<Entity, Double> e2 : community.entrySet())
	{
		String id = "\"" + e2.getKey().getName() + "\"";
		Double w = e2.getValue();
		if (!firstElementCommunity)
		{
			sb.append(",");
		}
		sb.append(id).append("=").append(w);
		firstElementCommunity = false;
	}
	return sb;
}

private static Map<Entity, Double> readCommunity(String text)
{
	Map<Entity, Double> result = new HashMap<>();
	if ("null".equals(text) || "".equals(text))
		return result;

	String[] pairsEntityDouble = text.split(",");
	for (String pairEntityDouble : pairsEntityDouble)
	{
		String[] pairEntityDoubleVector = pairEntityDouble.split("=");
		Entity e = new Entity(Long.valueOf(pairEntityDoubleVector[0]));
		Double w = Double.valueOf(pairEntityDoubleVector[1]);
		result.put(e, w);
	}
	return result;
}

public String getId()
{
	return id;
}

public Paths getPaths()
{
	return paths;
}

//Line format:
//id;input;context[;easyExpandedInput;easyExpandedContext;lexicalExpansionFeatures;paths;selectedPaths;communities;selectedCommunity;IndriQuery]
static public Query readQueryLine(Configuration config, String queryLine) throws Exception
{
	String[] queryParts = queryLine.split(";");
	String id = queryParts[0];
	String input = StringUtils.cleanString(queryParts[1]);
	String context = "";
	try
	{
		context = queryParts[2].trim().isEmpty() ? input : StringUtils.cleanString(queryParts[2]);
	} catch (Exception e)
	{
		println("[ERROR]: " + queryLine);
	}
	context = context.isEmpty() ? input : context;

	Query q = new Query(id, input, context, config.getLanguage());

	return q;
}

private static Set<Set<Long>> readCollectionIds(String string)
{
	Set queryPaths = new HashSet<>();
	String[] paths = string.split(",");
	for (String path : paths)
	{
		Set<Long> queryPath = new HashSet<>();
		String[] ids = path.split("-");
		for (String nodeId : ids)
		{
			queryPath.add(Long.valueOf(nodeId));
		}
		queryPaths.add(queryPath);
	}
	return queryPaths;
}

@Override
public int compareTo(Query o)
{
	int id1 = Integer.valueOf(this.id);
	int id2 = Integer.valueOf(o.id);

	/*if (id1 - id2 < 0)
	 {
	 println(this.id + " comes before " + o.id);
	 }
	 if (id1 - id2 > 0)
	 {
	 println(this.id + " comes after " + o.id);
	 }*/
	return id1 - id2;

}

@Override
public String toString()
{
	return id + ";" + input + ";" + context;
}

public void setCommunities(Map<Set<Long>, Map<Entity, Double>> communities)
{
	this.communities = communities;
}

public Map<Set<Long>, Map<Entity, Double>> getCommunities()
{
	return communities;
}

public void setCommunity(Map<Entity, Double> community)
{
	this.community = community;
}

public Map<Entity, Double> getCommunity()
{
	return community;
}

public String getInput()
{
	return input;
}

public String getContext()
{
	return context;
}

public void setEasyExpandedInput(List<String> easyExpandedInput)
{
	this.easyExpandedInput = easyExpandedInput;
}

public void setEasyExpandedContext(List<String> easyExpandedContext)
{
	this.easyExpandedContext = easyExpandedContext;
}

public void setLexicalExpansionFeatures(Set<String> lexicalExpansionFeatures)
{
	this.lexicalIndriQuery = lexicalExpansionFeatures;
}

public Set<Token> getInputTokens()
{
	return inputTokens;
}

public void setInputTokens(Set<Token> tokens)
{
	this.inputTokens = tokens;

	for (Token t : this.inputTokens)
	{
		//if(t.getWeight()==null) 
		t.setWeight(1 / Math.sqrt(this.inputTokens.size()));
	}
//	inputTokensModule = 1.0 / Math.sqrt(this.inputTokens.size());
}

private void setContextTokens(Set<Token> tokens)
{
	this.contextTokens = tokens;
}

public void setSelectedPaths(Set<Set<Long>> selectedPaths)
{
	this.selectedPaths = new Paths(selectedPaths);
}

public void setSelectedPaths(Paths selectedPaths)
{
	this.selectedPaths = selectedPaths;
}

public Paths getSelectedPaths()
{
	return selectedPaths;
}

public void setTopologicalExpansionFeatures(String topologicalExpansionFeatures)
{
	this.topologicalIndriQuery = topologicalExpansionFeatures;
}

public String getTopologicalIndriQuery()
{
	return topologicalIndriQuery;
}

public String getLexicalIndriQuery()
{
	StringBuilder s = new StringBuilder();
	for (String expansionFeature : lexicalIndriQuery)
	{
		s.append(expansionFeature);
	}
	return s.toString();
}

public PrintWriter getWriterExpansionQuery()
{
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

public Set<Token> getContextTokens()
{
	return contextTokens;
}

public List<String> getEasyExpandedInput()
{
	return easyExpandedInput;
}

public List<String> getEasyExpandedContext()
{
	return easyExpandedContext;
}

public void setPaths(Set<Set<Long>> paths)
{
	this.paths = new Paths(paths);
}

public Set<Token> getEasyExpandedInputTokens(Configuration configuration) throws Exception
{
	Set<Token> easyExpandedInputTokens = new HashSet<>(inputTokens);
	for (String string : easyExpandedInput)
	{

		easyExpandedInputTokens.addAll(Token.tokenize(string, configuration.getLanguage(), true));
	}
	this.easyExpandedInputTokens = easyExpandedInputTokens;
	return easyExpandedInputTokens;
}

public Set<String> getTokenNames()
{
	Set<String> names = new HashSet<String>();
	{
		for (Token t : getInputTokens())
		{
			names.add(t.getName());
		}
		for (Token t : getContextTokens())
		{
			names.add(t.getName());
		}
		for (Token t : easyExpandedInputTokens)
		{
			names.add(t.getName());
		}
	}
	return names;
}

public void setResult(Map<String, Double> result)
{
	this.result = UtilsMap.sortByValue(result);
}

public Map<String, Double> getResult()
{

	return MapUtils.sortByValue( result);
}

public void setOriginalIndriQuery(String originalIndriQuery)
{
	this.originalIndriQuery = originalIndriQuery;
}

public void setTopologicalIndriQuery(String topologicalIndriQuery)
{
	this.topologicalIndriQuery = topologicalIndriQuery;
}

public void setConfigurableIndriQueryLine(String configurableIndriQueryLine)
{
	this.configurableIndriQueryLine = configurableIndriQueryLine;
}

	public String getConfigurableIndriQueryLine()
	{
		return configurableIndriQueryLine;
	}



static public void serialize(Configuration configuration, Query query) throws ParserConfigurationException, TransformerException
{
	Set<Query> queries = new HashSet<>();
	queries.add(query);
	serialize(configuration, queries);
}

public String getOriginalIndriQuery()
{
	return IndriModule.getCombineBase64Query(input);
}

static protected Map<String, String> convertToStringToHashMap(String text)
{
	Map<String, String> data = new HashMap<>();
	Pattern p = Pattern.compile("[\\{\\}\\=\\, ]++");
	String[] split = p.split(text);
	for (int i = 1; i + 2 <= split.length; i += 2)
	{
		data.put(split[i], split[i + 1]);
	}
	return data;
}

private static Set<Token> readTokensLine(String text)
{

	Set<Token> tokens = new HashSet<>();
	if (text.equals("null") || "".equals(text))
	{
		return tokens;
	}
	//Pattern p = Pattern.compile("[\\[\\[,\\]*+\\]]") ;
	/*Pattern p = Pattern.compile("[\\[\\]\\[\\]\\-\\,]++");
	 String[] split = p.split(text);
	 for (int i = 0; i < split.length; i += 4)
	 {
	 Long id = new Long(split[i + 2]);
	 String name = split[i + 1];
	 Double weight = new Double(split[i + 3]);
	 Token t = new Token(id, name, weight);
	 tokens.add(t);
	 }*/
	//println(tokens);

	String[] tokensSplit = text.split(";");
	for (String token : tokensSplit)
	{
		try
		{
			String[] tokenAttributes = token.split(",");
			String name = tokenAttributes[0];
			Long id = Long.valueOf(tokenAttributes[1]);
			Double weight = Double.valueOf(tokenAttributes[2]);

			Token t = new Token(name, weight);
			tokens.add(t);
		} catch (Exception e)
		{
			println("[ERROR] " + text);
			return null;
		}

	}

	return tokens;

}

public static void main(String[] args) throws FileNotFoundException, IOException, Exception
{
	//String s = "[[colored-2164390,0.5773502691896258], [beetles-1996115,0.5773502691896258], [volkswagen-1964552,0.5773502691896258]]";
	String s = "[vw beetle, volkswagen beetles, Volkswagen coleoptera]";
	s = "Volkswagen beetles any other color for example red blue or";
	s = "[[260070, 266537], [260070, 266536], [342621, 334168], [342405, 353350], [307040, 260070, 266484], [260070, 337896], [342405, 337896], [353425, 260070], [334168, 260070], [343865, 260070], [306575, 337896], [333677, 260070], [333678, 342597], [333678, 260070], [260070, 339870], [344158, 260070], [333678, 306575], [361621, 260070, 335789], [266536, 346202], [342405, 343319], [361622, 260070, 335789], [307040, 334168], [266536, 346201], [306575, 342402], [260070, 357611, 266484], [361620, 260070, 335789], [346201, 346202], [306575, 342403], [306575, 341576], [358766, 260070], [306575, 342405], [342597, 342405], [343319, 260070], [354110, 353425], [341576, 342405], [342597, 341576], [353350, 260070], [339871, 342402], [342403, 339871], [306575, 339871], [353243, 260070], [260070, 357611], [260070, 266537, 266484], [260070, 335789], [333678, 339871], [334509, 357611], [353350, 339871], [307040, 342621], [305470, 358766], [342597, 343319], [306575, 260070], [355718, 260070], [306575, 342597], [333678, 342402], [333678, 342403], [333678, 342405], [354110, 260070], [305470, 260070], [333677, 351808, 260070], [351807, 333677, 260070], [307038, 260070], [342405, 342402], [334168, 342403], [333677, 351810, 260070], [333677, 351809, 260070], [307040, 260070], [342403, 342402]]";
	readPathsLine(s);
}

private static List<String> readStringList(String text)
{
	List<String> asList = new ArrayList<>();
	if (text.equals("null"))
	{
		return asList;
	}

	String[] split = text.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

	for (String s : split)
	{
		asList.add(s.trim());
	}
	return asList;
}

private static Paths readPathsLine(String text)
{
	Paths paths = new Paths();
	if (text.equals("null") || text.isEmpty())
	{
		return paths;
	}

	String[] pathss = text.split(";");
	for (String s : pathss)
	{
		Set<Long> ids = new HashSet<>();

		String[] pathString = s.split(":");
		int pathId = Integer.valueOf(pathString[0]);

		String[] idsString = pathString[1].split(",");
		for (String s2 : idsString)
		{
			ids.add(Long.valueOf(s2));
		}
		Path path = new Path(pathId, ids);
//		println("Deserializing path: "+path);
		paths.add(path);
	}
	return paths;
}

private static String pathAndCommunitiesToString(Map<Set<Long>, Map<Entity, Double>> communities)
{
	StringBuilder sb = new StringBuilder();
	boolean firsCommunity = true;
	for (Map.Entry<Set<Long>, Map<Entity, Double>> e : communities.entrySet())
	{

		if (!firsCommunity)
		{
			sb.append(";");
		}
		Set<Long> path = e.getKey();
		boolean firstElementPath = true;
		for (Long id : path)
		{
			if (!firstElementPath)
			{
				sb.append("-");
			}
			sb.append(id);
			firstElementPath = false;
		}
		sb.append(",");
		Map<Entity, Double> community = e.getValue();
		sb.append(communityToString(community));
		firsCommunity = false;
	}

	return sb.toString();
}

private static Map<Set<Long>, Map<Entity, Double>> readCommunities(String text)
{
	Map<Set<Long>, Map<Entity, Double>> result = new HashMap<>();
	if (text.equals("null"))
	{
		return result;
	}

	String[] communities = text.split(";");
	for (String pathCommunity : communities)
	{
		String[] pathCommunitySplit = pathCommunity.split(",");
		HashSet<Long> path = new HashSet<>();
		for (String pathId : pathCommunitySplit[0].split("-"))
		{
			path.add(new Long(pathId));
		}
		Map<Entity, Double> community = new HashMap<>();
		for (int i = 1; i < pathCommunitySplit.length; i++)
		{
			String[] id_weight = pathCommunitySplit[i].split("=");
			Entity entity = new Entity(Long.valueOf(id_weight[0]));
			Double w = Double.valueOf(id_weight[1]);
			community.put(entity, w);
		}
		result.put(path, community);
	}
	//println("Communities reconstructed: " + communityToString(result));
	//println("Reconstructed communities is equal to original communities: " + equals(communityToString(result), text));
	return result;
}

private static boolean equals(String communityToString, String text)
{
	StringBuilder text1 = new StringBuilder();
	StringBuilder text2 = new StringBuilder();
	boolean result = true;
	for (int i = 0; i < communityToString.length(); i++)
	{
		text1.append(communityToString.charAt(i));
		text2.append(text.charAt(i));
		if (communityToString.charAt(i) != text.charAt(i))
		{
			result = false;
			break;
		}
	}
	println("communityToString: " + text1);
	println("text: " + text2);

	return result;
}

static private String readStringLine(String text)

{
	if (text.equals("null"))
	{
		return "";
	}

	return text;

}

private static Map<String, Double> readResults(String text)
{
	Map<String, Double> result = new HashMap<>();
	if (text.equals("null"))
	{
		return result;

	}

	text = text.replaceAll("\\{", "").replaceAll("\\}", "");
	String[] elements = text.split(",");
	String hold = null;
	for (String element : elements)
	{
		String[] resultID_weight = element.split("=");
		if (resultID_weight.length < 2)
		{
			hold = resultID_weight[0];
		} else
		{
			String resultID;
			if (hold != null)
			{
				resultID = hold + "," + resultID_weight[0];
				hold = null;
			} else
			{
				resultID = resultID_weight[0];
			}
			Double weight = new Double(resultID_weight[1]);
			result.put(resultID.trim(), weight);
		}
	}
	return result;
}

private static void println(Object string)
{
	System.out.println("	[Query.java]: " + string);
}

public void setTrecOutput(String trecOutput)
{
	this.trecOutput = trecOutput;
}

public static List<String> getTRECResults(Set<Query> queries)
{
	StringBuilder results = new StringBuilder();
	for (Query q : queries)
	{
		if (null != q.trecOutput)
			results.append(q.trecOutput);
	}
	List<String> asList = new ArrayList<>();
	if (!"".equals(results.toString()))
	{
		results.deleteCharAt(results.length() - 1);
		asList = Arrays.asList(results.toString().split(";"));
	}
	return asList;
}

static public boolean equals(Set<Query> querySet1, Set<Query> querySet2)
{
	Boolean res = true;

	Map<Long, Query> QueryMap1 = getQueryMap(querySet1);
	Map<Long, Query> QueryMap2 = getQueryMap(querySet2);

	for (Map.Entry<Long, Query> e : QueryMap1.entrySet())
	{
		Long id1 = e.getKey();

		Query query2 = e.getValue();
		Query query1 = QueryMap2.get(id1);
		if (query1 != null)
		{
			res &= Query.equals(query1, query2);
		} else
		{
			println("Query " + id1 + " is not in the second set");
		}
	}

	return res;

}

static public boolean equals(Query query1, Query query2)
{
	Boolean res = true;
	boolean resTmp;

	if (query1.communities != null && query2.communities != null)
	{
		resTmp = query1.communities.equals(query2.communities);//equals(query1.communities, query2.communities);
		if (resTmp == false)
		{
			res = false;
			println("Communities field is different");
			println("Original Queries: " + query1.communities);
			println("Deserialized Queries: " + query2.communities);
		}
	} else
	{

		res = false;
		println("Communities field is different");
		println("Original Queries: " + query1.communities);
		println("Deserialized Queries: " + query2.communities);
	}
	resTmp = query1.community.equals(query2.community);//compare(query1.community, query2.community);
	if (resTmp == false)
	{
		res = false;
		println("Community is different");
		println("Original Queries: " + query1.community);
		println("Deserialized Queries: " + query2.community);
	}
	resTmp = query1.configurableIndriQueryLine.equals(query2.configurableIndriQueryLine); //compare(query1.configurableIndriQueryLine, query2.configurableIndriQueryLine);
	if (resTmp == false)
	{
		res = false;
		println("ConfigurableIndriQueryLine is different");
		println("Original Queries: " + query1.configurableIndriQueryLine);
		println("Deserialized Queries: " + query2.configurableIndriQueryLine);
	}
	resTmp = query1.context.equals(query2.context);//compare(query1.context, query2.context);
	if (resTmp == false)
	{
		res = false;
		println("Context is different");
		println("Original Queries: " + query1.context);
		println("Deserialized Queries: " + query2.context);
	}
	resTmp = query1.contextTokens.equals(query2.contextTokens);// compare(query1.contextTokens, query2.context);
	if (resTmp == false)
	{
		res = false;
		println("ContextTones is different");
		println("Original Queries: " + query1.contextTokens);
		println("Deserialized Queries: " + query2.contextTokens);
	}
	resTmp = query1.easyExpandedContext.equals(query2.easyExpandedContext);// compare(query1.easyExpandedContext, query2.easyExpandedContext);
	if (resTmp == false)
	{
		res = false;
		println("EasyExpandedContext is different");
		println("Original Queries: " + query1.easyExpandedContext);
		println("Deserialized Queries: " + query2.easyExpandedContext);

	}
	resTmp = query1.easyExpandedInput.equals(query2.easyExpandedInput);//compare(query1.easyExpandedInput, query2.easyExpandedInput);
	if (resTmp == false)
	{
		res = false;
		println("EasyExpandedInput is different");
		println("Original Queries: " + query1.easyExpandedInput);
		println("Deserialized Queries: " + query2.easyExpandedInput);
	}
	resTmp = query1.easyExpandedInputTokens.equals(query2.easyExpandedInputTokens);//compare(query1.easyExpandedInputTokens, query2.easyExpandedInputTokens);
	if (resTmp == false)
	{
		res = false;
		println("EasyExpandedInputTokens are different");
		println("Original Queries: " + query1.easyExpandedInputTokens);
		println("Deserialized Queries: " + query2.easyExpandedInputTokens);
	}
	resTmp = query1.input.equals(query2.input);//compare(query1.input, query2.input);
	if (resTmp == false)
	{
		res = false;
		println("Input is different");
		println("Original Queries: " + query1.input);
		println("Deserialized Queries: " + query2.input);
	}
	resTmp = query1.inputTokens.equals(query2.inputTokens);//compare(query1.inputTokens, query2.inputTokens);
	if (resTmp == false)
	{
		res = false;
		println("inputTokens is different");
		println("Original Queries: " + query1.inputTokens);
		println("Deserialized Queries: " + query2.inputTokens);
	}
	resTmp = query1.lexicalIndriQuery.equals(query2.lexicalIndriQuery);// compare(query1.lexicalIndriQuery, query2.lexicalIndriQuery);
	if (resTmp == false && !query1.lexicalIndriQuery.isEmpty() && !query2.lexicalIndriQuery.isEmpty())
	{
		res = false;
		println("lexicalIndriQuery is different");
		println("Original Queries: " + query1.lexicalIndriQuery);
		println("Deserialized Queries: " + query2.lexicalIndriQuery);
	}
	resTmp = query1.originalIndriQuery.equals(query2.originalIndriQuery);// // compare(query1.originalIndriQuery, query2.originalIndriQuery);
	if (resTmp == false)
	{
		res = false;
		println("originalIndriQuery is different");
		println("Original Queries: " + query1.originalIndriQuery);
		println("Deserialized Queries: " + query2.originalIndriQuery);
	}
	resTmp = query1.paths.equals(query2.paths);//compare(query1.paths, query2.paths);
	if (resTmp == false)
	{
		res = false;
		println("paths is different");
		println("Original Queries: " + query1.paths);
		println("Deserialized Queries: " + query2.paths);
	}
	/*
	 resTmp = query1.result.equals(query2.result);//compare(query1.result, query2.result);
	 if (resTmp == false)
	 {
	 res = false;
	 println("result is different");
	 println("Original Queries: " + query1.result);
	 println("Deserialized Queries: " + query2.result);
	 }*/
	resTmp = query1.selectedPaths.equals(query2.selectedPaths);//compare(query1.selectedPaths, query2.selectedPaths);
	if (resTmp == false)
	{
		res = false;
		println("selectedPaths is different");
		println("Original Queries: " + query1.selectedPaths);
		println("Deserialized Queries: " + query2.selectedPaths);
	}
	resTmp = query1.topologicalIndriQuery.equals(query2.topologicalIndriQuery);//compare(query1.topologicalIndriQuery, query2.topologicalIndriQuery);
	if (resTmp == false)
	{
		res = false;
		println("topologicalIndriQuery is different");
		println("Original Queries: " + query1.topologicalIndriQuery);
		println("Deserialized Queries: " + query2.topologicalIndriQuery);
	}
	/*resTmp = query1.trecOutput.equals(query2.trecOutput);///compare(query1.trecOutput, query2.trecOutput);
	 if (resTmp == false)
		
	 {
	 res = false;
	 println("trecOutput is different");
	 }
	 */
	return res;

}

public boolean equals(Map<Set<Long>, Map<Long, Double>> map1, Map<Set<Long>, Map<Long, Double>> map2)
{
	return map1.equals(map2);
}

static private Map<Long, Query> getQueryMap(Set<Query> queriesSet1)
{
	Map<Long, Query> result = new HashMap<>();
	for (Query query : queriesSet1)
	{
		Long l = Long.valueOf(query.id);
		result.put(l, query);
	}
	return result;
}

private static String pathsToString(Paths paths)
{
	if (paths.isEmpty())
	{
		return "null";
	}
	StringBuilder sb = new StringBuilder();

	boolean firstPath = true;
	for (Path path : paths.getPaths())
	{
		if (!firstPath)
		{
			sb.append(";");
		}
		firstPath = false;
		boolean firstElement = true;
		int pathId = path.getId();
		sb.append(pathId).append(":");
		for (Long id : path.getIds())
		{
			if (!firstElement)
			{
				sb.append(",");
			}
			sb.append(id);
			firstElement = false;
		}

	}
	return sb.toString();
}

private static Set<String> readHashSet(String text)
{
	Set<String> asList = new HashSet<>();
	if (text.equals("null"))
	{
		return asList;
	}

	String[] split = text.replaceAll("\\[", "").replaceAll("\\]", "").trim().split(",");

	for (String s : split)
	{
		asList.add(s.trim());
	}
	return asList;
}

private static String tokensToString(Set<Token> tokens)
{
	StringBuilder sb = new StringBuilder();
	boolean first = true;
	for (Token t : tokens)
	{
		if (!first)
		{
			sb.append(";");
		}
		sb.append(t.getName().replaceAll(",", " ")).append(",").append(t.getId()).append(",").append(t.getWeight());
		first = false;
	}
	return sb.toString();
}

public void setInputEntities(Set<Entity> entities)
{
	this.inputEntities = entities;
}

public Set<Entity> getInputEntities()
{
	return inputEntities;
}

public void setEntityCommunities(Map<Entity, Map<Entity, Double>> communities)
{
	this.entityCommunities = communities;
}

public Map<Entity, Map<Entity, Double>> getEntityCommunities()
{
	return entityCommunities;
}

public void setPlainTopologicalQuery(StringBuilder plainQuery)
{

	if (this.plainTopologicalQuery == null)
	{
		this.plainTopologicalQuery = new StringBuilder();
	}
	plainTopologicalQuery.append("\n").append(plainQuery);

}

private static String entityCommunitiesToString(Map<Entity, Map<Entity, Double>> entityCommunities)
{
	if (entityCommunities == null || entityCommunities.isEmpty())
		return "null";
	StringBuilder sb = new StringBuilder();
	boolean firstEntity = true;
	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Entity entity = e.getKey();
		boolean entityAmbiguos = entity.isAmbiguous();
		String ambigusIndicator = "";
		if (entityAmbiguos)
			ambigusIndicator = "*";
		Map<Entity, Double> community = e.getValue();

		if (!firstEntity)
			sb.append("&");
		sb.append(entity.getId()).append(ambigusIndicator).append("-");
		boolean firstCommunityElement = true;
		for (Map.Entry<Entity, Double> e2 : community.entrySet())
		{
			if (!firstCommunityElement)
				sb.append(";");
			sb.append(e2.getKey().getId()).append(",").append(e2.getValue());
			firstCommunityElement = false;

		}
		firstEntity = false;
	}
	return sb.toString();
}

private static String entityCommunitiesToStringPlain(Map<Entity, Map<Entity, Double>> entityCommunities)
{
	if (entityCommunities == null || entityCommunities.isEmpty())
		return "null";
	StringBuilder sb = new StringBuilder();
	boolean firstEntity = true;
	for (Map.Entry<Entity, Map<Entity, Double>> e : entityCommunities.entrySet())
	{
		Entity entity = e.getKey();
		boolean entityAmbiguos = entity.isAmbiguous();
		String ambigusIndicator = "";
		if (entityAmbiguos)
			ambigusIndicator = "*";
		Map<Entity, Double> community = e.getValue();

		if (!firstEntity)
			sb.append("&");
		sb.append(entity.getName()).append(ambigusIndicator).append("-");
		boolean firstCommunityElement = true;
		for (Map.Entry<Entity, Double> e2 : community.entrySet())
		{
			if (!firstCommunityElement)
				sb.append(";");
			sb.append(e2.getKey().getName()).append(",").append(StringUtils.decimalFormat(e2.getValue(), 2));
			firstCommunityElement = false;

		}
		firstEntity = false;
	}
	return sb.toString();
}

private static Map<Entity, Map<Entity, Double>> readEntityCommunities(String text)
{
	if ("null".equals(text))
		return new HashMap<Entity, Map<Entity, Double>>();
	String[] entities = text.split("&");
	Map<Entity, Map<Entity, Double>> entityCommunities = new HashMap<>();
	for (String s : entities)
	{
		String[] split = s.split("-");
		//Long id = (new Article(split[0])).getId();
		String entityID = split[0];
		boolean ambiguous = entityID.contains("*");
		entityID = entityID.replace("*", "");
		Entity entity = new Entity(Long.valueOf(entityID));//
		if (ambiguous)
			entity.setAmbiguous(ambiguous);
		Map<Entity, Double> community = new HashMap<>();
		if (split.length == 2)
		{
			String[] values = split[1].split(";");
			for (String s2 : values)
			{
				String[] communityMembers = s2.split(",");
				String idCommunity = communityMembers[0];
				String weight = communityMembers[1];

				try{
					community.put(new Entity(Long.valueOf(idCommunity)), new Double(weight));
				}
				catch(Exception e)
				{
				println("[ERROR] : Entities are probably identified used a diferent Wipikedia Version");	
				}
			}
		}
		entityCommunities.put(entity, community);
	}
	return entityCommunities;
}

public void setEntitySynonyms(Map<String, Set<String>> entitySynonyms)
{
	this.entitySynonyms = entitySynonyms;
}

public void setContextEntities(Set<Entity> entities)
{
	this.contextEntities = entities;
}

public Set<Entity> getContextEntities()
{
	return contextEntities;
}

public void setInputEntities(Entity entity)
{
	Set<Entity> entities = new HashSet<>();
	entities.add(entity);
	this.inputEntities = entities;
}

public void setCommunityAfterRemoval(Map<Entity, Double> newCommunity)
{
	communityAfterRemoval = newCommunity;
}

public void setPartialResultsFiles(Set<String> partialResultsFiles)
{
	this.partialResultsFiles = partialResultsFiles;
}

public void setEntityCommunitiesCategoryFiltered(Map<Entity, Map<Entity, Double>> communitiesCategoryFiltered)
{
	this.entityCommunitiesCategoryFiltered = communitiesCategoryFiltered;
}

public Map<Entity, Map<Entity, Double>> getEntityCommunitiesCategoryFiltered()
{
	return entityCommunitiesCategoryFiltered;
}

public static TreeSet<Query> readQueries(Configuration configuration) throws FileNotFoundException, IOException, Exception
{
	String query_file = configuration.getQUERY_FILE();
	BufferedReader reader = FileManagement.ReadFiles.getReader(query_file);
	String queryLine;
	TreeSet<Query> queries = new TreeSet<>();
	while ((queryLine = reader.readLine()) != null)
	{
		if (queryLine.trim().charAt(0) != '#')
		{
			Query q = Query.readQueryLine(configuration, queryLine);
			if (configuration.getQueryId() == null || configuration.getQueryId().equals(q.getId()))
			{
				queries.add(q);
				println(q);
			}
		}
	}

	return queries;
}

private static String partialResultsFileToString(Set<String> partialResultsFiles)
{
	StringBuilder sb = new StringBuilder();
	boolean fe = true;
	for (String s : partialResultsFiles)
	{
		if (!fe)
			sb.append(",");
		sb.append(s);
		fe = false;
	}
	return sb.toString();
}

private static List<String> removeUnnecessaryResults(String queryId, List<String> asList)
{
	List<String> newList = new ArrayList<>();
	for (String s : asList)
	{
		if (s.startsWith(queryId + " 0 "))
			newList.add(s);
	}
	return newList;
}

	public void setRunIndriQuery(String runIndriQuery)
	{
		this.runIndriQuery = runIndriQuery;
	}

	public String getRunIndriQuery()
	{
		return runIndriQuery;
	}
	

}
