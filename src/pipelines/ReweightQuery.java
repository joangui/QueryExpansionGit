/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipelines;

import configurations.Configuration;
import java.util.Set;
import model.Query;
import utils.IndriModule;
import utils.TimeCalculator;

/**
 *
 * @author joan
 */
class ReweightQuery
{

public static TimeCalculator execute(Configuration configuration, Set<Query> queries) throws Exception
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

static public String execute(Configuration configuration, Query query) throws Exception
{

	String runIndriQuery = IndriModule.setWeightsExpansionIndriQuery(configuration, query.getConfigurableIndriQueryLine());
	query.setRunIndriQuery(runIndriQuery);

	return runIndriQuery;
}

}
