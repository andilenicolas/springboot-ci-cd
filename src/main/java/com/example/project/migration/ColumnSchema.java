package com.example.project.migration;

import lombok.Getter;

@Getter
public class ColumnSchema 
{
    private final String columnName;
    private final String dataType;
    private final boolean isNullable;
    private final Integer maxLength;

    public ColumnSchema(String columnName, String dataType, boolean isNullable, Integer maxLength) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.isNullable = isNullable;
        this.maxLength = maxLength;
    }

    @Override
    public String toString() {
        return "ColumnSchema{" +
                "columnName='" + columnName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isNullable=" + isNullable +
                ", maxLength=" + maxLength +
                '}';
    }
}
