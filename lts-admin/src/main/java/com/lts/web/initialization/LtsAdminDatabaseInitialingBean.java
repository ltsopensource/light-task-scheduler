package com.lts.web.initialization;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.web.repository.mapper.AbstractRepository;
import com.lts.web.repository.mapper.JobTrackerMonitorRepository;
import com.lts.web.repository.mapper.TaskTrackerMonitorRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建LTS-Admin数据库
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
@Component
public class LtsAdminDatabaseInitialingBean implements InitializingBean {

    @Autowired
    private TaskTrackerMonitorRepository taskTrackerMonitorRepository;
    @Autowired
    private JobTrackerMonitorRepository jobTrackerMonitorRepository;

    @Override
    public void afterPropertiesSet() throws Exception {

        execute(taskTrackerMonitorRepository, "sql/lts_admin_task_tracker_monitor_data.xml");
        execute(jobTrackerMonitorRepository, "sql/lts_admin_job_tracker_monitor_data.xml");

    }

    /**
     * 执行创建表语句
     */
    private void execute(AbstractRepository repository, String sqlXml) throws Exception {

        TableSchema tableSchema = getTableSchema(sqlXml);

        repository.executeSQL(tableSchema.table);

        if (CollectionUtils.isNotEmpty(tableSchema.indexes)) {
            for (String index : tableSchema.indexes) {
                try {
                    repository.executeSQL(index);
                } catch (BadSqlGrammarException e) {
                    if (!e.getMessage().contains("exists")) {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * 读取xml的sql
     */
    private TableSchema getTableSchema(String xml) throws ParserConfigurationException, IOException, SAXException {

        TableSchema tableSchema = new TableSchema();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream(xml);
        Document doc = builder.parse(in);
        Element root = doc.getDocumentElement();
        NodeList table = root.getElementsByTagName("table");

        tableSchema.table = table.item(0).getFirstChild().getNodeValue();

        NodeList indexes = root.getElementsByTagName("indexes");
        if (indexes != null) {
            tableSchema.indexes = new ArrayList<String>();
            NodeList indexList = ((Element) indexes.item(0)).getElementsByTagName("index");
            for (int i = 0; i < indexList.getLength(); i++) {
                Node index = indexList.item(i);
                tableSchema.indexes.add(index.getFirstChild().getNodeValue());
            }
        }
        return tableSchema;
    }

    private static class TableSchema {
        public String table;
        public List<String> indexes;
    }
}
