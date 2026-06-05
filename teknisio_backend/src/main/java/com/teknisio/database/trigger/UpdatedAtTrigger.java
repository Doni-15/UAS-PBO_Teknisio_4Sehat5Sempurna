package com.teknisio.database.trigger;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;

public class UpdatedAtTrigger implements Trigger {

  private int updatedAtIndex = -1;

  @Override
  public void init(
    Connection conn,
    String schemaName,
    String triggerName,
    String tableName,
    boolean before,
    int type
  ) throws SQLException {
    this.updatedAtIndex = findColumnIndex(conn, tableName, "updated_at");
  }

  @Override
  public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
    if (newRow == null || updatedAtIndex < 0) {
      return;
    }

    newRow[updatedAtIndex] = OffsetDateTime.now();
  }

  @Override
  public void close() {
    // No resource to close.
  }

  @Override
  public void remove() {
    // No resource to remove.
  }

  private int findColumnIndex(Connection conn, String tableName, String targetColumn) throws SQLException {
    String sql = "SELECT * FROM " + tableName + " WHERE 1 = 0";

    try (
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery(sql)
    ) {
      ResultSetMetaData metaData = resultSet.getMetaData();

      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        if (metaData.getColumnName(i).equalsIgnoreCase(targetColumn)) {
          return i - 1;
        }
      }
    }

    throw new SQLException("Column " + targetColumn + " not found in table " + tableName);
  }
}
