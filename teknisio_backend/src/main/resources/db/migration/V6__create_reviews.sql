-- ============================================================
-- Teknisio Migration V6: Reviews
-- H2 version
-- ============================================================

CREATE TABLE review (
  id_review            UUID DEFAULT RANDOM_UUID() PRIMARY KEY,

  id_permintaan        UUID NOT NULL UNIQUE,
  id_customer          UUID NOT NULL,
  id_teknisi_profile   UUID NOT NULL,

  rating               INTEGER NOT NULL,
  comment              TEXT,

  created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at           TIMESTAMP WITH TIME ZONE,

  CONSTRAINT fk_review_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE,

  CONSTRAINT fk_review_customer
    FOREIGN KEY (id_customer)
    REFERENCES users (id_user)
    ON DELETE RESTRICT,

  CONSTRAINT fk_review_teknisi_profile
    FOREIGN KEY (id_teknisi_profile)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE RESTRICT,

  CONSTRAINT chk_review_rating
    CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_review_customer
  ON review (id_customer);

CREATE INDEX idx_review_teknisi_profile
  ON review (id_teknisi_profile);

CREATE INDEX idx_review_created_at
  ON review (created_at DESC);

CREATE TRIGGER IF NOT EXISTS trg_review_updated_at
BEFORE UPDATE ON review
FOR EACH ROW
CALL "com.teknisio.database.trigger.UpdatedAtTrigger";
