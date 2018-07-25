package org.pigeon.map;

import com.google.common.collect.Maps;

import java.util.*;

/**
 * Created by pigeongeng on 2018/7/25.下午9:13
 */
public class MapUtils {

    public static <K, V extends Comparable<V>> Map<K, V> sortMapByValue(Map<K, V> input, final boolean desc) {

        LinkedHashMap<K, V> output = new LinkedHashMap<K, V>(input.size());
        ArrayList<Map.Entry<K, V>> entryList = new ArrayList<Map.Entry<K, V>>(input.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<K,V>>(){
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                if (desc) return o2.getValue().compareTo(o1.getValue());
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<K, V> entry : entryList) {
            output.put(entry.getKey(), entry.getValue());
        }

        return output;
    }

}
