/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author joan
 */
public class Paths
{

//private TreeSet<Path> paths;
private Map<Integer, Path> mapPaths;

public Paths(Set<Set<Long>> setPaths)
{
	//paths = new TreeSet<>();
	mapPaths = new HashMap<>();
	int i = 0;
	for (Set<Long> path : setPaths)
	{
//		paths.add(new Path(i, path));
		mapPaths.put(i, new Path(i,path));
		i++;
	}
}

public Paths()
{
//	paths = new TreeSet<>();
	mapPaths = new HashMap<>();
}

public Set<Path> getPaths()
{
	return new TreeSet(mapPaths.values());
}

public Path add(Path path)
{
	return mapPaths.put(path.getId(), path);
//	return paths.add(path);
}

public Path add(Set<Long> path)
{
	int size = mapPaths.size();
	Path p = new Path(size, path);
	return mapPaths.put(size, new Path(size,path));
	
	//return paths.add(p);
}

public int size()
{
	return mapPaths.size();
}

public Set<Long> get(int i)
{
	return mapPaths.get(i).getIds();
}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		boolean firstPath = true;
		for(int i = 0; i < this.size() ; i++)
		{
			Set<Long> get = this.get(i);
			if(!firstPath)
				sb.append(";");
			sb.append(get.toString());
		firstPath=false;		
		}
		return sb.toString();
	}

	boolean isEmpty()
	{
		return mapPaths.isEmpty();
	}

	

}



class Path implements Comparable<Path>
{

private int id;
private Set<Long> ids;

Path()
{
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int compareTo(Path o)
{
	return this.id - o.id;
}

public Path(int id, Set<Long> ids)
{
	this.id = id;
	this.ids = ids;
}

public Set<Long> getIds()
{
	return ids;
}

public int getId()
{
	return id;
}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(":");
		boolean firstElement=true;
		for(Long id : ids)
		{
			if(!firstElement)
				sb.append(",");
			sb.append(id);
			firstElement = false;
		}
		return sb.toString();
	}


}
