package com.example.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

public interface BaseData {

    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    default <V> V asViewObject(Class<V> clazz) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields();  // 获取 VO 类的所有字段
            Constructor<V> constructor = clazz.getConstructor(); // 获取 VO 的无参构造器
            V v = constructor.newInstance(); // 通过反射实例化 VO
            //for (Field declaredField : declaredFields) convert(declaredField, v); // 遍历 VO 的字段，并复制值
            Arrays.asList(declaredFields).forEach(declaredField -> convert(declaredField, v));
            return v; // 返回转换后的 VO
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(Field field, Object vo) {
        try {
            Field source = this.getClass().getDeclaredField(field.getName()); // 从当前对象获取相同字段名的字段
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(vo, source.get(this)); // 复制当前对象的字段值到 VO
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
    }
}
