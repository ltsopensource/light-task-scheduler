package com.lts.web.initialization;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.web.repository.mapper.CommonRepo;
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
 *
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
@Component
public class LtsAdminDatabaseInitialingBean implements InitializingBean {

    @Autowired
    CommonRepo commonRepo;

    @Override
    public void afterPropertiesSet() throws Exception {

        List<TableSchema> tableSchemas = getTableSchema("mybatis/sql/table-schema.xml");

        for (TableSchema tableSchema : tableSchemas) {
            // 建表
            commonRepo.executeSQL(tableSchema.table);
            // 建立索引
            if (CollectionUtils.isNotEmpty(tableSchema.indexes)) {
                for (String index : tableSchema.indexes) {
                    try {
                        commonRepo.executeSQL(index);
                    } catch (BadSqlGrammarException e) {
                        if (!e.getMessage().contains("exists")) {
                            throw e;
                        }
                    }
                }
            }
        }
    }


    /**
     * 读取xml的sql
     */
    private List<TableSchema> getTableSchema(String xml) throws ParserConfigurationException, IOException, SAXException {

        List<TableSchema> tableSchemas = new ArrayList<TableSchema>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream(xml);
        Document doc = builder.parse(in);
        Element rootXML = doc.getDocumentElement();

        NodeList tableSchemasXML = rootXML.getElementsByTagName("tableSchema");
        if (tableSchemasXML != null && tableSchemasXML.getLength() > 0) {
            for (int i = 0; i < tableSchemasXML.getLength(); i++) {
                Element tableSchemaXML = (Element) tableSchemasXML.item(i);

                NodeList tableXML = tableSchemaXML.getElementsByTagName("table");
                TableSchema tableSchema = new TableSchema();
                tableSchema.table = tableXML.item(0).getFirstChild().getNodeValue();

                NodeList indexes = tableSchemaXML.getElementsByTagName("indexes");
                if (indexes != null && indexes.getLength() > 0) {
                    tableSchema.indexes = new ArrayList<String>();
                    NodeList indexList = ((Element) indexes.item(0)).getElementsByTagName("index");
                    for (int j = 0; j < indexList.getLength(); j++) {
                        Node index = indexList.item(j);
                        tableSchema.indexes.add(index.getFirstChild().getNodeValue());
                    }
                }
                tableSchemas.add(tableSchema);
            }
        }
        return tableSchemas;
    }

    private static class TableSchema {
        public String table;
        public List<String> indexes;
    }


}
