-- ============================================================
-- Teknisio Migration V5: Seed default device categories
-- H2 version
-- ============================================================

MERGE INTO kategori_layanan (nama_kategori, icon, aktif, updated_at)
KEY (nama_kategori)
VALUES
  ('Air Conditioner', 'air-conditioner', TRUE, CURRENT_TIMESTAMP),
  ('Refrigerator', 'refrigerator', TRUE, CURRENT_TIMESTAMP),
  ('Washing Machine', 'washing-machine', TRUE, CURRENT_TIMESTAMP),
  ('Television', 'television', TRUE, CURRENT_TIMESTAMP),
  ('Fan', 'fan', TRUE, CURRENT_TIMESTAMP),
  ('Rice Cooker', 'rice-cooker', TRUE, CURRENT_TIMESTAMP),
  ('Oven', 'oven', TRUE, CURRENT_TIMESTAMP),
  ('Mixer', 'mixer', TRUE, CURRENT_TIMESTAMP);
