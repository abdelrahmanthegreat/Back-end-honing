-- CREATE TABLE statements

CREATE TABLE member (
    member_id   NUMBER PRIMARY KEY,
    full_name   VARCHAR2(100) NOT NULL,
    email       VARCHAR2(100),
    phone       VARCHAR2(20),
    join_date   DATE NOT NULL,
    city        VARCHAR2(50) NOT NULL
);

CREATE TABLE membership_plan (
    plan_id          NUMBER PRIMARY KEY,
    plan_name        VARCHAR2(50) NOT NULL,
    duration_months  NUMBER(3) NOT NULL,
    price            NUMBER(10,2) NOT NULL,
    CONSTRAINT chk_plan_duration CHECK (duration_months > 0),
    CONSTRAINT chk_plan_price    CHECK (price > 0)
);

CREATE TABLE trainer (
    trainer_id      NUMBER PRIMARY KEY,
    full_name       VARCHAR2(100) NOT NULL,
    specialization  VARCHAR2(50),
    hire_date       DATE NOT NULL
);

CREATE TABLE membership (
    membership_id      NUMBER PRIMARY KEY,
    member_id          NUMBER NOT NULL,
    plan_id            NUMBER NOT NULL,
    start_date         DATE NOT NULL,
    end_date           DATE NOT NULL,
    membership_status  VARCHAR2(20) NOT NULL,
    CONSTRAINT fk_membership_member FOREIGN KEY (member_id)
        REFERENCES member(member_id),
    CONSTRAINT fk_membership_plan   FOREIGN KEY (plan_id)
        REFERENCES membership_plan(plan_id),
    CONSTRAINT chk_membership_status CHECK (membership_status IN ('Active','Expired','Pending'))
);

CREATE TABLE fitness_class (
    class_id      NUMBER PRIMARY KEY,
    class_name    VARCHAR2(50) NOT NULL,
    trainer_id    NUMBER,
    capacity      NUMBER(4) NOT NULL,
    scheduled_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_class_trainer FOREIGN KEY (trainer_id)
        REFERENCES trainer(trainer_id),
    CONSTRAINT chk_class_capacity CHECK (capacity > 0)
);

CREATE TABLE class_booking (
    booking_id         NUMBER PRIMARY KEY,
    member_id          NUMBER NOT NULL,
    class_id           NUMBER NOT NULL,
    booking_date       DATE NOT NULL,
    attendance_status  VARCHAR2(20) NOT NULL,
    CONSTRAINT fk_booking_member FOREIGN KEY (member_id)
        REFERENCES member(member_id),
    CONSTRAINT fk_booking_class  FOREIGN KEY (class_id)
        REFERENCES fitness_class(class_id),
    CONSTRAINT chk_attendance_status CHECK (attendance_status IN ('Booked','Attended','Absent'))
);

CREATE TABLE payment (
    payment_id      NUMBER PRIMARY KEY,
    membership_id   NUMBER NOT NULL,
    payment_date    DATE NOT NULL,
    amount          NUMBER(10,2) NOT NULL,
    payment_method  VARCHAR2(20) NOT NULL,
    payment_status  VARCHAR2(20) NOT NULL,
    CONSTRAINT fk_payment_membership FOREIGN KEY (membership_id)
        REFERENCES membership(membership_id),
    CONSTRAINT chk_payment_amount  CHECK (amount > 0),
    CONSTRAINT chk_payment_method  CHECK (payment_method IN ('Cash','Card','Online')),
    CONSTRAINT chk_payment_status  CHECK (payment_status IN ('Successful','Pending','Failed'))
);

-- INSERT statements

-- MEMBER
INSERT INTO member VALUES (1, 'Alice Johnson', 'alice@example.com', '01011112222', DATE '2023-01-15', 'Cairo');
INSERT INTO member VALUES (2, 'Bob Smith', 'bob@example.com', '01022223333', DATE '2023-02-20', 'Giza');
INSERT INTO member VALUES (3, 'Charlie Brown', 'charlie@example.com', '01033334444', DATE '2023-03-10', 'Alexandria');
INSERT INTO member VALUES (4, 'Diana Prince', 'diana@example.com', '01044445555', DATE '2023-04-05', 'Cairo');
INSERT INTO member VALUES (5, 'Evan Wright', 'evan@example.com', '01055556666', DATE '2023-05-12', 'Luxor');
INSERT INTO member VALUES (6, 'Fiona Green', 'fiona@example.com', '01066667777', DATE '2023-06-18', 'Aswan');
INSERT INTO member VALUES (7, 'George Hall', 'george@example.com', NULL, DATE '2023-07-22', 'Cairo');
INSERT INTO member VALUES (8, 'Hannah Lee', 'hannah@example.com', '01088889999', DATE '2023-08-30', 'Giza');
INSERT INTO member VALUES (9, 'Ian Clark', 'ian@example.com', '01099990000', DATE '2023-09-14', 'Cairo');
INSERT INTO member VALUES (10, 'Julia Adams', 'julia@example.com', '01010101010', DATE '2023-10-01', 'Mansoura');
INSERT INTO member VALUES (11, 'Kevin White', 'kevin@example.com', NULL, DATE '2023-11-05', 'Cairo');
INSERT INTO member VALUES (12, 'Laura Black', 'laura@example.com', '01012121212', DATE '2023-12-10', 'Giza');
INSERT INTO member VALUES (13, 'No Contact', NULL, NULL, DATE '2024-01-02', 'Cairo');

-- MEMBERSHIP_PLAN
INSERT INTO membership_plan VALUES (1, 'Basic Monthly', 1, 500.00);
INSERT INTO membership_plan VALUES (2, 'Standard Quarterly', 3, 1200.00);
INSERT INTO membership_plan VALUES (3, 'Premium Yearly', 12, 5000.00);
INSERT INTO membership_plan VALUES (4, 'Student Plan', 6, 800.00);
INSERT INTO membership_plan VALUES (5, 'Day Pass Pack', 1, 200.00);

-- TRAINER
INSERT INTO trainer VALUES (1, 'John Trainer', 'Strength', DATE '2020-01-10');
INSERT INTO trainer VALUES (2, 'Sarah Coach', 'Yoga', DATE '2021-03-15');
INSERT INTO trainer VALUES (3, 'Mike Fitness', 'Cardio', DATE '2019-07-01');
INSERT INTO trainer VALUES (4, 'Linda Wellness', 'Pilates', DATE '2022-02-20');
INSERT INTO trainer VALUES (5, 'Tom Power', 'CrossFit', DATE '2021-11-05');
INSERT INTO trainer VALUES (6, 'Nina Zen', 'Meditation', DATE '2023-01-01');

-- MEMBERSHIP
INSERT INTO membership VALUES (1, 1, 1, DATE '2024-01-01', DATE '2024-02-01', 'Active');
INSERT INTO membership VALUES (2, 1, 2, DATE '2024-02-01', DATE '2024-05-01', 'Active');
INSERT INTO membership VALUES (3, 2, 1, DATE '2024-01-15', DATE '2024-02-15', 'Expired');
INSERT INTO membership VALUES (4, 3, 3, DATE '2024-01-01', DATE '2025-01-01', 'Active');
INSERT INTO membership VALUES (5, 4, 4, DATE '2024-03-01', DATE '2024-09-01', 'Pending');
INSERT INTO membership VALUES (6, 5, 1, DATE '2024-04-01', DATE '2024-05-01', 'Expired');
INSERT INTO membership VALUES (7, 6, 2, DATE '2024-05-01', DATE '2024-08-01', 'Active');
INSERT INTO membership VALUES (8, 8, 3, DATE '2024-06-01', DATE '2025-06-01', 'Active');
INSERT INTO membership VALUES (9, 9, 4, DATE '2024-07-01', DATE '2025-01-01', 'Pending');
INSERT INTO membership VALUES (10, 10, 1, DATE '2024-08-01', DATE '2024-09-01', 'Expired');

-- FITNESS_CLASS
INSERT INTO fitness_class VALUES (1, 'Morning Yoga', 2, 20, TIMESTAMP '2026-11-01 08:00:00');
INSERT INTO fitness_class VALUES (2, 'Strength Training', 1, 15, TIMESTAMP '2026-11-02 10:00:00');
INSERT INTO fitness_class VALUES (3, 'Cardio Blast', 3, 25, TIMESTAMP '2026-11-03 17:00:00');
INSERT INTO fitness_class VALUES (4, 'Pilates Core', 4, 12, TIMESTAMP '2026-11-04 09:00:00');
INSERT INTO fitness_class VALUES (5, 'CrossFit WOD', 5, 18, TIMESTAMP '2026-11-05 18:00:00');
INSERT INTO fitness_class VALUES (6, 'HIIT Session', 1, 20, TIMESTAMP '2026-11-06 07:00:00');
INSERT INTO fitness_class VALUES (7, 'Meditation Class', NULL, 15, TIMESTAMP '2026-11-07 19:00:00');
INSERT INTO fitness_class VALUES (8, 'Zumba Dance', 2, 30, TIMESTAMP '2026-11-08 16:00:00');

-- CLASS_BOOKING
INSERT INTO class_booking VALUES (1, 1, 1, DATE '2024-10-01', 'Booked');
INSERT INTO class_booking VALUES (2, 1, 2, DATE '2024-10-02', 'Attended');
INSERT INTO class_booking VALUES (3, 2, 1, DATE '2024-10-01', 'Attended');
INSERT INTO class_booking VALUES (4, 3, 3, DATE '2024-10-03', 'Booked');
INSERT INTO class_booking VALUES (5, 4, 4, DATE '2024-10-04', 'Absent');
INSERT INTO class_booking VALUES (6, 5, 5, DATE '2024-10-05', 'Attended');
INSERT INTO class_booking VALUES (7, 6, 6, DATE '2024-10-06', 'Booked');
INSERT INTO class_booking VALUES (8, 8, 3, DATE '2024-10-03', 'Attended');
INSERT INTO class_booking VALUES (9, 9, 1, DATE '2024-10-01', 'Booked');
INSERT INTO class_booking VALUES (10, 10, 2, DATE '2024-10-02', 'Attended');

-- PAYMENT
INSERT INTO payment VALUES (1, 1, DATE '2024-01-02', 500.00, 'Cash', 'Successful');
INSERT INTO payment VALUES (2, 2, DATE '2024-02-02', 1200.00, 'Card', 'Successful');
INSERT INTO payment VALUES (3, 3, DATE '2024-01-20', 500.00, 'Online', 'Successful');
INSERT INTO payment VALUES (4, 4, DATE '2024-01-05', 5000.00, 'Card', 'Successful');
INSERT INTO payment VALUES (5, 5, DATE '2024-03-05', 800.00, 'Cash', 'Pending');
INSERT INTO payment VALUES (6, 6, DATE '2024-04-05', 500.00, 'Online', 'Failed');
INSERT INTO payment VALUES (7, 7, DATE '2024-05-05', 1200.00, 'Card', 'Successful');
INSERT INTO payment VALUES (8, 8, DATE '2024-06-05', 5000.00, 'Cash', 'Successful');
INSERT INTO payment VALUES (9, 8, DATE '2024-07-05', 5000.00, 'Online', 'Successful');
INSERT INTO payment VALUES (10, 10, DATE '2024-08-05', 500.00, 'Cash', 'Failed');

COMMIT;
