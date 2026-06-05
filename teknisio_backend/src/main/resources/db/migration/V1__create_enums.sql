-- ============================================================
-- Teknisio Migration V1: ENUM types for H2
-- ============================================================

CREATE TYPE user_role AS ENUM (
  'CUSTOMER',
  'TECHNICIAN',
  'ADMIN'
);

CREATE TYPE user_status AS ENUM (
  'ACTIVE',
  'INACTIVE',
  'BANNED',
  'SUSPENDED'
);

CREATE TYPE teknisi_status AS ENUM (
  'ONLINE',
  'OFFLINE',
  'BUSY',
  'ON_LEAVE'
);

CREATE TYPE request_status AS ENUM (
  'WAITING',
  'ACCEPTED',
  'ON_PROGRESS',
  'COMPLETED',
  'CANCELLED',
  'REJECTED'
);
