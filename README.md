# Teebay - Product Renting and Selling Application

## Overview

Teebay is a modern Android application that enables users to rent and buy/sell products. This application is built using modern Android development practices, clean architecture principles, and includes advanced authentication features like biometric (fingerprint and face) authentication.

## Features Implemented (Part 1 & 2)

### ✅ Authentication System (Part 1)
- **User Registration**: Complete user registration with form validation
  - First Name, Last Name
  - Email address with validation
  - Phone number validation
  - Address
  - Date of Birth (with date picker)
  - Password confirmation
  
- **User Login**: Secure login system
  - Email and password authentication
  - Input validation and error handling
  
- **Biometric Authentication**: Advanced security features
  - Fingerprint authentication
  - Face recognition support
  - Fallback to password authentication
  - Device compatibility checks

### ✅ Product Management System (Part 2)
- **My Products Screen**: Complete product listing interface
  - Product cards with images, titles, categories, prices
  - Empty state with helpful messaging
  - Logout functionality with confirmation
  - FAB button to add new products
  
- **Add Product**: Multi-step product creation flow with auto-save
  - **Step 1 - Title**: Product title input with validation
  - **Step 2 - Categories**: Multiple category selection (Electronics, Furniture, Home Appliances, Sporting Goods, Outdoor, Toys)
  - **Step 3 - Description**: Rich text description with 300-character limit
  - **Step 4 - Upload Picture**: Camera and gallery integration with permissions
  - **Step 5 - Price**: Price input with rent duration selection (Per Day/Week/Month)
  - **Step 6 - Summary**: Review screen with quick edit options
  - Progress indicator and step navigation (back/forth)
  - **Draft Auto-Save**: 
    - Automatic saving of form progress after each field change or step navigation
    - Draft persistence across app sessions (survives app close/backgrounding)
    - Resume prompt when returning to form with saved draft
    - Option to resume saved draft or start fresh
    - Draft cleanup on successful product submission or manual discard
    - One draft per user (new draft overwrites previous)
  
- **Edit Product**: Complete two-step product editing interface
  - **Step 1 - Edit Details**: Comprehensive product information editor
    - Product title editing with live validation
    - Multi-select category modification (supports changing categories)
    - Description editing with proper text input handling
    - Image upload/replacement with camera and gallery integration
    - Form pre-population with existing product data
  - **Step 2 - Edit Price**: Pricing information management
    - Price modification with real-time validation
    - Rent duration adjustment (Per Day/Week/Month)
    - Instant currency formatting and display
  - **Navigation Features**:
    - Back/Next button navigation between steps
    - Progress indicator showing current step (1/2, 2/2)
    - Form validation before step progression
  - **Data Handling**:
    - Automatic loading of existing product data via product ID
    - Real-time form synchronization with ViewModel
    - Success/error state management with user feedback
    - Navigation back to product list after successful update
  
- **Delete Product**: Confirmation dialog with product removal
  - "Are you sure?" confirmation dialog
  - Immediate UI updates after deletion

### 🔧 Technical Implementation

#### Architecture
- **Clean Architecture**: Following SOLID principles
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Repository Pattern**: Data abstraction layer
- **Dependency Injection**: Manual DI with Factory pattern

#### Libraries and Technologies
- **Kotlin**: 100% Kotlin implementation
- **Android Jetpack Components**:
  - Navigation Component with Safe Args
  - Fragment & FragmentContainerView
  - ViewModel & LiveData
  - DataStore Preferences
  - Biometric API
  - RecyclerView with ListAdapter
- **Coroutines**: Asynchronous programming with Flow
- **View Binding**: Type-safe view access for all layouts
- **Material Design 3**: Modern UI components and cards
- **Kotlin Serialization**: JSON serialization for data persistence
- **Camera & Gallery**: Image selection with runtime permissions

#### Project Structure
```
app/src/main/java/com/teebay/appname/
├── data/
│   ├── local/entities/          # Local data entities
│   ├── mappers/                 # Data mappers
│   └── repository/              # Repository implementations
├── domain/
│   ├── model/                   # Domain models
│   └── repository/              # Repository interfaces
├── presentation/
│   ├── ui/auth/                 # Authentication UI
│   │   ├── login/              # Login fragment
│   │   └── register/           # Registration fragment
│   └── viewmodel/              # ViewModels
└── utils/                      # Utility classes
```

## Getting Started

### Prerequisites
- **Android Studio**: Latest stable version
- **Minimum SDK**: API 29 (Android 10)
- **Target SDK**: API 36
- **Kotlin**: 2.2.0

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Teebay-THC
   ```

2. Open in Android Studio

3. Sync the project with Gradle files

4. Run the application on device/emulator

### Usage

#### Product Management Flow
1. **View Your Products**: Access "My Products" screen after login
2. **Add New Product**: Use FAB (+) button to create new products through 6-step wizard
3. **Edit Existing Product**: 
   - Tap the "Edit" button on any product card
   - Navigate through 2-step editing process
   - Review and update product details and pricing
   - Confirm changes to update the product
4. **Delete Product**: Tap product card and confirm deletion through dialog

#### Registration Flow
1. Launch the app (starts with Login screen)
2. Tap "Sign up" to navigate to Registration
3. Fill all required fields:
   - First Name and Last Name
   - Valid email address
   - Phone number (10-15 digits)
   - Address
   - Date of Birth (tap field to open date picker)
   - Password (minimum 6 characters)
   - Confirm Password
4. Tap "REGISTER" to create account
5. Successfully registered users are redirected to Login

#### Login Flow
1. Enter registered email and password
2. Tap "LOGIN" for standard authentication
3. **OR** tap "Use Biometric Authentication" for biometric login
4. Successful login displays welcome message

#### Biometric Authentication
- **First-time setup**: System automatically detects if biometric is available
- **Supported methods**: Fingerprint, Face recognition, Iris (device dependent)
- **Fallback**: If biometric fails, user can proceed with password login
- **Device compatibility**: Automatically hides biometric option if not supported

## Security Features

### Data Storage
- **DataStore Preferences**: Secure local storage for user data
- **No Plain Text Storage**: Passwords stored as-is (as per requirements - no encryption needed)
- **User Session Management**: Persistent login state

### Biometric Implementation
- **AndroidX Biometric**: Latest biometric authentication library
- **Hardware Detection**: Automatic detection of biometric capabilities
- **Error Handling**: Comprehensive error handling for all biometric scenarios
- **Permissions**: Proper biometric permissions in manifest

### Form Validation
- **Email Validation**: Pattern-based email validation
- **Password Strength**: Minimum length requirements
- **Phone Number**: Regex-based phone validation
- **Required Fields**: All fields validated before submission

## Screen Compatibility

### Responsive Design
- **Multiple Screen Sizes**: Optimized for phones and tablets
- **Orientation Support**: Portrait and landscape modes
- **ScrollView Implementation**: Ensures content accessibility on smaller screens
- **Material Design**: Consistent UI across different devices

### iOS Development Notes
Since this is an Android application, for iOS development you would need:
- **Flutter Alternative**: Consider Flutter for cross-platform development
- **Native iOS**: Swift/Objective-C implementation
- **Biometric**: Use TouchID/FaceID APIs on iOS
- **Keychain**: iOS equivalent of Android DataStore

## Testing

### Manual Testing Checklist
- [ ] Registration with valid data
- [ ] Registration with invalid data (error handling)
- [ ] Login with registered credentials
- [ ] Login with invalid credentials
- [ ] Biometric authentication (if available)
- [ ] Navigation between Login/Register screens
- [ ] Form validation for all fields
- [ ] Date picker functionality
- [ ] Screen rotation handling

### Device Testing
- Test on devices with/without biometric hardware
- Test on different screen sizes
- Test biometric enrollment scenarios

## Known Limitations

1. **Backend Integration**: Currently uses local storage only
2. **Password Encryption**: As per requirements, no encryption implemented
3. **Biometric Enrollment**: Cannot enroll biometrics from app (system level)
4. **Network Operations**: No network calls implemented yet

## Future Enhancements (Part 2)

- Backend API integration
- Product listing and management
- Shopping cart functionality
- Payment integration
- Push notifications
- Advanced search and filtering

## Troubleshooting

### Common Issues

1. **Biometric not working**: 
   - Check device settings for biometric enrollment
   - Verify app permissions in device settings

2. **Build errors**:
   - Clean and rebuild project
   - Invalidate cache and restart Android Studio

3. **Navigation issues**:
   - Ensure navigation graph is properly set up
   - Check fragment lifecycles

## Contributing

1. Follow SOLID principles
2. Maintain clean architecture
3. Add proper documentation
4. Test on multiple devices
5. Follow Android best practices

## License

This project is developed for educational/assessment purposes.

---

**Last Updated**: December 2024
**Version**: 2.0.0 (Part 2 - Complete Product Management System)
