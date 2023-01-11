package com.mycompany;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;

public class TestPage extends WebPage
{
    private static final long serialVersionUID = 1L;
    private LDM ldm = new LDM();
    protected AjaxLink<Void> refreshNotinmarkup = null;
    protected WebMarkupContainer notinmarkupContainer = null;
    protected Label notinmarkup = null;
    protected AjaxLink<Void> refreshListe = null;
    protected WebMarkupContainer listeContainer = null;
    protected ListView<String> liste = null;

    public TestPage( final PageParameters parameters )
    {
        super( parameters );

        add( new Label( "renderCount", this::getRenderCount ) );
        add( new Label( "model", ldm ) );

        add( new ExternalLink( "stale", () -> {
            var baseUrl = urlFor( new RenderPageRequestHandler( getPage() ) );
            return baseUrl + "-999.-btn";
        } ) );
        add( new Link<Void>( "home" )
        {
            @Override
            public void onClick()
            {
                setResponsePage( new HomePage3( null ) );
            }
        } );

        add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

        notinmarkup = new Label("notinmarkup", Model.of("not in markup"));
//        queue(notinmarkup);

        notinmarkupContainer = new WebMarkupContainer("notinmarkupContainer");
        notinmarkupContainer.setOutputMarkupId(true);
        queue(notinmarkupContainer);

        refreshNotinmarkup = new AjaxLink<Void>("refreshNotinmarkup")
        {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                target.add(notinmarkupContainer);
            }
        };
        queue(refreshNotinmarkup);

//        refreshListe = new AjaxLink<Void>("refreshListe")
//        {
//            @Override
//            public void onClick(AjaxRequestTarget target)
//            {
//                target.add(listeContainer);
//            }
//        };
//        queue(refreshListe);
//
//        listeContainer = new WebMarkupContainer("listeContainer");
//        listeContainer.setOutputMarkupId(true);
//        queue(listeContainer);
//
//
//        liste = new ListView<String>("liste")
//        {
//            @Override
//            protected void populateItem(ListItem<String> item)
//            {
//                final Label inliste1 = new Label("inliste1", Model.of("inliste1"));
//                item.queue(inliste1);
//
//                final Label inliste2 = new Label("inliste2", Model.of("inliste2"));
//                item.queue(inliste2);
//            }
//        };
//        queue(liste);
//
//        ArrayList<String> listeValues = new ArrayList<>();
//        listeValues.add("value 1");
//        liste.setList(listeValues);
    }

    @Override
    protected void onBeforeRender()
    {
        System.out.println( "Model still attached? " + ldm.isAttached() );

        super.onBeforeRender();
    }

    @Override
    protected void onDetach()
    {
        super.onDetach();

        System.out.println( "Page detaching" );
    }

    private class LDM extends LoadableDetachableModel<String>
    {
        @Override
        protected String load()
        {
            return "Dummy";
        }
    }
}
