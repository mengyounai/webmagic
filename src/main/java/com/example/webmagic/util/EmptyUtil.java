package com.example.webmagic.util;

import java.util.Collection;
import java.util.Map;

public class EmptyUtil {
    /**
     * 判断对象为空
     *
     * @param obj
     *            对象名
     * @return 是否为空
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if ((obj instanceof Collection)) {
            return ((Collection)obj).size() == 0;
        }
        if ((obj instanceof Map)) {
            return ((Map)obj).size() == 0;
        }
        if ((obj instanceof CharSequence)) {
            return (obj.toString()).trim().equals("");
        }
        return false;
    }

    /**
     * 判断对象不为空
     *
     * @param obj
     *            对象名
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }




}