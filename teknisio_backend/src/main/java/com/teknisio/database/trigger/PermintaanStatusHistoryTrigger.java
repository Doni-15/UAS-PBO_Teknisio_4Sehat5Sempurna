package com.teknisio.database.trigger;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class PermintaanStatusHistoryTrigger implements Trigger {

  private static final int ID_PERMINTAAN = 0;
  private static final int STATUS = 9;
  private static final int DIUBAH_OLEH_TERAKHIR = 21;

  @Override
  public void init(
    Connection conn,
    String schemaName,
    String triggerName,
    String tableName,
    boolean before,
    int type
  ) {
    // No initialization needed.
  }

  @Override
  public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
    if (newRow == null) {
      return;
    }

    if (oldRow == null) {
      insertHistory(
        conn,
        newRow[ID_PERMINTAAN],
        newRow[DIUBAH_OLEH_TERAKHIR],
        null,
        valueOf(newRow[STATUS]),
        "Service request created"
      );
      return;
    }

    String oldStatus = valueOf(oldRow[STATUS]);
    String newStatus = valueOf(newRow[STATUS]);

    if (!Objects.equals(oldStatus, newStatus)) {
      insertHistory(
        conn,
        newRow[ID_PERMINTAAN],
        newRow[DIUBAH_OLEH_TERAKHIR],
        oldStatus,
        newStatus,
        "Service request status updated"
      );
    }
  }

  @Override
  public void close() {
    // No resource to close.
  }

  @Override
  public void remove() {
    // No resource to remove.
  }

  private void insertHistory(
    Connection conn,
    Object idPermintaan,
    Object diubahOleh,
    String statusSebelum,
    String statusSesudah,
    String catatan
  ) throws SQLException {
    String sql = """
      INSERT INTO riwayat_status (
        id_permintaan,
        diubah_oleh,
        status_sebelum,
        status_sesudah,
        catatan
      ) VALUES (?, ?, ?, ?, ?)
      """;

    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setObject(1, idPermintaan);
      statement.setObject(2, diubahOleh);

      if (statusSebelum == null) {
        statement.setNull(3, java.sql.Types.VARCHAR);
      } else {
        statement.setString(3, statusSebelum);
      }

      statement.setString(4, statusSesudah);
      statement.setString(5, catatan);
      statement.executeUpdate();
    }
  }

  private String valueOf(Object value) {
    return value == null ? null : value.toString();
  }
}
