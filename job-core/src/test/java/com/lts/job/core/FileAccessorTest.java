package com.lts.job.core;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.file.FileAccessor;
import com.lts.job.core.file.FileException;
import com.lts.job.core.file.Line;
import com.lts.job.core.util.JSONUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class FileAccessorTest {

    @Test
    public void test(){

        try {
            FileAccessor fileAccessor = FileAccessor.create("/Users/hugui/Documents/JOB/QN_JOB.info");

            fileAccessor.empty();

            Node node = new Node();
            node.setPath("ddd中文测试");
            node.setNodeType(NodeType.CLIENT);

            fileAccessor.addOneLine(new Line(JSONUtils.toJSONString(node)));
            fileAccessor.addOneLine(new Line(JSONUtils.toJSONString(node)));
            fileAccessor.addOneLine(new Line(JSONUtils.toJSONString(node)));

            List<Line> lines = new ArrayList<Line>();
            for (int i = 0; i < 5; i++) {
                node.setPath("中文测试" + i);
                lines.add(new Line(JSONUtils.toJSONString(node)));
            }

            fileAccessor.addLines(lines);

            lines = fileAccessor.readLines();
            for (int i = 0; i < lines.size(); i++) {
                System.out.println(i + " : " + lines.get(i));
            }

        } catch (FileException e) {
            e.printStackTrace();
        }


    }

}
