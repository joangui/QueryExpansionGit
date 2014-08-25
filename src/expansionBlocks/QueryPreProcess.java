/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expansionBlocks;

import Parameters.Parameters;
import Strings.StringUtils;
import Strings.Tokenize.Tokenizer;
import Structures.Pair;
import configurations.Configuration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Article;
import model.Query;
import utils.IndriModule;
import utils.TimeCalculator;
import utils.TokenFilter;

/**
 *
 * @author joan
 */
public class QueryPreProcess
{

public static void execute(Configuration configuration, Query query) throws Exception
{
	println("OBTAINING MATCHING DOCUMENTS WITH INDRI");

	String queryInput = query.getInput();
	println("Query Input: \"" + queryInput + "\"");

	String cleanedQueryInput = StringUtils.removeInBrackets(queryInput).replaceAll("\"", "").replace(" eg ", " ").replaceAll(":", "");
	println("Cleaned Query Input: \"" + cleanedQueryInput + "\"");
	//cleanedQueryInput = StringUtils.removeFilteredWords(cleanedQueryInput, configuration.getStopwordFilesPath());

	List<String> easyExpandedInput;
	Pair<List<String>, Set<String>> easyExtension = extendWithEasyExtension(configuration, cleanedQueryInput);
	easyExpandedInput = easyExtension.getFirst();
	Set<String> lexicalExpansionFeatures = easyExtension.getSecond();
	/*if (cleanedQueryInput.contains("-"))
	{
		Pair<List<String>, Set<String>> easyExtension2 = extendWithEasyExtension(configuration, cleanedQueryInput.replaceAll("-", " "));
		Set<String> rebuildInputs = rebuildOriginalStructure(cleanedQueryInput, easyExtension2);

		easyExpandedInput.addAll(easyExtension2.getFirst());
		easyExpandedInput = new ArrayList<>(new HashSet(easyExpandedInput));
		Set<String> lexicalExpansionFeatures2 = easyExtension2.getSecond();
		lexicalExpansionFeatures.addAll(lexicalExpansionFeatures2);
	}*/

	String queryContext = query.getContext();

	String cleanedQueryContext = StringUtils.removeInBrackets(queryContext).replaceAll("\"", "").replace(" eg ", " ").replaceAll(":", "");
	cleanedQueryContext = StringUtils.removeFilteredWords(cleanedQueryContext, configuration.getParameters().getStringList("FILTER_PATHS"));

	List<String> easyExpandedContext = new ArrayList<>();
	easyExpandedContext.add(cleanedQueryContext);

easyExpandedContext = new ArrayList<>();	
easyExpandedContext.add(query.getContext().toLowerCase().trim());

	

	//Retorna contextList i inputList
	query.setEasyExpandedInput(easyExpandedInput);
	query.setEasyExpandedContext(easyExpandedContext);
	query.setLexicalExpansionFeatures(lexicalExpansionFeatures);

}

static private Pair<List<String>, Set<String>> extendWithEasyExtension(Configuration configuration, String phrase) throws Exception
{
	println("Doing easy extension for \"" + phrase + "\"");
	int finalMatrixSize = 1;
	phrase = phrase.replace(",", "\\ ").replace("  ", " ");
	String[] inputVector = phrase.split(" ");
	StringBuilder inputVecorSB = new StringBuilder();
	for (String s : inputVector)
	{
		inputVecorSB.append(s).append(", ");
	}
	println(inputVecorSB);
	List<List<String>> inputMatrix = new ArrayList<>();
	Map<Integer, Set<String>> positionSynonyms = new HashMap<>();
	int p = 0;
	for (String s : inputVector)
	{
		Set<String> wordList = new HashSet<>();
		wordList.add(s.toLowerCase());
		List<String> tokenizedList = Tokenizer.getTokenizedList(s, configuration.getLanguage(), true);
		if (!tokenizedList.isEmpty())
		{
			String tokenWord = (s.toLowerCase().trim());
			List<String> tokenWordSet = new ArrayList<>();
			tokenWordSet.add(s.toLowerCase().trim());
			tokenWordSet = new ArrayList<>(TokenFilter.remove(configuration.getFilterFilePaths(), tokenWordSet));
			if (!tokenWordSet.isEmpty())
			{
				{
					String tokenName = tokenWord;//.getName().toLowerCase();
					wordList.add(tokenName);
					Set<String> synonymsThroughArticles = Article.getSynonymsThroughArticles(tokenWord);
					for (String t : synonymsThroughArticles)
					{
						if (t.length() > 1)
						{
							tokenName = t.replaceAll("\\.", "").toLowerCase();
							wordList.add(tokenName);
						}
					}
					println("Easy Extension words for \"" + tokenWord + "\" :" + wordList + " (" + wordList.size() + ")");
				}
			}
		}
		positionSynonyms.put(p, wordList);
		p++;
		finalMatrixSize *= wordList.size();
		inputMatrix.add(new ArrayList(wordList));
	}
	println("positionSynonums:" + positionSynonyms);
	Set<String> createSynonymsList = createSynonymsList(positionSynonyms);
	
	Set<String> lexicalExpansionFeatures = new HashSet<>();
	List<String> noBase64 = new ArrayList<>();
	Set<String> base64Inclouded = new HashSet<>();
	List<String> result = new ArrayList<>();

	for (String newQueryString : createSynonymsList)

	{
		String newQueryMod = newQueryString.replaceAll("[^A-Za-z0-9 ]", "*");
		Boolean addable = true;
		if (!newQueryString.equals(newQueryMod))
		{
			addable = false;
		}
		String[] split = newQueryString.trim().split("\\ ");

//		println("Check: \"" + newQueryString + "\"");
		if (addable
				&& !phrase.contains(newQueryString)
				&& !base64Inclouded.contains((newQueryString)) && IndriModule.hasResults(configuration,"#1" + "(" + (newQueryString) + ") "))
		{
//			System.out.print("SIMPLE_EASY_EXTENSION#w" + (split.length) + "(" + (newQueryString) + ") ");
			StringBuilder expansionFeature = new StringBuilder();
			expansionFeature.append("SIMPLE_EASY_EXTENSION#w").append(split.length).append("(").append(newQueryString).append(") ");
			lexicalExpansionFeatures.add(expansionFeature.toString().trim());
			//lexicalExpansionFeatures.append("SIMPLE_EASY_EXTENSION#w").append(split.length).append("(").append(newQueryString).append(") ");
			base64Inclouded.add((newQueryString));
			noBase64.add("#w" + (split.length) + "(" + (newQueryString) + ")");
		}
		result.add(newQueryString.toString().trim());
	}
//	print();
	println("Not codified: " + noBase64);

	println("Expansion Features from lexical expansion: " + lexicalExpansionFeatures);

	result.add(phrase.replaceAll("\\.", "").toLowerCase());
	result = new ArrayList<>(new HashSet(result));
	println("result Easy Query Extension: " + result);
	return new Pair<>(result, lexicalExpansionFeatures);
}

public static void main(String[] args) throws IOException, Exception
{
	//Query q = new Query("71", "colored Volkswagen beetles", "Volkswagen beetles in any other color, for example, red, blue, green or yellow");

	Parameters parameters = new Parameters("input.cfg");
	Configuration c = new Configuration(parameters);

	Query q = Query.readQueryLine(c, "71;colored Volkswagen beetles;Volkswagen beetles in any other color, for example, red, blue, green or yellow; ; ; ; ; ; ; ; ; ; ; ; ;");

	execute(c, q);
}

private static void println(Object string)
{
	System.out.println("	[QueryPreProcess.java]: " + string);
}

private static void println()
{
	System.out.println("	[QueryPreProcess.java]: ");
}

private static void print(Object string)
{
	System.out.print("	[QueryPreProcess.java]: " + string);
}

private static void print()
{
	System.out.print("	[QueryPreProcess.java]: ");
}

public static TimeCalculator execute(Configuration configuration, Set<Query> queries) throws Exception
{
	TimeCalculator tc = new TimeCalculator();
	for (Query query : queries)
	{
			tc.insert(query.getId()+" "+query.getInput(),System.currentTimeMillis());
			execute(configuration, query);
			tc.insert(query.getId()+" "+query.getInput(),System.currentTimeMillis());
	}
	return tc;
}

private static Set<String> createSynonymsList(Map<Integer, Set<String>> positionSynonyms)
{
	Set<String> result = new HashSet<>();
	createSynonymsList(result, "", positionSynonyms, 0);
	println("Synonyms: " + result);
	return result;
}

private static void createSynonymsList(Set<String> result, String phrase, Map<Integer, Set<String>> positionSynonyms, int i)
{

	String original = phrase;
	Set<String> listOfWord = positionSynonyms.get(i);
	for (String word : listOfWord)
	{
		phrase = original + " " + word;
		if (i == positionSynonyms.size() - 1)
		{
//			println("Phrase:" + phrase);
			result.add(phrase.trim());
		} else
		{
			createSynonymsList(result, phrase, positionSynonyms, i + 1);
		}
	}

	/*
	 String word = listOfWord.removeFirst();
	 listOfWord.addLast(word);
	 positionSynonyms.put(i, listOfWord);
	 phrase += " " + word;
	 if (i == positionSynonyms.size() - 1)
	 {
	 println("Phrase:" +phrase);
	 phrase="";
	 combinations++;
	 if (combinations < totalCominations)
	 {
			
	 createSynonymsList(phrase, positionSynonyms, 0, combinations, totalCominations);
	 }
	 } else
	 createSynonymsList(phrase, positionSynonyms, i + 1, combinations, totalCominations);*/
}

/*private static Set<String> rebuildOriginalStructure(String cleanedQueryInput, Pair<List<String>, Set<String>> easyExtension2)
{
	Set<String> result = new HashSet<>();
	String[] slashSeparetion = cleanedQueryInput.split("-");
	for (String s : easyExtension2.getSecond())
	{
		for (String s2 : slashSeparetion)
		{
			String[] split = s2.split(" ");
			String matchWord = split[split.length - 1];
			//s2.r

		}

	}
}*/

}
