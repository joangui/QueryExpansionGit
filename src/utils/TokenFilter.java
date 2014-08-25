/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import DEFS.Definitions;
import Strings.Tokenize.Tokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Token;

/**
 *
 * @author joan
 */
public class TokenFilter
{
final static Map<String,Set<String>> filters = new HashMap<>();

static public Set<String> loadFilter(String path)
{

	Set<String> filteredWords = filters.get(path);
	if(filteredWords!=null) return filteredWords;
	
	filteredWords = new HashSet<>();
	BufferedReader reader = FileManagement.ReadFiles.getReader(path);
	String line;
	try
	{
		while ((line = reader.readLine()) != null)
		{
			if (!line.isEmpty()&&line.charAt(0) != '#')
				filteredWords.add(line.toLowerCase().trim());
		}
	} catch (IOException ex)
	{
		Logger.getLogger(TokenFilter.class.getName()).log(Level.SEVERE, null, ex);
	}
	try
	{
		reader.close();
	} catch (IOException ex)
	{
		Logger.getLogger(TokenFilter.class.getName()).log(Level.SEVERE, null, ex);
	}

	filters.put(path, filteredWords);
	return filteredWords;

}

/*static public Set<String> loadFilter2(String path)
{

	Set<String> clrs = new HashSet<>();
	BufferedReader reader = null;
	reader = ReadFiles.getReader(path);
	String string;
	try
	{
		while ((string = reader.readLine()) != null)
		{
			if (!string.isEmpty())
			{
				clrs.add(string.toLowerCase().trim());
			}
		}
	} catch (IOException ex)
	{
		Logger.getLogger(TokenFilter.class.getName()).log(Level.SEVERE, null, ex);
	}
	try
	{
		reader.close();
	} catch (IOException ex)
	{
		Logger.getLogger(TokenFilter.class.getName()).log(Level.SEVERE, null, ex);
	}

	return clrs;

}
*/
public static Set<Token> remove(List<String> filtersPaths, Set<Token> tokens, Definitions.LANGUAGE language)
{
	//System.out.println("Tokens: " + tokens);
	Set<String> stringTokens = Token.getSetString(tokens);
	//System.out.println("String tokens" + stringTokens);

	if (filtersPaths != null)
	{
		Set<String> stopWords = new HashSet<String>();
		for (String filterPath : filtersPaths)
		{
			if (filterPath != null && !filterPath.trim().isEmpty())
			{
				stopWords.addAll(loadFilter(filterPath));
			}
		}

		stringTokens.removeAll(stopWords);
		try
		{
//                System.out.println("No Tokenized set: " + stringTokens);
			Set<String> tokenizedSet = Tokenizer.getTokenizedSet(stringTokens, language, true);
			//              System.out.println("   Tokenized set: " + tokenizedSet);
			return Token.getTokens(tokenizedSet);
		} catch (IOException ex)
		{
			Logger.getLogger(TokenFilter.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
	return tokens;
}

public static Set<String> remove(List<String> filtersPaths, List<String> tokens)
{
	//System.out.println("Tokens: " + tokens);
	Set<String> stringTokens = new HashSet<String>(tokens);
	//System.out.println("String tokens" + stringTokens);

	if (filtersPaths != null)
	{
		Set<String> stopWords = new HashSet<String>();
		for (String filterPath : filtersPaths)
		{
			if (filterPath != null && !filterPath.trim().isEmpty())
			{
				stopWords.addAll(loadFilter(filterPath));
			}
		}

		stringTokens.removeAll(stopWords);
	}
	return new HashSet<String>(stringTokens);
}

public static boolean isFiltered(List<String> filtersPaths, String token)
{

	if (filtersPaths != null)
	{
		Set<String> stopWords = new HashSet<>();
		for (String filterPath : filtersPaths)
		{
			if (filterPath != null && !filterPath.trim().isEmpty())
			{
				stopWords.addAll(loadFilter(filterPath));
			}
		}
		return stopWords.contains(token);
	}
	return false;
}

/**
 *
 * @param filtersPaths
 * @param stringTokens
 * @return
 */
}
