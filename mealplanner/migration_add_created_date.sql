-- Migration script to add created_date column to dishes table
-- Run this script on your database to add the created_date column

-- Add the created_date column
ALTER TABLE dishes ADD COLUMN created_date TIMESTAMP;

-- Update existing records with current timestamp
UPDATE dishes SET created_date = CURRENT_TIMESTAMP WHERE created_date IS NULL;

-- Make the column NOT NULL after setting default values
ALTER TABLE dishes ALTER COLUMN created_date SET NOT NULL;

-- Add a default value for future inserts
ALTER TABLE dishes ALTER COLUMN created_date SET DEFAULT CURRENT_TIMESTAMP; 