package com.github.marevol;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

import com.github.marevol.rest.IndexCountRestAction;

public class IndexCountPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "IndexCountPluginTest";
    }

    @Override
    public String description() {
        return "This is a elasticsearch-indexcount plugin.";
    }

    // for Rest API
    public void onModule(final RestModule module) {
        module.addRestAction(IndexCountRestAction.class);
    }

}
