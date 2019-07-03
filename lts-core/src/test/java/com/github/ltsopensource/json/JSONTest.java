package com.github.ltsopensource.json;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.json.TypeReference;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 12/29/15.
 */
@Ignore("LOL assert with sout :))")
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
        JobRunResult result = new JobRunResult();
        result.setAction(Action.EXECUTE_EXCEPTION);
        result.setMsg("fxxdfdasaf");

        String json = new JSONObject(result).toString();
        System.out.println(json);

        JobRunResult result2 = JSONObject.parseObject(json, new TypeReference<JobRunResult>() {
        }.getType());
        System.out.println(result2);
    }

    @Test
    public void fastjsonWriterTest(){
        Job job = new Job();
        job.setCronExpression("xcvxcvxfadsf");
        job.setParam("xxx", "fadsfads");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            com.alibaba.fastjson.JSONObject.toJSONString(job);
        }

        // 2969
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void ltsjsonWriterTest(){
        Job job = new Job();
        job.setCronExpression("xcvxcvxfadsf");
        job.setParam("xxx", "fadsfads");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            JSONObject.toJSONString(job);
        }

        // 6940
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void ltsjsonParseTest(){
        String json = "{\"needFeedback\":false,\"schedule\":true,\"extParams\":{\"xxx\":\"fadsfads\"},\"replaceOnExist\":false,\"priority\":100,\"cronExpression\":\"xcvxcvxfadsf\",\"retryTimes\":0}";
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            Job job = JSONObject.parseObject(json, new TypeReference<Job>(){}.getType());
        }
        // 7255
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void fastjsonParseTest(){
        String json = "{\"needFeedback\":false,\"schedule\":true,\"extParams\":{\"xxx\":\"fadsfads\"},\"replaceOnExist\":false,\"priority\":100,\"cronExpression\":\"xcvxcvxfadsf\",\"retryTimes\":0}";
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            Job job = com.alibaba.fastjson.JSONObject.parseObject(json, new TypeReference<Job>(){}.getType());
        }
        // 4724
        System.out.println(System.currentTimeMillis() - start);
    }
}
