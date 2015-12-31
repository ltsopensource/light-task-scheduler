package com.lts.json;

import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.json.TypeReference;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 12/29/15.
 */
public class JSONTest {

    @Test
    public void testMap() throws Exception {

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("xxx", 22222);
        map.put("xxct", 432432);
        map.put("fasdfads", null);

        String json = new JSONObject(map).toString();

        System.out.println(json);
        Map<String, Integer> tmap = JSONObject.parseObject(json, new TypeReference<HashMap<String, Integer>>() {
        }.getType());
        System.out.println(tmap);
    }


    @Test
    public void testBean() {

        Job job = new Job();
        job.setCronExpression("xcvxcvxfadsf");
        job.setParam("xxx", "fadsfads");

        String json = new JSONObject(job).toString();
        System.out.println(json);

        Job destJob = JSONObject.parseObject(json, new TypeReference<Job>() {
        }.getType());
        System.out.println(destJob);
    }

    @Test
    public void testArray(){
        byte[] b = new byte[]{
                1,2,
                3,3
        };

        String json = new JSONArray(b).toString();
        System.out.println(json);

        byte[] b2 = JSONObject.parseObject(json, new TypeReference<byte[]>() {
        }.getType());
        System.out.println(b2);
    }

    @Test
    public void testEnum(){
        TaskTrackerJobResult result = new TaskTrackerJobResult();
        result.setAction(Action.EXECUTE_EXCEPTION);
        result.setMsg("fxxdfdasaf");

        String json = new JSONObject(result).toString();
        System.out.println(json);

        TaskTrackerJobResult result2 = JSONObject.parseObject(json, new TypeReference<TaskTrackerJobResult>() {
        }.getType());
        System.out.println(result2);
    }

    @Test
    public void fastjsonTest(){
        Job job = new Job();
        job.setCronExpression("xcvxcvxfadsf");
        job.setParam("xxx", "fadsfads");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            com.alibaba.fastjson.JSONObject.toJSONString(job);
        }

        // 2585
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void ltsjsonTest(){
        Job job = new Job();
        job.setCronExpression("xcvxcvxfadsf");
        job.setParam("xxx", "fadsfads");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            JSONObject.toJSONString(job);
        }

        // 10707
        System.out.println(System.currentTimeMillis() - start);
    }
}
