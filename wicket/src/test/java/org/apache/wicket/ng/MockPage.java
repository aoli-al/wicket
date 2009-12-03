/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ng;

import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestablePage;

/**
 * Simple {@link RequestablePage} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class MockPage extends MockComponent implements RequestablePage
{
	
	private static final long serialVersionUID = 1L;
	
	private int pageId;
	
	/**
	 * Construct.
	 * 
	 * @param pageId
	 */
	public MockPage()
	{
		setPath("");
	}

	
	/**
	 * Construct.
	 * @param pageId
	 * @param pageVersion
	 * @param pageMapName
	 */
	public MockPage(int pageId)
	{
		setPageId(pageId);
	}
	
	/**
	 * Sets the page id
	 * @param pageId
	 * @return <code>this</code>
	 */
	public MockPage setPageId(int pageId)
	{
		this.pageId = pageId;
		return this;
	}
	
	@Override
	public RequestablePage getPage()
	{
		return this;
	}

	public int getPageId()
	{
		return pageId;
	}	
	
	private PageParametersNg pageParameters = new PageParametersNg();

	public PageParametersNg getPageParametersNg()
	{
		return pageParameters;
	}		

	private boolean bookmarkable;
	
	public boolean isBookmarkable()
	{
		return bookmarkable;
	}
	
	/**
	 * Sets the bookmarkable flags
	 * 
	 * @param bookmarkable
	 * @return <code>this</code>
	 */
	public MockPage setBookmarkable(boolean bookmarkable)
	{
		this.bookmarkable = bookmarkable;
		return this;
	}

	private boolean stateless = false;
	
	/**
	 * Sets the stateless flag
	 * 
	 * @param stateless
	 * @return <code>this</code>
	 */
	public MockPage setPageStateless(boolean stateless)
	{
		this.stateless = stateless;
		return this;
	}
	
	public boolean isPageStateless()
	{
		return stateless;
	}

	public void renderPage()
	{
	}
	
	private boolean createBookmarkable;

	public boolean wasCreatedBookmarkable()
	{
		return createBookmarkable;
	}
	
	/**
	 * Sets the createdBookmarkable flag.
	 * 
	 * @see RequestablePage#wasCreatedBookmarkable()
	 * 
	 * @param createdBookmarkable
	 * @return <code>this</code>
	 */
	public MockPage setCreatedBookmarkable(boolean createdBookmarkable)
	{
		this.createBookmarkable = createdBookmarkable;
		return this;
	}

	private int renderCount;
	
	public int getRenderCount()
	{
		return renderCount;
	}
	
	/**
	 * Sets the render count
	 * @param renderCount
	 */
	public void setRenderCount(int renderCount)
	{
		this.renderCount = renderCount;
	}
}
