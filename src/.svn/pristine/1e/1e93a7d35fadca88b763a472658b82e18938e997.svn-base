/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author joan
 */
public class RelevantDocuments
{

Set<Category> contextRelevantCategories;
Set<Article> contextRelevantArticles;
Set<Category> inputRelevantCategories;
Set<Article> inputRelevantArticles;

public RelevantDocuments(RelevantDocuments d)
{
	this.contextRelevantArticles = new HashSet<Article>(d.contextRelevantArticles);
	this.contextRelevantCategories = new HashSet<Category>(d.contextRelevantCategories);

	this.inputRelevantArticles = new HashSet<Article>(d.inputRelevantArticles);
	this.inputRelevantCategories = new HashSet<Category>(d.inputRelevantCategories);

}

public RelevantDocuments(Set<Category> contextRelatedCategories, Set<Article> contextRelatedArticles, Set<Category> queryRelatedCategories, Set<Article> queryRelatedArticles)
{
	this.contextRelevantCategories = contextRelatedCategories;
	this.contextRelevantArticles = contextRelatedArticles;
	this.inputRelevantCategories = queryRelatedCategories;
	this.inputRelevantArticles = queryRelatedArticles;
}

public RelevantDocuments()
{
	this.contextRelevantArticles = new HashSet<Article>();
	this.contextRelevantCategories = new HashSet<Category>();
	this.inputRelevantArticles = new HashSet<Article>();
	this.inputRelevantCategories = new HashSet<Category>();
}

public void setContextRelevantArticles(Set<Article> contextRelevantArticles)
{
	this.contextRelevantArticles = contextRelevantArticles;
}

public void setContextRelevantCategories(Set<Category> contextRelevantCategories)
{
	this.contextRelevantCategories = contextRelevantCategories;
}

public void setInputRelevantArticles(Set<Article> inputRelevantArticles)
{
	this.inputRelevantArticles = inputRelevantArticles;
}

public void setInputRelevantCategories(Set<Category> inputRelevantCategories)
{
	this.inputRelevantCategories = inputRelevantCategories;
}

public Set<Article> getContextRelevantArticles()
{
	return contextRelevantArticles;
}

public Set<Category> getContextRelevantCategories()
{
	return contextRelevantCategories;
}

public Set<Article> getInputRelevantArticles()
{
	return inputRelevantArticles;
}

public Set<Category> getQueryRelevantCategories()
{
	return inputRelevantCategories;
}

public Set<Article> getArticles()
{
	Set<Article> articles = new HashSet<Article>();
	articles.addAll(this.contextRelevantArticles);
	articles.addAll(this.inputRelevantArticles);

	return articles;
}

public Set<Category> getCategories()
{
	Set<Category> categories = new HashSet<Category>();
	categories.addAll(this.contextRelevantCategories);
	categories.addAll(this.inputRelevantCategories);

	return categories;
}

public List<Long> getInputIds()
{
	List<Long> nodesQ = new ArrayList(inputRelevantArticles.size() + inputRelevantCategories.size());
	for (Article a : inputRelevantArticles)
	{
		nodesQ.add(a.getId());
	}
	for (Category c : inputRelevantCategories)
	{
		nodesQ.add(c.getId());
	}
	return nodesQ;
}

public List<Long> getContextIds()
{
	List<Long> nodesC = new ArrayList(contextRelevantArticles.size() + contextRelevantCategories.size());
	for (Article a : contextRelevantArticles)
	{
		nodesC.add(a.getId());
	}
	for (Category c : contextRelevantCategories)
	{
		nodesC.add(c.getId());
	}
	return nodesC;
}
}
