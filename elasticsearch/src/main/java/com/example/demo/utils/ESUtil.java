package com.example.demo.utils;

import com.example.base.common.CommonException;
import com.example.base.common.CommonPage;
import com.example.base.utils.CommonUtil;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 17:34
 **/
@Component
public class ESUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ESUtil.class);
    private static final Logger LOGGER_ERROR = LoggerFactory.getLogger(ESUtil.class);

    @Autowired
    private TransportClient esClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.data.elasticsearch.highlight.tags:<em>}")
    private String tags;

    /**
     * 插入单条数据
     *
     * @param indexName 索引名
     * @param typeName  类型名
     * @param t         传入的对象
     * @return
     */
    public <T> boolean indexOne(String indexName, String typeName, T t) throws CommonException {
        try {
            checkNullIndex(indexName);
            IndexResponse response = this.esClient.prepareIndex(indexName, typeName)
                    .setSource(objectMapper.writeValueAsBytes(t), XContentType.JSON).get();
            if (response.status() == RestStatus.CREATED) {
                //创建成功
                LOGGER.info("插入单条ES数据成功！");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER_ERROR.error("插入单条ES数据失败！index：%s，type：%s", indexName, typeName);
            throw new CommonException("插入单条ES数据失败！");
        }
    }

    /**
     * 插入批量数据
     *
     * @param indexName 索引名
     * @param typeName  类型名
     * @param list      传入的对象
     * @return
     */
    public <T> boolean indexBulk(String indexName, String typeName, Collection<T> list) {
        try {
            //检查索引名是否为空
            checkNullIndex(indexName);
            //检查要插入的数据是否为空
            if (CollectionUtils.isEmpty(list)) return false;

            BulkRequestBuilder bulkRequestBuilder = this.esClient.prepareBulk();

            for (T t : list) {
                IndexRequestBuilder indexRequestBuilder = esClient.prepareIndex(indexName, typeName)
                        .setSource(objectMapper.writeValueAsBytes(t), XContentType.JSON);
                bulkRequestBuilder.add(indexRequestBuilder);
            }

            BulkResponse responses = bulkRequestBuilder.execute().actionGet();

            if (responses.status() == RestStatus.OK)
                return true;
            else
                return false;


        } catch (Exception e) {
            LOGGER_ERROR.error("插入批量ES数据失败！index：%s，type：%s", indexName, typeName);
            return false;
        }
    }

    /**
     * 修改单条数据
     *
     * @param indexName 索引名
     * @param typeName  类型名
     * @param t         传入的对象
     * @return
     */
    public <T> boolean updateOne(String indexName, String typeName, String esId, T t) throws CommonException {
        try {
            checkNullIndex(indexName);
            UpdateResponse response = this.esClient.prepareUpdate(indexName, typeName, esId)
                    .setDoc(objectMapper.writeValueAsBytes(t), XContentType.JSON).get();
            if (response.status() == RestStatus.OK) {
                //创建成功
                LOGGER.info("修改单条ES数据成功！");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER_ERROR.error("修改单条ES数据失败！index：%s，type：%s，esId：%s", indexName, typeName, esId);
            throw new CommonException("修改单条ES数据失败！");
        }
    }

    /**
     * 删除条件数据
     *
     * @param indexName  索引名
     * @param conditions 条件must in
     * @return
     */
    public boolean deleteAllByConditions(String indexName, List<ESSearchDto> conditions) throws CommonException {
        try {
            checkNullIndex(indexName);
            BulkByScrollResponse response = DeleteByQueryAction.INSTANCE
                    .newRequestBuilder(esClient).source(indexName)
                    .filter(builderQueries(conditions)).get();
            return true;
        } catch (Exception e) {
            LOGGER_ERROR.error("插入单条ES数据失败！index：%s", indexName);
            throw new CommonException("插入单条ES数据失败！");
        }
    }

    /**
     * 删除所有数据
     *
     * @param indexName 索引名
     * @return
     */
    public boolean deleteBulk(String indexName) {
        try {
            checkNullIndex(indexName);
            BulkByScrollResponse response = DeleteByQueryAction.INSTANCE
                    .newRequestBuilder(esClient).source(indexName).get();
            return true;
        } catch (Exception e) {
            LOGGER_ERROR.error("插入单条ES数据失败！index：%s", indexName);
            return false;
        }
    }

    /**
     * 根据字段条件查询分页
     * 返回所有字段
     *
     * @param indexName  索引名称,must in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @param pageable   分页,must in
     * @return CommonPage
     * @throws CommonException
     */
    public CommonPage queryAllPageByConditions(String indexName, List<ESSearchDto> conditions, SortBuilder sort, Pageable pageable) throws CommonException {

        try {
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int from = pageNumber * pageSize;

            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setFrom(from).setSize(pageSize).addSort(defaultScoreSort());

            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            List<Map<String, String>> maps = mapperResultToMap(response.getHits());
            Long count = queryCount(indexName, builderQueries(conditions));
            return CommonPage.toPage(maps, pageNumber, pageSize, Integer.parseInt(count.toString()));
        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }


    }

    /**
     * 根据指定的条件查询并返回list
     * 返回所有字段
     *
     * @param indexName  索引名称,nust in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @return
     */
    public List<Map<String, String>> queryAllListByFields(String indexName, List<ESSearchDto> conditions, SortBuilder sort) throws CommonException {
        try {
            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setSize(10000).addSort(defaultScoreSort());

            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = null;
            if (CollectionUtils.isEmpty(conditions))
                response = srb.execute().actionGet();
            else
                response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            return mapperResultToMap(response.getHits());
        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }


    /**
     * 根据字段条件查询分页
     * 返回所有字段
     *
     * @param indexName  索引名称,must in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @param pageable   分页,must in
     * @return CommonPage
     * @throws CommonException
     */
    public CommonPage queryAllPageByConditions(String indexName, List<ESSearchDto> conditions, SortBuilder sort, Pageable pageable, Class clazz) throws CommonException {

        try {
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int from = pageNumber * pageSize;

            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setFrom(from).setSize(pageSize).addSort(defaultScoreSort());
            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            List list = mapperResultToBean(response.getHits(), clazz);
            Long count = queryCount(indexName, builderQueries(conditions));
            return CommonPage.toPage(list, pageNumber, pageSize, Integer.parseInt(count.toString()));
        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }


    /**
     * 根据指定的条件查询并返回list
     * 返回所有字段
     *
     * @param indexName  索引名称,nust in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @return
     */
    public <T> List<T> queryAllListByFields(String indexName, List<ESSearchDto> conditions, SortBuilder sort, Class clazz) throws CommonException {
        try {
            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setSize(10000).addSort(defaultScoreSort());
            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            return mapperResultToBean(response.getHits(), clazz);
        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }


    /**
     * 根据字段条件查询分页
     * 返回指定字段
     *
     * @param indexName  索引名称,must in
     * @param params     指定字段 ,must in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @param pageable   分页,must in
     * @return CommonPage
     * @throws CommonException
     */
    public CommonPage queryPageByFields(String indexName, List<String> params, List<ESSearchDto> conditions, SortBuilder sort, Pageable pageable) throws CommonException {

        try {
            checkNullIndex(indexName);
            if (CollectionUtils.isEmpty(params)) {
                LOGGER_ERROR.error("指定字段不能为空！");
                throw new CommonException("指定字段不能为空！");
            }
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int from = pageNumber * pageSize;

            String[] shows = params.toArray(new String[params.size()]);

            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setFrom(from).setSize(pageSize)
                    .setFetchSource(shows, null).addSort(defaultScoreSort());
            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            List<Map<String, String>> maps = mapperResultToMap(response.getHits());
            Long count = queryCount(indexName, builderQueries(conditions));
            return CommonPage.toPage(maps, pageNumber, pageSize, Integer.parseInt(count.toString()));

        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }

    /**
     * 根据指定的条件查询并返回list
     * 返回指定字段
     *
     * @param indexName  索引名称,nust in
     * @param params     指定字段,must in
     * @param conditions 条件,should in
     * @param sort       见{buildSort}方法
     * @return
     */
    public List<Map<String, String>> queryListByFields(String indexName, List<String> params, List<ESSearchDto> conditions, SortBuilder sort) throws CommonException {
        try {
            checkNullIndex(indexName);
            if (CollectionUtils.isEmpty(params)) {
                LOGGER_ERROR.error("指定字段不能为空！");
                throw new CommonException("指定字段不能为空！");
            }

            String[] shows = params.toArray(new String[params.size()]);
            SearchRequestBuilder srb = esClient.prepareSearch(indexName)
                    .setFetchSource(shows, null).setSize(10000).addSort(defaultScoreSort());
            if (null != sort)
                srb = srb.addSort(sort);

            SearchResponse response = srb.setQuery(builderQueries(conditions)).execute().actionGet();
            return mapperResultToMap(response.getHits());
        } catch (Exception e) {
            String msg = "查询列表错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }


    /**
     * 根据指定字段来统计数量
     *
     * @param indexName  索引名称，must in
     * @param field      指定统计数量的字段,must in
     * @param conditions 条件,should in
     * @return
     */
    public Long queryCount(String indexName, String field, List<ESSearchDto> conditions) throws CommonException {
        try {
            checkNullIndex(indexName);
            if (StringUtils.isBlank(field))
                throw new CommonException("统计字段不能为空！");
            ValueCountAggregationBuilder aggregation =
                    AggregationBuilders
                            .count("count")
                            .field(field);
            SearchRequestBuilder sr = esClient.prepareSearch(indexName).addAggregation(aggregation);
            if (CollectionUtils.isNotEmpty(conditions))
                sr = sr.setQuery(builderQueries(conditions));
            SearchResponse response = sr.execute().get();
            ValueCount count = response.getAggregations().get("count");
            return count.getValue();
        } catch (Exception e) {
            String msg = "查询计数错误，索引为：" + indexName;
            LOGGER_ERROR.error(msg, e);
            throw new CommonException(msg);
        }
    }

    /**
     * @param indexName 索引名称
     * @param value     查询的值 多个值用空格分隔
     * @param fields    条件字段
     * @param pageable  分页信息
     * @return
     */
    public <T> CommonPage<T> queryAllStringMatch(String indexName, String value, String[] fields, Pageable pageable, Class clazz) throws CommonException {
        checkNullIndex(indexName);
        QueryBuilder builder = getQueryStringQueryBuilder(value, fields);

        int number = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int from = number * size;

        HighlightBuilder hiBuilder = new HighlightBuilder();
        hiBuilder.preTags(tags);
        String postTags = tags.replace("<", "</");
        hiBuilder.postTags(postTags);
        hiBuilder.field(fields.length == 1 ? fields[0] : "*");

        SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setFrom(from).setSize(size).addSort(defaultScoreSort())
                .setQuery(builder).highlighter(hiBuilder).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        List<T> ts = mapperHighlightResult(hits, clazz);
        Long aLong = 0L;
        try {
            aLong = queryCount(value, builder);
        } catch (CommonException e) {
            e.printStackTrace();
        }

        return CommonPage.toPage(ts, pageable.getPageNumber(), pageable.getPageSize(), Integer.parseInt(aLong.toString()));

    }

    /**
     * 查询总数
     *
     * @param indexName
     * @param queryBuilder
     * @return
     * @throws CommonException
     */
    private Long queryCount(String indexName, QueryBuilder queryBuilder) throws CommonException {

        SearchResponse searchResponse = esClient.prepareSearch(indexName)
                .setQuery(queryBuilder).execute().actionGet();
        long totalHits = searchResponse.getHits().getTotalHits();
        return totalHits;
    }


    public QueryStringQueryBuilder getQueryStringQueryBuilder(String value, String[] fields) {

        Map<String, Float> map = new HashMap();
        int length = fields.length;
        for (int i = 0; i < length; i++) {
            map.put(fields[i], Float.valueOf(length - i));
        }
        return QueryBuilders.queryStringQuery(value).fields(map);

    }


    /**
     * 映射结果
     *
     * @param hits
     * @return
     */
    private List<Map<String, String>> mapperResultToMap(SearchHits hits) {
        if (null == hits || hits.getTotalHits() == 0)
            return null;
        List list = new ArrayList<>();

        Iterator<SearchHit> iterator = hits.iterator();
        for (; iterator.hasNext(); ) {
            Map map = new HashMap();
            SearchHit next = iterator.next();
            String rsStr = next.getSourceAsString();
            if (StringUtils.isNotBlank(rsStr)) {
                JSONObject jsonObject = new JSONObject(rsStr);
                JSONArray names = jsonObject.names();
                int length = names.length();
                for (int i = 0; i < length; i++) {
                    String key = names.get(i).toString();
                    String value = jsonObject.get(key).toString();
                    map.put(key, value);
                }
                list.add(map);
            }
        }
        return list;
    }


    /**
     * 映射结果
     *
     * @param hits
     * @return
     */
    private <T> List<T> mapperResultToBean(SearchHits hits, Class<T> clazz) {
        if (null == hits || hits.getTotalHits() == 0)
            return null;
        List<T> list = new ArrayList<>();
        Iterator<SearchHit> iterator = hits.iterator();
        for (; iterator.hasNext(); ) {
            SearchHit next = iterator.next();
            String rsStr = next.getSourceAsString();
            if (StringUtils.isNotBlank(rsStr))
                list.add(this.mapEntity(rsStr, clazz));
        }
        return list;
    }

    /**
     * 映射高亮结果
     *
     * @param hits
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> List<T> mapperHighlightResult(SearchHits hits, Class<T> clazz) {
        if (null == hits || hits.getTotalHits() == 0)
            return null;
        List<T> hlist = new ArrayList<>();
        Iterator<SearchHit> iterator = hits.iterator();
        for (; iterator.hasNext(); ) {
            SearchHit hit = iterator.next();
            T result = null;
            if (StringUtils.isNotBlank(hit.getSourceAsString())) {
                result = this.mapEntity(hit.getSourceAsString(), clazz);
            } else {
                result = this.mapEntity(hit.getFields().values(), clazz);
            }
            //高亮处理
            Collection<HighlightField> values = hit.getHighlightFields().values();
            for (HighlightField highlightField : values) {
                CommonUtil.setProperty(result, highlightField.name(), highlightField.getFragments()[0].toString());
            }

            if (null != result)
                hlist.add(result);
        }

        return hlist;
    }

    /**
     * 将单个source转换为指定的实体对象
     *
     * @param source source
     * @param clazz  要转化的类名
     * @param <T>
     * @return
     */
    private <T> T mapEntity(String source, Class<T> clazz) {
        if (StringUtils.isBlank(source)) {
            return null;
        } else {
            try {
                return this.objectMapper.readValue(source, clazz);
            } catch (IOException var4) {
                throw new ElasticsearchException("failed to map source [ " + source + "] to class " + clazz.getSimpleName(), var4);
            }
        }
    }

    private <T> T mapEntity(Collection<SearchHitField> values, Class<T> clazz) {
        return this.mapEntity(this.buildJSONFromFields(values), clazz);
    }

    /**
     * 集合转json
     *
     * @param values
     * @return
     */
    private String buildJSONFromFields(Collection<SearchHitField> values) {
        JsonFactory nodeFactory = new JsonFactory();

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            Iterator var5 = values.iterator();

            while (true) {
                while (var5.hasNext()) {
                    SearchHitField value = (SearchHitField) var5.next();
                    if (value.getValues().size() > 1) {
                        generator.writeArrayFieldStart(value.getName());
                        Iterator var7 = value.getValues().iterator();

                        while (var7.hasNext()) {
                            Object val = var7.next();
                            generator.writeObject(val);
                        }

                        generator.writeEndArray();
                    } else {
                        generator.writeObjectField(value.getName(), value.getValue());
                    }
                }

                generator.writeEndObject();
                generator.flush();
                return new String(stream.toByteArray(), Charset.forName("UTF-8"));
            }
        } catch (IOException var9) {
            return null;
        }
    }


    /**
     * 校验索引为空
     *
     * @param indexName
     * @return
     * @throws CommonException
     */
    private boolean checkNullIndex(String indexName) throws CommonException {
        if (StringUtils.isNotBlank(indexName))
            return true;
        LOGGER_ERROR.error("索引不能为空！index：%s", indexName);
        throw new CommonException("索引不能为空！");
    }

    /**
     * 构建排序器
     * 传一个值时，只传字段名，默认倒序。
     * <p>
     * 传两个值时，传字段名和排序。
     * 字段名
     * 排序 默认为倒序， 倒序传入desc，正序为asc
     *
     * @return
     */
    public SortBuilder buildSort(String... sort) {
        String field = sort[0];
        if (StringUtils.isBlank(field))
            return null;
        FieldSortBuilder sortBuilder = SortBuilders.fieldSort(field);
        String order = "";
        if (sort.length > 1) order = sort[1];
        if ("asc".equals(order))
            return sortBuilder.order(SortOrder.ASC);
        else
            return sortBuilder.order(SortOrder.DESC);
    }

    private SortBuilder defaultScoreSort() {
        return SortBuilders.scoreSort();
    }

    /**
     * 构建条件
     *
     * @param conditions
     * @return
     */
    private QueryBuilder builderQueries(List<ESSearchDto> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            LOGGER.info("条件为空！");
            return null;
        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        conditions.forEach(esSearchDto -> {
            ESSearchDto.ESType type = esSearchDto.getType();
            String field = esSearchDto.getField();
            String value = esSearchDto.getValue();
            if (StringUtils.isBlank(field) || StringUtils.isBlank(value))
                return;
            switch (type) {
                case MATCH:
                    //对于带有任意中文的字符串都是按照中文匹配规则来算
                    QueryBuilder mWildcardQuery = QueryBuilders
                            .wildcardQuery(field, wildcardQueryStr(value));
                    boolQueryBuilder.should(mWildcardQuery);
                    QueryBuilder matchQuery = QueryBuilders.matchQuery(field, value);
                    boolQueryBuilder.should(matchQuery);
                    break;
                case TERM:
                    TermQueryBuilder termQuery = QueryBuilders.termQuery(field, value);
                    boolQueryBuilder.must(termQuery);
                    break;
                case WILDCARD:
                    QueryBuilder wildcardQuery = QueryBuilders
                            .wildcardQuery(field, wildcardQueryStr(value));
                    boolQueryBuilder.must(wildcardQuery);
                    break;
                case NOT_NULL:
                    ExistsQueryBuilder existsQuery = QueryBuilders.existsQuery(field);
                    boolQueryBuilder.should(existsQuery);
                    break;
                case NULL:
                    ExistsQueryBuilder existsQuery1 = QueryBuilders.existsQuery(field);
                    boolQueryBuilder.mustNot(existsQuery1);
                    break;
                case RANGE_EX:
                    if (value.indexOf(",") != -1) {
                        String[] split = value.split(",");
                        if (split.length > 0) {
                            if (value.endsWith(",")) {
                                QueryBuilder from = QueryBuilders.rangeQuery(field).gt(split[0]);
                                boolQueryBuilder.must(from);
                            } else if (value.startsWith(",")) {
                                QueryBuilder from = QueryBuilders.rangeQuery(field).lt(split[1]);
                                boolQueryBuilder.must(from);
                            } else {
                                QueryBuilder from = QueryBuilders.rangeQuery(field)
                                        .gt(split[0]).lt(split[1]);
                                boolQueryBuilder.must(from);
                            }
                        }
                    }
                    break;
                case RANGE_IN:
                    if (value.indexOf(",") != -1) {
                        String[] split = value.split(",");
                        if (split.length > 0) {
                            if (value.endsWith(",")) {
                                QueryBuilder from = QueryBuilders.rangeQuery(field).from(split[0]);
                                boolQueryBuilder.must(from);
                            } else if (value.startsWith(",")) {
                                QueryBuilder from = QueryBuilders.rangeQuery(field).to(split[1]);
                                boolQueryBuilder.must(from);
                            } else {
                                QueryBuilder from = QueryBuilders.rangeQuery(field)
                                        .from(split[0]).to(split[1]);
                                boolQueryBuilder.must(from);
                            }
                        }
                    }
                    break;
                case IN:
                    String[] split = value.split(",");
                    TermsQueryBuilder terms = QueryBuilders.termsQuery(field, split);
                    boolQueryBuilder.must(terms);
                    break;

            }

        });
        LOGGER.info("构建条件完成！");
        return boolQueryBuilder;
    }

    private String wildcardQueryStr(String value) {
        return "*" + value + "*";
    }

    /*public AggregationBuilder buildAggergation(ESSearchDto condition) {
        if (null == condition) {
            LOGGER.info("条件为空！");
            return null;
        }
        AggregationBuilder aggregation = null;

        ESType type = condition.getType();
        String field = condition.getField();
        String value = condition.getValue();
        switch (type) {
            case RANGE_DATE:
                if (value.indexOf(",") == -1)
                    return null;
                String format = "yyyy-MM-dd HH:mm:ss";
                String[] split = value.split(",");
                if (value.startsWith(",")) {
                    aggregation = AggregationBuilders.dateRange("agg")
                            .field(field)
                            .format(format).addUnboundedTo(split[0]);
                    return aggregation;
                } else if (value.endsWith(",")) {
                    aggregation = AggregationBuilders.dateRange("agg")
                            .field(field)
                            .format(format).addUnboundedFrom(split[0]);
                    return aggregation;
                } else {
                    aggregation = AggregationBuilders.dateRange("agg")
                            .field(field)
                            .addRange(split[0], split[1]);
                    return aggregation;
                }

        }
        return aggregation;
    }*/

}