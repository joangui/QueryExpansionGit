/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import lemurproject.indri.*;
import java.util.*;

public class TestIndri 
{
    public static void main(String[] args) 
    {
        try {
            QueryEnvironment env = new QueryEnvironment();
            String myIndex = args[0];
            env.addIndex(myIndex);
            String q = args[1];
            System.out.println(q);
            ScoredExtentResult[] results = env.runQuery(q, 100);
            ParsedDocument[] documents = env.documents(results);
            for (int i = 0; i < documents.length; i++) {
                int passageBegin = results[i].begin;
                int passageEnd = results[i].end;
                int byteBegin = documents[i].positions[ passageBegin].begin;
                int byteEnd = documents[i].positions[ passageEnd - 1].end;
                String startText = documents[i].text + byteBegin;
                String id = startText.substring(startText.indexOf("id="), startText.indexOf("file"));
                System.out.println(id +" number: "+results[i].number+", ordinal: "+results[i].ordinal+", parent ordinal: "+results[i].parentOrdinal+" score: "+results[i].score );
            }
            env.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}