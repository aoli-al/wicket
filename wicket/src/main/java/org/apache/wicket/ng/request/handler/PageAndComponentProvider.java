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
package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.lang.Checks;

/**
 * Extension of {@link DefaultPageProvider} that is also capable of providing a Component belonging to the
 * page.
 * 
 * @see DefaultPageProvider
 * 
 * @author Matej Knopp
 */
public class PageAndComponentProvider extends DefaultPageProvider
{
	private RequestableComponent component;
	private String componentPath;

	/**
	 * @see DefaultPageProvider#PageProvider(RequestablePage)
	 * 
	 * @param page
	 * @param componentPath
	 */
	public PageAndComponentProvider(RequestablePage page, String componentPath)
	{
		super(page);
		setComponentPath(componentPath);
	}

	/**
	 * @see DefaultPageProvider#PageProvider(RequestablePage)
	 * 
	 * @param page
	 * @param component
	 */
	public PageAndComponentProvider(RequestablePage page, RequestableComponent component)
	{
		super(page);

		Checks.argumentNotNull(component, "component");

		this.component = component;
	}

	/**
	 * @see DefaultPageProvider#PageProvider(Class, PageParametersNg)
	 * 
	 * @param pageClass
	 * @param pageParameters
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends RequestablePage> pageClass,
		PageParametersNg pageParameters, String componentPath)
	{
		super(pageClass, pageParameters);
		setComponentPath(componentPath);
	}

	/**
	 * @see DefaultPageProvider#PageProvider(Class)
	 * 
	 * @param pageClass
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends RequestablePage> pageClass, String componentPath)
	{
		super(pageClass);
		setComponentPath(componentPath);
	}

	/**
	 * @see DefaultPageProvider#PageProvider(int, Class, Integer)
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends RequestablePage> pageClass,
		Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see DefaultPageProvider#PageProvider(int, Class, PageParametersNg, Integer)
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param pageParameters
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends RequestablePage> pageClass,
		PageParametersNg pageParameters, Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, pageParameters, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see DefaultPageProvider#PageProvider(int, Integer)
	 * 
	 * @param pageId
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Integer renderCount, String componentPath)
	{
		super(pageId, renderCount);
		setComponentPath(componentPath);
	}

	@Override
	protected boolean prepareForRenderNewPage()
	{
		return true;
	}

	/**
	 * Returns component on specified page with given path.
	 * 
	 * @return component
	 */
	public RequestableComponent getComponent()
	{
		if (component == null)
		{
			RequestablePage page = getPageInstance();
			component = page.get(componentPath);
		}
		if (component == null)
		{
			throw new ComponentNotFoundException("Could not find component '" + componentPath +
				"' on page '" + getPageClass());
		}
		return component;
	}

	/**
	 * Returns the component path.
	 * 
	 * @return component path
	 */
	public String getComponentPath()
	{
		if (componentPath != null)
		{
			return componentPath;
		}
		else
		{
			return component.getPath();
		}
	}

	private void setComponentPath(String componentPath)
	{
		Checks.argumentNotNull(componentPath, "componentPath");

		this.componentPath = componentPath;
	}
}
