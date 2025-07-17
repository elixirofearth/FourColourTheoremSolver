import { Link } from "react-router-dom";
import styles from "../styles/Buttons.module.css";

export function SignInButton() {
  return (
    <Link to="/login">
      <button className={styles.signInButton}>Sign In</button>
    </Link>
  );
}

export function ProfileButton() {
  return (
    <Link to="/profile">
      <button className={`${styles.profileButton} text-xs md:text-sm`}>
        Profile
      </button>
    </Link>
  );
}

interface SignOutButtonProps {
  onSignOut: () => void;
}

export function SignOutButton({ onSignOut }: SignOutButtonProps) {
  return (
    <button
      onClick={onSignOut}
      className={`${styles.signOutButton} text-xs md:text-sm`}
    >
      Sign Out
    </button>
  );
}
