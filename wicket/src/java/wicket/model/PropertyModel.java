/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;

import java.lang.reflect.Member;
import java.util.Map;

import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import wicket.ApplicationSettings;
import wicket.Component;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.util.convert.IConverter;
import wicket.util.convert.IStringConverter;

/**
 * A PropertyModel is used to dynamically access a model using an <a
 * href="www.ognl.org">Ognl expression </a>.
 * <p>
 * For example, take the following bean:
 * 
 * <pre>
 * public class Person
 * {
 * 	private String name;
 * 
 * 	public String getName()
 * 	{
 * 		return name;
 * 	}
 * 
 * 	public void setName(String name)
 * 	{
 * 		this.name = name;
 * 	}
 * }
 * </pre>
 * 
 * We could construct a label that dynamically fetches the name property of the given
 * person object like this:
 * 
 * <pre>
 * 
 *  
 *        Person person = getSomePerson();
 *        ...
 *        add(new Label(&quot;myLabel&quot;, person, &quot;name&quot;);
 *   
 *  
 * </pre>
 * 
 * Where 'myLabel' is the name of the component, and 'name' is the Ognl expression to get
 * the name property.
 * </p>
 * <p>
 * In the same fashion, we can create form components that work dynamically on the given
 * model object. For instance, we could create a text field that updates the name property
 * of a person like this:
 * 
 * <pre>
 * 
 *  
 *        add(new TextField(&quot;myTextField&quot;, person, &quot;name&quot;);
 *   
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * For conversions and formatting, the converter sub-framework is used. This allows for
 * plugging in custom converters and formatters that are used for converting and
 * formatting objects of a given type.
 * </p>
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.DetachableModel
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class PropertyModel extends DetachableModel implements IComponentAware
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3136339624173288385L;

	/**
	 * When true, additionial formatting is done on the result object. It handles the part
	 * of localization where you want model variables like numbers and dates formatted in a
	 * localized way.
	 */
	private boolean applyFormatting;

	/** Ognl context wrapper object. It contains the type converter. */
	private transient OgnlContext context;

	/** Ognl expression for property access. */
	private final String expression;

	/** The model. */
	private final IModel model;

	/** The component that uses this model. */
	private Component component;

	/**
	 * This class is registered with the Ognl context before parsing in order to be able to
	 * use our converters. It implements Ognl TypeConverter and uses the ConverterRegistry
	 * to lookup converters. If no converter is found for a given type, the default
	 * conversion of Ognl is used.
	 */
	protected final class OgnlConverterWrapper extends DefaultTypeConverter
	{
		/**
		 * Construct.
		 */
		public OgnlConverterWrapper()
		{
		}

		/**
		 * Converts the provided value to provided type using provided context.
		 * @param context Ognl context
		 * @param value The current, unconverted value
		 * @param toType The type that should be converted to
		 * @return Object the converted value
		 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map, java.lang.Object,
		 *      java.lang.Class)
		 */
		public Object convertValue(Map context, Object value, Class toType)
		{
			if (value == null)
			{
				return null;
			}

			if (!toType.isArray() && value instanceof String[] && ((String[])value).length == 1)
			{
				value = ((String[])value)[0];
			}

			if (value instanceof String && ((String)value).trim().equals(""))
			{
				return null;
			}
			IConverter converter = component.getConverter();
			return converter.convert(value, toType);
		}

		/**
		 * This method is only here to satisfy the interface. Method convertValue(Map,
		 * Object, Class) is called, so parameters member and propertyName are ignored.
		 * @param context The context
		 * @param target The target
		 * @param member The member
		 * @param propertyName The name of the property
		 * @param value The value
		 * @param toType The type to convert to
		 * @return the converted value
		 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map, java.lang.Object,
		 *      java.lang.Class)
		 */
		public Object convertValue(Map context, Object target,
				Member member, String propertyName,
				Object value, Class toType)
		{
			return convertValue(context, value, toType);
		}
	}

	/**
	 * Construct with an IModel object and a Ognl expression that works on the given model.
	 * Additional formatting will be used depending on the configuration setting.
	 * @param model the wrapper
	 * @param expression Ognl expression for property access
	 */
	public PropertyModel(final IModel model, final String expression)
	{
		super(null);

		checkModelNotNull(model);

		this.model = model;
		this.expression = expression;
		this.applyFormatting = false;

		// PropertyModel might be used while RequestCycle.get() has not yet been
		// initialized (e.g. may happen during junit tests)
		if (RequestCycle.get() != null)
		{
			ApplicationSettings settings = RequestCycle.get().getApplication().getSettings();
			this.applyFormatting = settings.getPropertyModelDefaultApplyFormatting();
		}
	}

	/**
	 * Construct with an IModel object and a Ognl expression that works on the given model.
	 * @param model the model that is used to get and set the property values
	 * @param expression Ognl expression for property access
	 * @param applyFormatting When true, additionial formatting is done on the result
	 *           object. It handles the part of localization where you want model variables
	 *           like numbers and dates formatted in a localized way. See also properties
	 *           formatterName and formatPattern.
	 */
	public PropertyModel(final IModel model, final String expression, boolean applyFormatting)
	{
		super(null);

		checkModelNotNull(model);

		this.model = model;
		this.expression = expression;
		this.applyFormatting = applyFormatting;
	}

	/**
	 * Sets the component that uses this model.
	 * @param component the component that uses this model
	 * @see wicket.model.IComponentAware#setComponent(wicket.Component)
	 */
	public void setComponent(Component component)
	{
		this.component = component;
	}
	
	/**
	 * Gets the value that results when the given Ognl expression is applied to the model
	 * object (Ognl.getValue).
	 * @return the value that results when the given Ognl expression is applied to the
	 *         model object
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		String expr = getExpression();
		if (expr == null || expr.trim().length() == 0)
		{
			// No expression will cause OGNL to throw an exception. The OGNL
			// expression to return the current object is "#this". Instead
			// of throwing that exception, we'll provide a meaningfull
			// return value
			return model.getObject();
		}
		IModel theModel = getModel();
		if (theModel == null)
		{
			return null;
		}
		Object modelObject = theModel.getObject();
		if (modelObject == null)
		{
			return null;
		}
		try
		{
			OgnlContext ctx = getContext();
			Object raw = Ognl.getValue(expr, ctx, modelObject);
			if (applyFormatting)
			{
				IStringConverter converter = component.getConverter();
				return converter.toString(raw);
			}
			else
			{
				return raw;
			}
		}
		catch (OgnlException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Applies the Ognl expression on the model object using the given object argument
	 * (Ognl.setValue).
	 * @param object the object that will be used when applying Ognl.setValue on the model
	 *           object
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		try
		{
			String expr = getExpression();
			Object target = getModel().getObject();
			OgnlContext ctx = getContext();
			Ognl.setValue(expr, ctx, target, object);
		}
		catch (OgnlException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Initializes the instance variables of this property model, and in case the
	 * wrapped model is a {@link IDetachableModel}, calls attach on the wrapped model.
	 * @see wicket.model.DetachableModel#doAttach()
	 */
	protected final void doAttach()
	{
		if (model instanceof IDetachableModel)
		{
			((IDetachableModel)model).attach();
		}

		// Save the reference to the current locale
		Session session = RequestCycle.get().getSession();
	}

	/**
	 * Unsets this property model's instance variables and, in case the
	 * wrapped model is a {@link IDetachableModel}, calls dettach on the
	 * wrapped model.
	 * @see wicket.model.DetachableModel#doDetach()
	 */
	protected final void doDetach()
	{
		if (model instanceof IDetachableModel)
		{
			((IDetachableModel)model).detach();
		}

		// Reset OGNL context
		this.context = null;
	}

	/**
	 * Gets whether to apply formatting when getObject is invoked.
	 * @return Whether to apply formatting when getObject is invoked.
	 */
	protected final boolean getApplyFormatting()
	{
		return applyFormatting;
	}

	/**
	 * Gets the Ognl context that is used for evaluating expressions. It contains the type
	 * converter that is used to access the converter framework.
	 * @return the Ognl context that is used for evaluating expressions.
	 */
	protected final OgnlContext getContext()
	{
		if (context == null)
		{
			// Setup ognl context for this request
			this.context = new OgnlContext();
			context.setTypeConverter(new OgnlConverterWrapper());
		}
		return context;
	}

	/**
	 * Gets the Ognl expression that works on the model. This expression is used with both
	 * Ognl.getValue (used in getObject) and Ognl.setValue (used in setObject). Usually,
	 * this expression accords with simple property acces (like if we have a Person object
	 * with a name property, the expression would be 'name'), but it can in principle
	 * contain any valid Ognl expression that has meaning with both the Ognl.getValue and
	 * Ognl.setValue operations.
	 * @return expression the Ognl expression that works on the model.
	 */
	protected final String getExpression()
	{
		return expression;
	}

	/**
	 * Gets the model on which the Ognl expressions are applied. The expression will
	 * actually not be applied on the instance of IModel, but (naturally) on the wrapped
	 * model object or more accurate, the object that results from calling getObject on the
	 * instance of IModel.
	 * @return The model on which the Ognl expressions are applied.
	 */
	protected final IModel getModel()
	{
		return model;
	}

	/**
	 * Sets the Ognl context that is used for evaluating expressions. It contains the type
	 * converter that is used to access the converter framework.
	 * @param context the Ognl context that is used for evaluating expressions
	 */
	protected final void setContext(OgnlContext context)
	{
		this.context = context;
	}

	/**
	 * Checks the given model; if null, a null pointer exception is thrown.
	 * @param model the model to check
	 */
	private final void checkModelNotNull(final IModel model)
	{
		if (model == null)
		{
			throw new IllegalArgumentException("Model parameter must not be null");
		}
	}
}