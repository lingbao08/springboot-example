package com.example.demo.utils;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 17:35
 **/

public class ESSearchDto {
    //字段名
    private String field;
    //类型 类型的说明参见内部类ESType处的说明
    //默认为TERM模式（模糊模式）
    private ESType type;
    //值
    private String value;
    //关系
    private ESRelation relation;

    public ESSearchDto() {
        this.relation = ESRelation.AND;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ESType getType() {
        if (null == type)
            return ESType.MATCH;
        return type;
    }

    public void setType(ESType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ESRelation getRelation() {
        if (null == relation)
            relation = ESRelation.AND;
        return relation;
    }

    public void setRelation(ESRelation relation) {
        this.relation = relation;
    }

    public enum ESType {
        /*
        匹配 存在即输出
         */
        MATCH,
        /*等价于mysql的like
        模糊（通配符匹配，速度可能会极慢）等价于mysql的like
        模糊查询 等价于mysql的like，支持单词的通配符匹配。
        默认使用方法：如搜索admin，可以输入“ad”来搜索。*/
        WILDCARD,
        /*等价于mysql的like 按分词器取词
        模糊（精准与单个字或者单词）
        不支持对单词使用部分字符来匹配整个单词，比如搜索admin，必须输入admin，而不能输入ad（查询无结果）。
        对于汉字支持间接匹配，如搜索“我爱吃蛋糕”，可以输入“吃 糕”来搜索。*/

        TERM,
        /*范围（都不包含）有且只有一个,，
        适用于分词了，eg:1,3
         在范围中传入的时间格式为“2018-03-26 14:49:03”，如“2018-03-26 14:53:52,2018-03-26 14:54:00”
        逗号前后任意一者可以为空字符串，都为空的话，传传空字符串。*/

        RANGE_EX,
        /*范围（都包含）有且只有一个,*/

        RANGE_IN,
        /*等价于mysql中的in，eg:1,2,3,4*/
        IN,
        /* 等价于mysql 中is null
        * 不会对空字串进行分词或者查询。
        * 如果要对空值进行处理，需要对该值设置为keyword，并设置null_value
        * */
        NULL,
        /* 等价于mysql 中is not null*/
        NOT_NULL,
    }

    public enum ESRelation{
        //或者，并且
        OR,AND,
    }
}
