package com.github.marevol;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.codelibs.elasticsearch.runner.net.Curl;
import org.codelibs.elasticsearch.runner.net.CurlResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexCountPluginTest {

    private ElasticsearchClusterRunner runner;

    @Before
    public void setUp() throws Exception {
        // Cluster Runnerのインスタンス作成
        runner = new ElasticsearchClusterRunner();
        // Cluster Runnerの初期化
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(final int number, final Builder settingsBuilder) {
                // elasticsearch.ymlに書くような内容を記述
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("index.number_of_replicas", 0);
            }
            // Elasticsearchのノード数を1に設定
        }).build(newConfigs().ramIndexStore().numOfNode(1));

        // クラスタ状態がYellowになるのを待つ
        runner.ensureYellow();
    }

    @After
    public void cleanUp() throws Exception {
        // Cluster Runnerの終了
        runner.close();
        // 作成したものを削除(ただし、Windowsでは消せない…)
        runner.clean();
    }

    @Test
    public void test_runCluster() throws Exception {
        final String index = "dataset";
        final String type = "item";

        // インデックスの作成
        runner.createIndex(index, null);

        // 適当に1000ドキュメント作成
        for (int i = 1; i <= 1000; i++) {
            final IndexResponse indexResponse1 = runner.insert(index, type,
                    String.valueOf(i), "{\"msg\":\"test " + i + "\", \"id\":\""
                            + i + "\"}");
            assertTrue(indexResponse1.isCreated());
        }
        runner.refresh();

        // 1000ドキュメント作成されたか確認
        {
            final SearchResponse searchResponse = runner.search(index, type,
                    null, null, 0, 10);
            assertEquals(1000, searchResponse.getHits().getTotalHits());
        }

        // /_index/count にGETでアクセス
        try (CurlResponse curlResponse = Curl.get(runner.node(),
                "/_index/count").execute()) {
            assertEquals(200, curlResponse.getHttpStatusCode());
            // レスポンスで返却されたJSONをMapで取得
            Map<String, Object> content = curlResponse.getContentAsMap();
            // {"size":1} が返却される想定
            assertEquals(1, content.get("size"));
        }
    }
}
