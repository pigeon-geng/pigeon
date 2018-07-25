package org.pigeon.map;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by pigeongeng on 2018/7/25.下午9:36
 */
public class MapUtilsTest {


    @Test
    public void test() {
        Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 2);
        map.put("c", 3);
        map.put("b", 1);
        Assert.assertEquals("{c=3, a=2, b=1}", MapUtils.sortMapByValue(map, true).toString());
    }
}
