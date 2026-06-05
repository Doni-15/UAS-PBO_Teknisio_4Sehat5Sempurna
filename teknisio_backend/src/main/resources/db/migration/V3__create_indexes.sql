-- ============================================================
-- Teknisio Migration V3: Indexes
-- H2 version
-- ============================================================

-- ============================================================
-- UNIQUE INDEXES
-- ============================================================

CREATE UNIQUE INDEX uq_users_email_active
  ON users (email);

CREATE UNIQUE INDEX uq_users_no_telepon_active
  ON users (no_telepon);

CREATE UNIQUE INDEX uq_kategori_layanan_nama_active
  ON kategori_layanan (nama_kategori);

-- ============================================================
-- LOOKUP / FILTER INDEXES
-- ============================================================

CREATE INDEX idx_users_role_status
  ON users (role, status_akun);

CREATE INDEX idx_teknisi_profile_user
  ON teknisi_profile (id_user);

CREATE INDEX idx_teknisi_profile_status
  ON teknisi_profile (status_ketersediaan);

CREATE INDEX idx_kategori_layanan_active
  ON kategori_layanan (aktif);

CREATE INDEX idx_teknisi_kategori_kategori
  ON teknisi_kategori_layanan (id_kategori);

CREATE INDEX idx_teknisi_kategori_teknisi
  ON teknisi_kategori_layanan (id_teknisi_profile);

CREATE INDEX idx_permintaan_pengguna
  ON permintaan_layanan (id_pengguna);

CREATE INDEX idx_permintaan_teknisi_profile
  ON permintaan_layanan (id_teknisi_profile);

CREATE INDEX idx_permintaan_status
  ON permintaan_layanan (status);

CREATE INDEX idx_permintaan_waktu
  ON permintaan_layanan (waktu_permintaan DESC);

CREATE INDEX idx_permintaan_kategori_kategori
  ON permintaan_layanan_kategori (id_kategori);

CREATE INDEX idx_riwayat_status_permintaan
  ON riwayat_status (id_permintaan, created_at ASC);

CREATE INDEX idx_user_session_user
  ON user_session (id_user);

CREATE INDEX idx_user_session_expired_at
  ON user_session (expired_at);

CREATE INDEX idx_user_session_active
  ON user_session (id_user, expired_at);
