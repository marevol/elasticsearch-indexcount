package com.github.marevol.rest;

import static org.elasticsearch.rest.RestStatus.*;

import java.io.IOException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class IndexCountRestAction extends BaseRestHandler {

    @Inject
    public IndexCountRestAction(final Settings settings, final Client client,
            final RestController controller) {
        super(settings, controller, client);

        // /_index/countにGETでアクセスされた場合に呼ばれるように設定
        controller.registerHandler(RestRequest.Method.GET, "/_index/count",
                this);
    }

    @Override
    protected void handleRequest(final RestRequest request,
            final RestChannel channel, final Client client) {
        // インデックス情報を取得する
        client.admin().indices().prepareGetIndex()
                .execute(new ActionListener<GetIndexResponse>() {
                    @Override
                    public void onResponse(GetIndexResponse response) {
                        try {
                            // レスポンスのJSONを生成する
                            final XContentBuilder builder = JsonXContent
                                    .contentBuilder();
                            builder.startObject();
                            builder.field("size", response.getIndices().length);
                            builder.endObject();
                            // レスポンスを返却する
                            channel.sendResponse(new BytesRestResponse(OK,
                                    builder));
                        } catch (final IOException e) {
                            handleErrorRequest(channel, e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        handleErrorRequest(channel, e);
                    }
                });
    }

    private void handleErrorRequest(final RestChannel channel, final Throwable e) {
        try {
            // エラーレスポンスを返却する
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (final IOException e1) {
            logger.error("Failed to send a failure response.", e1);
        }
    }

}
