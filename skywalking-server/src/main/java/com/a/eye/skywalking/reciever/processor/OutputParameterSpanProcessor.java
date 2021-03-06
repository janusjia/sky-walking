package com.a.eye.skywalking.reciever.processor;

import com.a.eye.skywalking.protocol.OutputParameterSpan;
import com.a.eye.skywalking.protocol.common.AbstractDataSerializable;
import com.a.eye.skywalking.reciever.conf.Config;
import com.a.eye.skywalking.reciever.util.HBaseUtil;
import com.a.eye.skywalking.reciever.util.SpanUtil;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

@DefaultProcessor
public class OutputParameterSpanProcessor extends ParameterSpanProcessor {
    @Override
    public void doSaveHBase(Connection connection, List<AbstractDataSerializable> serializedObjects) {
        List<Put> puts = new ArrayList<Put>();
        // convert to put
        for (AbstractDataSerializable serializedObject : serializedObjects) {
            OutputParameterSpan inputParametersSpan = (OutputParameterSpan) serializedObject;
            String columnName = inputParametersSpan.getTraceLevelId() + "-RET";
            Put put = new Put(Bytes.toBytes(inputParametersSpan.getTraceId()),
                    SpanUtil.getTSBySpanTraceId(inputParametersSpan.getTraceId()));
            put.addColumn(Bytes.toBytes(Config.HBaseConfig.TraceParamTable.FAMILY_COLUMN_NAME),
                    Bytes.toBytes(columnName), inputParametersSpan.getData());
            puts.add(put);
        }
        // save
        HBaseUtil.batchSavePuts(connection, Config.HBaseConfig.TraceParamTable.TABLE_NAME, puts);
    }

    @Override
    public int getProtocolType() {
        return 4;
    }
}
