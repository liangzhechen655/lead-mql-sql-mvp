CREATE TABLE sales_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  role VARCHAR(30) NOT NULL,
  team VARCHAR(80)
);

CREATE TABLE lead_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(80) NOT NULL,
  phone VARCHAR(30) NOT NULL UNIQUE,
  source VARCHAR(80) NOT NULL,
  channel VARCHAR(80),
  status VARCHAR(40) NOT NULL,
  call_status VARCHAR(40) NOT NULL,
  wechat_status VARCHAR(40) NOT NULL,
  grade VARCHAR(20) NOT NULL,
  owner_id BIGINT,
  invalid_reason VARCHAR(200),
  remark VARCHAR(500),
  version BIGINT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  imported_at TIMESTAMP,
  assigned_at TIMESTAMP,
  last_follow_up_at TIMESTAMP,
  mql_at TIMESTAMP,
  sql_at TIMESTAMP,
  CONSTRAINT fk_lead_owner FOREIGN KEY (owner_id) REFERENCES sales_user(id)
);

CREATE TABLE follow_up_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lead_id BIGINT NOT NULL,
  operator_id BIGINT,
  method VARCHAR(50) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  next_follow_up_at TIMESTAMP,
  created_at TIMESTAMP,
  CONSTRAINT fk_follow_lead FOREIGN KEY (lead_id) REFERENCES lead_record(id),
  CONSTRAINT fk_follow_operator FOREIGN KEY (operator_id) REFERENCES sales_user(id)
);

CREATE TABLE status_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lead_id BIGINT NOT NULL,
  from_status VARCHAR(40),
  to_status VARCHAR(40) NOT NULL,
  operator_name VARCHAR(80) NOT NULL,
  reason VARCHAR(500),
  changed_at TIMESTAMP,
  CONSTRAINT fk_status_lead FOREIGN KEY (lead_id) REFERENCES lead_record(id)
);

CREATE TABLE operation_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lead_id BIGINT,
  action VARCHAR(80) NOT NULL,
  operator_name VARCHAR(80) NOT NULL,
  detail VARCHAR(1000),
  created_at TIMESTAMP,
  CONSTRAINT fk_operation_lead FOREIGN KEY (lead_id) REFERENCES lead_record(id)
);

CREATE TABLE import_batch (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  file_name VARCHAR(160) NOT NULL,
  total_rows INT NOT NULL,
  inserted_rows INT NOT NULL,
  duplicate_rows INT NOT NULL,
  failed_rows INT NOT NULL,
  operator_name VARCHAR(80) NOT NULL,
  created_at TIMESTAMP
);

CREATE TABLE third_party_callback_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lead_id BIGINT,
  system_name VARCHAR(60) NOT NULL,
  event_type VARCHAR(60) NOT NULL,
  result VARCHAR(30) NOT NULL,
  payload VARCHAR(1000),
  created_at TIMESTAMP,
  CONSTRAINT fk_callback_lead FOREIGN KEY (lead_id) REFERENCES lead_record(id)
);
