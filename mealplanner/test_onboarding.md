# Testing User Onboarding Functionality

## Prerequisites

1. Ensure the application is running
2. Have access to a Google account for testing
3. Database should be accessible

## Test Scenarios

### 1. New User Onboarding

**Steps:**
1. Open the application in a browser
2. Click "Sign in with Google"
3. Use a Google account that has never been used with this application
4. Complete the OAuth flow
5. Verify you are redirected to the dashboard

**Expected Results:**
- User should be automatically created in the database
- User should have "EMPLOYEE" role
- User should be able to access the dashboard
- Check application logs for onboarding messages

**Log Messages to Look For:**
```
New user detected: [email]. Creating user with EMPLOYEE role.
Successfully created new user: [email] with role: EMPLOYEE and ID: [id]
```

### 2. Existing User Login

**Steps:**
1. Sign out of the application
2. Sign back in with the same Google account
3. Verify you are redirected to the appropriate page based on your role

**Expected Results:**
- User should not be created again
- Existing user data should be preserved
- User should be logged in with their existing role

**Log Messages to Look For:**
```
Existing user found: [email] with role: [role]
```

### 3. Role-Based Access

**Test EMPLOYEE Role:**
- Should be able to access `/dashboard`
- Should be able to view meal plans
- Should be able to provide reactions and feedback
- Should NOT be able to access `/weekly-plan` or `/dishes`

**Test ADMIN Role:**
- Should be able to access all features
- Should be redirected to `/weekly-plan` after login

## Database Verification

### Check User Creation

```sql
-- View all users
SELECT * FROM users ORDER BY id;

-- Check specific user
SELECT * FROM users WHERE email = 'your-test-email@gmail.com';
```

### Verify Role Assignment

```sql
-- Count users by role
SELECT role, COUNT(*) as count FROM users GROUP BY role;
```

## Debug Endpoints

### `/debug-user`
- Access this endpoint while logged in
- Shows current user information
- Useful for verifying user creation and role assignment

### `/test-auth`
- Shows authentication details
- Useful for debugging OAuth flow

## Troubleshooting

### Common Issues

1. **User not created:**
   - Check database connectivity
   - Verify OAuth configuration
   - Check application logs for errors

2. **Wrong role assigned:**
   - Verify `DEFAULT_ROLE` constant in `UserOnboardingService`
   - Check if user already exists with different role

3. **OAuth errors:**
   - Verify Google OAuth credentials in `application.properties`
   - Check if redirect URI is configured correctly

### Log Analysis

Look for these log patterns:
- `"Starting user onboarding process for email: {}"`
- `"User processed through onboarding: {} with role: {}"`
- `"Created CustomUserDetails with authorities: {}"`

## Performance Testing

### Concurrent User Creation

1. Use multiple Google accounts simultaneously
2. Verify no duplicate users are created
3. Check database constraints are working

### Database Performance

1. Monitor database performance during user creation
2. Verify indexes on email column are working
3. Check for any deadlocks or contention

## Security Testing

### Input Validation

1. Test with various email formats
2. Verify SQL injection protection
3. Check for proper error handling

### Authorization

1. Verify role-based access control
2. Test privilege escalation attempts
3. Check session management 