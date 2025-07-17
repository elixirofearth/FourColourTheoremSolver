import { useLocation } from "react-router-dom";
import NavBar from "./NavBar";

export default function ConditionalNavBar() {
  const location = useLocation();

  // Don't render NavBar on login or signup pages
  if (location.pathname === "/login" || location.pathname === "/signup") {
    return null;
  }

  return <NavBar />;
}
