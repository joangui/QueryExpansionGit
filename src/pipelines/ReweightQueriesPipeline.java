/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import configurations.Configuration;
import static configurations.Configuration.EXPANSION_BLOCK.DESERIALIZE;
import static configurations.Configuration.EXPANSION_BLOCK.EVAL_RESULTS;
import static configurations.Configuration.EXPANSION_BLOCK.EXECUTE_QUERIES;
import static configurations.Configuration.EXPANSION_BLOCK.READ_QUERIES;
import static configurations.Configuration.EXPANSION_BLOCK.REWEIGHT_QUERIES;
import static configurations.Configuration.EXPANSION_BLOCK.SERIALIZE;
import expansionBlocks.EvalResults;
import expansionBlocks.ExecuteQuery;
import java.util.SortedSet;
import java.util.TreeSet;
import model.Query;
import utils.TimeCalculator;

/**
 *
 * @author joan
 */
public class ReweightQueriesPipeline
{

public static void execute(Configuration configuration) throws Exception
{
	Configuration.EXPANSION_BLOCK startPoint = configuration.getStartPoint();
	Configuration.EXPANSION_BLOCK endPoint = configuration.getEndPoint();
	Configuration.EXPANSION_BLOCK currentPoint;

	SortedSet<Query> queries = null;
	StringBuilder sb = new StringBuilder();
	TimeCalculator tc = new TimeCalculator();

	tc.insert(DESERIALIZE, System.currentTimeMillis());
	queries = new TreeSet(Query.deserialize(configuration));
	tc.insert(DESERIALIZE, System.currentTimeMillis());

	switch (startPoint)
	{
		case REWEIGHT_QUERIES:
			currentPoint= REWEIGHT_QUERIES;
			tc.insert(currentPoint, System.currentTimeMillis());
			TimeCalculator Reweight_Query_TC = ReweightQuery.execute(configuration, queries);
			tc.insert(currentPoint, System.currentTimeMillis());
			tc.insert(currentPoint, Reweight_Query_TC);
			Query.serialize(configuration, queries);
			if (endPoint == currentPoint)
				break;

		case EXECUTE_QUERIES:
			currentPoint= EXECUTE_QUERIES;
			tc.insert(currentPoint, System.currentTimeMillis());
			TimeCalculator Execute_Query_TC = ExecuteQuery.execute(configuration, queries);
			tc.insert(currentPoint, System.currentTimeMillis());
			tc.insert(currentPoint, Execute_Query_TC);
			Query.serialize(configuration, queries);
			if (endPoint == currentPoint)
				break;

		case EVAL_RESULTS:
			currentPoint = EVAL_RESULTS;
			tc.insert(currentPoint, System.currentTimeMillis());
			EvalResults.execute(configuration, queries);
			tc.insert(currentPoint, System.currentTimeMillis());
			Query.serialize(configuration, queries);
			if (endPoint == currentPoint)
				break;
	}
	tc.insert(SERIALIZE, System.currentTimeMillis());
	Query.serialize(configuration, queries);
	tc.insert(SERIALIZE, System.currentTimeMillis());

	tc.print(queries.size());
}

}
