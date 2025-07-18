import { useLocation } from "react-router-dom";
import NavBar from "./NavBar";

export default function ConditionalNavBar() {
  const location = useLocation();

  // Don't show navbar on login and signup pages for cleaner auth experience
  const hideNavBarRoutes = ["/login", "/signup"];
  const shouldHideNavBar = hideNavBarRoutes.includes(location.pathname);

  if (shouldHideNavBar) {
    return null;
  }

  return <NavBar />;
}
