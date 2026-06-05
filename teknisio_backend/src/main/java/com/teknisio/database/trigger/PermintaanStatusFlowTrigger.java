package com.teknisio.database.trigger;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Objects;

public class PermintaanStatusFlowTrigger implements Trigger {

  private static final int STATUS = 9;
  private static final int WAKTU_PERMINTAAN = 15;
  private static final int WAKTU_DITERIMA = 16;
  private static final int WAKTU_DIPROSES = 17;
  private static final int WAKTU_SELESAI = 18;
  private static final int WAKTU_DIBATALKAN = 19;
  private static final int WAKTU_DITOLAK = 20;

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
      handleInsert(newRow);
      return;
    }

    handleUpdate(oldRow, newRow);
  }

  @Override
  public void close() {
    // No resource to close.
  }

  @Override
  public void remove() {
    // No resource to remove.
  }

  private void handleInsert(Object[] newRow) throws SQLException {
    String newStatus = valueOf(newRow[STATUS]);

    if (newStatus == null) {
      newRow[STATUS] = "WAITING";
      newStatus = "WAITING";
    }

    if (newRow[WAKTU_PERMINTAAN] == null) {
      newRow[WAKTU_PERMINTAAN] = OffsetDateTime.now();
    }

    if (!"WAITING".equals(newStatus)) {
      throw new SQLException("Initial service request status must be WAITING");
    }
  }

  private void handleUpdate(Object[] oldRow, Object[] newRow) throws SQLException {
    String oldStatus = valueOf(oldRow[STATUS]);
    String newStatus = valueOf(newRow[STATUS]);

    if (Objects.equals(oldStatus, newStatus)) {
      return;
    }

    validateTransition(oldStatus, newStatus);
    fillStatusTimestamp(newRow, newStatus);
  }

  private void validateTransition(String oldStatus, String newStatus) throws SQLException {
    if ("WAITING".equals(oldStatus) && !isOneOf(newStatus, "ACCEPTED", "REJECTED", "CANCELLED")) {
      throw new SQLException("Invalid status transition from WAITING to " + newStatus);
    }

    if ("ACCEPTED".equals(oldStatus) && !isOneOf(newStatus, "ON_PROGRESS", "CANCELLED")) {
      throw new SQLException("Invalid status transition from ACCEPTED to " + newStatus);
    }

    if ("ON_PROGRESS".equals(oldStatus) && !isOneOf(newStatus, "COMPLETED", "CANCELLED")) {
      throw new SQLException("Invalid status transition from ON_PROGRESS to " + newStatus);
    }

    if (isOneOf(oldStatus, "COMPLETED", "CANCELLED", "REJECTED")) {
      throw new SQLException("Status " + oldStatus + " is final and cannot be changed");
    }
  }

  private void fillStatusTimestamp(Object[] newRow, String newStatus) {
    OffsetDateTime now = OffsetDateTime.now();

    if ("ACCEPTED".equals(newStatus) && newRow[WAKTU_DITERIMA] == null) {
      newRow[WAKTU_DITERIMA] = now;
    }

    if ("ON_PROGRESS".equals(newStatus) && newRow[WAKTU_DIPROSES] == null) {
      newRow[WAKTU_DIPROSES] = now;
    }

    if ("COMPLETED".equals(newStatus) && newRow[WAKTU_SELESAI] == null) {
      newRow[WAKTU_SELESAI] = now;
    }

    if ("CANCELLED".equals(newStatus) && newRow[WAKTU_DIBATALKAN] == null) {
      newRow[WAKTU_DIBATALKAN] = now;
    }

    if ("REJECTED".equals(newStatus) && newRow[WAKTU_DITOLAK] == null) {
      newRow[WAKTU_DITOLAK] = now;
    }
  }

  private boolean isOneOf(String value, String... allowedValues) {
    for (String allowedValue : allowedValues) {
      if (allowedValue.equals(value)) {
        return true;
      }
    }

    return false;
  }

  private String valueOf(Object value) {
    return value == null ? null : value.toString();
  }
}
