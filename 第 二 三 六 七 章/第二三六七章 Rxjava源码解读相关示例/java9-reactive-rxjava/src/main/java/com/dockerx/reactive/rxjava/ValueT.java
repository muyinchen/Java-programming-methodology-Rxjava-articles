package com.dockerx.reactive.rxjava;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/3/20 0:48.
 */
public class ValueT<T> {
    private final List list;

    public ValueT(List list) {
        this.list = list;
    }

    public List getList() {
        return list;
    }

    public static ValueT<List> valuetest(List o, Consumer consumer){
        ValueT<List> operatorsTest = new ValueT<>(o);
        consumer.accept(o);
        o.clear();
        consumer.accept(operatorsTest.getList());
        return operatorsTest;
    }

    public static void main(String[] args) {
        List list1=new ArrayList();
        list1.add(1);
        System.out.println(list1.hashCode());
        ValueT<List> valuetest = valuetest(list1, o -> System.out.println(o.hashCode()));
        System.out.println(valuetest.hashCode());
        list1.add(2);
        System.out.println(list1.hashCode());
        System.out.println(valuetest.hashCode());
    }

}
