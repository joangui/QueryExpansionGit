/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import configurations.Configuration;
import java.io.IOException;

/**
 *
 * @author joan
 */
public class PipelineChooser
{

/**
 * @param args the command line arguments
 * @throws java.io.IOException
 */
public static void main(String[] args) throws IOException, Exception
{

	

	Configuration configuration = Configuration.getConfiguration("input.cfg");
	if(configuration==null) return;
	Configuration.PIPELINE pipeline = configuration.getPipeline();

	switch (pipeline)
	{
		case QUERY_EXPANSION:
			QueryExpansionPipeline.execute(configuration);
			break;
		case MULTIPLE_ENTITY_QUERY_EXPANSION:
			MultipleEntityQueryExpansionPipeline.execute(configuration);
			break;
		case ADINTON_EXPERIMENTS:
			AdintonPipeline.execute(configuration);
			break;
		case ENTITY_TEST_PIPELINE:
			EntityTestPipeLine.execute(configuration);
			break;

		case HARDCODED_QUERY:
			HardcodedQueryPipeline.execute(configuration);
			break;

		case COLLECTION_IDENTIFY_ENTITIES:
			ResultCollectionIdentifyEntities.execute(configuration);
			break;

		case REWEIGHT_QUERIES:
			ReweightQueriesPipeline.execute(configuration);
			break;
			
	}

}



private static void println(Object string)
{
	System.out.println("	[PipelineChooser.java]: " + string);
}



}
