package com.github.ltsopensource.jvmmonitor.mbean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public class JVMMemory implements JVMMemoryMBean {

    private static final JVMMemory instance = new JVMMemory();

    public static JVMMemory getInstance() {
        return instance;
    }

    private MemoryMXBean memoryMXBean;

    private MemoryPoolMXBean permGenMxBean;
    private MemoryPoolMXBean oldGenMxBean;
    private MemoryPoolMXBean edenSpaceMxBean;
    private MemoryPoolMXBean pSSurvivorSpaceMxBean;

    private JVMMemory() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();

        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean item : list) {
            if ("CMS Perm Gen".equals(item.getName()) 
                    || "Perm Gen".equals(item.getName()) 
                    || "PS Perm Gen".equals(item.getName()) 
                    || "G1 Perm Gen".equals(item.getName()) 
                    ) {
                permGenMxBean = item;
            } else if ("CMS Old Gen".equals(item.getName()) 
                    || "Tenured Gen".equals(item.getName()) 
                    || "PS Old Gen".equals(item.getName()) 
                    || "G1 Old Gen".equals(item.getName()) 
                    ) {
                oldGenMxBean = item;
            } else if ("Par Eden Space".equals(item.getName()) 
                    || "Eden Space".equals(item.getName()) 
                    || "PS Eden Space".equals(item.getName()) 
                    || "G1 Eden".equals(item.getName()) 
                    ) {
                edenSpaceMxBean = item;
            } else if ("Par Survivor Space".equals(item.getName()) 
                    || "Survivor Space".equals(item.getName()) 
                    || "PS Survivor Space".equals(item.getName()) 
                    || "G1 Survivor".equals(item.getName()) 
                    ) {
                pSSurvivorSpaceMxBean = item;
            }
        }
    }

    // Memory Heap
    @Override
    public long getHeapMemoryCommitted() {
        return memoryMXBean.getHeapMemoryUsage().getCommitted();
    }

    @Override
    public long getHeapMemoryInit() {
        return memoryMXBean.getHeapMemoryUsage().getInit();
    }

    @Override
    public long getHeapMemoryMax() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    @Override
    public long getHeapMemoryUsed() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    // Memory NonHeap
    @Override
    public long getNonHeapMemoryCommitted() {
        return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
    }

    @Override
    public long getNonHeapMemoryInit() {
        return memoryMXBean.getNonHeapMemoryUsage().getInit();
    }

    @Override
    public long getNonHeapMemoryMax() {
        return memoryMXBean.getNonHeapMemoryUsage().getMax();
    }

    @Override
    public long getNonHeapMemoryUsed() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }

    // memory permGen

    @Override
    public long getPermGenCommitted() {
        if (null == permGenMxBean) {
            return 0;
        }
        return permGenMxBean.getUsage().getCommitted();
    }

    @Override
    public long getPermGenInit() {
        if (null == permGenMxBean) {
            return 0;
        }
        return permGenMxBean.getUsage().getInit();
    }

    @Override
    public long getPermGenMax() {
        if (null == permGenMxBean) {
            return 0;
        }
        return permGenMxBean.getUsage().getMax();
    }

    @Override
    public long getPermGenUsed() {
        if (null == permGenMxBean) {
            return 0;
        }
        return permGenMxBean.getUsage().getUsed();
    }

    // memory oldGen

    @Override
    public long getOldGenCommitted() {
        if (null == oldGenMxBean) {
            return 0;
        }
        return oldGenMxBean.getUsage().getCommitted();
    }

    @Override
    public long getOldGenInit() {
        if (null == oldGenMxBean) {
            return 0;
        }
        return oldGenMxBean.getUsage().getInit();
    }

    @Override
    public long getOldGenMax() {
        if (null == oldGenMxBean) {
            return 0;
        }
        return oldGenMxBean.getUsage().getMax();
    }

    @Override
    public long getOldGenUsed() {
        if (null == oldGenMxBean) {
            return 0;
        }
        return oldGenMxBean.getUsage().getUsed();
    }

    // memory edenSpace
    @Override
    public long getEdenSpaceCommitted() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getCommitted();
    }

    @Override
    public long getEdenSpaceInit() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getInit();
    }

    @Override
    public long getEdenSpaceMax() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getMax();
    }

    @Override
    public long getEdenSpaceUsed() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getUsed();
    }

    // memory survivor
    @Override
    public long getSurvivorCommitted() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getCommitted();
    }

    @Override
    public long getSurvivorInit() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getInit();
    }

    @Override
    public long getSurvivorMax() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getMax();
    }

    @Override
    public long getSurvivorUsed() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getUsed();
    }

}

