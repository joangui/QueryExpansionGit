/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import FileManagement.FolderNavigation;
import configurations.Configuration;
import edu.upc.dama.utils.objects.UtilsMap;
import expansionBlocks.IdentifyEntities;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Article;
import model.Entity;

/**
 *
 * @author joan
 */
class ResultCollectionIdentifyEntities
{

static StringBuilder currentfile = new StringBuilder();
static Set<Entity> removableEntities = new HashSet<>();
static Set<String> extensions = new HashSet<>();

static
{
	removableEntities.add(new Entity(427137L));
	removableEntities.add(new Entity(290930L));
	removableEntities.add(new Entity(258385L));
	removableEntities.add(new Entity(5812718L));
	removableEntities.add(new Entity(5811607L));
	removableEntities.add(new Entity(282828L));
	removableEntities.add(new Entity(2332057L));
	
	extensions.add(".gif");
	extensions.add(".icons");
	extensions.add(".JPEG");
	extensions.add(".jpg");
	extensions.add(".JPG");
	extensions.add(".PNG");

}

static void execute(Configuration configuration) throws IOException, Exception
{
	String rootFolder = configuration.getRootFolderResCol();
	String resultFolder = configuration.getResultFolderResCol();
	if (resultFolder.charAt(resultFolder.length() - 1) != '/')
		resultFolder += '/';

	List<String> files = FolderNavigation.doFileFilterExtensionListing(rootFolder, "xml");

	int i = 0;

	long t1, t2;

	println("Processing " + files.size() + " files");
	t1 = System.currentTimeMillis();
	for (String file : files)
	{
		//inicializeRemovableEntities(removableEntities);
		currentfile.setLength(0);
		currentfile.append(file);

		String resultFile = resultFolder + file.substring(file.lastIndexOf("/") + 1, file.length());
		Set<Entity> findEntities = processFile(configuration, file);
		//println("		entities: " + findEntities);
		printFile(resultFile, findEntities);

		i++;
		if (i % 1000 == 0)
		{
			t2 = System.currentTimeMillis();
			println(i + " files out of " + files.size() + " processed in " + (t2 - t1) / 1000.0 + " seconds.");
			t1 = t2;
		}
	}

}

private static void println(Object string)
{
	System.out.println("	[ResultCollectionIdentifyEntities.java]: " + string);
}

static public void main(String[] argv) throws IOException, Exception
{

	Configuration configuration = Configuration.getConfiguration("input.cfg");
	if (configuration == null)
		return;

	//Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/14/131953.xml");
	//Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/10/97766.xml");
//	Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/12/115025.xml");
	//Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/8/78243.xml");
//	Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/1/6638.xml");
	Set<Entity> foundEntities = processFile(configuration, "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata/21/208970.xml");
	println("		entities: " + foundEntities);
	printFile("./test.xml", foundEntities);
}

private static String treatXMLFile(String file) throws IOException
{
	BufferedReader reader = FileManagement.ReadFiles.getReader(file);
	String line = "";
	StringBuilder sb = new StringBuilder();
	while ((line = reader.readLine()) != null)
	{
		line = line.trim();
		if (!line.isEmpty())
		{
			for (String extension : extensions)
			{
				line = line.replaceAll(extension, "");
			}
			line = removeUrl(line);
			if (line.startsWith("<comment>"))
			{
				line = treatCommentLine(line);
			}
			line = line.replaceAll("\\<.*?\\>", "").replaceAll("\\|([A-z0-9]+)\\=", " ").replaceAll("[^\\p{L}\\p{Nd}]+", " ");//.replaceAll("[^\\p{L}\\p{Z}]"," ").trim();
			if (!line.isEmpty())
				sb.append(" " + line);
		}
	}

	reader.close();
	return sb.toString();
}

static private String removeUrl(String commentstr)
{
	String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
	Matcher m = p.matcher(commentstr);
	int i = 0;
	while (m.find())
	{
		//try
		{
			String group = m.group(i);
			if (group != null)
			{
				commentstr = commentstr.replaceAll(Pattern.quote(group), "").trim();
				i++;
			} else
			{
				println("m'estic tocant els ous");
			}
		}

	}
	return commentstr;
}

private static void printFile(String file, Set<Entity> findEntities) throws FileNotFoundException, UnsupportedEncodingException
{
	String file2 = file.replace(".xml", ".entity");
	StringBuilder sb = new StringBuilder();
	StringBuilder sb2 = new StringBuilder();

	PrintWriter writer = FileManagement.Writer.getWriter(file);
	sb.append("<DOC>\n");
	writer.println("<DOC>");

	sb.append("<DOCNO>").append(file.substring(file.lastIndexOf("/") + 1, file.length() - 4)).append("</DOCNO>\n");
	writer.println("<DOCNO>" + (file.substring(file.lastIndexOf("/") + 1, file.length() - 4)) + "</DOCNO>");

	sb2.append(file.substring(file.lastIndexOf("/") + 1, file.length()) + "\n");

	sb.append("<TEXT>\n");
	writer.println("<TEXT>");

	for (Entity entity : findEntities)
	{
		boolean first = true;
		Set<String> names = new HashSet<>();
		for (String name : entity.getEntityNames())
		{
			names.add(name.replace("(", "").replace(")", "").replace("disambiguation", "").trim());
		}
		for (String name : names)
		{
			if (!first)
			{
				sb.append("; ");
				writer.print(";");
			}
			sb.append(name);
			writer.print(name);

			first = false;
		}
		sb.append(".\n");
		writer.println();

		String ambiguity = entity.isAmbiguous() ? "*" : "";
		sb2.append(entity.getId() + ambiguity + "\n");
	}
	sb.append("</TEXT>\n");
	writer.println("</TEXT>");

	sb.append("</DOC>");
	writer.println("</DOC>");

	//println("\n" + sb);
	//writer.print(sb);
	writer.close();
	writer = FileManagement.Writer.getWriter(file2);
	writer.print(sb2);
	writer.close();

}

private static Set<Entity> treatEntities(Set<Entity> entities, Configuration configuration) throws Exception
{

	Set<Entity> removableEntitiesLocal = new HashSet<>();
	Set<Entity> resultEntities = new HashSet<>();
	Map<String, Map<Integer, Entity>> mapWordsEntities = new HashMap<>();

	for (Entity e : entities)
	{
		String entityName = e.getName();
		if (!e.isStopWord(configuration.getLanguage(), configuration.getFilterFilePaths()))
		{
			resultEntities.add(e);
			String[] entityNameWord = entityName.split("\\ ");
			for (String word : entityNameWord)
			{
				Map<Integer, Entity> entitiesByWord = mapWordsEntities.get(word) == null ? new HashMap<Integer, Entity>() : mapWordsEntities.get(word);
				entitiesByWord.put(entityNameWord.length, e);
				mapWordsEntities.put(word, entitiesByWord);
			}
		}
	}

	for (Map.Entry<String, Map<Integer, Entity>> e : mapWordsEntities.entrySet())
	{
		String word = e.getKey();
		Map<Integer, Entity> entitiesSizePair = new TreeMap<>(UtilsMap.sortByKey(e.getValue()));
		if (entitiesSizePair.size() > 1)
			for (Map.Entry<Integer, Entity> e2 : entitiesSizePair.entrySet())
			{
				Entity entity = e2.getValue();
				int size = e2.getKey();
				for (Map.Entry<Integer, Entity> e3 : entitiesSizePair.entrySet())
				{
					Entity entity2 = e3.getValue();
					int size2 = e3.getKey();
					if (size < size2)
					{
						String name = entity.getName();
						String name2 = entity2.getName();
						if ((" " + name2 + " ").contains((" " + name + " ")))
						{
							removableEntitiesLocal.add(entity);
							break;

						}
					}
				}
			}

	}

	//println("removable entities: "+removableEntities);
	resultEntities.removeAll(removableEntitiesLocal);
	return resultEntities;

}

private static Set<Entity> processFile(Configuration configuration, String file) throws IOException, Exception
{
	//println("Working with; "+file);
	String extractedText = treatXMLFile(file);
	Set<Entity> findEntities = IdentifyEntities.findEntities(configuration, extractedText);
	findEntities = treatEntities(findEntities, configuration);
	findEntities.removeAll(removableEntities);

	return findEntities;
}

private static String treatCommentLine(String line)
{
	StringBuilder sb = new StringBuilder();
	if (line.contains("|"))
	{
		String[] split = line.split("\\|");
		for (String s : split)
		{
			String[] split1 = s.split("=");
			if (split1.length > 1 && !split1[0].equalsIgnoreCase("Permission"))
			{
				sb.append(split1[1]).append(" ");
			}

		}
	} else
		sb.append(line);

	return sb.toString().trim();
}



private static void inicializeRemovableEntities(Set<Entity> removableEntitiesLocal)
{
	removableEntitiesLocal.clear();
	removableEntitiesLocal.add(new Entity(427137L));
	removableEntitiesLocal.add(new Entity(290930L));
	removableEntitiesLocal.add(new Entity(258385L));
	removableEntitiesLocal.add(new Entity(5812718L));
	removableEntitiesLocal.add(new Entity(5811607L));
	removableEntitiesLocal.add(new Entity(282828L));
	removableEntitiesLocal.add(new Entity(5812718L));
}

}
