package com.mycompany;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    protected AjaxLink<Void> refreshNotinmarkup = null;
    protected WebMarkupContainer notinmarkupContainer = null;
    protected Label notinmarkup = null;
    protected AjaxLink<Void> refreshListe = null;
    protected WebMarkupContainer listeContainer = null;
    protected ListView<String> liste = null;


    public HomePage(final PageParameters parameters) {
        super(parameters);

        add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

        notinmarkup = new Label("notinmarkup", Model.of("not in markup"));
        queue(notinmarkup);

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

        refreshListe = new AjaxLink<Void>("refreshListe")
        {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                target.add(listeContainer);
            }
        };
        queue(refreshListe);

        listeContainer = new WebMarkupContainer("listeContainer");
        listeContainer.setOutputMarkupId(true);
        queue(listeContainer);


        liste = new ListView<String>("liste")
        {
            @Override
            protected void populateItem(ListItem<String> item)
            {
                final Label inliste1 = new Label("inliste1", Model.of("inliste1"));
                item.queue(inliste1);

                final Label inliste2 = new Label("inliste2", Model.of("inliste2"));
                item.queue(inliste2);
            }
        };
        queue(liste);

        ArrayList<String> listeValues = new ArrayList<>();
        listeValues.add("value 1");
        liste.setList(listeValues);
    }
}
