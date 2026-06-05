-- ============================================================
-- Teknisio Migration V4: H2 Java triggers
-- H2 version
-- ============================================================

-- ============================================================
-- TRIGGERS: updated_at
-- ============================================================

CREATE TRIGGER IF NOT EXISTS trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

CREATE TRIGGER IF NOT EXISTS trg_teknisi_profile_updated_at
BEFORE UPDATE ON teknisi_profile
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

CREATE TRIGGER IF NOT EXISTS trg_kategori_layanan_updated_at
BEFORE UPDATE ON kategori_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

CREATE TRIGGER IF NOT EXISTS trg_teknisi_kategori_layanan_updated_at
BEFORE UPDATE ON teknisi_kategori_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

CREATE TRIGGER IF NOT EXISTS trg_permintaan_layanan_updated_at
BEFORE UPDATE ON permintaan_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

CREATE TRIGGER IF NOT EXISTS trg_user_session_updated_at
BEFORE UPDATE ON user_session
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";

-- ============================================================
-- TRIGGERS: service request status rules
-- ============================================================

CREATE TRIGGER IF NOT EXISTS trg_permintaan_status_flow_insert
BEFORE INSERT ON permintaan_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.PermintaanStatusFlowTrigger";

CREATE TRIGGER IF NOT EXISTS trg_permintaan_status_flow_update
BEFORE UPDATE ON permintaan_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.PermintaanStatusFlowTrigger";

CREATE TRIGGER IF NOT EXISTS trg_permintaan_status_history_insert
AFTER INSERT ON permintaan_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.PermintaanStatusHistoryTrigger";

CREATE TRIGGER IF NOT EXISTS trg_permintaan_status_history_update
AFTER UPDATE ON permintaan_layanan
FOR EACH ROW
CALL "com.teknisio.database.trigger.PermintaanStatusHistoryTrigger";
