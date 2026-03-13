CREATE TABLE IF NOT EXISTS vets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INT(4) UNSIGNED NOT NULL,
  specialty_id INT(4) UNSIGNED NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  UNIQUE (vet_id,specialty_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  INDEX(last_name),
  UNIQUE KEY uc_owner_name_telephone ((LOWER(first_name)), (LOWER(last_name)), telephone)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT(4) UNSIGNED NOT NULL,
  owner_id INT(4) UNSIGNED,
  INDEX(name),
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS visits (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id INT(4) UNSIGNED,
  visit_date DATE,
  description VARCHAR(255),
  FOREIGN KEY (pet_id) REFERENCES pets(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS appointment_types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  default_duration_minutes INT NOT NULL,
  specialty_id INT(4) UNSIGNED,
  description VARCHAR(255),
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_appointment_types_name (name),
  INDEX idx_appointment_types_name (name),
  CONSTRAINT chk_appointment_type_duration CHECK (default_duration_minutes >= 1),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  version INT NOT NULL DEFAULT 0,
  appointment_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
  notes VARCHAR(500),
  pet_id INT(4) UNSIGNED NOT NULL,
  vet_id INT(4) UNSIGNED NOT NULL,
  appointment_type_id INT(4) UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL,
  cancelled_at DATETIME,
  INDEX idx_appointment_vet_date (vet_id, appointment_date, start_time, end_time),
  INDEX idx_appointment_pet_date (pet_id, appointment_date),
  INDEX idx_appointment_status (status),
  CONSTRAINT chk_appointment_time_range CHECK (start_time < end_time),
  CONSTRAINT chk_appointment_status CHECK (status IN ('SCHEDULED','CONFIRMED','CANCELLED','COMPLETED')),
  CONSTRAINT chk_appointment_version CHECK (version >= 0),
  FOREIGN KEY (pet_id) REFERENCES pets(id),
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS clinic_schedule_config (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  day_of_week INT NOT NULL,
  open_time TIME NOT NULL,
  close_time TIME NOT NULL,
  slot_duration_minutes INT NOT NULL DEFAULT 30,
  is_open TINYINT(1) NOT NULL DEFAULT 1,
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_clinic_schedule_day (day_of_week),
  CONSTRAINT chk_clinic_schedule_day_range CHECK (day_of_week >= 1 AND day_of_week <= 7),
  CONSTRAINT chk_clinic_slot_duration_bounds CHECK (slot_duration_minutes >= 5),
  CONSTRAINT chk_clinic_open_close CHECK (is_open = 0 OR open_time < close_time)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_schedules (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  vet_id INT(4) UNSIGNED NOT NULL,
  day_of_week INT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  is_available TINYINT(1) NOT NULL DEFAULT 1,
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_vet_schedule_day (vet_id, day_of_week),
  CONSTRAINT chk_vet_schedule_day_range CHECK (day_of_week >= 1 AND day_of_week <= 7),
  CONSTRAINT chk_vet_schedule_time_range CHECK (start_time < end_time),
  FOREIGN KEY (vet_id) REFERENCES vets(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_time_off (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  vet_id INT(4) UNSIGNED NOT NULL,
  off_date DATE NOT NULL,
  reason VARCHAR(255),
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_vet_time_off (vet_id, off_date),
  FOREIGN KEY (vet_id) REFERENCES vets(id)
) engine=InnoDB;
