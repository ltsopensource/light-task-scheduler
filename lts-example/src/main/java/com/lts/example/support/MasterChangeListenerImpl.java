package com.lts.example.support;

import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.listener.MasterChangeListener;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class MasterChangeListenerImpl implements MasterChangeListener {

    /**
     * master 为 master节点
     * isMaster 表示当前节点是不是master节点
     *
     * @param master
     * @param isMaster
     */
    @Override
    public void change(Node master, boolean isMaster) {

        // 一个节点组master节点变化后的处理 , 譬如我多个JobClient， 但是有些事情只想只有一个节点能做。
        if(isMaster){
            System.out.println("我变成了节点组中的master节点了， 恭喜， 我要放大招了");
        }else{
            System.out.println(StringUtils.format("master节点变成了{}，不是我，我不能放大招，要猥琐", master));
        }
    }
}
