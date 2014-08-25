/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import Structures.Pair;
import configurations.Configuration.EXPANSION_BLOCK;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author joan
 */
public class TimeCalculator
{

Map<EXPANSION_BLOCK, Pair<Long, Long>> timesPerBlock;
Map<String, Pair<Long, Long>> timesPerQuery;
Map<String, Map<EXPANSION_BLOCK, Long>> timesPerQueryAndBlock;
Map<String, TimeCalculator> timeCalculatorPerQuery;

Map<EXPANSION_BLOCK, Pair<String, Long>> maxTimes;

private int numQueries = -1;

public TimeCalculator()
{
	timesPerBlock = new LinkedHashMap<>();
	timesPerQuery = new LinkedHashMap<>();
	timesPerQueryAndBlock = new LinkedHashMap<>();
	timeCalculatorPerQuery = new LinkedHashMap<>();
}

public void setNumQueries(int numQueries)
{
	this.numQueries = numQueries;
}

public void insert(EXPANSION_BLOCK block, long time)
{
	Pair<Long, Long> expansionBlockTime = timesPerBlock.get(block);
	if (expansionBlockTime == null)
	{
		expansionBlockTime = new Pair(time, null);
	} else
	{
		expansionBlockTime.setSecond(time);
	}
	timesPerBlock.put(block, expansionBlockTime);
}

public void insert(EXPANSION_BLOCK block, TimeCalculator timesPerQuery)
{
	for (Map.Entry<String, Pair<Long, Long>> e : timesPerQuery.timesPerQuery.entrySet())
	{
		String queryID = e.getKey();
		Pair<Long, Long> timesPair = e.getValue();
		long start_time = timesPair.getFirst();
		long end_time = timesPair.getSecond();
		long time = end_time - start_time;
		Map<EXPANSION_BLOCK, Long> expansionBlockTime = timesPerQueryAndBlock.get(queryID);
		if (expansionBlockTime == null)
			expansionBlockTime = new HashMap<>();
		expansionBlockTime.put(block, time);
		timesPerQueryAndBlock.put(queryID, expansionBlockTime);
	}

}

public void insert(String id, TimeCalculator tc)
{
	timeCalculatorPerQuery.put(id, tc);
}

public void insert(String id, long time)
{
	Pair<Long, Long> expansionBlockTime = timesPerQuery.get(id);
	if (expansionBlockTime == null)
	{
		expansionBlockTime = new Pair(time, null);
	} else
	{
		expansionBlockTime.setSecond(time);
	}
	timesPerQuery.put(id, expansionBlockTime);
}

public void print(int size)
{
	numQueries = size;
	print();
}

public void print()
{
	maxTimes = new HashMap<>();
	StringBuilder sb = new StringBuilder("\n");
	for (Map.Entry<EXPANSION_BLOCK, Pair<Long, Long>> e : timesPerBlock.entrySet())
	{
		EXPANSION_BLOCK block = e.getKey();
		Pair<Long, Long> timesPair = e.getValue();
		long start_time = timesPair.getFirst();
		long end_time = timesPair.getSecond();

		if (numQueries != -1)
			sb.append(block).append(" TIME: ").append((end_time - start_time) / 1000.0).append("s., AVG: ").append((end_time - start_time) / 1000.0 / numQueries).append("s. per query.\n");
		else
			sb.append(block).append(" TIME: ").append((end_time - start_time) / 1000.0).append("s.\n");
	}

	StringBuilder sb2 = new StringBuilder("\nTIMES PER QUERY\n");
	for (Map.Entry<String, Map<EXPANSION_BLOCK, Long>> e : timesPerQueryAndBlock.entrySet())
	{
		String queryID = e.getKey();
		sb2.append(queryID).append(":\n");
		Map<EXPANSION_BLOCK, Long> expansionBlocks = e.getValue();
		for (Map.Entry<EXPANSION_BLOCK, Long> e2 : expansionBlocks.entrySet())
		{
			EXPANSION_BLOCK block = e2.getKey();
			Long time = e2.getValue();

			updateMaxTimes(queryID, block, time);

			sb2.append("		" + block).append(" TIME: ").append(time / 1000.0).append("s.").append("\n");
		}

	}

	StringBuilder sb3 = new StringBuilder("\nMAX TIMES PER BLOCK\n");
	for (Map.Entry<EXPANSION_BLOCK, Pair<String, Long>> e : maxTimes.entrySet())
	{
		sb3.append(e.getKey()).append(": ").append(e.getValue().getFirst()).append(" - ").append(e.getValue().getSecond() / 1000.0).append("s.\n");
	}

	println(sb);
	println(sb3);
	println(sb2);

}

private static void println(Object string)
{
	System.out.println("	[TimeCalculator.java]: " + string);
}

public void aggregatePerBlockInformation(Map<EXPANSION_BLOCK, Pair<Long, Long>> timesPerBlock)
{
	for (Map.Entry<EXPANSION_BLOCK, Pair<Long, Long>> e : timesPerBlock.entrySet())
	{
		EXPANSION_BLOCK expansionBlock = e.getKey();
		Pair<Long, Long> timesPair = this.timesPerBlock.get(expansionBlock);
		if (timesPair == null)
		{
			this.timesPerBlock.put(expansionBlock, e.getValue());
		} else
		{
			long difference = e.getValue().getFirst() - timesPair.getSecond();
			Pair<Long, Long> newPair = new Pair<>(timesPair.getFirst(), e.getValue().getSecond() - difference);
			this.timesPerBlock.put(expansionBlock, newPair);
		}
	}
}

public Map<EXPANSION_BLOCK, Pair<Long, Long>> getTimesPerBlock()
{
	return timesPerBlock;
}

public Map<String, TimeCalculator> getTimeCalculatorPerQuery()
{
	return timeCalculatorPerQuery;
}

public void aggregatetimeCalculatorPerQuery(Map<String, TimeCalculator> timeCalculatorPerQuery)
{
	for (Map.Entry<String, TimeCalculator> e : timeCalculatorPerQuery.entrySet())
	{
		String queryID = e.getKey();
		TimeCalculator tc = e.getValue();

		Map<EXPANSION_BLOCK, Pair<Long, Long>> timesPerBlock = tc.timesPerBlock;
		aggregatePerBlockInformation(timesPerBlock);
		aggregatePerBlockInformation(queryID, timesPerBlock);
	}
}

private void aggregatePerBlockInformation(String queryID, Map<EXPANSION_BLOCK, Pair<Long, Long>> timesPerBlock)
{
	Map<EXPANSION_BLOCK, Long> queryBlock = timesPerQueryAndBlock.get(queryID);
	Map<EXPANSION_BLOCK, Long> newBlock = new HashMap<>();
	for (Map.Entry<EXPANSION_BLOCK, Pair<Long, Long>> e : timesPerBlock.entrySet())
	{
		newBlock.put(e.getKey(), e.getValue().getSecond() - e.getValue().getFirst());
	}
	if (queryBlock == null)
	{
		queryBlock = newBlock;
	} else
	{
		for (Map.Entry<EXPANSION_BLOCK, Long> e : newBlock.entrySet())
		{

			EXPANSION_BLOCK expansionBlock = e.getKey();
			Long time = queryBlock.get(expansionBlock);
			if (time == null)
				queryBlock.put(expansionBlock, e.getValue());
			else
				queryBlock.put(expansionBlock, time + e.getValue());

		}

	}
	timesPerQueryAndBlock.put(queryID, queryBlock);

}

private void updateMaxTimes(String queryID, EXPANSION_BLOCK block, Long time)
{
	Pair<String, Long> queryId_Time = maxTimes.get(block);
	Long newTime;
	String newQueryID = queryID;
	if (queryId_Time == null)
	{
		newTime = time;
		newQueryID = queryID;
	} else
	{
		newTime = time > queryId_Time.getSecond() ? time : queryId_Time.getSecond();
		newQueryID = time > queryId_Time.getSecond() ? queryID : queryId_Time.getFirst();
	}
	maxTimes.put(block, new Pair<String, Long>(newQueryID, newTime));
}

}
