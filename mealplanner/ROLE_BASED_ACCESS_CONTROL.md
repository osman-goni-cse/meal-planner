# Role-Based Access Control (RBAC) Implementation

This document describes the role-based access control implementation in the DSi Meal Planner application.

## Overview

The application implements role-based access control with two user roles:

- **Admin**: Can access all pages and features
- **Employee**: Can only access the dashboard page

## User Roles

### Admin Role
- **Access**: All pages and features
- **Pages**: Dashboard, Weekly Plan Management, Dish Management, Add New Dish
- **Database Role**: `Admin`
- **Spring Security Role**: `ROLE_ADMIN`

### Employee Role
- **Access**: Dashboard only
- **Pages**: Dashboard (Today's Menu)
- **Database Role**: `Employee`
- **Spring Security Role**: `ROLE_EMPLOYEE`

## Implementation Details

### Authentication Flow
1. **OAuth2 Login**: User logs in via Google OAuth2
2. **Database Lookup**: `CustomOAuth2UserService` looks up user by email in database
3. **Role Assignment**: User's role from database is converted to Spring Security authority
4. **Authority Creation**: Role is prefixed with `ROLE_` (e.g., `Admin` → `ROLE_ADMIN`)

### Security Configuration
- **File**: `SecurityConfig.java`
- **Method**: Role-based URL access control using Spring Security
- **Patterns**:
  - `/dashboard/**` - Accessible by both ADMIN and EMPLOYEE roles
  - `/weekly-plan/**` - Admin only
  - `/dishes/**` - Admin only
  - `/` - Public (redirects to dashboard)

### Access Denied Handling
- **Custom Access Denied Handler**: Redirects unauthorized users to `/access-denied`
- **Access Denied Page**: User-friendly error page with return to dashboard option

### Template Updates
- **Conditional Navigation**: Admin-only links hidden from Employee users
- **Thymeleaf Security Tags**: `sec:authorize="hasAuthority('ROLE_ADMIN')"` used throughout templates
- **Updated Templates**:
  - `dashboard.html` - Conditional weekly plan links
  - `fragments/layout.html` - Conditional sidebar navigation
  - `home.html` - Conditional quick action cards

### Logging
- **Controllers**: Added logging to track user access and roles
- **Debug Information**: User email and role logged for troubleshooting
- **Authentication Service**: Detailed logging of OAuth2 user creation and authorities

## Database Setup

### Test Users
The `add_test_users.sql` file contains sample users for testing:

```sql
INSERT INTO users (email, role) VALUES 
('your-email@gmail.com', 'Admin'),
('test@example.com', 'Employee');
```

### User Table Structure
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL
);
```

## Testing the Implementation

### Authentication Test
1. **Test Endpoint**: Visit `/test-auth` to verify authentication and role assignment
2. **Expected Output**: Should show your email and authorities (e.g., `[ROLE_ADMIN]`)

### Admin User Test
1. Login with an Admin user account
2. Verify access to all pages:
   - Dashboard ✅
   - Weekly Plan Management ✅
   - Dish Management ✅
   - Add New Dish ✅

### Employee User Test
1. Login with an Employee user account
2. Verify access to dashboard only:
   - Dashboard ✅
   - Weekly Plan Management ❌ (redirects to access denied)
   - Dish Management ❌ (redirects to access denied)
   - Add New Dish ❌ (redirects to access denied)

### Navigation Test
1. Employee users should not see admin-only navigation links
2. Admin users should see all navigation options
3. Direct URL access should be blocked for unauthorized users

## Troubleshooting

### Common Issues

#### 1. "User not found in database" Error
- **Cause**: Your Google email is not in the users table
- **Solution**: Add your email to the database:
  ```sql
  INSERT INTO users (email, role) VALUES ('your-email@gmail.com', 'Admin');
  ```

#### 2. Access Denied for All Pages
- **Cause**: Role not properly assigned or Spring Security role mismatch
- **Solution**: 
  - Check database role is `Admin` or `Employee` (case-sensitive)
  - Verify `/test-auth` shows correct authorities
  - Check application logs for authentication details

#### 3. Navigation Links Not Showing/Hiding
- **Cause**: Thymeleaf security expressions using wrong role names
- **Solution**: Ensure templates use `hasAuthority('ROLE_ADMIN')` not `hasRole('ROLE_ADMIN')`

### Debug Steps
1. **Check Authentication**: Visit `/test-auth` endpoint
2. **Check Database**: Verify user exists with correct role
3. **Check Logs**: Look for authentication and authority assignment logs
4. **Check Role Names**: Ensure database uses `Admin`/`Employee`, not `ROLE_ADMIN`/`ROLE_EMPLOYEE`

## Security Features

### URL Protection
- Spring Security intercepts all requests
- Role-based authorization at the URL level
- Automatic redirect to access denied page

### Template Protection
- Thymeleaf security tags prevent rendering of unauthorized content
- Conditional display based on user roles
- No sensitive information exposed in HTML source

### Logging and Monitoring
- Access attempts logged with user information
- Role information captured for audit trails
- Debug logging for troubleshooting

## Future Enhancements

### Potential Improvements
1. **Role Hierarchy**: Implement role inheritance (e.g., Admin > Manager > Employee)
2. **Permission Granularity**: Fine-grained permissions for specific actions
3. **Audit Trail**: Detailed logging of all user actions
4. **Session Management**: Enhanced session security and timeout handling
5. **API Security**: Role-based access control for REST endpoints

### Additional Roles
Consider adding these roles in the future:
- **Manager**: Can view reports and manage limited content
- **Viewer**: Read-only access to dashboard
- **Guest**: Limited access for external users 