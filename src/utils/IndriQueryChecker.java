/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import FileManagement.ReadFiles;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author joan
 */
public class IndriQueryChecker
{

/**
 * @param args the command line arguments
 */
public static void main(String[] args) throws FileNotFoundException, IOException
{

	BufferedReader reader = ReadFiles.getReader("oldQuery.txt");
	String s = reader.readLine();
	StringBuilder sb = new StringBuilder();
	StringBuilder offset = new StringBuilder();
	while (!s.isEmpty())
	{
		if (s.startsWith("#weight("))
		{
			sb.append("#weight(" + "\n");
			offset.append("    ");
			s = s.substring(s.indexOf("#weight("));
			s=s.replace("#weight(", "");
		} else
		{
			int index = s.indexOf("#weight");
			String substring;
			if (index != -1)
			{
				substring = s.substring(0, index);
			} else
			{
				substring = s;
			}
			sb.append(substring);
			s = s.replaceFirst(substring, "");
		}
	}
	System.out.println(sb);
}

}
