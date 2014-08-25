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
public class Univers {

Set<Article> articles;
Set<Category> categories;

public Univers(Set<Article> articles, Set<Category> categories)
{
		this.articles = new HashSet<>(articles);
		this.categories = new HashSet<>(categories);
}

public Univers()
{
		this.articles = new HashSet<>();
		this.categories = new HashSet<>();
}

public List<Long> getIds()
{
		List<Long> ids = new ArrayList(articles.size() + categories.size());
		for (Category c : categories)
		{
				ids.add(c.getId());
		}
		for (Article a : articles)
		{
				ids.add(a.getId());
		}
		return ids;
}

public Set<Article> getArticles()
{
		return articles;
}

private Set<Category> getCategories()
{
		return categories;
}

public void fusion(Univers universFromInputArticles)
{
		this.articles.addAll(universFromInputArticles.getArticles());
		this.categories.addAll(universFromInputArticles.getCategories());
}
}
