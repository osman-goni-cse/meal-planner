# Database Migration for View Dish Functionality

## Overview
This migration adds a `created_date` column to the `dishes` table to support the new view dish functionality.

## Migration Steps

### 1. Run the SQL Migration Script
Execute the following SQL script on your PostgreSQL database:

```sql
-- Add the created_date column
ALTER TABLE dishes ADD COLUMN created_date TIMESTAMP;

-- Update existing records with current timestamp
UPDATE dishes SET created_date = CURRENT_TIMESTAMP WHERE created_date IS NULL;

-- Make the column NOT NULL after setting default values
ALTER TABLE dishes ALTER COLUMN created_date SET NOT NULL;

-- Add a default value for future inserts
ALTER TABLE dishes ALTER COLUMN created_date SET DEFAULT CURRENT_TIMESTAMP;
```

### 2. Restart the Application
After running the migration, restart your Spring Boot application. The new `created_date` field will be automatically populated for new dishes.

## Features Added

### View Dish Modal
- **Location**: `fragments/view-dish-modal.html`
- **Functionality**: Displays detailed dish information in a modal
- **Features**:
  - Dish image with fallback placeholder
  - Basic information (name, category, meal period)
  - Description
  - Dietary information with visual indicators
  - Created date
  - Edit button (for ADMIN users only)

### Integration
- The view modal is integrated into the dish management page
- "View Details" links now open the modal instead of navigating to a separate page
- The modal uses the existing `/dishes/json/{id}` endpoint to fetch dish data

### Styling
- Professional modal design with Tailwind CSS
- Responsive layout
- Smooth animations and transitions
- Proper error handling for missing images
- Role-based access control for edit functionality

## Testing
1. Navigate to the dish management page
2. Click the three-dot menu on any dish card
3. Select "View Details"
4. Verify that the modal displays all dish information correctly
5. Test the edit functionality (if you have ADMIN role)
6. Test the close functionality

## Notes
- The migration script is located at `migration_add_created_date.sql`
- A default SVG placeholder image is provided at `static/images/default-dish.svg`
- The view modal includes proper error handling for missing data 