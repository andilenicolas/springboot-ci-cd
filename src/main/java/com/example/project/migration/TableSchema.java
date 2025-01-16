package com.example.project.migration;

import lombok.Getter;
import java.util.List;
import java.util.ArrayList;

@Getter
public class TableSchema 
{
    private final String tableName;
    private final List<ColumnSchema> columns;

    public TableSchema(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
    }

    public void addColumn(ColumnSchema column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "tableName='" + tableName + '\'' +
                ", columns=" + columns +
                '}';
    }
}
