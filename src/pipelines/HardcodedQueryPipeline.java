/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import DEFS.Definitions;
import configurations.Configuration;
import expansionBlocks.EvalResults;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import model.Query;
import utils.IndriModule;

/**
 *
 * @author joan
 */
public class HardcodedQueryPipeline
{

static void execute(Configuration configuration) throws Exception
{
	Query q = new Query("116", "house in mountains", "", Definitions.LANGUAGE.ENGLISH);
//	Query q = new Query("80", "wolf close up", "", Definitions.LANGUAGE.CATALAN);
//	String indriQuery = readIndriQuery("resultQuery80_indri.txt");
	String indriQuery = readIndriQuery("topologicalQuery116.txt");
	//String indriQuery = "#weight(1.0#wsyn(1.0 #1(cable car)0.1 #uw(cable cars) 0.1 #uw(cablecar) 0.1 #uw(cableway) 0.1 #uw(cable-car) )1.0#weight(0.12#wsyn(1.0 #1(california street cable railroad))0.12#wsyn(1.0 #1(clay street hill railroad))0.54#wsyn(1.0 #1(hybrid lift)0.1 #uw(combined installations) 0.1 #uw(hybrid chairlift) 0.1 #uw(chondola) 0.1 #uw(telemix) )0.12#wsyn(1.0 #1(chicago city railway))0.12#wsyn(1.0 #1(geary street, park and ocean railway)0.1 #uw(geary street, park &amp; ocean railway) )0.16#wsyn(1.0 #1(san francisco cable car system)0.1 #uw(san francisco cable car) 0.1 #uw(san francisco muni cable cars) 0.1 #uw(san francisco cable railway) 0.1 #uw(san francisco cable cars) )0.12#wsyn(1.0 #1(sutter street railway))0.18#wsyn(1.0 #1(cable car)1.0 #uw1(cable car) 0.1 #uw(cable operated street tramway) 0.1 #uw(cable traction) 0.1 #uw(cable car on rails) 0.1 #uw(electric cable cars) 0.1 #uw(cable tram) )0.12#wsyn(1.0 #1(market street railway)1.0 #uw1(market street railway) 0.1 #uw(united railroads of san francisco) 0.1 #uw(market street cable railway company) 0.1 #uw(market street railway company) 0.1 #uw(market street railroad) 0.1 #uw(market street cable railway) 0.1 #uw(market street railroad company) 0.1 #uw(united railroads) )0.11#wsyn(1.0 #1(cable grip))))";
	q.setTopologicalIndriQuery(indriQuery);

	Map<String, Double> videos = IndriModule.getVideos(configuration, q, indriQuery);
	q.setResult(videos);
	Set<Query> queries = new HashSet<>();
	queries.add(q);
	EvalResults.execute(configuration, queries);

}

private static String readIndriQuery(String file ) throws FileNotFoundException, IOException
{
	BufferedReader reader = FileManagement.ReadFiles.getReader(file);
	StringBuilder sb = new StringBuilder();

	String s; 
	while ((s=reader.readLine())!=null)
	{
		if(!s.trim().isEmpty()&&s.trim().charAt(0)!='%')
		sb.append(s.trim());
	}
	
	return sb.toString().trim();
	
}

}
