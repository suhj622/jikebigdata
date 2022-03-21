package com.suhj.main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    private static Configuration config;

    //获取执行环境
    static{

       config = HBaseConfiguration.create();
        //Add any necessary configuration files (hbase-site.xml, core-site.xml)
        config.addResource(new Path(System.getenv("HBASE_CONF_DIR"), "hbase-site.xml"));
        config.addResource(new Path(System.getenv("HADOOP_CONF_DIR"), "core-site.xml"));
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void deleteTable(String tablename) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(getConfig())) {
            Admin admin = connection.getAdmin();
            TableName table = TableName.valueOf(tablename);
            admin.disableTable(table);
            admin.deleteTable(table);
        }

    }

    public static void createOrOverwrite(Admin admin, HTableDescriptor table) throws IOException {
        if (admin.tableExists(table.getTableName())) {
            System.out.print("Table is existing now ... Deleting");
            admin.disableTable(table.getTableName());
            admin.deleteTable(table.getTableName());
            System.out.print(" Deleted.");
        }
        admin.createTable(table);
    }

    public static void createSchemaTables(String table_name, String[] table_cfs) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(getConfig())) {
            Admin admin = connection.getAdmin();
            //HTableDescriptor table = new HTableDescriptor(TableName.valueOf(table_name));
            TableDescriptorBuilder tableDescBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(table_name));

            for(String cf:table_cfs){
                //table.addFamily(new HColumnDescriptor(cf).setCompressionType(Algorithm.NONE));
                ColumnFamilyDescriptorBuilder columnDescBuilder = ColumnFamilyDescriptorBuilder
                        .newBuilder(Bytes.toBytes(cf)).setBlocksize(32 * 1024)
                        .setCompressionType(Compression.Algorithm.SNAPPY).setDataBlockEncoding(DataBlockEncoding.NONE);
                tableDescBuilder.setColumnFamily(columnDescBuilder.build());

            }
            System.out.print("Creating table. ");
            if(admin.tableExists(TableName.valueOf(table_name))){
                System.out.print("Table is existing now ... Deleting");
                admin.disableTable(TableName.valueOf(table_name));
                admin.deleteTable(TableName.valueOf(table_name));
                System.out.print(" Deleted.");
            }

            admin.createTable(tableDescBuilder.build());
            //createOrOverwrite(admin, table);
            System.out.println(" Done.");
        }
    }

    public  static  void writeValuesToTable(String table_name, Put put) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(getConfig())){
            Table table = connection.getTable(TableName.valueOf(table_name));
            table.put(put);
            //table.close();
        }
    }

    public static void writeValuesToTable(String table_name, String row_key, String cf, String column, String value) throws IOException{
        try(Connection connection = ConnectionFactory.createConnection(getConfig())){
            Table table = connection.getTable(TableName.valueOf(table_name));
            Put put = new Put(Bytes.toBytes(row_key));
            put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(column), Bytes.toBytes(value));
            table.put(put);
        }
    }

    public static void scanTable(String table_name) throws IOException{
        try (Connection connection = ConnectionFactory.createConnection(getConfig());){
            Table table = connection.getTable(TableName.valueOf(table_name));
            Scan scan = new Scan();
            ResultScanner resultScanner = table.getScanner(scan);
            for(Result result : resultScanner){
                Cell[] cells = result.rawCells();
                print(cells);
            }
        }
    }

    public static void getDataByRowKey(String table_name, String row_key) throws IOException{
        try(Connection connection = ConnectionFactory.createConnection(getConfig());){
            Table table = connection.getTable(TableName.valueOf(table_name));
            Get get = new Get(Bytes.toBytes(row_key));
            Result result = table.get(get);
            Cell[] cells = result.rawCells();
            print(cells);
        }
    }

    public static void print(Cell[] cells){
        for(Cell cell : cells){
            System.out.print("行键: " + Bytes.toString(CellUtil.cloneRow(cell)) + "\t");
            System.out.print("列簇: " + Bytes.toString(CellUtil.cloneFamily(cell)) + "\t");
            System.out.print("列: " + Bytes.toString(CellUtil.cloneQualifier(cell)) + "\t");
            System.out.print("值: " + Bytes.toString(CellUtil.cloneValue(cell)));
            System.out.println();
        }
    }


    public static void deleteRowsData(String table_name,String row_key) throws IOException{
        try (Connection connection = ConnectionFactory.createConnection(config);){
            Table table = connection.getTable(TableName.valueOf(table_name));
            List<Delete> deleteList = new ArrayList<>();
            Delete delete = new Delete(Bytes.toBytes(row_key));
            deleteList.add(delete);
            table.delete(deleteList);
        }

    }


    public static void main(String[] args) throws IOException {

        //1.建表并添加列族
        System.out.println("Begin to create table...");
        String table_name = "suhaojie:student";
        String[] table_cfs = new String[]{"info", "score"};
        createSchemaTables(table_name, table_cfs);
        //2.写入数据
//        //2.1 写入姓名 rowkey ->suhaojie
//        String row_key = "suhaojie";
//        Put put = new Put(Bytes.toBytes(row_key));
//        String cf_info = table_cfs[0];
//        String cf_score = table_cfs[1];
//        //2.2 写入学号 info:student_id -> G20220735020140
//        put.addColumn(Bytes.toBytes(cf_info), Bytes.toBytes("student_id"), Bytes.toBytes("G20220735020140"));
//        //2.3 写入班级 info:class -> 1
//        put.addColumn(Bytes.toBytes(cf_info), Bytes.toBytes("class"), Bytes.toBytes("1"));
//        //2.4 写入理解能力 score:understanding -> 60
//        put.addColumn(Bytes.toBytes(cf_score), Bytes.toBytes("understanding"), Bytes.toBytes("60"));
//        //2.5 写入编程能力 score:programming -> 61
//        put.addColumn(Bytes.toBytes(cf_score), Bytes.toBytes("programming"), Bytes.toBytes("61"));
//        writeValuesToTable(table_name, put);
        //3. 写入数据
        System.out.println("Begin to write data...");
        String[][] info = new String[6][4];
        info[0][0] = "suhaojie";  info[0][1] = "info";        info[0][2] = "student_id";       info[0][3] = "G20220735020140";
        info[1][0] = "suhaojie";  info[1][1] = "info";        info[1][2] = "class";            info[1][3] = "1";
        info[2][0] = "suhaojie";  info[2][1] = "score";       info[2][2] = "understanding";    info[2][3] = "60";
        info[3][0] = "suhaojie";  info[3][1] = "score";       info[3][2] = "programming";      info[3][3] = "61";
        info[4][0] = "lvping";    info[4][1] = "info";        info[4][2] = "student_id";       info[4][3] = "G111111111111111";
        info[5][0] = "lvping";    info[5][1] = "info";        info[5][2] = "student_id";       info[5][3] = "G222222222222222";
        for(String[] item:info){
            writeValuesToTable(table_name, item[0], item[1], item[2], item[3]);
        }

        //4. 查询所有数据
        System.out.println("Begin to scan table...");
        scanTable(table_name);
        //5. 查询row_key -> suhaojie 的数据
        System.out.println("Begin to get data for row_key=>suhaojie...");
        getDataByRowKey(table_name,"suhaojie");
        //6. 删除 rowkey -> suhaojie 的数据
        System.out.println("Begin to delete data for row_key=>suhaojie...");
        deleteRowsData(table_name, "suhaojie");
        //7. 再次查询所有数据
        System.out.println("Begin to scan table agin...");
        scanTable(table_name);
        //8. 删除表
        System.out.println("Begin to delete table...");
        deleteTable(table_name);

    }
}
