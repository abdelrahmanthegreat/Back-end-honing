-- 1. Membership details
SELECT m.membership_id,
       mem.full_name AS member_name,
       p.plan_name,
       m.start_date,
       m.membership_status
FROM membership m
INNER JOIN member mem ON m.member_id = mem.member_id
INNER JOIN membership_plan p ON m.plan_id = p.plan_id;

-- 2. Members and memberships
SELECT mem.member_id,
       mem.full_name,
       m.membership_id,
       m.start_date,
       m.membership_status
FROM member mem
LEFT JOIN membership m ON mem.member_id = m.member_id;

-- 3. Classes and bookings (RIGHT JOIN)
SELECT fc.class_id,
       fc.class_name,
       cb.booking_id,
       cb.member_id,
       cb.booking_date,
       cb.attendance_status
FROM class_booking cb
RIGHT JOIN fitness_class fc ON cb.class_id = fc.class_id;

-- 4. Trainers and classes
SELECT t.trainer_id,
       t.full_name AS trainer_name,
       fc.class_id,
       fc.class_name
FROM trainer t
LEFT JOIN fitness_class fc ON t.trainer_id = fc.trainer_id;

-- 5. Plans summary
SELECT p.plan_name,
       COUNT(m.membership_id) AS number_of_memberships,
       AVG(p.price) AS average_plan_price
FROM membership_plan p
JOIN membership m ON p.plan_id = m.plan_id
GROUP BY p.plan_id, p.plan_name
ORDER BY average_plan_price DESC;

-- 6. Member booking counts
SELECT mem.full_name,
       COUNT(cb.booking_id) AS total_bookings
FROM member mem
LEFT JOIN class_booking cb ON mem.member_id = cb.member_id
GROUP BY mem.member_id, mem.full_name
ORDER BY total_bookings DESC, mem.full_name ASC;

-- 7. Successful revenue by plan
SELECT p.plan_name,
       SUM(pay.amount) AS total_revenue
FROM payment pay
JOIN membership m ON pay.membership_id = m.membership_id
JOIN membership_plan p ON m.plan_id = p.plan_id
WHERE pay.payment_status = 'Successful'
GROUP BY p.plan_name
ORDER BY total_revenue DESC;

-- 8. Class booking counts
SELECT fc.class_name,
       COUNT(cb.booking_id) AS number_of_bookings
FROM fitness_class fc
LEFT JOIN class_booking cb ON fc.class_id = cb.class_id
GROUP BY fc.class_id, fc.class_name
ORDER BY number_of_bookings DESC;

-- 9. Membership status label
SELECT membership_id,
       CASE membership_status
         WHEN 'Active'  THEN 'Active Membership'
         WHEN 'Expired' THEN 'Expired Membership'
         WHEN 'Pending' THEN 'Pending Membership'
       END AS status_label
FROM membership
ORDER BY membership_id;

-- 10. Member contact value
SELECT full_name,
       NVL(phone, NVL(email, 'No contact details')) AS contact_value
FROM member;

-- 11. Amount paid per membership
SELECT m.membership_id,
       NVL(SUM(p.amount), 0) AS total_paid
FROM membership m
LEFT JOIN payment p ON m.membership_id = p.membership_id
GROUP BY m.membership_id
ORDER BY m.membership_id;

-- 12. Trainer class totals
SELECT t.full_name,
       COUNT(fc.class_id) AS class_count
FROM trainer t
LEFT JOIN fitness_class fc ON t.trainer_id = fc.trainer_id
GROUP BY t.trainer_id, t.full_name
ORDER BY class_count DESC;

-- 13. Payment status label
SELECT payment_id,
       membership_id,
       payment_method,
       amount,
       payment_date,
       CASE payment_status
         WHEN 'Successful' THEN 'Completed'
         WHEN 'Pending'    THEN 'Pending'
         WHEN 'Failed'     THEN 'Not completed'
       END AS status_label
FROM payment
ORDER BY payment_date DESC;

-- 14. Payment statistics
SELECT payment_method,
       SUM(amount) AS total_amount,
       AVG(amount) AS average_amount,
       MIN(amount) AS minimum_amount,
       MAX(amount) AS maximum_amount,
       COUNT(*) AS number_of_payments
FROM payment
GROUP BY payment_method;

-- 15. Members with multiple successful payments
SELECT mem.full_name,
       COUNT(pay.payment_id) AS successful_payments,
       SUM(pay.amount) AS total_paid
FROM member mem
JOIN membership m ON mem.member_id = m.member_id
JOIN payment pay ON m.membership_id = pay.membership_id
WHERE pay.payment_status = 'Successful'
GROUP BY mem.member_id, mem.full_name
HAVING COUNT(pay.payment_id) >= 2
ORDER BY total_paid DESC;
