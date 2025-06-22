-- Add test users to the users table
-- Replace 'your-email@gmail.com' with your actual Google email address

INSERT INTO users (email, role) VALUES 
('your-email@gmail.com', 'Admin'),
('test@example.com', 'Employee');

-- To see all users in the database:
-- SELECT * FROM users; 