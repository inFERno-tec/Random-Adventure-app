# Random Adventure Generator

A cross-platform mobile app that generates random suggestions for things to do based on your location and budget. Swipe, spin, or tap to discover new experiences in your area!

## Features

- **Location-based Discovery** — Use GPS or manually enter any city worldwide
- **Custom Budget** — Set your spending range from free to premium
- **All Categories** — Restaurants, Events, Outdoors, Entertainment, Shopping, Arts & Culture
- **Multiple Discovery Modes**:
  - Swipe Cards (Tinder-style)
  - Spin the Wheel (fun random selection)
  - List View (traditional browsing)
- **Place Details** — Ratings, photos, hours, phone, website, directions
- **Favorites** — Save places with personal notes
- **Filters** — Category toggles, distance radius, rating filter, open now only

## Screenshots

[Add your app screenshots here]

## Tech Stack

- **Framework:** Flutter (Dart)
- **State Management:** Riverpod
- **API:** Google Places API
- **Storage:** SharedPreferences + SQLite

## Getting Started

### Prerequisites

- Flutter SDK (3.x or latest)
- Dart SDK
- Google Cloud Account with Places API enabled

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/random-adventure-generator.git
   cd random-adventure-generator
   ```

2. **Install dependencies**
   ```bash
   flutter pub get
   ```

3. **Set up Google Places API**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project
   - Enable "Places API" and "Maps SDK" for Android/iOS
   - Create API credentials (API key)
   - Add your API key to `lib/services/api_keys.dart`:
     ```dart
     const String googlePlacesApiKey = 'YOUR_API_KEY_HERE';
     ```

4. **Configure Android**
   - Open `android/app/src/main/AndroidManifest.xml`
   - Add permissions:
     ```xml
     <uses-permission android:name="android.permission.INTERNET"/>
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
     ```
   - Add API key to `android/app/build.gradle` (android section):
     ```gradle
     manifestPlaceholders = [PLACES_API_KEY: "YOUR_API_KEY"]
     ```

5. **Configure iOS**
   - Open `ios/Runner/Info.plist`
   - Add permissions:
     ```xml
     <key>NSLocationWhenInUseUsageDescription</key>
     <string>We need your location to find adventures near you</string>
     <key>NSLocationAlwaysUsageDescription</key>
     <string>We need your location to find adventures near you</string>
     ```
   - Add API key to `ios/Runner/AppDelegate.swift`:
     ```swift
     GMSServices.provideAPIKey("YOUR_API_KEY")
     ```

6. **Run the app**
   ```bash
   flutter run
   ```

## Project Structure

```
lib/
├── main.dart                 # App entry point
├── app.dart                  # App configuration
├── models/                   # Data models
│   ├── place.dart           # Place model
│   ├── user_preferences.dart # User preferences model
│   └── favorite.dart        # Favorite model
├── providers/               # State management
│   ├── location_provider.dart
│   ├── places_provider.dart
│   ├── favorites_provider.dart
│   └── preferences_provider.dart
├── screens/                 # App screens
│   ├── home_screen.dart
│   ├── discovery_screen.dart
│   ├── place_details_screen.dart
│   ├── favorites_screen.dart
│   └── settings_screen.dart
├── widgets/                 # Reusable widgets
│   ├── adventure_card.dart
│   ├── budget_slider.dart
│   ├── category_chips.dart
│   ├── spin_wheel.dart
│   └── place_card_swipe.dart
├── services/                # API & data services
│   ├── google_places_service.dart
│   ├── location_service.dart
│   └── database_service.dart
└── utils/                   # Utilities
    ├── constants.dart
    ├── theme.dart
    └── helpers.dart

assets/
└── images/                  # App images

test/
└── ...                     # Unit & widget tests
```

## API Usage

### Google Places API Endpoints

1. **Nearby Search** — Find places around a location
2. **Text Search** — Search by city name
3. **Place Details** — Get full place information

### Free Tier

- **Google Places API:** $200/month free credit
- Sufficient for development and personal use

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License — see the LICENSE file for details.

## Acknowledgments

- [Google Places API](https://developers.google.com/maps/documentation/places/web-service/overview)
- [Flutter](https://flutter.dev/)
- [Riverpod](https://riverpod.dev/)

---

**Made with ❤️ using Flutter**
