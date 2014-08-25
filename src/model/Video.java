package model;

import edu.upc.dama.dex.bean.EntityBean;
import edu.upc.dama.dex.core.DEX;
import edu.upc.dama.dex.core.DbGraph;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.GraphPool;
import edu.upc.dama.dex.core.Objects;
import edu.upc.dama.dex.core.Session;
import edu.upc.dama.dex.core.TextStream;
import edu.upc.dama.dex.core.Value;
import edu.upc.dama.dex.utils.DexUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Video extends EntityBean
{

public static int CLASS_ID; // identificador de classe
public static int TOKEN_ID;
public static int VIDEO_ID;
public static String VIDEO_ID_NAME = "Video";
public static int ARTICLE_TOKENS_ID;
public static int CATEGORY_ID;
public static int BELONGS_ID;
public static int REDIRECTS_ID;
public static long TITLE_ATTR_ID;
public static long CONTENT_ATTR_ID;
public static long TOKEN_ATTR_ID;
public static long TOKEN_WEIGHT_ATTR_ID;
public static long NAME_ATTR_ID;
public static String NAME_ATTR_NAME = "name";
public static String TITLE_ATTR_NAME = "title";
public static String CONTENT_ATTR_NAME = "content";
public static String TOKEN_ATTR_NAME = "token";
public static String TOKEN_ID_NAME = "Token";
public static String TOKEN_WEIGHT_ATTR_NAME = "weight";
public static String CATEGORY_ID_NAME = "Category";
public static String BELONGS_ID_NAME = "belongs";
public static String REDIRECTS_ID_NAME = "redirects";
public static String ARTICLE_TOKENS_ID_NAME = "article_tokens";
public static String DOC_TOKEN_TF_IDF_ATTR_TYPE = "weight";
public static String VIDEO_TOKENS_ID_NAME = "video_tokens";
public static int VIDEO_TOKENS_ID;
public static long VIDEO_TITLE_ID;
public static long VIDEO_TYPE_ID;
public static long VIDEO_TEXT_ID;
public static long VIDEO_ABSTRACT_ID;
public static long VIDEO_YEAR_ID;
public static String VIDEO_TITLE_ID_NAME = "title";
public static String VIDEO_TYPE_ID_NAME = "doc_type";
public static String VIDEO_TEXT_ID_NAME = "text";
public static String VIDEO_ABSTRACT_ID_NAME = "abstract";
public static String VIDEO_YEAR_ID_NAME = "year";

static
{
	Graph g = DexUtil.getDBGraph();
	Set<Long> attributes;

	CLASS_ID = g.findType(Video.class.getSimpleName());

	TOKEN_ID = g.findType(TOKEN_ID_NAME);
	TOKEN_ATTR_ID = g.findAttribute(TOKEN_ID, TOKEN_ATTR_NAME);
	TOKEN_WEIGHT_ATTR_ID = g.findAttribute(TOKEN_ID, TOKEN_WEIGHT_ATTR_NAME);

	CATEGORY_ID = g.findType(CATEGORY_ID_NAME);
	NAME_ATTR_ID = g.findAttribute(CATEGORY_ID, NAME_ATTR_NAME);

	BELONGS_ID = g.findType(BELONGS_ID_NAME);

	REDIRECTS_ID = g.findType(REDIRECTS_ID_NAME);

	VIDEO_TOKENS_ID = g.findType(VIDEO_TOKENS_ID_NAME);

	attributes = g.getAttributesFromType(CLASS_ID);

	HashMap<String, Long> attribute = new HashMap<String, Long>(
			attributes.size());
	attributes = g.getAttributesFromType(CLASS_ID);

	attribute = new HashMap<>(
			attributes.size());

	Iterator<Long> attributesIt = attributes.iterator();
	while (attributesIt.hasNext())
	{
		long currentAttr = attributesIt.next();
		Graph.AttributeData aData = g.getAttributeData(currentAttr);
		attribute.put(aData.getName(), currentAttr);
	}

	VIDEO_TITLE_ID = attribute.get(VIDEO_TITLE_ID_NAME);
	VIDEO_TYPE_ID = attribute.get(VIDEO_TYPE_ID_NAME);
	VIDEO_TEXT_ID = attribute.get(VIDEO_TEXT_ID_NAME);
	VIDEO_ABSTRACT_ID = attribute.get(VIDEO_ABSTRACT_ID_NAME);

	/*  public static String VIDEO_TITLE_ID_NAME="title";
	 public static String VIDEO_TYPE_ID_NAME="type";
	 public static String VIDEO_TEXT_ID_NAME="text";
	 public static String VIDEO_ABSTRACT_ID_NAME="abstract";*/
}

private GraphPool gpool;
private long id;
private String title;
private int year;
private String doc_type;
private String text;
private String vid_abstract;
private String tags;

public String getTags()
{
	return tags;
}

public void setTags(String tags)
{
	this.tags = tags;
}

/* public Video(long id)
 {
 this.id = id;
 }
 */
/*public Video(long id, String title, String type, String text, String abs, int year)
 {
 this.id = id;
 this.title = title;
 this.doc_type = type;
 this.text = text;
 this.vid_abstract = abs;
 this.year = year;

 this.setObjectIdentifier(id);
 }*/
public Video(long id)
{
	this.id = id;
	this.title = null;
	this.doc_type = null;
	this.text = null;
	this.vid_abstract = null;
	this.year = -1;
	this.setObjectIdentifier(id);
}

public Video(String s)
{
	//	s="Gustave Doré - The Holy Bible - Plate CXLI, The Judas Kiss [66968]";
	Graph g = DexUtil.getDBGraph();
	String VIDEO_TITLE_ID_NAME = "title";
	int type = g.findType(Video.class.getSimpleName());
	long VIDEO_TITLE_ID = g.findAttribute(type, VIDEO_TITLE_ID_NAME);

	Objects ret = g.select(VIDEO_TITLE_ID, Graph.OPERATION_ILIKE, new Value("[" + s + "]"));
	if (ret.size() > 1)
		println("[WARNING] : Retrived video [" + s + "] may be not correct.");
	this.id = ret.first();
	ret.close();

	this.title = null;
	this.doc_type = null;
	this.text = null;
	this.vid_abstract = null;
	this.year = -1;
	this.setObjectIdentifier(id);
}

/*public Video(Video v) throws IOException
 {
 this.id = v.getId();
 this.title = v.getTitle();
 this.doc_type = v.doc_type;
 this.text = v.getText();
 this.vid_abstract = v.getVidAbstract();
 this.year = v.getYear();


 }*/
public long getId()
{
	return id;
}

public void setId(long id)
{
	this.id = id;
}

public String getTitle()
{
	Graph g = DexUtil.getDBGraph();
	Value v = g.getAttribute(id, g.findAttribute(g.getType(id), "title"));
	if (!v.isNull())
	{
		title = v.getString();
	} else
	{
		title = "";
	}
	/*if(title==null)
	 {
	 title=videoDAO.getTitle(id);
		
	 }*/

	return title;
}

public void setTitle(String title)
{
	this.title = title;
}

public int getYear()
{
	if (year == -1)
	{
		year = getYear(id);
	}
	return year;
}

public void setYear(int year)
{
	this.year = year;
}

public String getDocType()
{
	if (doc_type == null)
	{
		doc_type = getDocType(id);
	}

	return doc_type;
}

public void setDocType(String doc_type)
{
	this.doc_type = doc_type;
}

public String getText() throws Exception
{
	Graph g = DexUtil.getDBGraph();
	if (text == null)
	{
		text = getStringFromTextAttribute(g.getAttribute(id, g.findAttribute(g.getType(id), "text")));
		//text = videoDAO.getText(id);
	}
	return text;
}

public void setText(String text)
{
	this.text = text;
}

public String getAbstract() throws Exception
{
	Graph g = DexUtil.getDBGraph();
	if (vid_abstract == null)
	{
		vid_abstract = getStringFromTextAttribute(g.getAttribute(id, g.findAttribute(g.getType(id), "abstract")));
		//vid_abstract = videoDAO.getAbstract(id);
	}
	return vid_abstract;
}

public void setAbstract(String vid_abstract)
{
	this.vid_abstract = vid_abstract;
}

private String getStringFromTextAttribute(Value value) throws IOException
{
	// Value value = graph.getAttribute(nodeId, attributteType);
	if (value.isNull())
	{
		return "";
	}
	TextStream tstrm = value.getTextStream();
	StringBuilder sb = new StringBuilder();
	char[] buff = new char[200];
	int len = 0;
	while ((len = tstrm.read(buff, 0, 200)) > 0)
	{
		sb.append(buff, 0, len);
	}
	tstrm.close();
	String result = new String(String.valueOf(sb.toString()).getBytes(), "UTF-8");
	return result;
}

public static <T extends Comparable<? super T>> List<T> CollectionToSortedList(Collection<T> c)
{
	List<T> list = new ArrayList<T>(c);
	java.util.Collections.sort(list);
	return list;
}

public int getPuntuation(Set<Token> QCST)
{
	DEX dex = new DEX();
	StringBuilder path = new StringBuilder();
	Set<String> QCS = new HashSet<String>();
	for (Token t : QCST)
	{
		QCS.add(t.getName());
	}
	List<String> QCSList = CollectionToSortedList(QCS);
	for (String string : QCSList)
	{
		path.append("-" + string);
	}

	//path = new StringBuilder(Normalizer.normalize(path, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
	path = new StringBuilder(Normalizer.normalize(path.toString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

	try
	{

		gpool = dex.open("./videoPuntuations" + path + ".dex");
	} catch (FileNotFoundException e)
	{
		try
		{
			gpool = dex.create("./videoPuntuations" + path + ".dex");
		} catch (FileNotFoundException ex)
		{
			Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	Session s = gpool.newSession();
	DbGraph g = s.getDbGraph();

	int videoNode_Type_id = g.findType("video");
	if (videoNode_Type_id == Graph.INVALID_NODE)
	{
		videoNode_Type_id = g.newNodeType("video");
	}

	long valoracio_attr_id = g.findAttribute(videoNode_Type_id, "valoracio");
	if (valoracio_attr_id == Graph.INVALID_ATTRIBUTE)
	{
		valoracio_attr_id = g.newAttribute(videoNode_Type_id, "valoracio", Value.INT);
	}

	long videoID = g.findAttribute(videoNode_Type_id, "id");
	if (videoID == Graph.INVALID_ATTRIBUTE)
	{
		videoID = g.newAttribute(videoNode_Type_id, "id", Value.LONG);
	}

	Value v = new Value();

	Objects currentVideo = g.select(videoID, Graph.OPERATION_EQ, v.setLong(id));
	int punt = 0;
	//g.s
	if (currentVideo.isEmpty())
	{
		System.out.println("El video \"" + getTitle() + "\" no té valoració feta");
		System.out.println("Què vols fer? 1: Veure contingut del video; 2: Introduir valoració");
		Scanner in = new Scanner(System.in);
		int option = in.nextInt();
		while (option != 1 && option != 2)
		{
			option = in.nextInt();
		}
		if (option == 1)
		{
			try
			{
				System.out.println("Abstract:");
				System.out.println(getAbstract());
				System.out.println("Content:");
				System.out.println(getText());
			} catch (Exception ex)
			{
				Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Introduir puntuacio: 0 (no relacionat ni amb la query ni amb el context), 1 (relacionat amb la query o el context), 2 (relacionat amb la query i el context)");
		punt = in.nextInt();
		while (punt != 1 && punt != 2 & punt != 0)
		{
			punt = in.nextInt();
		}
		Value id_v = new Value();
		id_v.setLong(id);
		Value valoracio_v = new Value();
		valoracio_v.setInt(punt);
		long newVideo = g.newNode(videoNode_Type_id);
		g.setAttribute(newVideo, videoID, id_v);
		g.setAttribute(newVideo, valoracio_attr_id, valoracio_v);
		currentVideo.close();

	} else
	{
		long currentVideo_id = currentVideo.first();
		punt = g.getAttribute(currentVideo_id, valoracio_attr_id).getInt();
		currentVideo.close();

	}

	s.close();
	gpool.close();
	dex.close();

	return punt;
}

@Override
public String toString()
{
//return title;		
	return getTitle();
}

public int getYear(long id)
{
	Graph g = DexUtil.getDBGraph();
	Value v = g.getAttribute(id, VIDEO_YEAR_ID);
	int ret;
	if (!v.isNull())
	{
		ret = v.getInt();
	} else
	{
		ret = 0;
	}
	return ret;

}

public String getDocType(long id)
{
	Graph g = DexUtil.getDBGraph();
	Value v = g.getAttribute(id, VIDEO_TYPE_ID);
	String ret;
	if (!v.isNull())
	{
		ret = v.getString();
	} else
	{
		ret = "";
	}
	return ret;
}

private static void println(Object string)
{
	System.out.println("	[Video.java]: " + string);
}

}
