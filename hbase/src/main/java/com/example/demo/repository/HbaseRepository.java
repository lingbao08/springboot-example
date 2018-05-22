package com.example.demo.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.TableCallback;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 18:16
 **/

@Component
public class HbaseRepository {

    @Autowired
    private HbaseTemplate hbaseTemplate;

    /**
     * 根据列进行过滤查询
     *
     * @param tableName     表名
     * @param size          每页展示条数
     * @param filters       条件集合
     * @param showParameter 展示字段，全部字段可以传*
     */
    public Map<String, Object> getRowByColumnFilter(String tableName, final int size, String nextRowKey, List<Filter> filters, List<String> showParameter) {
        try {
            Map<String, Object> map = new HashMap<>();
            return hbaseTemplate.execute(tableName, new TableCallback<Map>() {
                @Override
                public Map doInTable(HTableInterface hTableInterface) throws Throwable {
                    FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
                    Filter filterNum = new PageFilter(size + 1);// 每页展示条数
                    fl.addFilter(filterNum);
                    // 过滤器的添加
                    if (CollectionUtils.isNotEmpty(filters))
                        fl.addFilter(filters);

                    Scan scan = new Scan();
                    scan.setFilter(fl);// 为查询设置过滤器的list
                    if (StringUtils.isNotBlank(nextRowKey))
                        scan.setStartRow(nextRowKey.getBytes());
                    ResultScanner rscanner = hTableInterface.getScanner(scan);
                    Result[] results = rscanner.next(size);
                    map.put("content", mapJson(results, showParameter));
                    String rowNext = "endPage";
                    //下一次的起始rowkey
                    for (Result result : rscanner) {
                        rowNext = Bytes.toString(result.getRow());
                    }
                    map.put("nextRowKey", rowNext);
                    //总数
                    Long count = rowCount(tableName);
                    map.put("totalCount",count);
                    return map;
                }
            });
        } catch (Exception e) {
            //表不存在的异常
            if (e.getCause() instanceof TableNotFoundException)
                throw new IllegalArgumentException(e.getCause().getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 批量根据rowkey进行查询
     *
     * @param tableName     表名
     * @param rowKeys       rowkey数组
     * @param showParameter 展示字段，全部字段可以传*
     * @return
     */
    public List<Map<String, String>> getRowByRowKey(String tableName, List<String> rowKeys, List<String> showParameter) {
        return hbaseTemplate.execute(tableName, new TableCallback<List>() {
            @Override
            public List doInTable(HTableInterface table) throws Throwable {
                List<Get> gets = new ArrayList<>();
                for (String rowKey : rowKeys) {
                    Get get = new Get(rowKey.getBytes());
                    gets.add(get);
                }
                Result[] results = table.get(gets);
                return mapJson(results, showParameter);
            }
        });
    }

    public Long rowCount(String tableName) {
        return hbaseTemplate.execute(tableName, new TableCallback<Long>() {
            Long rowCount = 0L;

            @Override
            public Long doInTable(HTableInterface hTableInterface) throws Throwable {
                Scan scan = new Scan();
                scan.setFilter(new FirstKeyOnlyFilter());
                ResultScanner resultScanner = hTableInterface.getScanner(scan);
                for (Result result : resultScanner) {
                    rowCount += result.size();
                }
                return rowCount;
            }
        });
    }

    /**
     * 结果映射为list
     *
     * @param results
     * @param showParameter
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> mapJson(Result[] results, List<String> showParameter) throws IOException {

        List<Map<String, String>> list = new ArrayList();
        for (Result result : results) {
            Map map = new HashMap();
            List<Cell> cells = result.listCells();
            for (Cell cell : cells) {
                String rowKey = Bytes.toString(cell.getRow());
                map.put("rowKey", rowKey);
                String value = Bytes.toString(cell.getValue());
                String qualifier = Bytes.toString(cell.getQualifier());
                if ("*".equals(showParameter.get(0)) || showParameter.indexOf(qualifier) != -1) {
                    // 此处写映射值
                    map.put(qualifier, value);
                }
            }
            // 此处追加到list
            list.add(map);
        }
        return list;
    }

    /**
     * 创建列过滤器
     * <p>
     * 使用该列过滤器，需要先获取comparator，模糊匹配使用 new SubstringComparator("ww").
     *
     * @param family     列族名
     * @param qualifier  列名
     * @param operator   操作符有 =,!=,>,>=,<,<=。模糊和精确查询都用=。
     * @param comparator
     * @return
     */
    public SingleColumnValueFilter createColumnFilter(String family, String qualifier, String operator, ByteArrayComparable comparator) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                Bytes.toBytes(family),//列族
                Bytes.toBytes(qualifier),//列名
                getOp(operator),//比较符
                comparator);//比较的值  如果确定的值，eg:"ww".getBytes().精确查询ww.
        filter.setFilterIfMissing(true);//当该列不能存在时，是否忽略。
        // 如上，所有不包含参考列的行都可以被过滤掉。
        return filter;
    }

    /**
     * 创建列精确值过滤器
     *
     * @param family    列族名
     * @param qualifier 列名
     * @param operator  操作符有 =,!=,>,>=,<,<=。模糊和精确查询都用=。
     * @param value
     * @return
     */
    public SingleColumnValueFilter createColumnExactFilter(String family, String qualifier, String operator, String value) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                Bytes.toBytes(family),//列族
                Bytes.toBytes(qualifier),//列名
                getOp(operator),//比较符
                value.getBytes());//比较的值  如果确定的值，eg:"ww".getBytes().精确查询ww.
        filter.setFilterIfMissing(true);//当该列不能存在时，是否忽略。
        // 如上，所有不包含参考列的行都可以被过滤掉。
        return filter;
    }

    /**
     * 获取操作符
     *
     * @param operator
     * @return
     */
    private static CompareFilter.CompareOp getOp(String operator) {
        switch (operator) {
            case "=":
                return CompareFilter.CompareOp.EQUAL;
            case "!=":
                return CompareFilter.CompareOp.NOT_EQUAL;
            case ">":
                return CompareFilter.CompareOp.GREATER;
            case ">=":
                return CompareFilter.CompareOp.GREATER_OR_EQUAL;
            case "<":
                return CompareFilter.CompareOp.LESS;
            case "<=":
                return CompareFilter.CompareOp.LESS_OR_EQUAL;
            default:
                return CompareFilter.CompareOp.NO_OP;
        }
    }

}
