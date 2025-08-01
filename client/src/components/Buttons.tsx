import { Link } from "react-router-dom";

export function SignInButton() {
  return (
    <Link to="/login">
      <button className="px-3 sm:px-6 py-2 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300 focus:outline-none text-sm sm:text-base">
        Sign In
      </button>
    </Link>
  );
}

export function ProfileButton() {
  return (
    <Link to="/profile">
      <button className="px-3 sm:px-6 py-2 bg-gradient-to-r from-emerald-500 to-teal-600 text-white font-semibold rounded-full hover:from-emerald-600 hover:to-teal-700 transform hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300 focus:outline-none text-sm sm:text-base">
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
      className="px-3 sm:px-6 py-2 bg-gradient-to-r from-red-500 to-pink-600 text-white font-semibold rounded-full hover:from-red-600 hover:to-pink-700 transform hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300 focus:outline-none text-sm sm:text-base"
    >
      Sign Out
    </button>
  );
}
