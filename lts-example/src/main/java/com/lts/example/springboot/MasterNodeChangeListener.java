package com.lts.example.springboot;

import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.spring.boot.annotation.MasterNodeListener;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@MasterNodeListener(nodeTypes = {})
public class MasterNodeChangeListener extends MasterChangeListenerImpl {
}
