package com.example.base.common;


import com.example.base.utils.DozerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lingbao on 2017/11/3.
 *
 * @author lingbao
 * @Description
 * @Modify
 */

public class CommonPage<T> implements Page<T> {

    //总页数
    private Integer totalPages;
    //每页条数
    private Integer size;
    //当前页数
    private Integer number;
    //总条数
    private Integer totalElements;
    //内容
    private List<T> content;

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    @Override
    public int getTotalPages() {
        return this.totalPages;
    }

    @Override
    public long getTotalElements() {
        return this.totalElements;
    }

    @Override
    public Stream<T> stream() {
        return null;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> function) {
        return null;
    }

    @Override
    public <R> Streamable<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return null;
    }

    @Override
    public Streamable<T> filter(Predicate<? super T> predicate) {
        return null;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getNumberOfElements() {
        return this.content.size();
    }

    @Override
    public List<T> getContent() {
        return this.content;
    }

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public Pageable getPageable() {
        return null;
    }

    @Override
    public Pageable nextPageable() {
        return null;
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super T> action) {

    }

    @Override
    public Spliterator<T> spliterator() {
        return null;
    }


    /**
     * 把Spring里的page转换为CommonPage，并转换其中存储的list。
     * @param page Spring的page对象
     * @param clazz 传入的类
     * @return
     */
    public static CommonPage toPageByDozer(Page page,Class clazz){
        CommonPage commonPage = new CommonPage();
        if (null != page) {
            commonPage.setContent(DozerUtil.dozerList(page.getContent(),clazz));
            commonPage.setNumber(page.getNumber());
            commonPage.setSize(page.getSize());
            commonPage.setTotalElements(Long2Int(page.getTotalElements()));
            commonPage.setTotalPages(page.getTotalPages());
        }
        return commonPage;
    }

    public static <T> CommonPage<T> toPage(List<T> list, int pageNumber, int pageSize, int count) {
        CommonPage commonPage = new CommonPage();
        if (CollectionUtils.isNotEmpty(list)) {
            commonPage.setContent(list);
            commonPage.setNumber(pageNumber);
            commonPage.setSize(pageSize);
            commonPage.setTotalElements(count);
            commonPage.setTotalPages(count/pageSize+1);
        }
        return commonPage;
    }

    private static int Long2Int(long l) {
        return Integer.parseInt(String.valueOf(l));
    }
}
