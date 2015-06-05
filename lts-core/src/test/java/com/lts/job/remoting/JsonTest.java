package com.lts.job.remoting;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class JsonTest {

    public static void main(String[] args) {

        Job job = new Job();
        job.setJ(11);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", 1111);
        map.put("key2", "ddd");
        map.put("key3", new JobInfo());

        job.setMap(map);

        List<JobInfo> list = new ArrayList<JobInfo>();

        JobInfo jobInfo = new JobInfo();
        jobInfo.setI(11111);
        map.put("hahah", true);
        jobInfo.setMap(map);

        list.add(jobInfo);

        job.setJobList(list);

        String json = JSON.toJSONString(job);
        System.out.println(json);

        Job job1 = JSON.parseObject(json, Job.class);

        System.out.println(job1);
    }

    private static class JobInfo {

        private int i;
        private Map<String, Object> map;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public String toString() {
            return "JobInfo{" +
                    "i=" + i +
                    ", map=" + map +
                    '}';
        }
    }

    private static class Job {
        private int j;
        private List<JobInfo> jobList;

        private Map<String, Object> map;

        public int getJ() {
            return j;
        }

        public List<JobInfo> getJobList() {
            return jobList;
        }

        public void setJobList(List<JobInfo> jobList) {
            this.jobList = jobList;
        }

        public void setJ(int j) {
            this.j = j;
        }


        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public String toString() {
            return "Job{" +
                    "j=" + j +
                    ", jobList=" + jobList +
                    ", map=" + map +
                    '}';
        }
    }
}
