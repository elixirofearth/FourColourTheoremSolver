# Four-Colour Map Theorem Solver - React Client

This is a React 19 + Vite migration of the original NextJS 13 application. The app allows users to draw maps and solve them using the Four-Colour Theorem.

## Features

- 🎨 Interactive map drawing using p5.js
- 🔐 User authentication (login/signup)
- 💾 Save and manage created maps
- 📱 Mobile-friendly responsive design
- 🎯 Four-colour theorem solver integration

## Technology Stack

- **React 19** (Release Candidate)
- **Vite** for build tooling
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **React Router DOM** for navigation
- **p5.js** for canvas drawing
- **CSS Modules** for component-specific styles

## Getting Started

### Prerequisites

- Node.js (v18 or higher)
- npm or yarn

### Installation

1. Navigate to the react_client directory:

```bash
cd react_client
```

2. Install dependencies:

```bash
npm install
```

3. Create environment file:

```bash
echo "VITE_API_GATEWAY_URL=http://localhost:8080" > .env.development
```

4. Start the development server:

```bash
npm run dev
```

5. Open your browser and navigate to `http://localhost:5173`

## Project Structure

```
src/
├── components/         # Reusable React components
│   ├── Canvas.tsx     # p5.js drawing canvas
│   ├── NavBar.tsx     # Navigation bar
│   ├── LoginForm.tsx  # Authentication forms
│   └── ...
├── pages/             # Route components
│   ├── HomePage.tsx   # Main drawing interface
│   ├── LoginPage.tsx  # Login page
│   └── ...
├── utils/             # Utility functions
│   ├── sketch.ts      # p5.js sketch logic
│   └── ...
├── styles/            # CSS modules
└── App.tsx            # Main app component
```

## Key Differences from NextJS Version

### Routing

- **Before**: NextJS App Router (`useRouter`, `usePathname`)
- **After**: React Router DOM (`useNavigate`, `useLocation`)

### Images

- **Before**: NextJS `Image` component
- **After**: Standard HTML `img` tags

### Environment Variables

- **Before**: `process.env.NEXT_PUBLIC_*`
- **After**: `import.meta.env.VITE_*`

### Font Loading

- **Before**: NextJS font optimization
- **After**: Google Fonts via CSS import

## Mobile-Friendly Improvements

- Responsive navigation with smaller text on mobile
- Touch-optimized canvas drawing
- Flexible button layouts that wrap on small screens
- Responsive typography with `text-xs md:text-sm lg:text-base` patterns
- Mobile-first CSS approach with Tailwind breakpoints

## API Integration

The app connects to the backend API Gateway at the URL specified in `VITE_API_GATEWAY_URL`. Make sure your backend services are running:

1. Authentication Service
2. Map Storage Service
3. Solver Service
4. API Gateway

## Building for Production

```bash
npm run build
```

The built files will be in the `dist/` directory.

## Known Issues

- Some linter warnings related to React 19 RC compatibility with other packages
- CSS module typing may show warnings (functionality works correctly)

## Contributing

This project was migrated from NextJS 13 to provide better performance and a more modern development experience while maintaining all original functionality.
