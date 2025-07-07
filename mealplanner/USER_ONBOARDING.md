# User Onboarding Functionality

## Overview

The DSI Meal Planner application now includes automatic user onboarding functionality. When a new user logs in via OAuth (Google), the system automatically creates a user account with the "EMPLOYEE" role if they don't already exist in the database.

## How It Works

### 1. OAuth Authentication Flow

When a user signs in with Google OAuth:

1. The user is redirected to Google for authentication
2. Google returns user information including email address
3. The system checks if a user with that email exists in the database
4. If the user exists, they are logged in with their existing role
5. If the user doesn't exist, a new user is created with "EMPLOYEE" role

### 2. Implementation Details

#### UserOnboardingService
- **Location**: `src/main/java/com/example/mealplanner/service/UserOnboardingService.java`
- **Purpose**: Centralized service for handling user onboarding logic
- **Key Methods**:
  - `onboardUser(String email)`: Creates or retrieves a user
  - `userExists(String email)`: Checks if a user exists

#### OAuth Services
- **CustomOidcUserService**: Handles OIDC (OpenID Connect) authentication
- **CustomOAuth2UserService**: Handles OAuth2 authentication
- Both services now use the `UserOnboardingService` for consistent user handling

### 3. User Roles

- **EMPLOYEE**: Default role for new users
  - Can access dashboard
  - Can view meal plans
  - Can provide feedback and reactions
- **ADMIN**: Administrative role (must be manually assigned)
  - Can manage dishes
  - Can create weekly meal plans
  - Has access to all features

### 4. Database Schema

The `users` table contains:
- `id`: Primary key
- `email`: Unique email address (from OAuth)
- `role`: User role ("EMPLOYEE" or "ADMIN")

## Testing

### Unit Tests
- **Location**: `src/test/java/com/example/mealplanner/service/UserOnboardingServiceTest.java`
- Tests cover:
  - Existing user retrieval
  - New user creation
  - User existence checking

### Manual Testing
1. Use a new Google account to sign in
2. Verify the user is created with "EMPLOYEE" role
3. Sign out and sign back in to verify existing user handling

## Configuration

No additional configuration is required. The onboarding functionality is automatically enabled when using OAuth authentication.

## Security Considerations

- Only users with valid Google accounts can be onboarded
- Email addresses are used as unique identifiers
- New users are automatically assigned the "EMPLOYEE" role (least privileged)
- Admin roles must be manually assigned by database administrators

## Troubleshooting

### Common Issues

1. **User not created**: Check database connectivity and logs
2. **Wrong role assigned**: Verify the `DEFAULT_ROLE` constant in `UserOnboardingService`
3. **OAuth errors**: Check Google OAuth configuration in `application.properties`

### Logs

The onboarding process is logged with the following patterns:
- `"Starting user onboarding process for email: {}"`
- `"Existing user found: {} with role: {}"`
- `"New user detected: {}. Creating user with {} role."`
- `"Successfully created new user: {} with role: {} and ID: {}"`

## Future Enhancements

Potential improvements to consider:
- Email verification for new users
- Custom onboarding flow with additional user information
- Role assignment based on email domain
- Welcome email notifications
- User profile completion workflow 