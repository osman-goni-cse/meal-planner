-- Add test users to the users table
-- Replace 'your-email@gmail.com' with your actual Google email address

INSERT INTO users (email, role) VALUES 
('your-email@gmail.com', 'ADMIN'),
('test@example.com', 'EMPLOYEE');

-- To see all users in the database:
-- SELECT * FROM users; 