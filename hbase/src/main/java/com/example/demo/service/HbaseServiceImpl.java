package com.example.demo.service;

import com.example.demo.entity.HbaseDo;
import com.example.demo.repository.HbaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 18:24
 **/

public class HbaseServiceImpl {

    @Autowired
    private HbaseRepository hbaseRepository;

    public static final String FAMILY_NAME = "lingbao";
    private String tableName ="TABLE_1";
    private int size = 10;

    public void test() {

        Set<HbaseDo> conditions = new HashSet<>();


        List<String> exportFields = new ArrayList(){{add("1");}};
        Map<String, Object> result = hbaseRepository
                .getRowByColumnFilter(tableName, size, null, getFilterList(conditions), exportFields);

        System.out.println(result);
    }

    /**
     * 获取查询条件的list
     *
     * @param filtersClause
     * @return
     */
    public List<Filter> getFilterList(Set<HbaseDo> filtersClause) {
        List<Filter> filters = new ArrayList<>();
        boolean isLike = true;//是否模糊查询
        if (null == filtersClause || filtersClause.size() == 0)
            return null;
        for (HbaseDo condition : filtersClause) {
            String value = condition.getValue();
            String paramEn = condition.getParamEn();

            if (StringUtils.isBlank(paramEn))
                return null;
            //该条件只有一个值
            if (value.indexOf(",") == -1) {
                //先设置精确条件，然后判断是否是S1字符串类型，若是，则模糊查询
                SingleColumnValueFilter filter = hbaseRepository.createColumnExactFilter(FAMILY_NAME, paramEn, "=", value);

                //针对S1类型，字符串就模糊查询
                if (isLike)
                    filter = hbaseRepository.createColumnFilter(FAMILY_NAME, paramEn, "=", new SubstringComparator(value));

                filters.add(filter);
            } else {
                //该条件有两个值，比如日期段
                String[] split = value.split(",");
                String s0 = split[0];
                String s1 = split[1];
                if (StringUtils.isNotBlank(s0)) {
                    SingleColumnValueFilter filter = hbaseRepository.createColumnExactFilter(FAMILY_NAME, paramEn, ">=", s0);
                    filters.add(filter);
                }
                if (StringUtils.isNotBlank(s1)) {
                    SingleColumnValueFilter filter = hbaseRepository.createColumnExactFilter(FAMILY_NAME, paramEn, "<=", s1);
                    filters.add(filter);
                }

            }
        }
        return filters;
    }
}
