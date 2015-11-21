package com.lts.web.service;

import com.lts.core.commons.utils.BeanUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.json.JSON;
import com.lts.core.domain.monitor.JVMMonitorData;
import com.lts.core.domain.monitor.JobTrackerMonitorData;
import com.lts.core.domain.monitor.MonitorData;
import com.lts.core.domain.monitor.TaskTrackerMonitorData;
import com.lts.core.json.TypeReference;
import com.lts.core.support.SystemClock;
import com.lts.web.repository.domain.*;
import com.lts.web.repository.mapper.*;
import com.lts.web.request.MonitorDataAddRequest;
import com.lts.web.request.MonitorDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
@Service
public class MonitorDataService {

    @Autowired
    TaskTrackerMonitorRepo taskTrackerMonitorRepo;
    @Autowired
    JobTrackerMonitorRepo jobTrackerMonitorDataRepo;
    @Autowired
    JVMInfoRepo jvmInfoRepo;
    @Autowired
    JVMGCRepo jvmGCRepo;
    @Autowired
    JVMMemoryRepo jvmMemoryRepo;
    @Autowired
    JVMThreadRepo jvmThreadRepo;

    /**
     * 添加TaskTracker监控数据
     */
    public void addTaskTrackerMonitorData(MonitorDataAddRequest request) {

        List<TaskTrackerMonitorData> mds = JSON.parse(request.getData(),
                new TypeReference<List<TaskTrackerMonitorData>>(){});
        if (CollectionUtils.isEmpty(mds)) {
            throw new IllegalArgumentException("monitorData can not be null");
        }

        List<TaskTrackerMonitorDataPo> pos = new ArrayList<TaskTrackerMonitorDataPo>(mds.size());
        for (TaskTrackerMonitorData monitorData : mds) {
            TaskTrackerMonitorDataPo po = new TaskTrackerMonitorDataPo();

            BeanUtils.copyProperties(po, monitorData);

            po.setNodeType(request.getNodeType());
            po.setIdentity(request.getIdentity());
            po.setNodeGroup(request.getNodeGroup());
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }
        taskTrackerMonitorRepo.insert(pos);

        // add JVM monitor data
        addJVMMonitorData(mds, request);
    }

    public List<? extends AbstractMonitorDataPo> queryMonitorDataSum(MonitorDataRequest request) {
        switch (request.getNodeType()) {
            case JOB_CLIENT:
                return null;
            case JOB_TRACKER:
                return jobTrackerMonitorDataRepo.querySum(request);
            case TASK_TRACKER:
                return taskTrackerMonitorRepo.querySum(request);
			default:
				return null;
        }
    }

    /**
     * 添加JobTracker监控数据
     */
    public void addJobTrackerMonitorData(MonitorDataAddRequest request) {
        List<JobTrackerMonitorData> mds = JSON.parse(request.getData(),
                new TypeReference<List<JobTrackerMonitorData>>(){});
        if (CollectionUtils.isEmpty(mds)) {
            throw new IllegalArgumentException("monitorData can not be null");
        }

        List<JobTrackerMonitorDataPo> pos = new ArrayList<JobTrackerMonitorDataPo>(mds.size());
        for (JobTrackerMonitorData monitorData : mds) {
            JobTrackerMonitorDataPo po = new JobTrackerMonitorDataPo();

            BeanUtils.copyProperties(po, monitorData);

            po.setNodeType(request.getNodeType());
            po.setIdentity(request.getIdentity());
            po.setNodeGroup(request.getNodeGroup());
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }
        jobTrackerMonitorDataRepo.insert(pos);

        // add JVM monitor data
        addJVMMonitorData(mds, request);
    }

    private <T extends MonitorData> void addJVMMonitorData(List<T> mds, MonitorDataAddRequest request) {
        int size = mds.size();
        List<JVMGCDataPo> jvmGCDataPos = new ArrayList<JVMGCDataPo>(size);
        List<JVMMemoryDataPo> jvmMemoryDataPos = new ArrayList<JVMMemoryDataPo>(size);
        List<JVMThreadDataPo> jvmThreadDataPos = new ArrayList<JVMThreadDataPo>(size);

        for (T md : mds) {
            JVMMonitorData jvmMonitorData = md.getJvmMonitorData();
            Long timestamp = md.getTimestamp();
            // gc
            JVMGCDataPo jvmgcDataPo = getDataPo(jvmMonitorData.getGcMap(), JVMGCDataPo.class, request, timestamp);
            jvmGCDataPos.add(jvmgcDataPo);
            // memory
            JVMMemoryDataPo jvmMemoryDataPo = getDataPo(jvmMonitorData.getMemoryMap(), JVMMemoryDataPo.class, request, timestamp);
            jvmMemoryDataPos.add(jvmMemoryDataPo);
            // thread
            JVMThreadDataPo jvmThreadDataPo = getDataPo(jvmMonitorData.getThreadMap(), JVMThreadDataPo.class, request, timestamp);
            jvmThreadDataPos.add(jvmThreadDataPo);
        }

        jvmGCRepo.insert(jvmGCDataPos);
        jvmMemoryRepo.insert(jvmMemoryDataPos);
        jvmThreadRepo.insert(jvmThreadDataPos);
    }


    private static final Map<String, Method> CACHED_METHOD_MAP = new ConcurrentHashMap<String, Method>();

    static {
        cacheMethod(JVMGCDataPo.class);
        cacheMethod(JVMMemoryDataPo.class);
        cacheMethod(JVMThreadDataPo.class);
    }

    private static void cacheMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set")) {
                CACHED_METHOD_MAP.put(clazz.getSimpleName() + "_" + method.getName().substring(3), method);
            }
        }
    }

    /**
     * 根据Map得到 持久化对象
     */
    private <T extends AbstractMonitorDataPo> T getDataPo(Map<String, Object> dataMap, Class<T> clazz,
                                                          MonitorDataAddRequest request, Long timestamp) {
        try {
            T data = clazz.newInstance();
            if (CollectionUtils.isNotEmpty(dataMap)) {
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    Method method = CACHED_METHOD_MAP.get(clazz.getSimpleName() + "_" + entry.getKey());
                    if (method != null) {
                        String string = String.valueOf(entry.getValue());
                        Object value = entry.getValue();
                        Class<?> parameterType = method.getParameterTypes()[0];
                        if (parameterType == Long.class || parameterType == long.class) {
                            value = Long.valueOf(string);
                        } else if (parameterType == Integer.class || parameterType == int.class) {
                            value = Integer.valueOf(string);
                        } else if (parameterType == Float.class || parameterType == float.class) {
                            value = Float.valueOf(string);
                        } else if (parameterType == Double.class || parameterType == double.class) {
                            value = Double.valueOf(string);
                        } else if (parameterType == Short.class || parameterType == short.class) {
                            value = Short.valueOf(string);
                        } else if (parameterType == Boolean.class || parameterType == boolean.class) {
                            value = Boolean.valueOf(string);
                        } else if (parameterType == String.class) {
                            value = string;
                        }
                        // TODO others
                        method.invoke(data, value);
                    }
                }
            }

            data.setNodeType(request.getNodeType());
            data.setNodeGroup(request.getNodeGroup());
            data.setIdentity(request.getIdentity());
            data.setGmtCreated(SystemClock.now());
            data.setTimestamp(timestamp);

            return data;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 添加JVMInfo 信息
     */
    public void addJVMInfoData(MonitorDataAddRequest request) {
        JVMInfoDataPo data = new JVMInfoDataPo();
        data.setNodeType(request.getNodeType());
        data.setNodeGroup(request.getNodeGroup());
        data.setIdentity(request.getIdentity());
        data.setGmtCreated(SystemClock.now());
        data.setTimestamp(SystemClock.now());
        data.setJvmInfo(request.getData());
        jvmInfoRepo.insert(data);
    }

}
