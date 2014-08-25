/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import Strings.StringUtils;
import Structures.MapUtils;
import java.util.Map;
import model.Entity;

/**
 *
 * @author joan
 */
public class StringUtilsQueryExpansion
{

	public static String MapDoubleValueToString(Map<Entity, Double> map)
	{
		map = MapUtils.sortByValue(map);
		StringBuilder sb = new StringBuilder("{");
		boolean firstElement =true;
		for(Map.Entry e :  map.entrySet())
		{
			if(!firstElement)
				sb.append(",");
			firstElement=false;
			sb.append(e.getKey().toString()).append("=").append(StringUtils.decimalFormat((Double) e.getValue(), 2));
		}
		sb.append("}");
		return sb.toString();
	}
	
}
