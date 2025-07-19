import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Provider } from "react-redux";
import { PersistGate } from "redux-persist/integration/react";
import { store, persistor } from "./store/store";
import ConditionalNavBar from "./components/ConditionalNavBar";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignUpPage from "./pages/SignUpPage";
import ProfilePage from "./pages/ProfilePage";
import MapPage from "./pages/MapPage";
import NotificationProvider from "./contexts/NotificationContext";
import "./index.css";

function App() {
  return (
    <Provider store={store}>
      <PersistGate loading={null} persistor={persistor}>
        <NotificationProvider>
          <Router>
            <div className="min-h-screen font-fredoka">
              <ConditionalNavBar />
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignUpPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/maps/:id" element={<MapPage />} />
              </Routes>
            </div>
          </Router>
        </NotificationProvider>
      </PersistGate>
    </Provider>
  );
}

export default App;
