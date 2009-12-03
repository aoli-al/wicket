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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ng.request.component.PageFactory;
import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.util.lang.Generics;


/**
 * A factory that constructs Pages.
 * 
 * @see org.apache.wicket.settings.ISessionSettings#setPageFactory(PageFactory)
 * @see PageFactory
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultPageFactory implements PageFactory
{
	/** Map of Constructors for Page subclasses */
	private final Map<Class<?>, Constructor<?>> constructorForClass = Generics.newConcurrentHashMap();

	/**
	 * @see PageFactory#newPage(Class)
	 */
	public final <C extends RequestablePage> Page newPage(final Class<C> pageClass)
	{
		try
		{
			// throw an exception in case default constructor is missing
			// => improved error message
			final Constructor<? extends RequestablePage> constructor = pageClass.getConstructor((Class[])null);

			return processPage(newPage(constructor, null), null);
		}
		catch (NoSuchMethodException e)
		{
			// a bit of a hack here..
			Constructor<?> constructor = constructor(pageClass, PageParametersNg.class);
			if (constructor != null)
			{
				return newPage(constructor, new PageParametersNg());
			}
			else
			{
				throw new WicketRuntimeException("Unable to create page from " + pageClass +
					". Class does not have a default contructor", e);
			}
		}
	}

	/**
	 * @see PageFactory#newPage(Class, PageParametersNg)
	 */
	public final <C extends RequestablePage> Page newPage(final Class<C> pageClass,
		final PageParametersNg parameters)
	{
		// Try to get constructor that takes PageParameters
		Constructor<?> constructor = constructor(pageClass, PageParametersNg.class);

		// If we got a PageParameters constructor
		if (constructor != null)
		{
			// return new Page(parameters)
			return processPage(newPage(constructor, parameters), parameters);
		}

		// Always try default constructor if one exists
		return processPage(newPage(pageClass), parameters);
	}

	/**
	 * Looks up a one-arg Page constructor by class and argument type.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            The class of page
	 * @param argumentType
	 *            The argument type
	 * @return The page constructor, or null if no one-arg constructor can be found taking the given
	 *         argument type.
	 */
	private final <C extends RequestablePage> Constructor<?> constructor(final Class<C> pageClass,
		final Class<PageParametersNg> argumentType)
	{
		// Get constructor for page class from cache
		Constructor<?> constructor = constructorForClass.get(pageClass);

		// Need to look up?
		if (constructor == null)
		{
			try
			{
				// Try to find the constructor
				constructor = pageClass.getConstructor(new Class[] { argumentType });

				// Store it in the cache
				constructorForClass.put(pageClass, constructor);
			}
			catch (NoSuchMethodException e)
			{
				return null;
			}
		}

		return constructor;
	}

	/**
	 * Creates a new Page using the given constructor and argument.
	 * 
	 * @param constructor
	 *            The constructor to invoke
	 * @param argument
	 *            The argument to pass to the constructor or null to pass no arguments
	 * @return The new page
	 * @throws WicketRuntimeException
	 *             Thrown if the Page cannot be instantiated using the given constructor and
	 *             argument.
	 */
	private final Page newPage(final Constructor<?> constructor, final Object argument)
	{
		try
		{
			if (argument != null)
				return (Page)constructor.newInstance(new Object[] { argument });
			else
				return (Page)constructor.newInstance(new Object[] { });
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
		catch (InvocationTargetException e)
		{
			// honor redirect exception contract defined in IPageFactory
			// TODO - Honor the exceptions
// if (e.getTargetException() instanceof AbortException ||
// e.getTargetException() instanceof AuthorizationException ||
// e.getTargetException() instanceof MarkupException)
// {
// throw (RuntimeException)e.getTargetException();
// }
			throw new WicketRuntimeException(createDescription(constructor, argument), e);
		}
	}

	private Page processPage(Page page, PageParametersNg pageParameters)
	{
		// the page might have not propagate page parameters from constructor. if that's the case
		// we force the parameters
		if (pageParameters != null && page.getPageParametersNg() != pageParameters)
		{
			page.getPageParametersNg().assign(pageParameters);
		}

		page.setWasCreatedBookmarkable(true);

		return page;
	}

	private String createDescription(Constructor<?> constructor, Object argument)
	{
		if (argument != null)
			return "Can't instantiate page using constructor " + constructor + " and argument " +
				argument;
		else
			return "Can't instantiate page using constructor " + constructor;
	}
}